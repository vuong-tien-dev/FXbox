package com.vtstudio.fxbox.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

import com.vtstudio.fxbox.downloader.FXDownloader;

public class NetworkChangeReceiver extends BroadcastReceiver {

    public static final String ACTION_NETWORK_AVAILABLE = "NETWORK_AVAILABLE";
    public static final String ACTION_NETWORK_NOT_AVAILABLE = "NETWORK_NOT_AVAILABLE";

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);

        Log.d("FXDownloader", "onReceive");
        // Kiểm tra trạng thái kết nối mạng
        if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            // Mạng có sẵn
            // TODO: Xử lý khi mạng khả dụng trở lại

            // Gửi thông báo đến FXDownloader
            Intent serviceIntent = new Intent(context, FXDownloader.class);
            serviceIntent.setAction(ACTION_NETWORK_AVAILABLE);
            context.startService(serviceIntent);
        } else {
            // Mạng không khả dụng
            // TODO: Xử lý khi mạng bị mất

            // Gửi thông báo đến FXDownloader
            Intent serviceIntent = new Intent(context, FXDownloader.class);
            serviceIntent.setAction(ACTION_NETWORK_NOT_AVAILABLE);
            context.startService(serviceIntent);
        }
    }
}