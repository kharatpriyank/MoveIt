package com.example.android.moveit.file_related;

/**
 * Created by priyank on 18/3/17.
 */

public class FileStateObject {
    private long size;
    private long transferredBytes;
    private String fileName;

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }



    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getTransferredBytes() {
        return transferredBytes;
    }

    public void setTransferredBytes(long transferredBytes) {
        this.transferredBytes = transferredBytes;
    }
    public void addTranseferBytes(long transferredBytes){
        this.transferredBytes += transferredBytes;
    }
}