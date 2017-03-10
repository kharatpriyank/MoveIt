package com.example.android.moveit.activities;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.example.android.moveit.R;
import com.example.android.moveit.broadcast_receivers.MyWifiP2pBroadcastReceiver;
import com.example.android.moveit.utilities.BrIntentFilterWrapper;
import com.example.android.moveit.utilities.FileTasksWrapper;
import com.example.android.moveit.utilities.M;
import com.example.android.moveit.utilities.qr_code_related.QRCodeManager;
import com.example.android.moveit.utilities.qr_code_related.QRCodeShower;
import com.example.android.moveit.wifi_related.WifiP2pWrapper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ReceiveActivity extends AppCompatActivity implements FileTasksWrapper.ProgressShower, QRCodeShower, FileTasksWrapper.FileReceiver {
    public static final int TIMEOUT = 10000;
    Unbinder unbinder;
    @BindView(R.id.qrCodeBox)
    ImageView imageView;
    private ProgressDialog progressDialog;
    private MyWifiP2pBroadcastReceiver wifiP2pBroadcastReceiver;
    private WifiP2pWrapper wifiP2pAdapter;
    private QRCodeManager qrCodeManager;
    private BrIntentFilterWrapper intentFilterWrapper;
    private FileTasksWrapper fileTasksWrapper;


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
        fileTasksWrapper = FileTasksWrapper.getInstance(null, false);
        progressDialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
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
    public void displayQRCode(final Bitmap bitmap) {

        if (imageView != null) {
            imageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void receiveFile() {
        String contents = fileTasksWrapper.receiveFile();
//        if (contents != null)
//            M.T(this, contents);
    }

    @Override
    public void showProgress(String textCaption) {
        if (progressDialog != null) {
            progressDialog.setMessage(textCaption);
            progressDialog.show();
        } else {
            M.L("Can't show ProgressDialog since it's null, RecevieActivity");
        }
    }

    @Override
    public void hideProgress() {
        if (progressDialog != null) {
            progressDialog.hide();

        } else {
            M.L("Can't HIde ProgressDialog since it's null, RecevieActivity");
        }
    }
}
