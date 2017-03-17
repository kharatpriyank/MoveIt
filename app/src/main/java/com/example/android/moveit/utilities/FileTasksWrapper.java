package com.example.android.moveit.utilities;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Priyank on 02-03-2017.
 */

public class FileTasksWrapper {
    public static final int MY_PORT = 6969;
    public static final int FILE_PICKER_CODE = 5593;
    public static final int TIMEOUT = 5000;
    //singleton object
    private static FileTasksWrapper instatnce;
    private File file;



    private InetAddress receiverAddress;


    private FileTasksWrapper() {

    }

    //Singleton creator.
    public static FileTasksWrapper getInstance() {
        if (instatnce == null) {
            synchronized (FileTasksWrapper.class) {
                if (instatnce == null) {
                    instatnce = new FileTasksWrapper();
                }
            }
        }
        return instatnce;
    }

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

    public boolean sendAFile() {
        Socket sendSocket = null;
        InputStream is = null;
        OutputStream out = null;
        try {
            sendSocket = new Socket();
            sendSocket.bind(null);
            sendSocket.connect(new InetSocketAddress(receiverAddress, MY_PORT), TIMEOUT);
            M.L("Socket connected");
            is = new ByteArrayInputStream("THis is something to send".getBytes());
            out = sendSocket.getOutputStream();
            copyFile(is, out);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
                    M.L("Send file error : " + e.getMessage());
            return false;
        } finally {
            try {
                if (sendSocket != null)
                    sendSocket.close();
                if (is != null)
                    is.close();
                if (out != null)
                    out.close();
            } catch (Exception e) {
                M.L("Errror in sendFile finally : " + e.getMessage());
            }

        }
    }

    public String receiveFile() {
        ServerSocket serverSocket = null;
        Socket socket = null;
        InputStream inputStream = null;
        StringBuffer sb = new StringBuffer();


        try {
            serverSocket = new ServerSocket(MY_PORT);
            socket = serverSocket.accept();
            socket.setReuseAddress(true);
            if (socket.isConnected()) {
                M.L("Socket Connected");
                inputStream = socket.getInputStream();
                Scanner sc = new Scanner(inputStream);
                while (sc.hasNext())
                    sb.append(sc.next());
                M.L("SUccess : " + sb.toString());
                return sb.toString();
            } else return "kahi ahi jhala re babba";

        } catch (IOException e) {
            M.L("Inside receiveFile error : " + e.getMessage());
            return "Kahi nahi re bbaba";
        } finally {
            try {
                if (socket != null)
                    socket.close();
                if (serverSocket != null)
                    serverSocket.close();
                if (inputStream != null)
                    inputStream.close();
            } catch (Exception e) {
                M.L("Inside receiveFile finally error : " + e.getMessage());
                return "Kahi nahi re babba";
            }
        }

    }

    public InetAddress getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(InetAddress receiverAddress) {
        this.receiverAddress = receiverAddress;
    }


    public void setFile(File file) {
        this.file = file;
    }
}
