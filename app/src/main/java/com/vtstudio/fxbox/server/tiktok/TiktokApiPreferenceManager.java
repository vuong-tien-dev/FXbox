package com.vtstudio.fxbox.server.tiktok;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TiktokApiPreferenceManager {
    public static final String SHARED_SERVER_FAILED_COUNT = "server_failed_count";
    public static final String SHARED_API_KEY_FAILED_COUNT = "api_key_failed_count";

    public static void increaseValue(@NonNull String sharedPrefKey, @NonNull String key, @NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPrefKey, Context.MODE_PRIVATE);
        final int failedCount = sharedPreferences.getInt(key, 0);
        sharedPreferences.edit().putInt(key, failedCount + 1).apply();
    }

    public static void increaseFailedServerCount(@NonNull String server, @NonNull Context context) {
        increaseValue(SHARED_SERVER_FAILED_COUNT, server, context);
    }

    public static void increaseFailedApiKeyCount(@NonNull String apiKey, @NonNull Context context) {
        increaseValue(SHARED_API_KEY_FAILED_COUNT, apiKey, context);
    }

    public static int getValueFromKey(@NonNull String sharedPrefKey, @NonNull String key, @NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPrefKey, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, 0);
    }

    public static int getFailedCountByApiKey(@NonNull String apiKey, @NonNull Context context) {
        return getValueFromKey(SHARED_API_KEY_FAILED_COUNT, apiKey, context);
    }

    public static int getFailedCountByServer(@NonNull String server, @NonNull Context context) {
        return getValueFromKey(SHARED_SERVER_FAILED_COUNT, server, context);
    }

    public static void clearAll(@NonNull Context context) {

        SharedPreferences server = context.getSharedPreferences(SHARED_SERVER_FAILED_COUNT, Context.MODE_PRIVATE);
        SharedPreferences apiKey = context.getSharedPreferences(SHARED_API_KEY_FAILED_COUNT, Context.MODE_PRIVATE);

        server.edit().clear().apply();
        apiKey.edit().clear().apply();

    }

    public static void checkIntervalMonthAndClear(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("TiktokApiPreferenceManager", Context.MODE_PRIVATE);
        long last_time = sharedPreferences.getLong("LAST_TIME", System.currentTimeMillis());
        long currentTime = System.currentTimeMillis();
        if ((currentTime - last_time) / (24 * 3600 * 1000) > 30) {
            clearAll(context);
        }
    }

    public static Map<String, Integer> getTreeMap(@NonNull String sharedPrefKey, @NonNull Context context, @NonNull List<String> keys) {
        Map<String, Integer> failedServersMap = new HashMap<>();

        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPrefKey, Context.MODE_PRIVATE);
        for (int i = 0; i < keys.size(); i++) {
            String server = keys.get(i);
            if (server != null) {
                final int value = sharedPreferences.getInt(server, 0);
                failedServersMap.put(server, value);
            }
        }

        // Tạo một TreeMap từ HashMap để sắp xếp theo thứ tự tăng dần của giá trị
        Map<String, Integer> sortedMap = new TreeMap<>((o1, o2) -> {
            int result = failedServersMap.get(o1).compareTo(failedServersMap.get(o2));
            // Nếu giá trị giống nhau, sắp xếp theo key
            if (result == 0) {
                return o1.compareTo(o2);
            }
            return result;
        });
        sortedMap.putAll(failedServersMap);

//        for (String key : sortedMap.keySet()) {
//            Log.d("TiktokApiPref", "Server: " + key + " failed count: " + sortedMap.get(key));
//        }

        return failedServersMap;
    }

    public static List<String> sortServerByFailedCount(@NonNull Context context, @NonNull List<String> servers) {
        return new ArrayList<>(getTreeMap(SHARED_SERVER_FAILED_COUNT, context, servers).keySet());
    }

    public static List<String> sortApiKeysByFailedCount(@NonNull Context context, @NonNull List<String> apiKeys) {
        return new ArrayList<>(getTreeMap(SHARED_API_KEY_FAILED_COUNT, context, apiKeys).keySet());
    }

    public static List<String> getSortedServersByFailedCount(@NonNull Context context) {
        return sortServerByFailedCount(context, TikTokApi.getAllServers());
    }

    public static List<String> getSortedApiKeysByFailedCount(@NonNull Context context) {
        return sortApiKeysByFailedCount(context, new ArrayList<String>(Arrays.asList(TikTokApi.getApiKeys())));
    }
}
