package com.vtstudio.fxbox.broadcast;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;

import com.vtstudio.fxbox.media.player.FxPlayer;
import com.vtstudio.fxbox.notifications.MediaNotificationCreator;

public class BluetoothBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("FxPlayer", "onReceive");
        String action = intent.getAction();
        String playAction = null;
        if (action != null) {
            switch (action) {
                case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED:
                    int status = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE,  -1);
                    if(status == BluetoothAdapter.STATE_CONNECTED) {
                        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

                        if(isBluetoothReady(audioManager)) {
                            playAction = MediaNotificationCreator.ACTION_PLAY;
                            Log.d("FxPlayer", "onConnectionStateChanged Bluetooth adapter connected");
                        } else {
                            Log.d("FxPlayer", "Bluetooth connected but not ready. Waiting...");
                            // Dùng Runnable đệ quy để kiểm tra lại sau 500ms
                            Handler handler = new Handler();
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    if (isBluetoothReady(audioManager)) {
                                        Log.d("FxPlayer", "Bluetooth is now ready. Starting playback.");
                                        Intent mIntent = new Intent(context, FxPlayer.class);
                                        mIntent.setAction(MediaNotificationCreator.ACTION_PLAY);
                                        context.startService(mIntent);
                                    } else {
                                        Log.d("FxPlayer", "Bluetooth still not ready. Rechecking in 500ms...");
                                        handler.postDelayed(this, 500);
                                    }
                                }
                            };
                            handler.postDelayed(runnable, 500);
                        }

                    } else if(status == BluetoothA2dp.STATE_DISCONNECTED) {
                        Log.d("FxPlayer", "onConnectionStateChanged Bluetooth adapter disconnected");
                        playAction = MediaNotificationCreator.ACTION_PAUSE;
                    }
                    break;
//                case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED:
//                    int cStatus = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE,  -1);
//                    if(cStatus == BluetoothHeadset.STATE_CONNECTED) {
//                        playAction = MediaNotificationCreator.ACTION_PLAY;
//                    } else if(cStatus == BluetoothHeadset.STATE_DISCONNECTED) {
//                        playAction = MediaNotificationCreator.ACTION_PAUSE;
//                        Log.d("FxPlayer", "onConnectionStateChanged Bluetooth headset disconnected");
//                    }
//                    break;
//                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
//                    playAction = MediaNotificationCreator.ACTION_PAUSE;
//                    break;
            }
        }

        if(playAction != null) {
            Intent mIntent = new Intent(context, FxPlayer.class);
            mIntent.setAction(playAction);
            context.startService(mIntent);
        }
    }

    private static boolean isBluetoothReady(AudioManager audioManager) {
        AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
        boolean isBluetoothReady = false;
        for (AudioDeviceInfo device : devices) {
            if (device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP) {
                isBluetoothReady = true;
                break;
            }
        }

        return isBluetoothReady;
    }
}
