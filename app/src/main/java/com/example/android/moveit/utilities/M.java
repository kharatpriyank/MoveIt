package com.example.android.moveit.utilities;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Priyank on 10-02-2017.
 */

//Debugging class-->For Toasts and Logs.
public class M{

    public static String MY_TAG = "Priyankssss";
    public static void L(String message){
        Log.d(MY_TAG,message);
    }
    public static void T(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
