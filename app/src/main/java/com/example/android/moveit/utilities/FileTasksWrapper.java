package com.example.android.moveit.utilities;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Priyank on 02-03-2017.
 */

public class FileTasksWrapper {
    public static final int FILE_PICKER_CODE = 5593;

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        long startTime = System.currentTimeMillis();

        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
            inputStream.close();
            long endTime = System.currentTimeMillis() - startTime;
            Log.v("", "Time taken to transfer all bytes is : " + endTime);

        } catch (IOException e) {
            M.L(e.toString());
            return false;
        }
        return true;
    }
}
