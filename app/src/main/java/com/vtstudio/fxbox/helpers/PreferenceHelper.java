package com.vtstudio.fxbox.helpers;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;

public class PreferenceHelper {
    private static final String SHARED_PREFERENCES_KEY = "fxbox_shared_pref";
    private static final String APP_NAME_DIRECTORY = "Fxbox";
    /////////// keys store  //////////
    // Các tùy chọn liên quan đến phương tiện
    public static final String TIME_SAVED_TO_STORAGE = "time_saved_to_storage"; // Mô tả phương tiện
    public static final String RAW_RESPONSE = "time_saved_to_storage"; // Mô tả phương tiện

    // Thư mục chứa các hình thu nhỏ của video
    public static final String MEDIA_THUMBNAIL_DIRECTORY = "video_thumbnail";
    public static final String MEDIA_LARGE_ICON_DIRECTORY = "large_icon";
    public static final String IMAGE_CACHE_DIRECTORY = "img_cache";

    // Các tùy chọn liên quan đến tải xuống video
    public static final String VIDEO_DOWNLOAD_DIRECTORY = "FxShorts"; // Thư mục chứa các video đã tải xuống
    public static final String MUSIC_DOWNLOAD_DIRECTORY = "FxMusic";// Thư mục chứa các audio đã tải xuống
    public static final String LOG_DIRECTORY = "log";
    public static final String SHORTS_DIRECTORY = "shorts";
    public static final String YOUTUBE_DIRECTORY = "youtube";
    public static final String SAVE_STORAGE_DIRECTORY = "save_storage";
    public static final String VIDEO_DIRECTORY = "videos";
    public static final String IMAGE_DIRECTORY = "images";
    public static final String AUDIO_DIRECTORY = "audio";
    public static final String AUTHOR_DIRECTORY = "author";
    public static final String SUBTITLES_DIRECTORY = "subtitles";

    public static final String VIDEO_DOWNLOAD_DIRECTORY_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + VIDEO_DOWNLOAD_DIRECTORY;
    public static final String MUSIC_DOWNLOAD_DIRECTORY_PATH =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + MUSIC_DOWNLOAD_DIRECTORY; // Đường dẫn đến thư mục chứa các video đã tải xuống

    public static final int APP = 0;
    public static final int SAVE_STORAGE = 1;

    // shorts
    private static String LOG_FILE_PATH;
    private static String SHORTS_DIRECTORY_PATH;
    private static String SAVE_STORAGE_DIRECTORY_PATH;
    private static String SHORTS_AUDIO_DIRECTORY_PATH;
    private static String SHORTS_VIDEO_DIRECTORY_PATH;
    private static String SHORTS_IMAGE_DIRECTORY_PATH;
    private static String SHORTS_AUTHOR_IMAGE_DIRECTORY_PATH;
    private static String SHORTS_AUDIO_IMAGE_DIRECTORY_PATH;

    // youtube
    private static String YOUTUBE_DIRECTORY_PATH;
    private static String YOUTUBE_VIDEO_DIRECTORY_PATH;
    private static String YOUTUBE_VIDEO_IMAGE_DIRECTORY_PATH;
    private static String YOUTUBE_SUBTITLES_DIRECTORY_PATH;
    private static String YOUTUBE_AUTHOR_IMAGE_DIRECTORY_PATH;

    public static String getImageCacheDirectory (Context context)
    {
        return new ContextWrapper(context.getApplicationContext()).getDir(IMAGE_CACHE_DIRECTORY, Context.MODE_PRIVATE).getAbsolutePath();
    }

    public static String getLogFilePath(Context context){

        if(LOG_FILE_PATH != null) return LOG_FILE_PATH;

        String external_path = context.getExternalFilesDir(null).getAbsolutePath();
        File file = new File(external_path, LOG_DIRECTORY);

        if(!file.exists()){
            boolean isCreated = file.mkdirs();
            if(!isCreated) throw new IllegalStateException("Cannot create internal log directory");
        }

        LOG_FILE_PATH = file.getAbsolutePath();
        return LOG_FILE_PATH;
    }

