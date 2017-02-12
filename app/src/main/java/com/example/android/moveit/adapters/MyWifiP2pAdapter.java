package com.example.android.moveit.adapters;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;

import com.example.android.moveit.utilities.M;

/**
 * Created by Priyank on 10-02-2017.
 */

//Singleton class
public class MyWifiP2pAdapter {
    private WifiManager wifiManager;
    private boolean isSendState;
    private static MyWifiP2pAdapter instance;
    public WifiP2pManager wifiP2pManager;
    public WifiP2pManager.Channel wifiP2pChannel;
    private Context context;

    //Private Constructor for singleton
    private MyWifiP2pAdapter(final Context context) {
        this.context = context;
        wifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        wifiP2pChannel = wifiP2pManager.initialize(context, context.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                M.L("WifiP2pManager init not completed.");
            }
        });
    }

    public void setWifiStatus(boolean isOn) {
        wifiManager.setWifiEnabled(isOn);
    }

    public void setContext(Context context) {
        this.context = context;
    }
    public void setSendState(boolean sendState) {
        isSendState = sendState;
    }
    public boolean isSendState() {
        return isSendState;
    }

    //singleton generator
    public static MyWifiP2pAdapter getInstance(Context context) {
        if (instance == null) {
            //Making it Thread safe
            synchronized (MyWifiP2pAdapter.class) {
                if (instance == null) {
                    instance = new MyWifiP2pAdapter(context);
                    instance.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                }
            }
        }
        return instance;
    }
}
