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
import android.widget.Toast;

import com.example.android.moveit.R;
import com.example.android.moveit.activities.BarcodeCaptureActivity;
import com.example.android.moveit.activities.MainActivity;
import com.example.android.moveit.activities.ReceiveActivity;
import com.example.android.moveit.file_related.FileStateObject;
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
    private ProgressDialog connWaitDialog,progressDialog;
    private boolean isProgressBarShown,isComplete;

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
    private Observable<FileStateObject> fileStateObjectObservable;

    public ShareFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        progressDialog = new ProgressDialog(mainActivity,ProgressDialog.STYLE_SPINNER);
        isProgressBarShown = false;
        fileTasksWrapper = FileTasksWrapper.getInstance();
        fileStateObjectObservable = fileTasksWrapper.getMetaDataObservable();
        wifiP2PWrapper = WifiP2pWrapper.getInstance(mainActivity);
        discoveryObservable = wifiP2PWrapper.getDiscoveryObservable();
        peersListObservable = wifiP2PWrapper.getPeersListObservable();
        connectionObservable = wifiP2PWrapper.getWifiP2pConnectionObservable();
        qrCodeManager = QRCodeManager.getInstance(mainActivity);
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
                M.L("Inside SenD side BrObservable Observer, event : "+action);
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

                    connectionObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                            .doOnNext(new Consumer<WifiP2pInfo>() {
                                @Override
                                public void accept(WifiP2pInfo wifiP2pInfo) throws Exception {
                                    if (networkInfo.isConnected()) {
                                        M.L("Inside OnConnectionInfoListener Observable : " + wifiP2pInfo.groupOwnerAddress);
                                        hideConnectionWaitDialog();
                                        fileTasksWrapper.setReceiverAddress(wifiP2pInfo.groupOwnerAddress);
                                        Toast.makeText(mainActivity, "Peer Connected, Sending file now.......", Toast.LENGTH_LONG).show();
                                    }else{
                                        M.L("Inside onConncetionLIstener Observable: Cannot connect to peer device.");
                                    }

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
        fileStateObjectObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<FileStateObject>() {
            @Override
            public void accept(FileStateObject fileStateObject) throws Exception {
                if (wifiP2PWrapper.isSendState()) {
                    if (!fileTasksWrapper.isFirstMetadataSent()) {
                        M.L("Inside ShareFragment : File Size : "+fileStateObject.getSize()+" bytes.");
                        //Show the progress dialog here.

                    } else {
                        if(!isProgressBarShown){
                            showProgressDialog();
                            isProgressBarShown = true;
                        }
                        float progress = (((float)fileStateObject.getTransferredBytes()/fileStateObject.getSize())*FileTasksWrapper.MAX_PROGRESS);
                        //Use fileStateObject.getProgress to update progressbar while sharing.
                       M.L("Inside ShareFragment : Progress : "+progress);
                        updateProgress((int)progress);
                        //hide progress bar if progress == FileTasksWrapper.MAX_PRORESS

                        if(fileStateObject.getTransferredBytes() == fileStateObject.getSize() && !isComplete) {
                            hideProgressDialog();
                            M.T(mainActivity, fileStateObject.getFileName()+" Sent!");
                            isComplete = true;
                        }

                    }
                }
            }
        });


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
                            showConnectionWaitDialog("Connecting to peer device");
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
                            fileTasksWrapper.setReceiveFile(com.nononsenseapps.filepicker.Utils.getFileForUri(uri));
                            M.L("Inside shareFragment onActivityResult : file obtained");

                        }
                    });

        } else M.L("Request code didn't match ");
    }

    private void showConnectionWaitDialog(String message) {
        connWaitDialog = new ProgressDialog(mainActivity, ProgressDialog.STYLE_SPINNER);
        connWaitDialog.setIndeterminate(true);
        connWaitDialog.setMessage(message);
        connWaitDialog.setCancelable(false);
        connWaitDialog.show();
    }

    private void hideConnectionWaitDialog() {
        if (connWaitDialog.isShowing())
            connWaitDialog.dismiss();
    }
    public void showProgressDialog(){
        progressDialog.setIndeterminate(false);
        progressDialog.setTitle(R.string.progress_bar_title);
        progressDialog.setCancelable(false);
        progressDialog.setMax(FileTasksWrapper.MAX_PROGRESS);
        progressDialog.show();
    }
    public void updateProgress(int progress){
        progressDialog.setProgress(progress);
        progressDialog.setMessage("Progress : "+progress+"%");

    }
    public void hideProgressDialog(){
        if(progressDialog.isShowing()) {
            progressDialog.dismiss();

        }
    }

}
