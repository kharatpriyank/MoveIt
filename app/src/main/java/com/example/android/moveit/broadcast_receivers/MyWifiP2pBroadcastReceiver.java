package com.example.android.moveit.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;

import com.example.android.moveit.adapters.MyWifiP2pAdapter;
import com.example.android.moveit.utilities.M;
import com.example.android.moveit.utilities.qr_code_related.QRCodeManager;

/**
 * Created by Priyank on 10-02-2017.
 */

//Singleton BroadcastReceiver
public class MyWifiP2pBroadcastReceiver extends BroadcastReceiver {

    private static MyWifiP2pBroadcastReceiver instance;
    private MyWifiP2pAdapter myWifiP2pAdapter;
    private MyWifiP2pBroadcastReceiver(MyWifiP2pAdapter myWifiP2pAdapter){
        this.myWifiP2pAdapter = myWifiP2pAdapter;
    }
    //Singleton thread-safe object creation.
    public static MyWifiP2pBroadcastReceiver getInstance(MyWifiP2pAdapter myWifiP2pAdapter) {
        if(instance == null){
            synchronized (MyWifiP2pBroadcastReceiver.class){
                if(instance == null){
                    instance = new MyWifiP2pBroadcastReceiver(myWifiP2pAdapter);
                }
            }
        }
        return instance;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        M.L(action);
        boolean isSendState = myWifiP2pAdapter.isSendState();
        if(action.equals(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)){
            //Check whether it's a receiving side
            if(!isSendState){
                //Generate a QR code if so.
                QRCodeManager qrCodeManager = QRCodeManager.getInstance(context);
            }
        }

    }
}
