package com.example.android.moveit.wifi_related;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import io.reactivex.Observable;

/**
 * Created by Priyank on 10-02-2017.
 */

//Singleton BroadcastReceiverObservable getter
public class WifiP2pEventsObservableCreator {
    public static Observable<Intent> broadcastReceiverObservable;

    public synchronized static Observable<Intent> getBroadcastReceiverObservable(Context context, IntentFilter intentFilter) {
        if (broadcastReceiverObservable == null) {
            broadcastReceiverObservable = RxBroadcastReceiver.create(context, intentFilter);
        }
        return broadcastReceiverObservable;
    }
}
