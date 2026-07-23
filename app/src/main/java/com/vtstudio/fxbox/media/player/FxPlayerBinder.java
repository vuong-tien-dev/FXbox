package com.vtstudio.fxbox.media.player;

import android.os.Binder;

import java.lang.ref.WeakReference;

public class FxPlayerBinder extends Binder implements FxPlayerServiceBinder {
    private final WeakReference<FxPlayer> service;

    public FxPlayerBinder(FxPlayer service) {
        this.service = new WeakReference<FxPlayer>(service);
    }

    @Override
    public FxPlayer getService() {
        return service.get();
    }
}
