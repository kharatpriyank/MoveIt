package com.example.android.moveit.fragments.main_activity_fragments;


import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.moveit.R;
import com.example.android.moveit.activities.MainActivity;
import com.example.android.moveit.activities.ReceiveActivity;
import com.example.android.moveit.adapters.MyWifiP2pAdapter;
import com.example.android.moveit.utilities.M;
import com.example.android.moveit.utilities.qr_code_related.QRCodeManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import mehdi.sakout.fancybuttons.FancyButton;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShareFragment extends Fragment implements MainActivity.MainActivityCommunicator {
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
    private MyWifiP2pAdapter myWifiP2pAdapter;
    private QRCodeManager qrCodeManager;

    public ShareFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        myWifiP2pAdapter = MyWifiP2pAdapter.getInstance(mainActivity);
        qrCodeManager = QRCodeManager.getInstance(mainActivity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_share, container, false);
        unbinder = ButterKnife.bind(this,view);
        //sendBtn Listener
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myWifiP2pAdapter.setWifiStatus(true);
                myWifiP2pAdapter.setSendState(true);
                myWifiP2pAdapter.wifiP2pManager.discoverPeers(myWifiP2pAdapter.wifiP2pChannel,
                        new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                qrCodeManager.startDetection(mainActivity);
                                M.L("Detection Staarted");
                            }

                            @Override
                            public void onFailure(int i) {

                            }
                        });


            }
        });
        //receiveBtnListener
        receiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myWifiP2pAdapter.setWifiStatus(true);
                myWifiP2pAdapter.setSendState(false);
                Intent intent = new Intent(mainActivity, ReceiveActivity.class);
                startActivity(intent);


            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();
        myWifiP2pAdapter.setWifiStatus(false);
        super.onDestroy();

    }


    @Override
    public void handleMessage(String message) {
        connectionStatus.setText(message);
        if (myWifiP2pAdapter.connect(message)) {
            // M.T(mainActivity,"Establishing connection with "+message);
        } else {
            //  M.T(mainActivity,"Cannot establish connection with "+message);
        }
    }
}
