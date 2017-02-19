package com.example.android.moveit.fragments.main_activity_fragments;


import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.moveit.R;
import com.example.android.moveit.activities.BarcodeCaptureActivity;
import com.example.android.moveit.activities.MainActivity;
import com.example.android.moveit.activities.ReceiveActivity;
import com.example.android.moveit.adapters.WifiP2pWrapper;
import com.example.android.moveit.background_tasks.connect_to_peer.ConnectPeerTask;
import com.example.android.moveit.utilities.M;
import com.example.android.moveit.utilities.qr_code_related.QRCodeManager;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import mehdi.sakout.fancybuttons.FancyButton;

import static com.example.android.moveit.adapters.WifiP2pWrapper.isListFound;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShareFragment extends Fragment {
    public static final String TAG = "Share";
    //View related regerences
    Unbinder unbinder;
    @BindView(R.id.sendBtn)
    FancyButton sendBtn;
    @BindView(R.id.receiveBtn)
    FancyButton receiveBtn;
    @BindView(R.id.connectionStatus)
    TextView connectionStatus;
    //other references
    private MainActivity mainActivity;
    private WifiP2pWrapper wifiP2PWrapper;
    private QRCodeManager qrCodeManager;
    private ConnectPeerTask connectPeerTask;

    public ShareFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        wifiP2PWrapper = WifiP2pWrapper.getInstance(mainActivity);
        qrCodeManager = QRCodeManager.getInstance(mainActivity);
        connectPeerTask = new ConnectPeerTask(wifiP2PWrapper);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_share, container, false);
        unbinder = ButterKnife.bind(this, view);
        //sendBtn Listener
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(
                        mainActivity, BarcodeCaptureActivity.class
                ), BarcodeCaptureActivity.REQUEST_CODE);
            }
        });
        //receiveBtnListener
        receiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifiP2PWrapper.setWifiStatus(true);
                wifiP2PWrapper.setSendState(false);
                Intent intent = new Intent(mainActivity, ReceiveActivity.class);
                mainActivity.startActivity(intent);


            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();
        wifiP2PWrapper.setWifiStatus(false);
        super.onDestroy();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BarcodeCaptureActivity.REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    Point[] p = barcode.cornerPoints;
                    String address = barcode.displayValue;
                    connectPeerTask.setAddressToConnect(address);
                    synchronized (isListFound) {
                        if (isListFound) {
                            connectPeerTask.start();
                            final boolean isConnected;
                            Handler handler = new Handler() {
                                public void handleMessage(Message message) {
                                    Bundle connectionStatus = message.getData();
                                    M.L("Connected or not : " + connectionStatus.getBoolean(ConnectPeerTask.CONNECT_PEER_TASK_TAG));
                                }
                            };
                        } else {
                            M.T(mainActivity, "WifiP2pNotInitialized, please try again");
                        }
                    }

                } else {
                    M.L("barcode return kartana jhol aahe");
                }
            } else {
                M.L("BarcodeCaptureActivity mdhe jhol aahe");
            }
        } else M.L("Request code didn't match");

    }
}
