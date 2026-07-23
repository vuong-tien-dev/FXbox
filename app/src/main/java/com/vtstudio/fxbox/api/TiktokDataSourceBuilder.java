package com.vtstudio.fxbox.api;

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
import com.vtstudio.fxbox.listeners.OnFailedListener;
import com.vtstudio.fxbox.listeners.OnSuccessListener;
import com.vtstudio.fxbox.media.models.tiktok.ShortsMusic;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.models.TiktokPackage;
import com.vtstudio.fxbox.utils.BitmapUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TiktokDataSourceBuilder {
    public static final String VIDEO_THUMBNAIL_CACHE_DIR = "cache_thumbnails";

    private static String authorAvatarDirPath;
    private static String musicAvatarDirPath;
    private static String videoThumbnailDirPath;
    private static boolean dirPathCreated;

    private String authorAvatarId;
    private String musicAvatarId;
    private String videoThumbnailId;
    private Context context;
    private ExecutorService executor;
    private Handler eventHandler;
    private TiktokPackage api;
    private boolean isCreated;
    private OnSuccessListener onSuccessListener;
    private OnFailedListener onFailedListener;

    public TiktokDataSourceBuilder(@NonNull Context context, @NonNull TiktokPackage api) {
        if (!dirPathCreated) {

            authorAvatarDirPath = ShortsUser.getAvatarDirectoryPath();
            musicAvatarDirPath = ShortsMusic.getAvatarDirectoryPath();

            File videoThumbnailDirFile = new File(context.getExternalFilesDir(null), VIDEO_THUMBNAIL_CACHE_DIR);

            if (!videoThumbnailDirFile.exists()) videoThumbnailDirFile.mkdirs();

            videoThumbnailDirPath = videoThumbnailDirFile.getAbsolutePath();
            dirPathCreated = true;
        }
        this.executor = Executors.newSingleThreadExecutor();
        this.api = api;
        this.context = context;
    }

    public TiktokDataSourceBuilder(@NonNull Context context) {
        if (!dirPathCreated) {
            authorAvatarDirPath = ShortsUser.getAvatarDirectoryPath();
            musicAvatarDirPath = ShortsMusic.getAvatarDirectoryPath();

            File videoThumbnailDirFile = new File(context.getExternalFilesDir(null), VIDEO_THUMBNAIL_CACHE_DIR);

            if (!videoThumbnailDirFile.exists()) videoThumbnailDirFile.mkdirs();

            videoThumbnailDirPath = videoThumbnailDirFile.getAbsolutePath();

            dirPathCreated = true;
        }
        this.executor = Executors.newSingleThreadExecutor();
        this.context = context;
    }

    public void createAuthorAvatar(@NonNull String url, @NonNull String uid, int quality, boolean override) {
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
                    authorAvatar = Glide.with(context).asBitmap().load(url).timeout(15000).submit().get();
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

    public void createMusicThumbnail(@NonNull TiktokPackage api, int quality) {
        String musicAvtUrl = api.getMusicAvatarUrl();
        String musicId = api.getFxMusicAvatarId();

        if (isCreated || context == null || musicAvtUrl == null || musicId == null) return;
        eventHandler = new Handler();

        executor.execute(() -> {
            Bitmap musicThumbnail = null;

            // Kiểm tra và tải authorAvatar nếu chưa tồn tại
            try {
                File avtFile = new File(musicAvatarDirPath, musicId + ".jpg");

                // Kiểm tra và tải authorAvatar nếu chưa tồn tại

                musicThumbnail = Glide.with(context).asBitmap().load(musicAvtUrl).submit().get();

                if (musicThumbnail != null) {
                    Log.d("DataSourceBuilder", "Avatar is already");
                    avtFile.delete();
                    BitmapUtils.saveBitmapToInternalStorage(musicThumbnail, new File(musicAvatarDirPath), musicId, Bitmap.CompressFormat.JPEG, quality);
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

    public void createImageList(@NonNull TiktokPackage shorts) {
        if (isCreated || context == null) return;
        eventHandler = new Handler();

        executor.execute(() -> {
            List<String> imagesId = new ArrayList<>();
            List<String> imagePaths = new ArrayList<>();
            String video_id = shorts.getAwemeId();
            int i = 1;
            for (String url : shorts.getImageListUrl()) {
                try {
                    String img_id = i + "_" + video_id;
                    Glide.with(context)
                            .asBitmap()
                            .load(url)
                            .listener(new RequestListener<Bitmap>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                    String path = BitmapUtils.saveBitmapToInternalStorage(resource, new File(ShortsVideo.getImgListDirectoryPath()), img_id, Bitmap.CompressFormat.JPEG, 100);
                                    if(path!= null){
                                        imagesId.add(img_id);
                                        imagePaths.add(path);
                                    }
                                    return false;
                                }
                            }).submit().get();
                } catch (Exception e) {
                    eventHandler.post(() -> {
                        if (onFailedListener != null) {
                            this.onFailedListener.onFailed();
                        }
                        for(String p : imagePaths){
                            File file = new File(p);
                            file.delete();
                        }
                    });
                    isCreated = false;
                    return;
                }
                ++i;
            }


            shorts.setImageIdList(imagesId);
            shorts.setImageListPath(imagePaths);

            if(imagesId.size() == shorts.getImageListUrl().size() && imagePaths.size() == shorts.getImageListUrl().size()){
                eventHandler.post(() -> {
                    if (onSuccessListener != null) onSuccessListener.onSuccess();
                });
            } else {
                shorts.setImageListPath(null);
                shorts.setImageIdList(null);
                eventHandler.post(() -> {
                    if (onFailedListener != null) onFailedListener.onFailed();
                });
                for(String p : imagePaths){
                    File file = new File(p);
                    file.delete();
                }
            }

            isCreated = false;
        });
        isCreated = true;
    }


    public void createDataSource(boolean override) {
        if (isCreated || context == null) return;
        eventHandler = new Handler();

        executor.execute(() -> {
            Bitmap musicAvatar = null;
            Bitmap authorAvatar = null;
            Bitmap videoThumbnail = null;

            musicAvatarId = api.getFxMusicAvatarId();
            authorAvatarId = api.getFxAuthorAvatarId();
            videoThumbnailId = api.getAwemeId();

            // Kiểm tra và tải musicAvatar nếu chưa tồn tại
            try {
                Log.d("Tiktok", "music");
                File musicFile = new File(musicAvatarDirPath, musicAvatarId + ".jpg");
                if (!musicFile.exists() || override) {
                    try {
                        musicAvatar = Glide.with(context).asBitmap().load(api.getMusicAvatarUrl()).submit().get();
                    } catch (Exception e) {
                        Log.d("DataBuilder", "Error saving music thumbnail: " + e.getMessage());
                    }
                }

                Log.d("Tiktok", "author");
                // Kiểm tra và tải authorAvatar nếu chưa tồn tại
                File authorFile = new File(authorAvatarDirPath, authorAvatarId + ".png");
                if (!authorFile.exists() || override) {
                    authorAvatar = Glide.with(context).asBitmap().load(api.getAuthorAvatar()).submit().get();
                }

                Log.d("Tiktok", "video");
                // Kiểm tra và tải videoThumbnail nếu chưa tồn tại
                if (!new File(videoThumbnailDirPath, videoThumbnailId + ".jpg").exists()) {
                    videoThumbnail = Glide.with(context).asBitmap().load(api.getThumbnail()).submit().get();
                }

                if (musicAvatar != null) {
                    if (override) musicFile.delete();
                    BitmapUtils.saveBitmapToInternalStorage(musicAvatar, new File(musicAvatarDirPath), musicAvatarId, Bitmap.CompressFormat.JPEG, 100);
                   }

                if (authorAvatar != null) {
                    if (override) authorFile.delete();
                    BitmapUtils.saveBitmapToInternalStorage(authorAvatar, new File(authorAvatarDirPath), authorAvatarId, Bitmap.CompressFormat.PNG, 80);
                }

                if (videoThumbnail != null) {
                    BitmapUtils.saveBitmapToInternalStorage(videoThumbnail, new File(videoThumbnailDirPath), videoThumbnailId, Bitmap.CompressFormat.JPEG, 100);
                }


            } catch (ExecutionException | InterruptedException ex) {
                Log.d("DataBuilder",
                        "Error getting data from (" + api.getMusicAvatarUrl() + ", " + api.getAuthorAvatar() + ", " + api.getThumbnail() + ") Error: " +  ex.getMessage());
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

    public TiktokDataSourceBuilder doOnSuccess(@NonNull OnSuccessListener onSuccessListener) {
        this.onSuccessListener = onSuccessListener;
        return this;
    }

    public TiktokDataSourceBuilder doOnFailed(@NonNull OnFailedListener onFailedListener) {
        this.onFailedListener = onFailedListener;
        return this;
    }

    public String getAuthorAvatarId() {
        return authorAvatarId;
    }

    public String getMusicAvatarId() {
        return musicAvatarId;
    }

    public String getAuthorAvatarPath() {
        return buildFilePath(authorAvatarDirPath, authorAvatarId, ".jpg");
    }

    public String getMusicAvatarPath() {
        return buildFilePath(musicAvatarDirPath, musicAvatarId, ".jpg");
    }

    public String getVideoThumbnailPath() {
        return buildFilePath(videoThumbnailDirPath, videoThumbnailId, ".jpg");
    }

    public String getAuthorAvatarDirPath() {
        return authorAvatarDirPath;
    }

    public String getMusicAvatarDirPath() {
        return musicAvatarDirPath;
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
