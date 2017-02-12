package com.example.android.moveit.activities;

import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.android.moveit.R;
import com.example.android.moveit.adapters.MyWifiP2pAdapter;
import com.example.android.moveit.broadcast_receivers.MyWifiP2pBroadcastReceiver;
import com.example.android.moveit.fragments.main_activity_fragments.AdaptFragment;
import com.example.android.moveit.fragments.main_activity_fragments.CloneFragment;
import com.example.android.moveit.fragments.main_activity_fragments.ShareFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.Unbinder;

import static butterknife.ButterKnife.bind;

public class MainActivity extends AppCompatActivity {
    //UI related references
    @BindView(R.id.myToolBar)
    Toolbar myToolbar;
    @BindView(R.id.myTabLayout)
    TabLayout myTabLayout;
    @BindView(R.id.myViewPager)
    ViewPager myViewPager;
    private MyFragmentPagerAdapter myPagerAdapter;
    private Unbinder unbinder;
    private ShareFragment shareFragment;
    private CloneFragment cloneFragment;
    private AdaptFragment adaptFragment;

    //other References
    private MyWifiP2pAdapter myWifiP2pAdapter;
    private MyWifiP2pBroadcastReceiver myWifiP2pBroadcastReceiver;
    private IntentFilter wifiP2pFilter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        myPagerAdapter.addFragment(shareFragment,ShareFragment.TAG);
        myPagerAdapter.addFragment(adaptFragment,AdaptFragment.TAG);
        myPagerAdapter.addFragment(cloneFragment,CloneFragment.TAG);
        myViewPager.setAdapter(myPagerAdapter);
        myTabLayout.setupWithViewPager(myViewPager);
    }

    private void init() {
        unbinder = bind(this);
        setSupportActionBar(myToolbar);
        shareFragment = new ShareFragment();
        cloneFragment = new CloneFragment();
        adaptFragment = new AdaptFragment();
        myPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        myWifiP2pAdapter = MyWifiP2pAdapter.getInstance(this);
        myWifiP2pBroadcastReceiver = MyWifiP2pBroadcastReceiver.getInstance(myWifiP2pAdapter);

        //WIfiP2p IntentFilter
        wifiP2pFilter = new IntentFilter();
        wifiP2pFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiP2pFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        wifiP2pFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiP2pFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        wifiP2pFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(myWifiP2pBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(myWifiP2pBroadcastReceiver,wifiP2pFilter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;
        private List<String> titles;

        MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new ArrayList<>();
            titles = new ArrayList<>();

        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }
}
