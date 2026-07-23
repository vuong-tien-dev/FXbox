package com.vtstudio.fxbox.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.vtstudio.fxbox.listeners.OnContentEventsListener;
import com.vtstudio.fxbox.services.ContentService;

public class ContentTransmitter extends BroadcastReceiver {
    private OnContentEventsListener listener;
    private boolean isRegister = false;
    private Context context;

    public Context getContext() {
        return context;
    }
    public ContentTransmitter(Context context) {
        this.context = context;
    }

    public void setOnContentEventsListener(OnContentEventsListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Xử lý sự kiện khi dữ liệu video đã được tải xong
        // Ví dụ: cập nhật giao diện người dùng để hiển thị danh sách các video
        Log.d("TAG", "start onReceive ") ;

        if(listener == null)
        {
            return;
        }

        String action = intent.getStringExtra(ContentService.ACTION_KEY);

        switch (action)
        {
            case ContentService.ON_LOADED_VIDEO_FROM_STORAGE:
                Log.d("TAG", "start call on loaded ");
                listener.onLoadedVideosFromStorage();
                break;
            case ContentService.ON_DATASTORE_SYNCHRONIZED:
                Log.d("TAG", "start call on sync " );
                listener.onDataSoreSynchronized();
                break;
        }
    }
    public void addAction(String action)
    {
        if(isRegister) {
            Intent intent = new Intent(context, ContentService.class).putExtra(ContentService.ACTION_KEY, action);
            context.startService(intent);
        }
    }

    public void startCommunication()
    {
        if(!isRegister)
        {
            LocalBroadcastManager.getInstance(context).registerReceiver(this, new IntentFilter(ContentService.CONTENT_ACTION));
            isRegister = true;
        }
    }

    public void release (){
        context = null;
        listener = null;
    }

    public void endCommunication()
    {
        if(isRegister)
        {
            try {
                context.unregisterReceiver(this);
            } catch (IllegalArgumentException e) {
                // do nothing
            }
            isRegister = false;
        }
    }
}