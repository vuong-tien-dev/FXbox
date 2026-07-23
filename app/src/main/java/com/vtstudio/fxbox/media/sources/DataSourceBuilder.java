package com.vtstudio.fxbox.media.sources;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.utils.BitmapUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DataSourceBuilder {
    private static DataSourceBuilder instance;
    private final MediaMetadataRetriever retriever;
    private Context context;

    private DataSourceBuilder(@NonNull Context context) {
        retriever = new MediaMetadataRetriever();
        this.context = context;
    }

    public void createThumbnailSizeIfIsImageList (@NonNull FxMediaVideo video) {
        if(video instanceof ShortsVideo) {
            ShortsVideo shortsVideo = (ShortsVideo) video;
            if(shortsVideo.isImageList()) {
                List<String> imageList = shortsVideo.getImageListPath();
                if(imageList != null && imageList.size() > 0) {
                    String target = imageList.get(0);
                    if(target != null && new File(target).exists()) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                        Bitmap bitmap = BitmapFactory.decodeFile(target, options);
                        if(bitmap != null) {
                            video.setWidth(bitmap.getWidth());
                            video.setHeight(bitmap.getHeight());
                        }
                    }
                }
            }
        }
    }

    public void createFxThumbnail(@NonNull FxMediaVideo video) {

        if(video instanceof ShortsVideo && ((ShortsVideo) video).isImageList()) return;

        if (video.getFxThumbnailId() != null) {
            File file = new File(video.getFxThumbnailPath());
            file.delete();
        }

        String newThumbnailId = String.valueOf(System.currentTimeMillis());
        Bitmap thumbnail = BitmapUtils.getVideoFrame(context, video.getPlayUri());

        if (thumbnail != null) {
            // Lưu thông tin mới vào Video object
            if(video.getWidth() == 0) video.setWidth(thumbnail.getWidth());
            if(video.getHeight() == 0) video.setHeight(thumbnail.getHeight());
            video.setFxThumbnailId(newThumbnailId);

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(thumbnail, (int) (thumbnail.getWidth() * 0.5f), (int) (thumbnail.getHeight() * 0.5f), false);
            thumbnail.recycle();
            BitmapUtils.saveBitmapToInternalStorage(resizedBitmap, new File(FxMediaVideo.getFxThumbnailDirectoryPath()), newThumbnailId, Bitmap.CompressFormat.JPEG, 60);
            resizedBitmap.recycle();
        }
    }

    public Bitmap createFxNotificationLargeIcon(@NonNull FxMediaVideo video) throws ExecutionException, InterruptedException {

        File file = new File(video.getFxNotificationLargeIconPath());
        if (file.exists()) file.delete();

        File directory = new File(FxMediaVideo.getFxNotificationLargeIconDirectoryPath());

        Bitmap thumbnail = Glide.with(context).asBitmap().load(video.getPlayUri()).override(250).skipMemoryCache(true).dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.NONE).submit().get();

        if (thumbnail != null) {
            String notificationLargeId = String.valueOf(video.getMediaStoreName());
            BitmapUtils.saveBitmapToInternalStorage(thumbnail, directory, notificationLargeId, Bitmap.CompressFormat.JPEG, 60);
            return thumbnail;
        }

        return null;
    }

    public synchronized void createMediaDuration(@NonNull FxMediaVideo mediaVideo) {
        try {
            retriever.setDataSource(context, mediaVideo.getPlayUri());
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInMillisecond = Long.parseLong(time);
            mediaVideo.setDuration(timeInMillisecond);
        } catch (Exception ignored) {

        }
    }

    public static DataSourceBuilder get(@NonNull Context context) {
        if (instance == null) {
            instance = new DataSourceBuilder(context.getApplicationContext());
        }
        return instance;
    }

    public static void killInstance() throws IOException {
        if (instance != null) {
            instance.retriever.release();
            instance.context = null;
            instance = null;
        }
    }
}
