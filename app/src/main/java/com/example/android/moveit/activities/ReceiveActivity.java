package com.example.android.moveit.activities;

import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.example.android.moveit.R;
import com.example.android.moveit.file_related.FileTasksWrapper;
import com.example.android.moveit.utilities.M;
import com.example.android.moveit.utilities.qr_code_related.QRCodeManager;
import com.example.android.moveit.wifi_related.WifiP2pEventsObservableCreator;
import com.example.android.moveit.wifi_related.WifiP2pWrapper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class ReceiveActivity extends AppCompatActivity {
    public static final int TIMEOUT = 10000;
    Unbinder unbinder;
    @BindView(R.id.qrCodeBox)
    ImageView imageView;

    private WifiP2pEventsObservableCreator wifiP2pBroadcastReceiver;
    private WifiP2pWrapper wifiP2pAdapter;
    private QRCodeManager qrCodeManager;
    private FileTasksWrapper fileTasksWrapper;
    private Observable<Intent> brObservable;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        unbinder = ButterKnife.bind(this);
        init();

    }

    private void init() {
        qrCodeManager = QRCodeManager.getInstance(this);
        wifiP2pAdapter = WifiP2pWrapper.getInstance(this);
        brObservable = wifiP2pAdapter.getWifiP2pBRObservable();
        brObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Intent>() {
                    @Override
                    public void accept(Intent intent) throws Exception {
                        String action = intent.getAction();
                        if (action.equals(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)) {
                            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                            if (imageView != null) {
                                M.L("Inside ReceiveActivity brObservable : QrCode contents-->" + device.deviceAddress);
                                imageView.setImageBitmap(
                                        qrCodeManager.generateQrCode(device.deviceAddress,
                                                QRCodeManager.QR_WIDTH, QRCodeManager.QR_HEIGHT)
                                );
                            } else {
                                M.L("Inside ReceiveActivity brObservable : imageView null, cannot show QR code");
                            }
                        }
                    }
                }).filter(new Predicate<Intent>() {
            @Override
            public boolean test(Intent intent) throws Exception {
                if(intent.getAction().equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)){
                    NetworkInfo info = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                    if(info.isConnected())
                        return true;

                }return false;
            }
        }).observeOn(Schedulers.io()).subscribe(new Consumer<Intent>() {
            @Override
            public void accept(Intent intent) throws Exception {
                fileTasksWrapper.receiveFile();

            }
        });
        fileTasksWrapper = FileTasksWrapper.getInstance();

    }
    @Override
    protected void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }


}
