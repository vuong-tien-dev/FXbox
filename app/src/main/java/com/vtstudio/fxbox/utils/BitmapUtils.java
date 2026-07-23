package com.vtstudio.fxbox.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;

import com.vtstudio.fxbox.helpers.PreferenceHelper;
import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.media.sources.DataSourceBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class BitmapUtils {

    public static Bitmap cropSquared(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        int x = (bitmap.getWidth() - size)/2;
        int y = (bitmap.getHeight() - size)/2;

        return Bitmap.createBitmap(bitmap, x, y, size, size);
    }

    public static String saveBitmapToInternalStorage(Bitmap bitmapImage, File directory, String name) {
        // Create imageDir
        File myPath = new File(directory, name + ".jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            return myPath.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Nullable
    public static String saveBitmapToInternalStorage(Bitmap bitmapImage, File directory, String name, Bitmap.CompressFormat format, int quality) {
        // Create imageDir
        if (!directory.exists()) {
            directory.mkdirs();
        }
        String extension = format == Bitmap.CompressFormat.JPEG ? ".jpg" : ".png";
        File myPath = new File(directory, name + extension);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(format, quality, fos);
            return myPath.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Bitmap getVideoFrame(Context context, Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {/*from  w  w  w .ja  va  2 s.c  o m*/
            retriever.setDataSource(context, uri);
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            return retriever.getFrameAtTime(MediaMetadataRetriever.OPTION_CLOSEST);
        } catch (IllegalArgumentException ex) {
            Log.e("TAG", "error getting video frame", ex);

        } catch (RuntimeException ex) {
            Log.e("TAG", "error getting video frame", ex);
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException | IOException ex) {
            }
        }
        return null;
    }

    public static Bitmap getVideoFrame(Context context, Uri uri, int position) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {/*from  w  w  w .ja  va  2 s.c  o m*/
            retriever.setDataSource(context, uri);
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            return retriever.getFrameAtTime(position, MediaMetadataRetriever.OPTION_CLOSEST);
        } catch (IllegalArgumentException ex) {
            Log.e("TAG", "error getting video frame", ex);

        } catch (RuntimeException ex) {
            Log.e("TAG", "error getting video frame", ex);
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException | IOException ex) {
            }
        }
        return null;
    }

    public static Bitmap getImageCache(Context context, String id) {
        File file = new File(PreferenceHelper.getImageCacheDirectory(context), id + ".jpg");
        if (file.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                return BitmapFactory.decodeStream(fis);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public static Bitmap getImage(String path) {
        File file = new File(path);
        if (file.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                return BitmapFactory.decodeStream(fis);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void getNotificationIconAsync(FxMediaVideo video, Context context, ExecutorService executor, Callback callback) {
        executor.execute(() -> {
            Bitmap icon = getNotificationIcon(video, context);
            if (icon != null) {
                icon = cropSquared(icon);
            }
            Bitmap finalIcon = icon;
            new Handler(Looper.getMainLooper()).post(() -> {  callback.onIconLoaded(finalIcon);  });
        });
    }


    public interface Callback {
        void onIconLoaded(Bitmap icon);
    }

    public static Bitmap getNotificationIcon(FxMediaVideo video, Context context) {
        File file = null;
        boolean isImageList = false;
        if (video instanceof ShortsVideo) {
            ShortsVideo shortsVideo3 = (ShortsVideo) video;
            if (shortsVideo3.isImageList() && shortsVideo3.getImageListPath() != null) {
                file = new File(shortsVideo3.getImageListPath().get(0));
                isImageList = true;
            }
        }

        if (!isImageList) {
            file = new File(video.getFxNotificationLargeIconPath());
        }

        if (file.exists()) {
            Log.d("BitmapUtils", video.getMediaStoreName() + " has already been");
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565; // Sử dụng RGB_565 để giảm bộ nhớ tiêu tốn
                return BitmapFactory.decodeStream(fis, null, options);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            Bitmap bitmap = null;
            try {
                bitmap = DataSourceBuilder.get(context).createFxNotificationLargeIcon(video);
                if (bitmap != null) {
                    return bitmap;
                }
            } catch (ExecutionException | InterruptedException ignored) {
                return null;
            }

        }
        return null;
    }
}
