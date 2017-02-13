package com.example.android.moveit.adapters;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;

import com.example.android.moveit.utilities.M;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Priyank on 10-02-2017.
 */

//Singleton class
public class MyWifiP2pAdapter {
    private static MyWifiP2pAdapter instance;
    public WifiP2pManager wifiP2pManager;
    public WifiP2pManager.Channel wifiP2pChannel;
    private WifiManager wifiManager;
    private boolean isSendState, isConnected;
    private Context context;
    private WifiP2pManager.PeerListListener myPeerListener;
    private List<WifiP2pDevice> wifiP2pDevices;

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
        myPeerListener = new MyPeerListListener();
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

    public void setWifiStatus(boolean isOn) {
        wifiManager.setWifiEnabled(isOn);
    }

    public void setContext(Context context) {
        this.context = context;
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
        if (wifiP2pManager != null) {
            if (wifiP2pDevices != null) {
                for (WifiP2pDevice device : wifiP2pDevices) {
                    //search in discovered lists
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
            }
        }
        return isConnected;
    }

    //discover objects in wifiP2p environment
    public class MyPeerListListener implements WifiP2pManager.PeerListListener {

        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            wifiP2pDevices = new ArrayList<>(wifiP2pDeviceList.getDeviceList());
        }
    }
}
