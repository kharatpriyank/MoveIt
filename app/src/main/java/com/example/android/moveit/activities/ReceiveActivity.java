package com.example.android.moveit.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.example.android.moveit.R;
import com.example.android.moveit.adapters.WifiP2pWrapper;
import com.example.android.moveit.broadcast_receivers.MyWifiP2pBroadcastReceiver;
import com.example.android.moveit.utilities.BrIntentFilterWrapper;
import com.example.android.moveit.utilities.qr_code_related.QRCodeManager;
import com.example.android.moveit.utilities.qr_code_related.QRCodeShower;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ReceiveActivity extends AppCompatActivity implements QRCodeShower {
    Unbinder unbinder;
    @BindView(R.id.qrCodeBox)
    ImageView imageView;
    private MyWifiP2pBroadcastReceiver wifiP2pBroadcastReceiver;
    private WifiP2pWrapper wifiP2pAdapter;
    private QRCodeManager qrCodeManager;
    private BrIntentFilterWrapper intentFilterWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        unbinder = ButterKnife.bind(this);
        init();

    }

    private void init() {
        qrCodeManager = QRCodeManager.getInstance(this);
        wifiP2pAdapter = WifiP2pWrapper.getInstance(this);
        intentFilterWrapper = BrIntentFilterWrapper.getInstance();
        wifiP2pBroadcastReceiver = MyWifiP2pBroadcastReceiver.getInstance(wifiP2pAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiP2pBroadcastReceiver);

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(wifiP2pBroadcastReceiver, intentFilterWrapper.wifiP2pFilter);
    }

    @Override
    protected void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }

    @Override
    public void displayQRCode(Bitmap bitmap) {
        if (imageView != null) {
            imageView.setImageBitmap(bitmap);
        }
    }
}
