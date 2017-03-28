package com.example.android.moveit.file_related;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.example.android.moveit.R;
import com.example.android.moveit.utilities.M;
import com.example.android.moveit.wifi_related.WifiP2pWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by Priyank on 02-03-2017.
 */

public class FileTasksWrapper {
    public static final int DATA_PORT = 6699;
    public static final int META_DATA_PORT = 6999;
    public static final int FILE_PICKER_CODE = 5593;
    public static final int TIMEOUT = 5000;
    public static final int MAX_PROGRESS = 100;
    private static final int BYTE_BUFF = 1024;
    private static final String FOLDER_NAME = "MoveIt";

    private static ProgressDialog progressDialog;

    //singleton object
    private static FileTasksWrapper instatnce;
    private File receiveFile;
    private FileStateObject fileStateObject;
    private Kryo metaDataCryo;
    private WifiP2pWrapper wifiP2pWrapper;
    private PublishSubject<FileStateObject> fileStateObjectPublishSubject;
    private boolean isFirstMetadataSent;


    private InetAddress receiverAddress;


    private FileTasksWrapper() {
        metaDataCryo = new Kryo();
        metaDataCryo.register(FileStateObject.class);
        fileStateObjectPublishSubject = PublishSubject.create();
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
        instatnce.isFirstMetadataSent = false;
        return instatnce;
    }


    public void sendFile() {
        Socket clientMetaSocket = null;
        Socket clientDataSocket = null;
        OutputStream clientMetaOutputStream = null, clientDataOutputStream = null;
        InputStream fileInputStream = null;
        try {
            clientMetaSocket = new Socket();
            clientDataSocket = new Socket();
            clientDataSocket.bind(null);
            clientMetaSocket.bind(null);
            clientMetaSocket.connect(new InetSocketAddress(receiverAddress, META_DATA_PORT));
            if (clientMetaSocket.isConnected()) {
                clientMetaOutputStream = clientMetaSocket.getOutputStream();
                sendMetaData(clientMetaOutputStream);
                clientDataSocket.connect(new InetSocketAddress(receiverAddress, DATA_PORT));
                if (clientDataSocket.isConnected()) {
                    clientDataOutputStream = clientDataSocket.getOutputStream();
                    fileInputStream = new FileInputStream(receiveFile);
                    copyStreamData(fileInputStream, clientDataOutputStream);
                    M.L("Inside FileTasksWrapper::sendFile : dataSocket Connected");
                } else {
                    M.L("Inside FileTasksWrapper::sendFile : dataSocket notConnected");
                }
            } else {
                M.L("Inside FileTasksWrapper::sendFile : metaDataSocket not connected");
            }
        } catch (Exception e) {
            M.L("Inside FileTasksWrapper::sendFile : try exception-->" + e.getMessage());

        } finally {
            try {
                if (clientMetaSocket != null) {
                    clientMetaSocket.close();
                    M.L("Inside FileTasksWrapper::sendFile : clientMetaSocket close");
                }
                if (clientDataSocket != null) {
                    clientDataSocket.close();
                    M.L("Inside FileTasksWrapper::receiveFile : clientDataSocket close");
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                    M.L("Inside FileTasksWrapper::sendFile : fileInputStream close");

                }
                if (clientMetaOutputStream != null) {
                    clientMetaOutputStream.close();
                    M.L("Inside FileTasksWrapper::sendFile : clientMetaOutputStream close");

                }
                if (clientDataOutputStream != null) {
                    clientDataOutputStream.close();
                    M.L("Inside FileTasksWrapper::sendFile : clientDataOutputStream close");

                }
            } catch (Exception e) {
                M.L("Inside FileTasksWrapper::sendFile : finally exception-->" + e.getMessage());
            }
        }
    }

