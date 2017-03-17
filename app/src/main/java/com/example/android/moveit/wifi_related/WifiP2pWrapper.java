package com.example.android.moveit.wifi_related;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import com.example.android.moveit.utilities.M;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by Priyank on 10-02-2017.
 */

//Singleton class
public class WifiP2pWrapper {
    public static final String DISCOVERY_STATE_DISCOVERY_STARTED = "discovery_started";
    public static final String DISCOVERY_STATE_DISCOVERY_NOT_STARTED = "discovered_not_started";
    public static final int CONNECTION_SUCCESS_CODE = 99;
    public static final int CONNECTION_UNSUCCESSFULL_CODE = 100;
    public static final int CONNECTION_INDETERMINATE_CODE = 101;

    private static WifiP2pWrapper instance;
    public WifiP2pManager wifiP2pManager;
    public WifiP2pManager.Channel wifiP2pChannel;
    private WifiManager wifiManager;
    private boolean isSendState;
    private Context context;
    private Observable<Intent> wifiP2pBRObservable;
    private Observable<String> discoveryObservable;
    private Observable<List<WifiP2pDevice>> peersListObservable;
    private Observable<WifiP2pInfo> wifiP2pConnectionObservable;
    private List<WifiP2pDevice> wifiP2pDevices;



    //Private Constructor for singleton
    private WifiP2pWrapper(final Context context) {
        this.context = context;
        wifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        wifiP2pChannel = wifiP2pManager.initialize(context, context.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                M.L("WifiP2pManager channel Disconnected.");
            }
        });
        wifiP2pBRObservable = RxBroadcastReceiver.create(context,
                BrIntentFilterWrapper.getInstance().wifiP2pFilter);
        discoveryObservable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> e) throws Exception {
                wifiP2pManager.discoverPeers(wifiP2pChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        M.L("Inside DiscoveryObservable : " + DISCOVERY_STATE_DISCOVERY_STARTED);
                        e.onNext(DISCOVERY_STATE_DISCOVERY_STARTED);

                    }

                    @Override
                    public void onFailure(int i) {
                        M.L("Inside DiscoveryObservable : " + DISCOVERY_STATE_DISCOVERY_NOT_STARTED);
                        wifiP2pManager.discoverPeers(wifiP2pChannel, this);
                    }
                });
            }
        });
        peersListObservable = Observable.create(new ObservableOnSubscribe<List<WifiP2pDevice>>() {
            @Override
            public void subscribe(final ObservableEmitter<List<WifiP2pDevice>> e) throws Exception {
                wifiP2pManager.requestPeers(wifiP2pChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                        List<WifiP2pDevice> wifiP2pDevices
                                = new ArrayList<WifiP2pDevice>(wifiP2pDeviceList.getDeviceList());
                        M.L("Inside PeersListObservable : List changed");
                        e.onNext(wifiP2pDevices);
                    }
                });
            }
        });
        wifiP2pConnectionObservable = Observable.create(new ObservableOnSubscribe<WifiP2pInfo>() {
            @Override
            public void subscribe(final ObservableEmitter<WifiP2pInfo> e) throws Exception {
                wifiP2pManager.requestConnectionInfo(wifiP2pChannel, new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
                        M.L("Inside ConnectionInfoObservable : connection changed");
                        e.onNext(wifiP2pInfo);
                    }
                });
            }
        });


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

    public void connect(String address) {
        if ((wifiP2pDevices != null) && (!wifiP2pDevices.isEmpty())) {
            boolean isDeviceInList = false;
            for (WifiP2pDevice wifiP2pDevice : wifiP2pDevices) {
                if (wifiP2pDevice.deviceAddress.equals(address))
                    isDeviceInList = true;
            }
            if (isDeviceInList) {
                WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
                wifiP2pConfig.deviceAddress = address;
                wifiP2pManager.connect(wifiP2pChannel, wifiP2pConfig, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        //ConnectionInfoListener will notify us , no need to go ahead here.
                        M.L("Inside connect Method : Connection successfull");
                    }

                    @Override
                    public void onFailure(int i) {
                        M.L("Inside Connect method : Connection Unsuccessfull");
                    }
                });
            } else {
                M.L("Inside Connect Method : Device not in the peer list.");
            }
        } else {
            M.L("Inside Connect Method : Peer devices not discovered");
        }
    }


    //Setters
    public void setWifiStatus(boolean isOn) {
        wifiManager.setWifiEnabled(isOn);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    //Getters
    public boolean isWifiOn() {
        return wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
    }

    public boolean isSendState() {
        return isSendState;
    }

    public void setSendState(boolean sendState) {
        isSendState = sendState;
    }

    public Observable<Intent> getWifiP2pBRObservable() {
        return wifiP2pBRObservable;
    }

    public Observable<String> getDiscoveryObservable() {
        return discoveryObservable;
    }

    public Observable<WifiP2pInfo> getWifiP2pConnectionObservable() {
        return wifiP2pConnectionObservable;
    }

    public Observable<List<WifiP2pDevice>> getPeersListObservable() {
        return peersListObservable;
    }

    public List<WifiP2pDevice> getWifiP2pDevices() {
        return wifiP2pDevices;
    }

    public void setWifiP2pDevices(List<WifiP2pDevice> wifiP2pDevices) {
        this.wifiP2pDevices = wifiP2pDevices;
    }


}
