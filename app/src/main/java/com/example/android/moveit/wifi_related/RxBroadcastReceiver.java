package com.example.android.moveit.wifi_related;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.lang.ref.WeakReference;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;

/**
 * Created by Priyank on 12-03-2017.
 */


/**
 * RxJava based broadcast reciever that registers its local BroadcastReceiver until end of subscription.
 * Listens for update and passes Intent to the Stream (Subscriber).
 * <p>
 * Created on 11/18/16.
 */
public class RxBroadcastReceiver implements ObservableOnSubscribe<Intent>, Disposable {
    protected final WeakReference<Context> contextWeakReference;

    private Emitter<? super Intent> emitter;
    private IntentFilter intentFilter;
    private BroadcastReceiver broadcastReceiver;

    /**
     * @param context
     * @param intentFilter
     */
    private RxBroadcastReceiver(Context context, IntentFilter intentFilter) {
        contextWeakReference = new WeakReference<Context>(context.getApplicationContext());
        this.intentFilter = intentFilter;
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                emitter.onNext(intent);
            }
        };
    }

    /**
     * Creates Observable with intent filter for Broadcast receiver.
     *
     * @param context
     * @param intentFilter
     * @return
     */
    public static Observable<Intent> create(Context context, IntentFilter intentFilter) {
        return Observable.defer(() -> Observable.create(new RxBroadcastReceiver(context, intentFilter)));

    }

    @Override
    public void subscribe(ObservableEmitter<Intent> emitter) throws Exception {
        this.emitter = emitter;
        if (contextWeakReference != null && contextWeakReference.get() != null) {
            contextWeakReference.get().registerReceiver(broadcastReceiver, intentFilter);
        }
    }

    /**
     * Unregisters receiver, cleans its reference.
     */
    @Override
    public void dispose() {
        if (contextWeakReference != null && contextWeakReference.get() != null && broadcastReceiver != null) {
            contextWeakReference.get().unregisterReceiver(broadcastReceiver);

        }
        broadcastReceiver = null;
    }

    /**
     * Is Disposed.
     *
     * @return
     */
    @Override
    public boolean isDisposed() {
        return broadcastReceiver == null;
    }


}
