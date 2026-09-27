package com.vtstudio.fxbox.network;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

/**
 * Quản lý cấu hình kết nối Desktop trong SharedPreferences
 */
public class DesktopConnectionPreference {

    private static final String PREF_NAME = "fx_desktop_prefs";
    private static final String KEY_IP = "fx_desktop_ip";
    private static final String KEY_PORT = "fx_desktop_port";
    private static final String KEY_DEVICE_ID = "fx_desktop_device_id";

    // Giá trị IP mặc định theo yêu cầu của Boss Lee Chan
    public static final String DEFAULT_IP = "192.168.1.7";
    public static final int DEFAULT_PORT = 9710;

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static String getDesktopIp(Context context) {
        return getPrefs(context).getString(KEY_IP, DEFAULT_IP);
    }

    public static void setDesktopIp(Context context, String ip) {
        getPrefs(context).edit().putString(KEY_IP, ip).apply();
    }

    public static int getDesktopPort(Context context) {
        return getPrefs(context).getInt(KEY_PORT, DEFAULT_PORT);
    }

    public static void setDesktopPort(Context context, int port) {
        getPrefs(context).edit().putInt(KEY_PORT, port).apply();
    }

    public static String getDeviceId(Context context) {
        SharedPreferences prefs = getPrefs(context);
        String id = prefs.getString(KEY_DEVICE_ID, null);
        if (id == null) {
            id = UUID.randomUUID().toString();
            prefs.edit().putString(KEY_DEVICE_ID, id).apply();
        }
        return id;
    }
}
