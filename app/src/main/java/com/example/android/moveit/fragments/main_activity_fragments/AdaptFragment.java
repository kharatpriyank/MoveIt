package com.example.android.moveit.fragments.main_activity_fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.moveit.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdaptFragment extends Fragment {


    public static final String TAG = "Adapt";

    public AdaptFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_adapt, container, false);
    }

}
