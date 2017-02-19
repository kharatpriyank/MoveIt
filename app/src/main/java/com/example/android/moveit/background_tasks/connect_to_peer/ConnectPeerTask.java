package com.example.android.moveit.background_tasks.connect_to_peer;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import com.example.android.moveit.adapters.WifiP2pWrapper;

public class ConnectPeerTask extends HandlerThread {
    public static final String CONNECT_PEER_TASK_TAG = ConnectPeerTask.class.getSimpleName();
    private WifiP2pWrapper wifiP2pWrapper;
    private Handler thisHandler, uiHandler;

    private String addressToConnect;

    public ConnectPeerTask(WifiP2pWrapper wifiP2pWrapper) {
        super(CONNECT_PEER_TASK_TAG, Process.THREAD_PRIORITY_BACKGROUND);
        this.wifiP2pWrapper = wifiP2pWrapper;

        thisHandler = new Handler();
        uiHandler = new Handler(Looper.getMainLooper());
        addressToConnect = null;
    }

    @Override
    public void run() {
        super.run();
        synchronized (wifiP2pWrapper) {
            if (wifiP2pWrapper != null && addressToConnect != null) {
                boolean isConnected = wifiP2pWrapper.connect(addressToConnect);
                Message message = Message.obtain();
                Bundle connectionStatus = new Bundle();
                connectionStatus.putBoolean(CONNECT_PEER_TASK_TAG, isConnected);
                message.setData(connectionStatus);
                uiHandler.sendMessage(message);
                quit();
            }
        }
    }

    public void setAddressToConnect(String addressToConnect) {
        this.addressToConnect = addressToConnect;
    }


}