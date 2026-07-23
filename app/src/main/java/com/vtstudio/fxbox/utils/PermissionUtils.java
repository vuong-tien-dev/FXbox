package com.vtstudio.fxbox.utils;

import
        android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

public class PermissionUtils {
    public static final int REQUEST_CODE = 2000;
    public static final int REQUEST_PERMISSION_SETTING = 1000;
        public static boolean hasPermissions(Context context, String... permissions) {
            if (context != null && permissions != null) {
                for (String permission : permissions) {
                    if (ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                        return false;
                    }
                }
            }
            return true;
        }
    }