package com.vtstudio.fxbox.activity;

import android.Manifest;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.broadcast.ContentTransmitter;
import com.vtstudio.fxbox.listeners.OnContentEventsListener;
import com.vtstudio.fxbox.services.ContentService;
import com.vtstudio.fxbox.utils.PermissionUtils;
import com.vtstudio.fxbox.utils.WindowUtils;

import java.util.List;


public class Splash extends AppCompatActivity {

    private ContentService provider;
    private BroadcastReceiver videoLoadedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /////// hide status and nav bar //////////
        WindowUtils.setSystemBarsHide(getWindow(), true);
        /////////////////////////////////////////

        //// hide action bar //////////
        ActionBar actionBar = getActionBar();
        if(actionBar != null){
            actionBar.hide();
        }

        /////////// set night mode /////////////
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        ///////////////////////////////////////

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001 // Mã request tùy ý
                );
            }
        }

        setContentView(R.layout.activity_splash);

    }

    @Override
    protected void onResume() {
        Handler delayHandler = new Handler();
        super.onResume();

        boolean hasPermission = false;

        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.R)
            hasPermission = PermissionUtils.hasPermissions(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        else
            hasPermission = true;

        // Kiểm tra quyền truy cập bộ nhớ ngoài
        if (hasPermission) {
            // Khởi tạo dữ liệu và chờ tải xong
            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);

            // Kiểm tra xem service mong muốn có trong danh sách hay không
            String serviceName = "com.vtstudio.fxbox.services.PlayerService";
            boolean isServiceRunning = false;

            for (ActivityManager.RunningServiceInfo service : runningServices) {
                if (service.service.getClassName().equals(serviceName)) {
                    isServiceRunning = true;
                    break;
                }
            }
            // Xử lý kết quả
            if (isServiceRunning) {
                delayHandler.postDelayed(() -> {
                    // Service đang hoạt động
                    startActivity(new Intent(Splash.this, Main.class));
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                }, 1000);

            } else {
                // Service không hoạt động
                ContentTransmitter contentTransmitter = new ContentTransmitter(this);
                contentTransmitter.startCommunication();
                contentTransmitter.addAction(ContentService.ACTION_SYNC_DATASTORE);

                contentTransmitter.setOnContentEventsListener(new OnContentEventsListener() {
                    @Override
                    public void onLoadedVideosFromStorage() {
                        Log.d("ContentService", "on loaded - splash");
                    }

                    @Override
                    public void onDataSoreSynchronized() {
                        contentTransmitter.addAction(ContentService.ACTION_STOP_SERVICE);
                        contentTransmitter.endCommunication();
                        contentTransmitter.release();
                        startActivity(new Intent(Splash.this, Main.class));
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();
                    }
                });
            }
            ////////////
        } else {
            delayHandler.postDelayed(() -> {
                // Chuyển đến màn hình yêu cầu cấp quyền truy cập
                startActivity(new Intent(Splash.this, AllowAccess.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }, 1500);
        }
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, ContentService.class));
        super.onDestroy();
    }

}