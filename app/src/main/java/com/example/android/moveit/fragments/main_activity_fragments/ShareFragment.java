package com.example.android.moveit.fragments.main_activity_fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
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
import com.example.android.moveit.utilities.FileTasksWrapper;
import com.example.android.moveit.utilities.M;
import com.example.android.moveit.utilities.qr_code_related.QRCodeManager;
import com.example.android.moveit.wifi_related.WifiP2pWrapper;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.jakewharton.rxbinding.view.RxView;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import mehdi.sakout.fancybuttons.FancyButton;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static android.app.Activity.RESULT_OK;
import static android.app.ProgressDialog.STYLE_SPINNER;
import static rx.schedulers.Schedulers.io;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShareFragment extends Fragment implements FileTasksWrapper.ProgressShower {
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


    public ShareFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        wifiP2PWrapper = WifiP2pWrapper.getInstance(mainActivity);
        qrCodeManager = QRCodeManager.getInstance(mainActivity);
        //ProgressShower shit
        progressDialog = new ProgressDialog(mainActivity, STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_share, container, false);
        unbinder = ButterKnife.bind(this, view);


        //sendBtn Listener
        Observable<View> sendBtnOb = Observable.create(new ObservableOnSubscribe<View>() {
            @Override
            public void subscribe(final ObservableEmitter<View> e) throws Exception {
                sendBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        e.onNext(view);
                    }
                });
            }
        });
        sendBtnOb.observeOn(io.reactivex.schedulers.Schedulers.io()).doOnNext(new Consumer<View>() {
            @Override
            public void accept(View view) throws Exception {
                wifiP2PWrapper.startDiscovery();
            }
        }).observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread()).doOnNext(new Consumer<View>() {
            @Override
            public void accept(View view) throws Exception {
                Intent bcActivityIntent = new Intent(mainActivity, BarcodeCaptureActivity.class);
                startActivityForResult(bcActivityIntent, BarcodeCaptureActivity.REQUEST_CODE);
                Intent fileIntent = new Intent(mainActivity, FilePickerActivity.class);
                startActivityForResult(fileIntent, FileTasksWrapper.FILE_PICKER_CODE);
            }
        }).subscribe(new Consumer<View>() {
            @Override
            public void accept(View view) throws Exception {
                wifiP2PWrapper.setSendState(true);
            }
        });

        RxView.clicks(receiveBtn).observeOn(io()).doOnNext(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                wifiP2PWrapper.startDiscovery();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                wifiP2PWrapper.setSendState(false);
                Intent intent = new Intent(mainActivity, ReceiveActivity.class);
                mainActivity.startActivity(intent);
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
                    Observable.just(data).subscribeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread()).observeOn(io.reactivex.schedulers.Schedulers.io()).subscribe(new Consumer<Intent>() {
                        @Override
                        public void accept(Intent intent) throws Exception {
                            Barcode barcode = intent.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                            String address = barcode.displayValue;
                            M.L(address);
                            if (wifiP2PWrapper.connect(address)) {
                                M.L("Connection Established.");
                            } else {
                                M.L("Not connected, so can't send any data");
                            }

                        }
                    });

                } else {
                    M.L("barcode return kartana jhol aahe");
                }
            } else {
                M.L("BarcodeCaptureActivity mdhe jhol aahe");
            }
        } else if (requestCode == FileTasksWrapper.FILE_PICKER_CODE && resultCode == RESULT_OK) {
            Observable.just(data).subscribeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread()).observeOn(Schedulers.io()).subscribe(new Consumer<Intent>() {
                @Override
                public void accept(Intent intent) throws Exception {
                    Uri uri = intent.getData();
                    // A utility method is provided to transform the URI to a File object
                    File file = com.nononsenseapps.filepicker.Utils.getFileForUri(uri);
                    if (file != null) {
                        M.L("FIle aahe re babba " + file.getAbsolutePath() + file.getName() + " " + Thread.currentThread().getName());

                        fileTasksWrapper = FileTasksWrapper.getInstance(file, true);
                        if (wifiP2PWrapper.isGroupOwnerAddressPresent()) {
                            if (fileTasksWrapper.sendAFile(wifiP2PWrapper.getGroupOwnerAddress())) {
                                M.L("data sending successfull.");
                            }
                        } else {
                            M.L("Receiver address nahi bhetla");
                        }

                    }
                }
            });

        } else M.L("Request code didn't match ");
    }


    @Override
    public void showProgress(String textCaption) {
        if (progressDialog != null) {
            progressDialog.setMessage(textCaption);
            progressDialog.show();
        } else {
            M.L("Cannot show shareFragment progressdialog since it's null");
        }
    }

    @Override
    public void hideProgress() {
        if (progressDialog != null) {
            progressDialog.hide();
        } else {
            M.L("Cannot Hide ShareFragment progressdialog since it's null");
        }
    }
}
