package com.example.android.moveit.utilities.qr_code_related;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.example.android.moveit.activities.BarcodeCaptureActivity;
import com.example.android.moveit.utilities.M;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Created by Priyank on 11-02-2017.
 */

public class QRCodeManager {
    public static final int BARCODE_READER_REQUEST_CODE = 1;
    public static final int QR_WIDTH = 600;
    public static final int QR_HEIGHT = 600;


    //Singleton class, since only one instance is needed throughout application.
    private static QRCodeManager instance;
    private QRCodeWriter qrCodeWriter;
    private Context context;

    private QRCodeManager() {
        qrCodeWriter = new QRCodeWriter();

    }

    //Singleton Generator
    public static QRCodeManager getInstance(Context context) {
        if (instance == null) {
            synchronized (QRCodeManager.class) {
                if (instance == null) {
                    instance = new QRCodeManager();

                }
            }
        }
        instance.setContext(context);

        return instance;
    }

    public void setContext(Context context) {
        this.context = context;

    }

    //QRCode generation logic
    public Bitmap generateQrCode(String contents, int width, int height) {
        Bitmap bitmap = null;
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = qrCodeWriter.encode(contents, BarcodeFormat.QR_CODE, width, height);
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    bitmap.setPixel(i, j, bitMatrix.get(i, j) ? Color.BLACK : Color.WHITE);
                }
            }

        } catch (Exception e) {
            M.L(e.getMessage());
        } finally {
            return bitmap;
        }
    }

    //get captured QR code
    public String getQRCode(int requestCode, int resultCode, Intent data) {
        String returnVal = null;
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    returnVal = barcode.displayValue;
                    M.L("Address scanned : " + returnVal);
                } else {
                    M.L("Address not scanned");
                }

            }

        }
        return returnVal;
    }
}


