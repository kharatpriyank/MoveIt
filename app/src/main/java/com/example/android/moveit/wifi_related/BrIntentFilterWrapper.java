package com.example.android.moveit.wifi_related;

import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;

/**
 * Created by Priyank on 12-02-2017.
 */

public class BrIntentFilterWrapper {
    private static BrIntentFilterWrapper instance;
    public IntentFilter wifiP2pFilter;

    private BrIntentFilterWrapper() {
        wifiP2pFilter = new IntentFilter();
        wifiP2pFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiP2pFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        wifiP2pFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiP2pFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        wifiP2pFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
    }

    //Thread safe singleton
    public static BrIntentFilterWrapper getInstance() {
        if (instance == null) {
            synchronized (BrIntentFilterWrapper.class) {
                if (instance == null) {
                    instance = new BrIntentFilterWrapper();
                }
            }
        }
        return instance;
    }
}
