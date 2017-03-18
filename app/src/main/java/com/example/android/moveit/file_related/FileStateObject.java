package com.example.android.moveit.file_related;

/**
 * Created by priyank on 18/3/17.
 */

public class FileStateObject {
    private long size, progress;
    private String fileName;

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}