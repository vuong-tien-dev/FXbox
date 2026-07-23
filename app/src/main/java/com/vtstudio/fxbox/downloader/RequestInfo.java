package com.vtstudio.fxbox.downloader;

import java.io.File;

public class RequestInfo {
    protected final int id;
    public boolean clearThumbnail;
    protected String fileName;
    protected String dir;
    protected String url;
    protected File file;
    protected String mineType;
    protected String thumbnailPath;
    protected String title;
    protected long contentLength;
    protected long currentBytes;
    protected boolean isPause = true;
    protected boolean isCompleted = false;
    protected boolean isWaitingConnect = false;
    protected boolean isFailed = false;
    protected boolean isStarted = false;
    protected boolean saveWithModels;
    public long getContentLength() {
        return contentLength;
    }

    public long getCurrentBytes() {
        return currentBytes;
    }

    public boolean isPause() {
        return isPause;
    }

    public boolean isFailed() {return isFailed;}

    public boolean isFileRename() {
        return isFileRename;
    }

    protected boolean isFileRename;

    public int getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public File getFile() {
        return file;
    }

    public String getDir() {
        return dir;
    }

    public String getUrl() {
        return url;
    }

    public RequestInfo(int id) {
        this.id = id;
        this.mineType = MineType.VIDEO_MP4;
    }

    public String getMineType() {
        return mineType;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public boolean isWaitingConnect() {
        return isWaitingConnect;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public boolean isSaveWithModels() {
        return saveWithModels;
    }
}

