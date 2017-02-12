package com.example.android.moveit.utilities.qr_code_related;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.example.android.moveit.utilities.M;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Created by Priyank on 11-02-2017.
 */

public class QRCodeManager {
    public static final int QR_WIDTH = 600;
    public static final int QR_HEIGHT = 600;


    //Singleton class, since only one instance is needed throughout application.
    private static QRCodeManager instance;
    private QRCodeWriter qrCodeWriter;
    private BarcodeDetector barcodeDetector;
    private Context context;
    private Frame qrCodeFrame;

    private QRCodeManager() {
         qrCodeWriter = new QRCodeWriter();

    }

    public void setBarcodeDetector(BarcodeDetector barcodeDetector) {
        this.barcodeDetector = barcodeDetector;
    }

    public BarcodeDetector getBarcodeDetector() {
        return barcodeDetector;
    }

    public void setContext(Context context) {
        this.context = context;

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
        instance.setBarcodeDetector(new BarcodeDetector.Builder(context).
                setBarcodeFormats(Barcode.QR_CODE).build());
        return instance;
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

    //QRCode Detection Logic
    public String getContents(Bitmap bitmap) {
        String contents = null;
        try {

        } catch (Exception e) {
            M.L(e.getMessage());
        } finally {
            return contents;
        }
    }
}


