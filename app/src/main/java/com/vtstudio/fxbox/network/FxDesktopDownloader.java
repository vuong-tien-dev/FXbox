package com.vtstudio.fxbox.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.database.dao.CommentDao;
import com.vtstudio.fxbox.database.dao.ShortsMusicDao;
import com.vtstudio.fxbox.database.dao.ShortsUserDao;
import com.vtstudio.fxbox.database.dao.ShortsVideoDao;
import com.vtstudio.fxbox.media.models.tiktok.Comment;
import com.vtstudio.fxbox.media.models.tiktok.ShortsMusic;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * FXbox Desktop LAN Downloader & Sync Receiver
 * Cho phép điện thoại Android kéo (Pull/Download) video từ Desktop về lại máy
 * và tự động lưu đồng bộ 100% vào Room Database (FxRoomDB).
 */
public class FxDesktopDownloader {

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    private static final Gson gson = new Gson();

    public interface SyncListCallback {
        void onSuccess(List<ShortsVideo> videos);
        void onError(String error);
    }

    public interface DownloadVideoCallback {
        void onProgress(int progress);
        void onSuccess(ShortsVideo video);
        void onError(String error);
    }

    /**
     * Lấy danh sách video có trên Desktop
     */
    public static void fetchDesktopVideos(final Context context, final SyncListCallback callback) {
        String ip = DesktopConnectionPreference.getDesktopIp(context);
        int port = DesktopConnectionPreference.getDesktopPort(context);
        String url = "http://" + ip + ":" + port + "/api/sync/videos";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showToast(context, "Không thể kết nối đến FXbox Desktop!");
                if (callback != null) callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonStr = response.body().string();
                        JsonObject root = JsonParser.parseString(jsonStr).getAsJsonObject();
                        JsonArray dataArr = root.getAsJsonArray("data");

                        List<ShortsVideo> videoList = new ArrayList<>();
                        for (int i = 0; i < dataArr.size(); i++) {
                            JsonObject item = dataArr.get(i).getAsJsonObject();
                            JsonObject vObj = item.getAsJsonObject("shortsVideo");
                            ShortsVideo video = gson.fromJson(vObj, ShortsVideo.class);
                            if (video != null) {
                                videoList.add(video);
                            }
                        }

                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (callback != null) callback.onSuccess(videoList);
                        });
                    } catch (Exception e) {
                        if (callback != null) callback.onError("Lỗi phân tích JSON: " + e.getMessage());
                    }
                } else {
                    if (callback != null) callback.onError("Server trả về lỗi: " + response.code());
                }
            }
        });
    }

    /**
     * Tải trọn vẹn 1 video từ Desktop về điện thoại và lưu thẳng vào Room Database
     */
    public static void downloadVideoFromDesktop(
            final Context context,
            final String awemeId,
            final DownloadVideoCallback callback
    ) {
        String ip = DesktopConnectionPreference.getDesktopIp(context);
        int port = DesktopConnectionPreference.getDesktopPort(context);
        String url = "http://" + ip + ":" + port + "/api/sync/video/" + awemeId;

        showToast(context, "Đang tải video từ máy tính về...");

        Request request = new Request.Builder().url(url).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showToast(context, "Lỗi tải video: " + e.getMessage());
                if (callback != null) callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonStr = response.body().string();
                        JsonObject root = JsonParser.parseString(jsonStr).getAsJsonObject();

                        JsonObject vObj = root.getAsJsonObject("shortsVideo");
                        JsonObject uObj = root.has("shortsUser") && !root.get("shortsUser").isJsonNull() ? root.getAsJsonObject("shortsUser") : null;
                        JsonObject mObj = root.has("shortsMusic") && !root.get("shortsMusic").isJsonNull() ? root.getAsJsonObject("shortsMusic") : null;
                        JsonArray cArr = root.has("comments") ? root.getAsJsonArray("comments") : null;

                        ShortsVideo video = gson.fromJson(vObj, ShortsVideo.class);
                        ShortsUser user = uObj != null ? gson.fromJson(uObj, ShortsUser.class) : null;
                        ShortsMusic music = mObj != null ? gson.fromJson(mObj, ShortsMusic.class) : null;

                        // Tải file video MP4 về bộ nhớ thiết bị
                        String videoUrl = vObj.has("videoPath") ? vObj.get("videoPath").getAsString() : "";
                        if (!videoUrl.isEmpty()) {
                            File saveDir = context.getExternalFilesDir("FXbox_Videos");
                            if (saveDir != null && !saveDir.exists()) saveDir.mkdirs();
                            File localVideoFile = new File(saveDir, video.getAwemeId() + ".mp4");

                            downloadFile(videoUrl, localVideoFile);
                            video.setMediaStorePath(localVideoFile.getAbsolutePath());
                        }

                        // Lưu trực tiếp vào FxRoomDB trong background thread
                        Executors.newSingleThreadExecutor().execute(() -> {
                            FxRoomDB db = FxRoomDB.get(context);
                            ShortsVideoDao videoDao = db.shortsVideoDao();
                            ShortsUserDao userDao = db.shortsUserDao();
                            ShortsMusicDao musicDao = db.shortsMusicDao();
                            CommentDao commentDao = db.commentDao();

                            if (user != null) userDao.insert(user);
                            if (music != null) musicDao.insert(music);
                            if (video != null) videoDao.insert(video);

                            if (cArr != null) {
                                for (int i = 0; i < cArr.size(); i++) {
                                    Comment comment = gson.fromJson(cArr.get(i), Comment.class);
                                    if (comment != null) {
                                        commentDao.insert(comment);
                                    }
                                }
                            }

                            new Handler(Looper.getMainLooper()).post(() -> {
                                showToast(context, "Đã tải và đồng bộ video vào FXbox thành công! 🎉");
                                if (callback != null) callback.onSuccess(video);
                            });
                        });

                    } catch (Exception e) {
                        if (callback != null) callback.onError("Lỗi lưu video vào DB: " + e.getMessage());
                    }
                } else {
                    if (callback != null) callback.onError("Lỗi tải video từ Desktop: " + response.code());
                }
            }
        });
    }

    private static void downloadFile(String fileUrl, File destination) throws IOException {
        Request request = new Request.Builder().url(fileUrl).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("Không thể tải file từ " + fileUrl);
            }
            try (InputStream is = response.body().byteStream();
                 FileOutputStream fos = new FileOutputStream(destination)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
                fos.flush();
            }
        }
    }

    private static void showToast(final Context context, final String msg) {
        if (context == null) return;
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT).show()
        );
    }
}
