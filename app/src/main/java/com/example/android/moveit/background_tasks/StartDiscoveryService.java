package com.example.android.moveit.background_tasks;

import android.app.IntentService;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;

import com.example.android.moveit.adapters.WifiP2pWrapper;
import com.example.android.moveit.broadcast_receivers.MyWifiP2pBroadcastReceiver;
import com.example.android.moveit.utilities.BrIntentFilterWrapper;
import com.example.android.moveit.utilities.M;

/**
 * Created by Priyank on 14-02-2017.
 */

public class StartDiscoveryService extends IntentService {
    public static final String DETECT_PEERS_SERVICE = "DetectPeers";
    public static final int DISCOVERED = 909;
    private static int discoveryState;
    private WifiP2pWrapper wifiP2pWrapper;
    private Handler serviceHandler;
    private MyWifiP2pBroadcastReceiver myWifiP2pBroadcastReceiver;

    public StartDiscoveryService() {
        super(DETECT_PEERS_SERVICE);
//        serviceHandler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//            }
//        };
        wifiP2pWrapper = WifiP2pWrapper.getInstance(this);
        myWifiP2pBroadcastReceiver = MyWifiP2pBroadcastReceiver.getInstance(wifiP2pWrapper);
    }

    public static int getDiscoveryState() {
        return discoveryState;
    }

    public static void setDiscoveryState(int discoveryState) {
        StartDiscoveryService.discoveryState = discoveryState;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerReceiver(myWifiP2pBroadcastReceiver, BrIntentFilterWrapper.getInstance().wifiP2pFilter);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        synchronized (wifiP2pWrapper) {
            M.L("Kahitari jhala");
            wifiP2pWrapper.setWifiStatus(true);
            wifiP2pWrapper.setSendState(true);

            wifiP2pWrapper.wifiP2pManager.discoverPeers(wifiP2pWrapper.wifiP2pChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    M.L("Success in enabling discovery");
                }

                @Override
                public void onFailure(int i) {
                    if (i == WifiP2pManager.BUSY || i == WifiP2pManager.ERROR) {
                        M.L("Failure :Trying to discover peers again");
                        wifiP2pWrapper.wifiP2pManager.discoverPeers(wifiP2pWrapper.wifiP2pChannel, this);
                    }
                }
            });


        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myWifiP2pBroadcastReceiver);
    }
}