package com.vtstudio.fxbox.media.models;

import androidx.room.Ignore;

public class ApiModel extends BaseModel{
    protected String rawResponse;
    protected String responseServer;
    protected long timeDownloaded;

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    public String getResponseServer() {
        return responseServer;
    }

    public void setResponseServer(String responseServer) {
        this.responseServer = responseServer;
    }

    public long getTimeDownloaded() {
        return timeDownloaded;
    }

    public void setTimeDownloaded(long timeDownloaded) {
        this.timeDownloaded = timeDownloaded;
    }
}
