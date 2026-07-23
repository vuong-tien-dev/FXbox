package com.vtstudio.fxbox.media.models.youtube;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.vtstudio.fxbox.helpers.PreferenceHelper;
import com.vtstudio.fxbox.listeners.OnFailedListener;
import com.vtstudio.fxbox.listeners.OnSuccessListener;
import com.vtstudio.fxbox.media.models.tiktok.ShortsMusic;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.models.TiktokPackage;
import com.vtstudio.fxbox.utils.BitmapUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class YoutubeDataSourceBuilder {

    private static String authorAvatarDirPath;
    private static String videoThumbnailDirPath;

    private String authorAvatarId;
    private String videoThumbnailId;
    private Context context;
    private ExecutorService executor;
    private Handler eventHandler;
    private YTDetails api;
    private boolean isCreated;
    private OnSuccessListener onSuccessListener;
    private OnFailedListener onFailedListener;

    public YoutubeDataSourceBuilder(@NonNull Context context, @NonNull YTDetails details) {

        authorAvatarDirPath = YTUser.getUserAvatarDirectoryPath();

        videoThumbnailDirPath = PreferenceHelper.getYoutubeVideoThumbnailPath(context, 0);

        api = details;
        this.executor = Executors.newSingleThreadExecutor();
        this.context = context;
    }

    public YoutubeDataSourceBuilder(@NonNull Context context) {

        authorAvatarDirPath = YTUser.getUserAvatarDirectoryPath();

        videoThumbnailDirPath = PreferenceHelper.getYoutubeVideoThumbnailPath(context, 0);

        this.executor = Executors.newSingleThreadExecutor();
        this.context = context;
    }

    public void createDataSource(boolean override) {
        if (isCreated || context == null) return;
        eventHandler = new Handler();

        executor.execute(() -> {
            Bitmap authorAvatar = null;
            Bitmap videoThumbnail = null;

            authorAvatarId = api.getUser().getChannelId();
            videoThumbnailId = api.getVideo().getId();

            // Kiểm tra và tải musicAvatar nếu chưa tồn tại
            try {


                // Kiểm tra và tải authorAvatar nếu chưa tồn tại
                File authorFile = new File(authorAvatarDirPath, authorAvatarId + ".png");
                if (!authorFile.exists() || override) {
                    authorAvatar = Glide.with(context).asBitmap().load(api.getUser().getAvatarUrl()).submit().get();
                }

                // Kiểm tra và tải videoThumbnail nếu chưa tồn tại
                if (!new File(videoThumbnailDirPath, videoThumbnailId + ".jpg").exists()) {
                    videoThumbnail = Glide.with(context).asBitmap().load(api.getVideo().getThumbnailUrl()).submit().get();
                }

                if (authorAvatar != null) {
                    if (override) authorFile.delete();
                    BitmapUtils.saveBitmapToInternalStorage(authorAvatar, new File(authorAvatarDirPath), authorAvatarId, Bitmap.CompressFormat.PNG, 80);
                }

                if (videoThumbnail != null) {
                    BitmapUtils.saveBitmapToInternalStorage(videoThumbnail, new File(videoThumbnailDirPath), videoThumbnailId, Bitmap.CompressFormat.JPEG, 70);
                }


            } catch (ExecutionException | InterruptedException ignored) {
                eventHandler.post(() -> {
                    if (onFailedListener != null) {
                        this.onFailedListener.onFailed();
                    }
                });
                isCreated = false;
                return;
            }
            // Lưu trữ các bitmap đã tải xuống

            eventHandler.post(() -> {
                if (onSuccessListener != null) {
                    this.onSuccessListener.onSuccess();
                }
            });
            isCreated = false;
        });
        isCreated = true;
    }

    public void createUserAvatar(@NonNull String url, @NonNull String uid, int quality, boolean override) {
        if (isCreated || context == null) return;
        eventHandler = new Handler();

        executor.execute(() -> {
            Bitmap authorAvatar = null;

            authorAvatarId = uid;

            // Kiểm tra và tải authorAvatar nếu chưa tồn tại
            try {
                File avtFile = new File(authorAvatarDirPath, authorAvatarId + ".png");

                // Kiểm tra và tải authorAvatar nếu chưa tồn tại
                if (!avtFile.exists() || override) {
                    authorAvatar = Glide.with(context).asBitmap().load(url).submit().get();
                }

                if (authorAvatar != null) {
                    Log.d("DataSourceBuilder", "Avatar is already");
                    avtFile.delete();
                    BitmapUtils.saveBitmapToInternalStorage(authorAvatar, new File(authorAvatarDirPath), authorAvatarId, Bitmap.CompressFormat.PNG, quality);
                }

            } catch (ExecutionException | InterruptedException ignored) {
                eventHandler.post(() -> {
                    if (onFailedListener != null) {
                        onFailedListener.onFailed();
                    }
                });
                isCreated = false;
                return;
            }
            // Lưu trữ các bitmap đã tải xuống

            eventHandler.post(() -> {
                if (onSuccessListener != null) onSuccessListener.onSuccess();
            });
            isCreated = false;
        });
        isCreated = true;
    }

    public void createVideoThumbnail(@NonNull String url, @NonNull String id, int quality) {
        if (isCreated || context == null) return;
        eventHandler = new Handler();

        executor.execute(() -> {
            Bitmap videoThumbnail = null;

            // Kiểm tra và tải musicAvatar nếu chưa tồn tại
            try {

                videoThumbnail = Glide.with(context).asBitmap().load(url).submit().get();

                if (videoThumbnail != null) {
                    BitmapUtils.saveBitmapToInternalStorage(videoThumbnail, new File(videoThumbnailDirPath), id, Bitmap.CompressFormat.JPEG, quality);
                } else {
                    Log.d("DataSourceBuilder", "Failed to get videoThumbnail: " + url);
                }

            } catch (ExecutionException | InterruptedException ignored) {
                eventHandler.post(() -> {
                    if (onFailedListener != null) {
                        this.onFailedListener.onFailed();
                    }
                });
                isCreated = false;
                return;
            }
            // Lưu trữ các bitmap đã tải xuống

            eventHandler.post(() -> {
                if (onSuccessListener != null) {
                    this.onSuccessListener.onSuccess();
                }
            });
            isCreated = false;
        });
        isCreated = true;
    }


    public YoutubeDataSourceBuilder doOnSuccess(@NonNull OnSuccessListener onSuccessListener) {
        this.onSuccessListener = onSuccessListener;
        return this;
    }

    public YoutubeDataSourceBuilder doOnFailed(@NonNull OnFailedListener onFailedListener) {
        this.onFailedListener = onFailedListener;
        return this;
    }

    public String getAuthorAvatarId() {
        return authorAvatarId;
    }


    public String getAuthorAvatarPath() {
        return buildFilePath(authorAvatarDirPath, authorAvatarId, ".jpg");
    }

    public String getVideoThumbnailPath() {
        return buildFilePath(videoThumbnailDirPath, videoThumbnailId, ".jpg");
    }

    public String getAuthorAvatarDirPath() {
        return authorAvatarDirPath;
    }

    public String getVideoThumbnailDirPath() {
        return videoThumbnailDirPath;
    }

    private String buildFilePath(String dirPath, String fileId, String extension) {
        StringBuilder pathBuilder = new StringBuilder(dirPath);
        pathBuilder.append("/");
        pathBuilder.append(fileId);
        pathBuilder.append(extension);
        return pathBuilder.toString();
    }


    public void release() {
        // Giải phóng ExecutorService khi không còn cần thiết
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        executor = null;
        context = null;
        api = null;
        eventHandler = null;
        onFailedListener = null;
        onSuccessListener = null;
    }
}
