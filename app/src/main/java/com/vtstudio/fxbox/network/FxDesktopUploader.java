package com.vtstudio.fxbox.network;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
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
 * Hỗ trợ đẩy cả 2 loại Media sang Desktop:
 * 1. Video đơn lẻ (.mp4)
 * 2. Danh sách ảnh (Image List / Slideshow) + file âm thanh nền (.mp3)
 */
public class FxDesktopUploader {

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    public interface UploadCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    /**
     * Kiểm tra nhanh kết nối LAN tới Desktop
     */
    public static void testConnection(final Context context, final UploadCallback callback) {
        String ip = DesktopConnectionPreference.getDesktopIp(context);
        int port = DesktopConnectionPreference.getDesktopPort(context);
        String url = "http://" + ip + ":" + port + "/api/ping";

        Request request = new Request.Builder().url(url).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showToast(context, "Kết nối thất bại tới " + ip + ":" + port + " (" + e.getClass().getSimpleName() + ")");
                if (callback != null) callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    showToast(context, "Kết nối Desktop thành công! (IP: " + ip + ")");
                    if (callback != null) callback.onSuccess("OK");
                } else {
                    showToast(context, "Desktop phản hồi lỗi: " + response.code());
                    if (callback != null) callback.onError("HTTP " + response.code());
                }
            }
        });
    }

    /**
     * Bắn video MP4 + metadata sang máy tính Desktop
     */
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

        sendMultipartRequest(context, url, deviceId, deviceName, builder.build(), callback);
    }

    /**
     * Bắn Danh sách ảnh (Image List / Slideshow) + file nhạc nền sang Desktop
     */
    public static void pushImageListToDesktop(
            final Context context,
            final List<File> imageFiles,
            final File audioFile,
            final String metadataJson,
            final File thumbFile,
            final File avatarFile,
            final File musicThumbFile,
            final UploadCallback callback
    ) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            showToast(context, "Lỗi: Không tìm thấy ảnh nào trong danh sách!");
            if (callback != null) callback.onError("Danh sách ảnh rỗng");
            return;
        }

        String ip = DesktopConnectionPreference.getDesktopIp(context);
        int port = DesktopConnectionPreference.getDesktopPort(context);
        String url = "http://" + ip + ":" + port + "/api/push";

        String deviceId = DesktopConnectionPreference.getDeviceId(context);
        String deviceName = Build.MANUFACTURER + " " + Build.MODEL;

        showToast(context, "Đang gửi " + imageFiles.size() + " ảnh sang máy tính (" + ip + ")...");

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("deviceId", deviceId)
                .addFormDataPart("deviceName", deviceName)
                .addFormDataPart("metadata", metadataJson);

        // Đính kèm toàn bộ danh sách ảnh
        for (File img : imageFiles) {
            if (img != null && img.exists()) {
                builder.addFormDataPart("images", img.getName(),
                        RequestBody.create(MediaType.parse("image/jpeg"), img));
            }
        }

        // Đính kèm file âm thanh nền nếu có
        if (audioFile != null && audioFile.exists()) {
            builder.addFormDataPart("audio", audioFile.getName(),
                    RequestBody.create(MediaType.parse("audio/mpeg"), audioFile));
        }

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

        sendMultipartRequest(context, url, deviceId, deviceName, builder.build(), callback);
    }

    private static void sendMultipartRequest(
            final Context context,
            final String url,
            final String deviceId,
            final String deviceName,
            final RequestBody requestBody,
            final UploadCallback callback
    ) {
        Request request = new Request.Builder()
                .url(url)
                .header("X-Device-Id", deviceId)
                .header("X-Device-Name", deviceName)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String errMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                showToast(context, "Không thể gửi sang Desktop! (" + errMsg + ")");
                if (callback != null) callback.onError(errMsg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    showToast(context, "Đã gửi thành công sang Desktop! 🎉");
                    if (callback != null) callback.onSuccess(response.body() != null ? response.body().string() : "");
                } else if (response.code() == 403) {
                    showToast(context, "Desktop đã từ chối nhận file từ thiết bị này.");
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
                Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_LONG).show()
        );
    }
}
