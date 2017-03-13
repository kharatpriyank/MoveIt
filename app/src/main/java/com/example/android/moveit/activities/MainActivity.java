package com.example.android.moveit.activities;

import android.content.Intent;
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
import com.example.android.moveit.fragments.main_activity_fragments.AdaptFragment;
import com.example.android.moveit.fragments.main_activity_fragments.CloneFragment;
import com.example.android.moveit.fragments.main_activity_fragments.ShareFragment;
import com.example.android.moveit.utilities.M;
import com.example.android.moveit.utilities.qr_code_related.QRCodeManager;
import com.example.android.moveit.wifi_related.BrIntentFilterWrapper;
import com.example.android.moveit.wifi_related.WifiP2pWrapper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.Unbinder;
import io.reactivex.Observable;

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
    private WifiP2pWrapper wifiP2PWrapper;
    private BrIntentFilterWrapper brIntentFilterWrapper;
    private QRCodeManager qrCodeManager;
    private Observable<Intent> brObservable;
    // private Intent detectPeerServiceIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        myPagerAdapter.addFragment(shareFragment, ShareFragment.TAG);
        myPagerAdapter.addFragment(adaptFragment, AdaptFragment.TAG);
        myPagerAdapter.addFragment(cloneFragment, CloneFragment.TAG);
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
        wifiP2PWrapper = WifiP2pWrapper.getInstance(this);

        qrCodeManager = QRCodeManager.getInstance(this);
        wifiP2PWrapper.setWifiStatus(true);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        M.L("Inside MainActivity onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

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


