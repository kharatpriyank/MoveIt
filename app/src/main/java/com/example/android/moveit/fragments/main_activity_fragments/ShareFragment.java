package com.example.android.moveit.fragments.main_activity_fragments;


import android.content.Intent;
import android.graphics.Point;
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
import com.example.android.moveit.adapters.WifiP2pWrapper;
import com.example.android.moveit.utilities.FileTasksWrapper;
import com.example.android.moveit.utilities.M;
import com.example.android.moveit.utilities.qr_code_related.QRCodeManager;
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
import mehdi.sakout.fancybuttons.FancyButton;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

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

    // TextView connectionStatus;
    //other references
    private MainActivity mainActivity;
    private WifiP2pWrapper wifiP2PWrapper;
    private QRCodeManager qrCodeManager;

    public ShareFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        wifiP2PWrapper = WifiP2pWrapper.getInstance(mainActivity);
        qrCodeManager = QRCodeManager.getInstance(mainActivity);
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
            }
        }).doAfterNext(new Consumer<View>() {
            @Override
            public void accept(View view) throws Exception {
                Intent fileIntent = new Intent(mainActivity, FilePickerActivity.class);
                startActivityForResult(fileIntent, FileTasksWrapper.FILE_PICKER_CODE);
            }
        }).subscribe(new Consumer<View>() {
            @Override
            public void accept(View view) throws Exception {
                M.L("SendBtn Subscribed");
            }
        });

        RxView.clicks(receiveBtn).observeOn(Schedulers.io()).doOnNext(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                wifiP2PWrapper.startDiscovery();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
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
                            Point[] p = barcode.cornerPoints;

                            String address = barcode.displayValue;
                            M.L(address);
                            wifiP2PWrapper.connect(address);
                        }
                    });

                } else {
                    M.L("barcode return kartana jhol aahe");
                }
            } else {
                M.L("BarcodeCaptureActivity mdhe jhol aahe");
            }
        } else if (requestCode == FileTasksWrapper.FILE_PICKER_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            // A utility method is provided to transform the URI to a File object
            File file = com.nononsenseapps.filepicker.Utils.getFileForUri(uri);
            if (file != null)
                M.L("FIle aahe re babba " + file.getAbsolutePath() + file.getName());
        } else M.L("Request code didn't match ");
    }
}
