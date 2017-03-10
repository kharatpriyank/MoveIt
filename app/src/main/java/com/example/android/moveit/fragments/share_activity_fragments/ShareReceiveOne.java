package com.example.android.moveit.fragments.share_activity_fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.moveit.R;
import com.example.android.moveit.wifi_related.WifiP2pWrapper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShareReceiveOne extends Fragment {
    Unbinder unbinder;
    @BindView(R.id.qrCodeBox)
    ImageView qrCodeBox;

    private Activity context;
    private WifiP2pWrapper wifiP2pAdapter;
    public ShareReceiveOne() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
        wifiP2pAdapter = WifiP2pWrapper.getInstance(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_share_receive_one, container, false);
        unbinder = ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }
}
