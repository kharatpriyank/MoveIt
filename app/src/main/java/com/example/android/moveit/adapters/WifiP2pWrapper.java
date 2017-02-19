package com.example.android.moveit.adapters;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;

import com.example.android.moveit.background_tasks.StartDiscoveryService;
import com.example.android.moveit.utilities.M;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Priyank on 10-02-2017.
 */

//Singleton class
public class WifiP2pWrapper {
    public static Boolean isListFound = false;
    private static WifiP2pWrapper instance;
    public WifiP2pManager wifiP2pManager;
    public WifiP2pManager.Channel wifiP2pChannel;
    private WifiManager wifiManager;
    private boolean isSendState, isConnected;
    private Context context;
    private WifiP2pManager.PeerListListener myPeerListener;
    private List<WifiP2pDevice> wifiP2pDevices;

    //Private Constructor for singleton
    private WifiP2pWrapper(final Context context) {
        this.context = context;
        wifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        wifiP2pChannel = wifiP2pManager.initialize(context, context.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                M.L("WifiP2pManager init not completed.");
            }
        });
        myPeerListener = new MyPeerListListener();
    }

    //singleton generator
    public static WifiP2pWrapper getInstance(Context context) {
        if (instance == null) {
            //Making it Thread safe
            synchronized (WifiP2pWrapper.class) {
                if (instance == null) {
                    instance = new WifiP2pWrapper(context);
                    instance.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                }
            }
        }
        instance.setContext(context);
        return instance;
    }

    public void setWifiStatus(boolean isOn) {
        wifiManager.setWifiEnabled(isOn);
    }

    public boolean isWifiOn() {
        return wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
    }

    public boolean isSendState() {
        return isSendState;
    }

    public void setSendState(boolean sendState) {
        isSendState = sendState;
    }

    //connect to a device
    public boolean connect(final String deviceAddress) {
        isConnected = false;
        boolean deviceFound = false;
        WifiP2pConfig connectDevice = new WifiP2pConfig();
        connectDevice.deviceAddress = deviceAddress;
        boolean isDiscovered = (StartDiscoveryService.getDiscoveryState() == StartDiscoveryService.DISCOVERED);
        if (wifiP2pManager != null && isDiscovered) {
            //request the discovered peers into wifiP2pDevices
            wifiP2pManager.requestPeers(wifiP2pChannel, new MyPeerListListener());
            if (wifiP2pDevices != null) {
                //check if the address matches one of'em.
                for (WifiP2pDevice device : wifiP2pDevices) {
                    if (device.deviceAddress.equals(deviceAddress)) {
                        deviceFound = true;
                        break;
                    }
                }
                if (deviceFound) {
                    wifiP2pManager.connect(wifiP2pChannel, connectDevice, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            M.L("Connected to " + deviceAddress);
                            isConnected = true;
                        }

                        @Override
                        public void onFailure(int i) {
                            M.L("Not able to connect to " + deviceAddress);
                        }
                    });
                }
            } else {
                M.L("wifiP2pDevices list empty");
            }

        } else {
            M.L("connect Method : isDiscovered-->" + isDiscovered + " or  wifiP2pManager maybe null");
        }
        return isConnected;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    //discover objects in wifiP2p environment
    public class MyPeerListListener implements WifiP2pManager.PeerListListener {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            wifiP2pDevices = new ArrayList<>(wifiP2pDeviceList.getDeviceList());
            isListFound = wifiP2pDeviceList != null;
        }
    }


}