    /*
        Root directory: shorts
        Trả về thời đường dẫn external trong thư mục Android để lưu trữ  video ngắn
     */
    public static String getShortsVideoPath(Context context, int basePath){
        if(SHORTS_VIDEO_DIRECTORY_PATH != null && basePath == APP) return SHORTS_VIDEO_DIRECTORY_PATH;

        String dir_path = getShortsPath(context, basePath);

        File video = new File(dir_path, VIDEO_DIRECTORY);
        if(!video.exists()){
            boolean isCreated = video.mkdirs();
            if(!isCreated) throw new IllegalStateException("Cannot create internal video file directory");
        }

        if(basePath != APP) {
            return video.getAbsolutePath();
        }

        SHORTS_VIDEO_DIRECTORY_PATH = video.getAbsolutePath();
        Log.d("PreferenceHelper", SHORTS_VIDEO_DIRECTORY_PATH);
        return SHORTS_VIDEO_DIRECTORY_PATH;
    }

    /*
        Root directory: shorts
        Trả về thời đường dẫn external trong thư mục Android để lưu trữ  danh sách ảnh photos
     */
    public static String getShortsImagePath(Context context, int basePath){
        if(SHORTS_IMAGE_DIRECTORY_PATH != null && basePath == APP) return SHORTS_IMAGE_DIRECTORY_PATH;

        String dir_path = getShortsPath(context, basePath);

        File image = new File(dir_path, IMAGE_DIRECTORY);
        if(!image.exists()){
            boolean isCreated = image.mkdirs();
            if(!isCreated) throw new IllegalStateException("Cannot create internal image file directory");
        }

        if(basePath != APP) {
            return image.getAbsolutePath();
        }

        SHORTS_IMAGE_DIRECTORY_PATH = image.getAbsolutePath();
        Log.d("PreferenceHelper", SHORTS_IMAGE_DIRECTORY_PATH);
        return SHORTS_IMAGE_DIRECTORY_PATH;
    }

    /*
        Root directory: shorts
        Trả về thời đường dẫn external trong thư mục Android để lưu trữ âm nhạc ( là âm nhạc của danh sách ảnh photos)
     */
    public static String getShortsAudioPath(Context context, int basePath){
        if(SHORTS_AUDIO_DIRECTORY_PATH != null && basePath == APP) return SHORTS_AUDIO_DIRECTORY_PATH;

        String dir_path = getShortsPath(context, basePath);

        File audio = new File(dir_path, AUDIO_DIRECTORY);
        if(!audio.exists()){
            boolean isCreated = audio.mkdirs();
            if(!isCreated) throw new IllegalStateException("Cannot create internal audio file directory");
        }

        if(basePath != APP) {
            return audio.getAbsolutePath();
        }

        SHORTS_AUDIO_DIRECTORY_PATH = audio.getAbsolutePath();
        Log.d("PreferenceHelper", SHORTS_AUDIO_DIRECTORY_PATH);
        return SHORTS_AUDIO_DIRECTORY_PATH;
    }

    /*
        Đây là Root directory của shorts
        Trả về thời đường dẫn external + shorts (là thư mục video ngắn)
        Ví dụ /storage/emulated/0/Android/data/com.vtstudio.fxbox/files/shorts
     */
    public static String getShortsPath(Context context, int basePath){

        if(SHORTS_DIRECTORY_PATH != null && basePath == APP) return SHORTS_DIRECTORY_PATH;

        String external_path = context.getExternalFilesDir(null).getAbsolutePath();
        File file = null;

        if(basePath == APP) {
            file = new File(external_path, SHORTS_DIRECTORY);
        } else {
            file = new File(getSaveStoragePath(context), SHORTS_DIRECTORY);
        }

        if(!file.exists()){
            boolean isCreated = file.mkdirs();
            if(!isCreated) throw new IllegalStateException("Cannot create internal shorts file directory");
        }

        if(basePath != APP) {
            return file.getAbsolutePath();
        }

        SHORTS_DIRECTORY_PATH = file.getAbsolutePath();
        return SHORTS_DIRECTORY_PATH;
    }


