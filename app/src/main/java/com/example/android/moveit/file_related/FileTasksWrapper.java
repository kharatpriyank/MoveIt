package com.example.android.moveit.file_related;

import android.util.Log;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.example.android.moveit.utilities.M;
import com.example.android.moveit.wifi_related.WifiP2pWrapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Priyank on 02-03-2017.
 */

public class FileTasksWrapper {
    public static final int DATA_PORT = 6699;
    public static final int META_DATA_PORT = 6999;
    public static final int FILE_PICKER_CODE = 5593;
    public static final int TIMEOUT = 5000;
    public static final String UTF_8 = "UTF-8";
    private static final int BYTE_BUFF = 1024;
    //singleton object
    private static FileTasksWrapper instatnce;
    private File file;
    private FileStateObject fileStateObject;
    private Kryo metaDataCryo;
    private WifiP2pWrapper wifiP2pWrapper;


    private InetAddress receiverAddress;


    private FileTasksWrapper() {
        metaDataCryo = new Kryo();
        metaDataCryo.register(FileStateObject.class);
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


    public void sendFile() {
        Socket clientMetaSocket = null;

        OutputStream clientMetaOutputStream = null;
        try {
            clientMetaSocket = new Socket();
            clientMetaSocket.bind(null);
            clientMetaSocket.connect(new InetSocketAddress(receiverAddress, META_DATA_PORT));
            if (clientMetaSocket.isConnected()) {
                clientMetaOutputStream = clientMetaSocket.getOutputStream();
                sendMetaData(clientMetaOutputStream);
            } else {
                M.L("Inside FileTasksWrapper::sendFile : socket not connected");
            }
        } catch (Exception e) {
            M.L("Inside FileTasksWrapper::sendFile : exception-->" + e.getMessage());

        } finally {
            try {
                if (clientMetaSocket != null)
                    clientMetaSocket.close();
                if (clientMetaOutputStream != null)
                    clientMetaOutputStream.close();
            } catch (Exception e) {
                M.L("Inside FileTasksWrapper::sendFile : exception-->" + e.getMessage());
            }
        }
    }

    public void receiveFile(){
        ServerSocket metaServerSocket = null;
        Socket metaReceiveSocket = null;
        InputStream metaInputStream = null;
        OutputStream metaOutputStream = null;
        try{
            metaServerSocket = new ServerSocket(META_DATA_PORT);
            metaReceiveSocket = metaServerSocket.accept();
            metaInputStream = metaReceiveSocket.getInputStream();
            receiveMetaData(metaInputStream);
        }catch(Exception e){

        }finally{
           try{
               if(metaReceiveSocket != null)
                   metaReceiveSocket.close();
               if(metaServerSocket != null)
                   metaServerSocket.close();
               if(metaInputStream != null){
                   metaInputStream.close();
               }if(metaOutputStream != null){
                   metaOutputStream.close();
               }
           }catch(Exception e){
               M.L("Inside FileTasksWrapper::receiveFile : exception-->" + e.getMessage());
           }
        }
    }


    private boolean sendMetaData(OutputStream outputStream) {
        try {

            if (outputStream != null) {
                Output output = new Output(outputStream);
                metaDataCryo.register(FileStateObject.class);
                metaDataCryo.writeObject(output, fileStateObject);
                output.close();
                return true;

            } else {
                M.L("Inside FileTasksWrapper::sendMetaData : outputSteram is null.");
                return false;
            }


        } catch (Exception e) {
            M.L("Inside FileTasksWrapper:: sendMetaData : exception-->" + e.getMessage());
            return false;
        }
    }



    private boolean receiveMetaData(InputStream inputStream) {
        try {
            if (inputStream != null) {
                fileStateObject = null;
                Input input = new Input(inputStream);
                fileStateObject = metaDataCryo.readObject(input, FileStateObject.class);
                input.close();

            } else {
                M.L("Inside FileTasksWrapper::receiveMetaData : inputStream is null.");
                return false;
            }
        } catch (Exception e) {
            M.L("Inside FileTasksWrapper::receiveMetaData : exception-->" + e.getMessage());
        } finally {
            if (fileStateObject != null) {
                M.L("Inside FileTasksWrapper:: receiveMetaData : FileStateObject received. " + fileStateObject.getFileName());
                return true;

            } else {
                return false;
            }
        }
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


    public InetAddress getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(InetAddress receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public void setFile(File file) {
        this.file = file;
        fileStateObject = new FileStateObject();
        fileStateObject.setFileName(file.getName());
    }




}
