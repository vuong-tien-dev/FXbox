package com.vtstudio.fxbox.network;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * FXbox Desktop LAN Uploader
 * Chịu trách nhiệm đóng gói và gửi video ngắn + metadata từ Android sang Desktop qua HTTP LAN.
 */
public class FxDesktopUploader {

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    public interface UploadCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    /**
     * Bắn video + metadata sang máy tính Desktop
     *
     * @param context Context ứng dụng
     * @param videoFile File MP4 video lưu trên máy
     * @param metadataJson JSON metadata chứa thông tin video, user, music
     * @param thumbFile File ảnh thumbnail (nếu có, có thể null)
     * @param avatarFile File ảnh avatar (nếu có, có thể null)
     * @param callback Callback kết quả
     */
    public static void pushToDesktop(
            final Context context,
            final File videoFile,
            final String metadataJson,
            final File thumbFile,
            final File avatarFile,
            final UploadCallback callback
    ) {
        pushToDesktop(context, videoFile, metadataJson, thumbFile, avatarFile, null, callback);
    }

    public static void pushToDesktop(
            final Context context,
            final File videoFile,
            final String metadataJson,
            final File thumbFile,
            final File avatarFile,
            final File musicThumbFile,
            final UploadCallback callback
    ) {
        if (videoFile == null || !videoFile.exists()) {
            showToast(context, "Lỗi: File video không tồn tại trên máy!");
            if (callback != null) callback.onError("File video không tồn tại");
            return;
        }

        // Lấy IP & Port từ cấu hình (mặc định 192.168.1.7:9710)
        String ip = DesktopConnectionPreference.getDesktopIp(context);
        int port = DesktopConnectionPreference.getDesktopPort(context);
        String url = "http://" + ip + ":" + port + "/api/push";

        String deviceId = DesktopConnectionPreference.getDeviceId(context);
        String deviceName = Build.MANUFACTURER + " " + Build.MODEL;

        showToast(context, "Đang gửi video sang máy tính (" + ip + ")...");

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("deviceId", deviceId)
                .addFormDataPart("deviceName", deviceName)
                .addFormDataPart("metadata", metadataJson)
                .addFormDataPart("video", videoFile.getName(),
                        RequestBody.create(MediaType.parse("video/mp4"), videoFile));

        if (thumbFile != null && thumbFile.exists()) {
            builder.addFormDataPart("thumbnail", thumbFile.getName(),
                    RequestBody.create(MediaType.parse("image/jpeg"), thumbFile));
        }

        if (avatarFile != null && avatarFile.exists()) {
            builder.addFormDataPart("avatar", avatarFile.getName(),
                    RequestBody.create(MediaType.parse("image/jpeg"), avatarFile));
        }

        if (musicThumbFile != null && musicThumbFile.exists()) {
            builder.addFormDataPart("music_thumb", musicThumbFile.getName(),
                    RequestBody.create(MediaType.parse("image/jpeg"), musicThumbFile));
        }

        RequestBody requestBody = builder.build();


        Request request = new Request.Builder()
                .url(url)
                .header("X-Device-Id", deviceId)
                .header("X-Device-Name", deviceName)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showToast(context, "Không thể kết nối tới Desktop! Hãy kiểm tra Wi-Fi.");
                if (callback != null) callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    showToast(context, "Đã gửi video sang Desktop thành công! 🎉");
                    if (callback != null) callback.onSuccess(response.body() != null ? response.body().string() : "");
                } else if (response.code() == 403) {
                    showToast(context, "Desktop đã từ chối nhận video từ thiết bị này.");
                    if (callback != null) callback.onError("Device rejected by desktop");
                } else {
                    showToast(context, "Lỗi từ Desktop: Code " + response.code());
                    if (callback != null) callback.onError("Server error: " + response.code());
                }
            }
        });
    }

    private static void showToast(final Context context, final String msg) {
        if (context == null) return;
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT).show()
        );
    }
}