    public static String getSaveStoragePath(Context context){

        if(SAVE_STORAGE_DIRECTORY_PATH != null) return SAVE_STORAGE_DIRECTORY_PATH;

        String external_path = context.getExternalFilesDir(null).getAbsolutePath();
        File file = new File(external_path, SAVE_STORAGE_DIRECTORY);
        if(!file.exists()){
            boolean isCreated = file.mkdirs();
            if(!isCreated) throw new IllegalStateException("Cannot create internal shorts file directory");
        }
        SAVE_STORAGE_DIRECTORY_PATH = file.getAbsolutePath();
        return SAVE_STORAGE_DIRECTORY_PATH;
    }

    /*
        Root directory: shorts
        Trả về đường dẫn external trong thư mục Android dùng để lưu trữ hình ảnh của author video ngắn
     */
    public static String getShortsAuthorImagePath(Context context, int basePath){
        if(SHORTS_AUTHOR_IMAGE_DIRECTORY_PATH != null && basePath == APP) return SHORTS_AUTHOR_IMAGE_DIRECTORY_PATH;

        String dir_path = getShortsPath(context, basePath);

        File author = new File(dir_path, AUTHOR_DIRECTORY);
        if(!author.exists()){
            boolean isCreated = author.mkdirs();
            if(!isCreated) throw new IllegalStateException("Cannot create internal author file directory");
        }

        File image = new File(author, IMAGE_DIRECTORY);
        if(!image.exists()) {
            boolean isCreated = image.mkdirs();
            if(!isCreated) throw new IllegalStateException("Cannot create author image file directory");
        }

        if(basePath != APP) {
            return image.getAbsolutePath();
        }

        SHORTS_AUTHOR_IMAGE_DIRECTORY_PATH = image.getAbsolutePath();
        return SHORTS_AUTHOR_IMAGE_DIRECTORY_PATH;
    }

    /*
        Root directory: shorts
        Trả về đường dẫn external trong thư mục Android dùng lưu trữ hình ảnh của âm nhạc
     */
    public static String getShortsAudioImagePath(Context context, int basePath){
        if(SHORTS_AUDIO_IMAGE_DIRECTORY_PATH!= null && basePath == APP) return SHORTS_AUDIO_IMAGE_DIRECTORY_PATH;

        String dir_path = getShortsAudioPath(context, basePath);

        File image = new File(dir_path, IMAGE_DIRECTORY);
        if(!image.exists()) {
            boolean isCreated = image.mkdirs();
            if(!isCreated) throw new IllegalStateException("Cannot create audio image file directory");
        }

        if(basePath != APP) {
            return image.getAbsolutePath();
        }

        SHORTS_AUDIO_IMAGE_DIRECTORY_PATH = image.getAbsolutePath();
        return SHORTS_AUDIO_IMAGE_DIRECTORY_PATH;
    }

    /*
     *  @Youtube: Functions for youtube data source
     */

    public static String getYoutubePath(Context context, int basePath){

        if(YOUTUBE_DIRECTORY_PATH != null && basePath == APP) return YOUTUBE_DIRECTORY_PATH;

        String external_path = context.getExternalFilesDir(null).getAbsolutePath();
        File file = null;

        if(basePath == APP) {
            file = new File(external_path, YOUTUBE_DIRECTORY);
        } else {
            file = new File(getSaveStoragePath(context), YOUTUBE_DIRECTORY);
        }

        if(!file.exists()){
            boolean isCreated = file.mkdirs();
            if(!isCreated) throw new IllegalStateException("Cannot create internal youtube file directory");
        }

        if(basePath != APP) {
            return file.getAbsolutePath();
        }

        YOUTUBE_DIRECTORY_PATH = file.getAbsolutePath();
        return YOUTUBE_DIRECTORY_PATH;
    }

    public static String getYoutubeVideoPath(Context context, int basePath){
        if(YOUTUBE_VIDEO_DIRECTORY_PATH != null && basePath == APP) return YOUTUBE_VIDEO_DIRECTORY_PATH;

        String dir_path = getYoutubePath(context, basePath);

        File video = new File(dir_path, VIDEO_DIRECTORY);
        if(!video.exists()){
            boolean isCreated = video.mkdirs();
            if(!isCreated) throw new IllegalStateException("Cannot create internal video file directory");
        }

        if(basePath != APP) {
            return video.getAbsolutePath();
        }

        YOUTUBE_VIDEO_DIRECTORY_PATH = video.getAbsolutePath();
        Log.d("PreferenceHelper", YOUTUBE_VIDEO_DIRECTORY_PATH);
        return YOUTUBE_VIDEO_DIRECTORY_PATH;
    }

