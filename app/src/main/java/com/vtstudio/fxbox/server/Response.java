package com.vtstudio.fxbox.server;

public class Response<T> {
    private final T model;
    private final boolean isSuccess;
    private final String server;
    private final OnResponseListener.Errol errol;

    public Response(T model, boolean isSuccess, String server, OnResponseListener.Errol errol) {
        this.model = model;
        this.isSuccess = isSuccess;
        this.server = server;
        this.errol = errol;
    }

    public T getModel() {
        return model;
    }

    public boolean isSuccessfully() {
        return isSuccess;
    }

    public String getServer() {
        return server;
    }

    public OnResponseListener.Errol getErrol() {
        return errol;
    }
}