    public void receiveFile() {
        ServerSocket metaServerSocket = null, dataServerSocket = null;
        Socket metaReceiveSocket = null, dataReceiverSocket = null;
        InputStream metaInputStream = null, dataInputStream = null;
        OutputStream metaOutputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            metaServerSocket = new ServerSocket(META_DATA_PORT);
            dataServerSocket = new ServerSocket(DATA_PORT);
            metaReceiveSocket = metaServerSocket.accept();
            dataReceiverSocket = dataServerSocket.accept();
            if (metaReceiveSocket.isConnected()) {
                metaInputStream = metaReceiveSocket.getInputStream();
                M.L("Inside FileTasksWrapper::receiveFile : metaSocket connected.");
                receiveMetaData(metaInputStream);
                if (dataReceiverSocket.isConnected()) {
                    dataInputStream = dataReceiverSocket.getInputStream();
                    M.L("Inside FileTasksWrapper::receiveFile : dataSocket Connected.");
                    File folder = new File(Environment.getExternalStorageDirectory(), FOLDER_NAME);
                    if (!folder.exists()) {
                        folder.mkdirs();
                        M.L("Inside FileTasksWrapper::receiveFile : folder " + FOLDER_NAME + " created");
                    }
                    File file = new File(folder, fileStateObject.getFileName());
                    file.createNewFile();
                    fileOutputStream = new FileOutputStream(file);
                    copyStreamData(dataInputStream, fileOutputStream);
                    M.L("Inside FileTasksWrapper::receiveFile : File created, now start copying...");


                } else {
                    M.L("Inside FileTasksWrapper::receiveFile : dataSocket not connected.");
                }
            } else {
                M.L("Inside FileTasksWrapper::receiveFile : metaSocket not connected");
            }


        } catch (Exception e) {
            M.L("Inside FileTasksWrapper::receiveFile : Try exception-->" + e.getMessage());

        } finally {
            try {
                if (metaReceiveSocket != null) {
                    metaReceiveSocket.close();
                    M.L("Inside FileTasksWrapper::receiveFile : metaReciveSocket close");
                }
                if (dataReceiverSocket != null) {
                    dataReceiverSocket.close();
                    M.L("Inside FileTasksWrapper::receiveFile : dataReceiverSocket close");
                }
                if (dataServerSocket != null) {
                    dataServerSocket.close();
                    M.L("Inside FileTasksWrapper::receiveFile : dataServerSocket close");
                }
                if (metaServerSocket != null) {
                    metaServerSocket.close();
                    M.L("Inside FileTasksWrapper::receiveFile : metaServerSocket close");
                }
                if (metaInputStream != null) {
                    metaInputStream.close();
                    M.L("Inside FileTasksWrapper::receiveFile : metaInputStream close");
                }
                if (metaOutputStream != null) {
                    metaOutputStream.close();
                    M.L("Inside FileTasksWrapper::receiveFile : metaOutputStream close");
                }
                if (dataInputStream != null) {
                    dataInputStream.close();
                    M.L("Inside FileTasksWrapper::receiveFile : dataInputStream close");
                }

                if (fileOutputStream != null) {
                    fileOutputStream.close();
                    M.L("Inside FileTasksWrapper::receiveFile : fileOutputStream close");
                }

            } catch (Exception e) {
                M.L("Inside FileTasksWrapper::receiveFile : finally exception-->" + e.getMessage());
            }
        }
    }


    private boolean sendMetaData(OutputStream outputStream) {
        try {

            if (outputStream != null) {
                Output output = new Output(outputStream);
                metaDataCryo.writeObject(output, fileStateObject);
                fileStateObjectPublishSubject.onNext(fileStateObject);
                isFirstMetadataSent = true;
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
                M.L("Inside FileTasksWrapper::receiveMetadata: File size-->" + fileStateObject.getSize());
                fileStateObjectPublishSubject.onNext(fileStateObject);
                isFirstMetadataSent = true;
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

    public void copyStreamData(InputStream inputStream, OutputStream out) throws IOException {
        byte buf[] = new byte[BYTE_BUFF];
        int len;
        long startTime = System.currentTimeMillis();
        while ((len = inputStream.read(buf)) != -1) {
            out.write(buf, 0, len);
            fileStateObject.setProgress(fileStateObject.getProgress() + len);
            fileStateObjectPublishSubject.onNext(fileStateObject);
        }
        long endTime = System.currentTimeMillis() - startTime;
        M.L("Inside FileTasksWrapper:: copyStreamData : Time Required to complete data transfer : " + endTime);
    }


    public InetAddress getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(InetAddress receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public void setReceiveFile(File receiveFile) {
        this.receiveFile = receiveFile;
        fileStateObject = new FileStateObject();
        fileStateObject.setFileName(this.receiveFile.getName());
        fileStateObject.setSize(this.receiveFile.length());
    }

    public Observable<FileStateObject> getMetaDataObservable() {
        return fileStateObjectPublishSubject;
    }


    public boolean isFirstMetadataSent() {
        return isFirstMetadataSent;
    }

    public static void showProgressDialog(Context context,String message){
        progressDialog = new ProgressDialog(context,ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle(context.getString(R.string.transferring_file));
        progressDialog.setMessage(message);
        progressDialog.setMax(MAX_PROGRESS);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        M.L("Inside FileTasksWrapper(Static method)::showProgressDialog.");
        progressDialog.show();
    }

    public static void updateProgress(int progress){
        if(progressDialog != null && progressDialog.isShowing()){
            progressDialog.setProgress(progress);
        }else{
            M.L("Inside FileTasksWrapper(Static method):: update progress, progressDialog is null or not showing.");
        }
    }

    public static void hideProgressDialog(){
        if(progressDialog.isShowing()){
            M.L("Inside FileTasksWrapper(Static method)::showProgressDialog.");
            progressDialog.dismiss();
        }

    }
}