    public static String getYoutubeVideoThumbnailPath(Context context, int basePath){
        if(YOUTUBE_VIDEO_IMAGE_DIRECTORY_PATH != null && basePath == APP) return YOUTUBE_VIDEO_IMAGE_DIRECTORY_PATH;

        String dir_path = getYoutubeVideoPath(context, basePath);

        File image = new File(dir_path, IMAGE_DIRECTORY);
        if(!image.exists()) {
            boolean isCreated = image.mkdirs();
            if(!isCreated) throw new IllegalStateException("Cannot create video image file directory");
        }

        if(basePath != APP) {
            return image.getAbsolutePath();
        }

        YOUTUBE_VIDEO_IMAGE_DIRECTORY_PATH = image.getAbsolutePath();
        return YOUTUBE_VIDEO_IMAGE_DIRECTORY_PATH;
    }

    public static String getYoutubeSubtitlesPath(Context context, int basePath){
        if(YOUTUBE_SUBTITLES_DIRECTORY_PATH != null && basePath == APP) return YOUTUBE_SUBTITLES_DIRECTORY_PATH;

        String dir_path = getYoutubePath(context, basePath);

        File video = new File(dir_path, SUBTITLES_DIRECTORY);
        if(!video.exists()){
            boolean isCreated = video.mkdirs();
            if(!isCreated) throw new IllegalStateException("Cannot create internal subtitles file directory");
        }

        if(basePath != APP) {
            return video.getAbsolutePath();
        }

        YOUTUBE_SUBTITLES_DIRECTORY_PATH = video.getAbsolutePath();
        Log.d("PreferenceHelper", YOUTUBE_SUBTITLES_DIRECTORY_PATH);
        return YOUTUBE_SUBTITLES_DIRECTORY_PATH;
    }

    public static String getYoutubeAuthorImagePath(Context context, int basePath){
        if(YOUTUBE_AUTHOR_IMAGE_DIRECTORY_PATH != null && basePath == APP) return YOUTUBE_AUTHOR_IMAGE_DIRECTORY_PATH;

        String dir_path = getYoutubePath(context, basePath);

        File author = new File(dir_path, AUTHOR_DIRECTORY);
        if(!author.exists()){
            boolean isCreated = author.mkdirs();
            if(!isCreated) throw new IllegalStateException("Cannot create internal author file directory");
        }

        File image = new File(author, IMAGE_DIRECTORY);
        if(!image.exists()) {
            boolean isCreated = image.mkdirs();
            if(!isCreated) throw new IllegalStateException("Cannot create author image file directory");
        }

        if(basePath != APP) {
            return image.getAbsolutePath();
        }

        YOUTUBE_AUTHOR_IMAGE_DIRECTORY_PATH = image.getAbsolutePath();
        return YOUTUBE_AUTHOR_IMAGE_DIRECTORY_PATH;
    }

    /*
        Trả về thời gian Millisecond lưu dữ liệu bao gồm hình ảnh, video, shorts, music,... gần đây nhất. Nếu không có sẽ trả về không
     */
    public static long getTimeSavedToStorage (@NonNull Context context) {
        SharedPreferences appPrefs = context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        if(appPrefs != null) {
            return appPrefs.getLong(TIME_SAVED_TO_STORAGE, 0);
        }
        return 0;
    }

    /*
        Đặt thời gian Millisecond lưu dữ liệu bao gồm hình ảnh, video, shorts, music,... gần đây nhất. Trả về true nếu thành công
     */
    public static void putTimeSavedToStorage (@NonNull Context context, long timeSaved) {
        SharedPreferences appPrefs = context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        if(appPrefs != null) {
            appPrefs.edit().putLong(TIME_SAVED_TO_STORAGE, timeSaved).apply();
        }
    }
}