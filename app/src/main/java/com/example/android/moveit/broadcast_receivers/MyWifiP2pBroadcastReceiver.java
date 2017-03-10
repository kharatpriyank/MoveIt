package com.example.android.moveit.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

import com.example.android.moveit.utilities.FileTasksWrapper;
import com.example.android.moveit.utilities.M;
import com.example.android.moveit.utilities.qr_code_related.QRCodeManager;
import com.example.android.moveit.utilities.qr_code_related.QRCodeShower;
import com.example.android.moveit.wifi_related.WifiP2pWrapper;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.example.android.moveit.utilities.M.L;

/**
 * Created by Priyank on 10-02-2017.
 */

//Singleton BroadcastReceiver
public class MyWifiP2pBroadcastReceiver extends BroadcastReceiver {

    private static MyWifiP2pBroadcastReceiver instance;
    private WifiP2pWrapper wifiP2PWrapper;


    private MyWifiP2pBroadcastReceiver(WifiP2pWrapper wifiP2PWrapper) {
        this.wifiP2PWrapper = wifiP2PWrapper;
    }

    //Singleton thread-safe object creation.
    public static MyWifiP2pBroadcastReceiver getInstance(WifiP2pWrapper wifiP2PWrapper) {
        if (instance == null) {
            synchronized (MyWifiP2pBroadcastReceiver.class) {
                if (instance == null) {
                    instance = new MyWifiP2pBroadcastReceiver(wifiP2PWrapper);
                }
            }
        }
        return instance;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();
        //COde for checking if the context is QrCodeShower and FileReceiver.
        Class[] interfaces = context.getClass().getInterfaces();
        boolean isQrCodeShower = false;
        boolean tempIsFileReceiver = false;
        boolean tempIsProgressShower = false;
        for (Class c : interfaces) {
            if (c.equals(QRCodeShower.class)) {
                isQrCodeShower = true;
                break;
            }

        }
        for (Class c : interfaces) {
            if (c.equals(FileTasksWrapper.FileReceiver.class)) {
                tempIsFileReceiver = true;
                break;
            }
        }
        final boolean IS_FILE_RECEIVER = tempIsFileReceiver;
        //Match the actions.
        if (action.equals(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)) {

            QRCodeManager qrCodeManager = null;

            if (isQrCodeShower) {
                WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                QRCodeShower shower = (QRCodeShower) context;
                qrCodeManager = QRCodeManager.getInstance(context);
                shower.displayQRCode(qrCodeManager.generateQrCode(device.deviceAddress, QRCodeManager.QR_WIDTH, QRCodeManager.QR_HEIGHT));
                L("Receiver's Device address : " + device.deviceAddress);
            }
        } else {
            Observable.just(intent).subscribeOn(AndroidSchedulers.mainThread()).observeOn(Schedulers.io()).subscribe(new Consumer<Intent>() {
                @Override
                public void accept(Intent intent) throws Exception {


                    M.L(action);
                    if (action.equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)) {
                        NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                        if (networkInfo.isConnected()) {
                            wifiP2PWrapper.wifiP2pManager.requestConnectionInfo(wifiP2PWrapper.wifiP2pChannel,
                                    wifiP2PWrapper.getConnectionInfoListener());
                            if (!wifiP2PWrapper.isSendState()) {
                                //Now is the time to receive file
                                if (IS_FILE_RECEIVER) {
                                    M.L("Ithe Pohochloy");
                                    FileTasksWrapper.FileReceiver fileReceiver = (FileTasksWrapper.FileReceiver) context;
                                    fileReceiver.receiveFile();
                                }
                            }
                        }
                    }
                    if (action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)) {

                        wifiP2PWrapper.wifiP2pManager.requestPeers(wifiP2PWrapper.wifiP2pChannel,
                                wifiP2PWrapper.getMyPeerListWrapper().getMyPeerListListener());
                    }
                }
            });
        }

    }


}
