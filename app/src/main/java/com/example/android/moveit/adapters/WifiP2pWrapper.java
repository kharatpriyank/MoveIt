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
public class WifiP2pWrapper {
    private static WifiP2pWrapper instance;
    public WifiP2pManager wifiP2pManager;
    public WifiP2pManager.Channel wifiP2pChannel;
    private WifiManager wifiManager;
    private boolean isSendState, isConnected;
    private Context context;
    private MyPeersListWrapper myPeerListWrapper;
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
        myPeerListWrapper = new MyPeersListWrapper();
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

    public boolean connect(String address) {
        if (myPeerListWrapper.wifiP2pDevices != null) {
            if (myPeerListWrapper.isDeviceInList(address)) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = address;
                wifiP2pManager.connect(wifiP2pChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        isConnected = true;
                        M.L("Connection established");
                    }

                    @Override
                    public void onFailure(int i) {
                        M.L("Cannot connect:Connect failure");
                        if (i == WifiP2pManager.P2P_UNSUPPORTED) {
                            M.L("P2p Unsupported, connection failed");
                        } else {
                            M.L("P2p connect error : " + (i == WifiP2pManager.ERROR ? "Error" : "Busy"));
                        }
                        isConnected = false;
                    }
                });
            } else {
                M.L("Device is not in list.");
            }
        }
        return isConnected;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public MyPeersListWrapper getMyPeerListWrapper() {
        return myPeerListWrapper;
    }

    //start disocvery asynchronuously
    public void startDiscovery() {
        wifiP2pManager.discoverPeers(wifiP2pChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                M.L("Success in enabling discovery");
            }

            @Override
            public void onFailure(int i) {
                if (i == WifiP2pManager.BUSY || i == WifiP2pManager.ERROR) {
                    M.L("Failure :Trying to discover peers again");
                    wifiP2pManager.discoverPeers(wifiP2pChannel, this);
                }
            }
        });


    }

    //Wrapper to wrap wifiP2pDevices' changes
    public class MyPeersListWrapper {
        private List<WifiP2pDevice> wifiP2pDevices;
        private WifiP2pManager.PeerListListener myPeerListListener = new WifiP2pManager.PeerListListener() {

            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                MyPeersListWrapper.this.wifiP2pDevices = new ArrayList(wifiP2pDeviceList.getDeviceList());
                M.L("PeersListListener called, List Status :  " + (wifiP2pDeviceList != null));
            }
        };

        private MyPeersListWrapper() {

        }

        public boolean isDeviceInList(String address) {
            for (WifiP2pDevice device : wifiP2pDevices) {
                if (address.equals(device.deviceAddress))
                    return true;
            }
            return false;
        }

        public List<WifiP2pDevice> getWifiP2pDevices() {
            return wifiP2pDevices;
        }

        public WifiP2pManager.PeerListListener getMyPeerListListener() {
            return myPeerListListener;
        }
    }
}
