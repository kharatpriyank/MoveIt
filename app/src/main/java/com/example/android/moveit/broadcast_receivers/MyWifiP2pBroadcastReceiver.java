package com.example.android.moveit.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

import com.example.android.moveit.adapters.WifiP2pWrapper;
import com.example.android.moveit.background_tasks.StartDiscoveryService;
import com.example.android.moveit.utilities.M;
import com.example.android.moveit.utilities.qr_code_related.QRCodeManager;
import com.example.android.moveit.utilities.qr_code_related.QRCodeShower;

import static com.example.android.moveit.adapters.WifiP2pWrapper.isListFound;

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
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        QRCodeManager qrCodeManager = null;
        M.L(action);
        if (action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)) {
            if (intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST) != null) {
                StartDiscoveryService.setDiscoveryState(StartDiscoveryService.DISCOVERED);

                synchronized (isListFound) {
                    isListFound = true;
                    }

            } else {
                StartDiscoveryService.setDiscoveryState(0);
                M.L("No device left in isListFoundthe list");
            }
        }
        if (action.equals(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)) {

            Class[] interfaces = context.getClass().getInterfaces();
            boolean isQrCodeShower = false;
            for (Class c : interfaces) {
                if (c.equals(QRCodeShower.class)) {
                    isQrCodeShower = true;
                    break;
                }

            }
            if (isQrCodeShower) {
                WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                QRCodeShower shower = (QRCodeShower) context;
                qrCodeManager = QRCodeManager.getInstance(context);
                shower.displayQRCode(qrCodeManager.generateQrCode(device.deviceAddress, QRCodeManager.QR_WIDTH, QRCodeManager.QR_HEIGHT));
                M.L("Receiver's Device address : " + device.deviceAddress);
            }
        }
    }
}
