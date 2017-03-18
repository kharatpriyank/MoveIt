package com.example.android.moveit.fragments.main_activity_fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.moveit.R;
import com.example.android.moveit.activities.BarcodeCaptureActivity;
import com.example.android.moveit.activities.MainActivity;
import com.example.android.moveit.activities.ReceiveActivity;
import com.example.android.moveit.file_related.FileTasksWrapper;
import com.example.android.moveit.utilities.M;
import com.example.android.moveit.utilities.qr_code_related.QRCodeManager;
import com.example.android.moveit.wifi_related.WifiP2pWrapper;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import mehdi.sakout.fancybuttons.FancyButton;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShareFragment extends Fragment {
    public static final String TAG = "Share";
    //View related regerences
    Unbinder unbinder;
    @BindView(R.id.sendBtn)
    FancyButton sendBtn;
    @BindView(R.id.receiveBtn)
    FancyButton receiveBtn;
    private ProgressDialog progressDialog;

    // TextView connectionStatus;
    //other references
    private MainActivity mainActivity;
    private WifiP2pWrapper wifiP2PWrapper;
    private QRCodeManager qrCodeManager;
    private FileTasksWrapper fileTasksWrapper;
    //Observables
    private Observable<String> discoveryObservable;
    private Observable<List<WifiP2pDevice>> peersListObservable;
    private Observable<WifiP2pInfo> connectionObservable;
    private Observable<Intent> brObservable;


    public ShareFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        fileTasksWrapper = FileTasksWrapper.getInstance();
        wifiP2PWrapper = WifiP2pWrapper.getInstance(mainActivity);
        brObservable = wifiP2PWrapper.getWifiP2pBRObservable();
        brObservable.subscribeOn(Schedulers.io()).filter(new Predicate<Intent>() {
            @Override
            public boolean test(Intent intent) throws Exception {
                return wifiP2PWrapper.isSendState();
            }
        }).doOnNext(new Consumer<Intent>() {
            @Override
            public void accept(Intent intent) throws Exception {
                String action = intent.getAction();
                if (action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)) {
                    peersListObservable.subscribe(new Consumer<List<WifiP2pDevice>>() {
                        @Override
                        public void accept(List<WifiP2pDevice> wifiP2pDevices) throws Exception {
                            wifiP2PWrapper.setWifiP2pDevices(wifiP2pDevices);
                            if (wifiP2pDevices != null)
                                M.L("Inside PeersListenerObserver : Device list set.");
                            else
                                M.L("Inside PeersListenerObserver : Device List null/not set.");
                        }
                    });
                } else if (action.equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)) {
                    final NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                    connectionObservable.filter(new Predicate<WifiP2pInfo>() {
                        @Override
                        public boolean test(WifiP2pInfo wifiP2pInfo) throws Exception {
                            return networkInfo.isConnected();
                        }
                    }).subscribeOn(Schedulers.io())
                            .doOnNext(new Consumer<WifiP2pInfo>() {
                                @Override
                                public void accept(WifiP2pInfo wifiP2pInfo) throws Exception {
                                    M.L("Inside OnConnectionInfoListener Observable : " + wifiP2pInfo.groupOwnerAddress);
                                    hideProgressBar();
                                    fileTasksWrapper.setReceiverAddress(wifiP2pInfo.groupOwnerAddress);
                                    M.T(mainActivity, "Connected to Peer,sending file");


                                }
                            }).observeOn(Schedulers.io()).subscribe(new Consumer<WifiP2pInfo>() {
                        @Override
                        public void accept(WifiP2pInfo wifiP2pInfo) throws Exception {
                            fileTasksWrapper.sendFile();

                        }
                    });

                }
            }
        }).observeOn(Schedulers.io()).subscribe(new Consumer<Intent>() {
            @Override
            public void accept(Intent intent) throws Exception {


            }
        });
        discoveryObservable = wifiP2PWrapper.getDiscoveryObservable();
        peersListObservable = wifiP2PWrapper.getPeersListObservable();

        connectionObservable = wifiP2PWrapper.getWifiP2pConnectionObservable();
        qrCodeManager = QRCodeManager.getInstance(mainActivity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_share, container, false);
        unbinder = ButterKnife.bind(this, view);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifiP2PWrapper.setSendState(true);

                Intent bcIntent = new Intent(mainActivity, BarcodeCaptureActivity.class);
                Intent fileIntent = new Intent(mainActivity, FilePickerActivity.class);

                discoveryObservable.subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String s) throws Exception {
                                if (s.equals(WifiP2pWrapper.DISCOVERY_STATE_DISCOVERY_STARTED)) {
                                    M.L("Inside SHareFragment : " + WifiP2pWrapper.DISCOVERY_STATE_DISCOVERY_STARTED);

                                }
                            }
                        });
                startActivityForResult(fileIntent, FileTasksWrapper.FILE_PICKER_CODE);
                startActivityForResult(bcIntent, BarcodeCaptureActivity.REQUEST_CODE);
            }
        });
        receiveBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                wifiP2PWrapper.setSendState(false);
                discoveryObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String s) throws Exception {
                                M.L("Inside DiscoveryObservable after Receiver Btn : " + s);
                                if (s.equals(WifiP2pWrapper.DISCOVERY_STATE_DISCOVERY_STARTED)) {
                                    Intent receiveIntent = new Intent(mainActivity, ReceiveActivity.class);
                                    startActivity(receiveIntent);
                                } else {
                                    M.T(mainActivity, "Discovery failed, please try again.");
                                }

                            }
                        });

            }
        });


        return view;
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();
        wifiP2PWrapper.setWifiStatus(false);
        super.onDestroy();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BarcodeCaptureActivity.REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Observable.just(data).subscribeOn(AndroidSchedulers.mainThread())
                            .observeOn(Schedulers.io()).doOnNext(new Consumer<Intent>() {
                        @Override
                        public void accept(Intent intent) throws Exception {
                            Barcode barcode = intent.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                            String address = barcode.displayValue;
                            M.L("Connecting to -->" + address);
                            wifiP2PWrapper.connect(address);

                        }
                    }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Intent>() {
                        @Override
                        public void accept(Intent intent) throws Exception {
                            showProgressBar("Connecting to peer device");
                        }
                    });

                } else {
                    M.L("barcode return kartana jhol aahe");
                }
            } else {
                M.L("BarcodeCaptureActivity mdhe jhol aahe");
            }
        } else if (requestCode == FileTasksWrapper.FILE_PICKER_CODE && resultCode == RESULT_OK) {
            Observable.just(data).subscribeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                    .observeOn(Schedulers.newThread())
                    .subscribe(new Consumer<Intent>() {
                        @Override
                        public void accept(Intent intent) throws Exception {
                            Uri uri = intent.getData();
                            // A utility method is provided to transform the URI to a File object
                            fileTasksWrapper.setFile(com.nononsenseapps.filepicker.Utils.getFileForUri(uri));
                            M.L("Inside shareFragment onActivityResult : file obtained");

                        }
                    });

        } else M.L("Request code didn't match ");
    }

    private void showProgressBar(String message) {
        progressDialog = new ProgressDialog(mainActivity, ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgressBar() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }


}
