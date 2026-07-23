package com.vtstudio.fxbox.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;

import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.models.Media;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContentUtils {

    public static List<FxMediaVideo> getFxMediaVideoFromStorage(Context context) {
        List<FxMediaVideo> videoList = new ArrayList<>();

        // Khởi tạo một content resolver để truy vấn cơ sở dữ liệu của MediaStore
        ContentResolver contentResolver = context.getContentResolver();

        // Tạo một mảng chứa các cột của bảng MediaStore.Video
        String[] projection = new String[]{
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DATE_TAKEN,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED
        };

        // Sắp xếp theo thứ tự tăng dần theo tên file
        // Sắp xếp theo thứ tự tăng dần theo ngày
        String sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";

        // Thực hiện truy vấn cơ sở dữ liệu của MediaStore.Video
        try (Cursor cursor = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
        )) {
            // Duyệt qua tất cả các dòng của kết quả truy vấn
            // Lấy thông tin của mỗi video và lưu vào đối tượng Video

            int idColumn = ((Cursor) cursor).getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
            int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
            int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    //////////////// lấy data ////////////
                    long id = cursor.getLong(idColumn);
                    String path = cursor.getString(dataColumn);

                    //////////////////////////////////////////////

                    // Nếu định dạng tệp hợp lệ, lấy thông tin của video
                    // Thực hiện xử lý với thông tin của video ở đây

                    // thực hiện xử lí lấy thông tin
                    String title = cursor.getString(titleColumn);
                    long dateAdded = cursor.getLong(dateAddedColumn);
                    Uri contentUri = ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                    String stringId = cursor.getString(idColumn);
                    //String name = cursor.getString(nameColumn);
                    int size = cursor.getInt(sizeColumn);
                    /////////////////////////////////

                    /////// get parentFolder ////////////
                    int index = path.lastIndexOf("/");
                    String videoPath = path.substring(0, index);
                    index = videoPath.lastIndexOf("/");
                    String parentFolder = videoPath.substring(index + 1, videoPath.length());
                    ///////////////////////////////////
                    FxMediaVideo video = new FxMediaVideo(stringId, title, path, parentFolder, dateAdded, size, 0);
                    videoList.add(video);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ////// release retriever //////////

        //////////////////////////////////
        return videoList;
    }

    public static List<ShortsVideo> getShortsVideoFromStorage(Context context) {
        List<ShortsVideo> videoList = new ArrayList<>();

        // Khởi tạo một content resolver để truy vấn cơ sở dữ liệu của MediaStore
        ContentResolver contentResolver = context.getContentResolver();

        // Tạo một mảng chứa các cột của bảng MediaStore.Video
        String[] projection = new String[]{
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DATE_TAKEN,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED
        };

        // Sắp xếp theo thứ tự tăng dần theo tên file
        // Sắp xếp theo thứ tự tăng dần theo ngày
        String sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";

        // Thực hiện truy vấn cơ sở dữ liệu của MediaStore.Video
        try (Cursor cursor = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
        )) {
            // Duyệt qua tất cả các dòng của kết quả truy vấn
            // Lấy thông tin của mỗi video và lưu vào đối tượng Video

            int idColumn = ((Cursor) cursor).getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
            int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
            int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    //////////////// lấy data ////////////
                    long id = cursor.getLong(idColumn);
                    String path = cursor.getString(dataColumn);

                    //////////////////////////////////////////////

                    // Nếu định dạng tệp hợp lệ, lấy thông tin của video
                    // Thực hiện xử lý với thông tin của video ở đây

                    // thực hiện xử lí lấy thông tin
                    String title = cursor.getString(titleColumn);
                    long dateAdded = cursor.getLong(dateAddedColumn);
                    Uri contentUri = ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                    String stringId = cursor.getString(idColumn);
                    //String name = cursor.getString(nameColumn);
                    int size = cursor.getInt(sizeColumn);
                    /////////////////////////////////

                    /////// get parentFolder ////////////
                    int index = path.lastIndexOf("/");
                    String videoPath = path.substring(0, index);
                    index = videoPath.lastIndexOf("/");
                    String parentFolder = videoPath.substring(index + 1, videoPath.length());
                    ///////////////////////////////////
                    ShortsVideo video = new ShortsVideo(stringId, title, path, parentFolder, dateAdded, size, 0);
                    videoList.add(video);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ////// release retriever //////////

        //////////////////////////////////
        return videoList;
    }

    public static FxMediaVideo getFxMediaVideoByPath(Context context, @NonNull String MediaPath) {
        ContentResolver contentResolver = context.getContentResolver();

        try (Cursor cursor = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null,
                MediaStore.Video.Media.DATA + " = ?",
                new String[]{MediaPath},
                null
        )) {
            if (cursor != null) {
                cursor.moveToFirst();

                // Duyệt qua tất cả các dòng của kết quả truy vấn
                // Lấy thông tin của mỗi video và lưu vào đối tượng Video

                int idColumn = ((Cursor) cursor).getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
                int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);

                //////////////// lấy data ////////////
                long id = cursor.getLong(idColumn);
                String path = cursor.getString(dataColumn);

                //////////////////////////////////////////////

                // Nếu định dạng tệp hợp lệ, lấy thông tin của video
                // Thực hiện xử lý với thông tin của video ở đây

                // thực hiện xử lí lấy thông tin
                String title = cursor.getString(titleColumn);
                long dateAdded = cursor.getLong(dateAddedColumn);
                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                String stringId = cursor.getString(idColumn);
                //String name = cursor.getString(nameColumn);
                int size = cursor.getInt(sizeColumn);
                /////////////////////////////////

                /////// get parentFolder ////////////
                int index = path.lastIndexOf("/");
                String videoPath = path.substring(0, index);
                index = videoPath.lastIndexOf("/");
                String parentFolder = videoPath.substring(index + 1, videoPath.length());
                ///////////////////////////////////
                return new FxMediaVideo(stringId, title, path, parentFolder, dateAdded, size, 0);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public static ShortsVideo getShortsVideoByPath(Context context, @NonNull String MediaPath) {
        ContentResolver contentResolver = context.getContentResolver();

        try (Cursor cursor = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null,
                MediaStore.Video.Media.DATA + " = ?",
                new String[]{MediaPath},
                null
        )) {
            if (cursor != null) {
                cursor.moveToFirst();

                // Duyệt qua tất cả các dòng của kết quả truy vấn
                // Lấy thông tin của mỗi video và lưu vào đối tượng Video

                int idColumn = ((Cursor) cursor).getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
                int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);

                //////////////// lấy data ////////////
                long id = cursor.getLong(idColumn);
                String path = cursor.getString(dataColumn);

                //////////////////////////////////////////////

                // Nếu định dạng tệp hợp lệ, lấy thông tin của video
                // Thực hiện xử lý với thông tin của video ở đây

                // thực hiện xử lí lấy thông tin
                String title = cursor.getString(titleColumn);
                long dateAdded = cursor.getLong(dateAddedColumn);
                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                String stringId = cursor.getString(idColumn);
                //String name = cursor.getString(nameColumn);
                int size = cursor.getInt(sizeColumn);
                /////////////////////////////////

                /////// get parentFolder ////////////
                int index = path.lastIndexOf("/");
                String videoPath = path.substring(0, index);
                index = videoPath.lastIndexOf("/");
                String parentFolder = videoPath.substring(index + 1, videoPath.length());
                ///////////////////////////////////
                return new ShortsVideo(stringId, title, path, parentFolder, dateAdded, size, 0);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public static ShortsVideo getShortsMusicByPath(Context context, @NonNull String MediaPath) {
        Log.d("Downloaded", "start to get info music");
        ContentResolver contentResolver = context.getContentResolver();

        try (Cursor cursor = contentResolver.query(
               MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                MediaStore.Audio.Media.DATA + " = ?",
                new String[]{MediaPath},
                null
        )) {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();

                // Duyệt qua tất cả các dòng của kết quả truy vấn
                // Lấy thông tin của mỗi video và lưu vào đối tượng Video

                int idColumn = ((Cursor) cursor).getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED);

                Log.d("Downloaded", "success to get columns");

                //////////////// lấy data ////////////
                long id = cursor.getLong(idColumn);
                Log.d("Downloaded", "success to get id");
                String path = cursor.getString(dataColumn);

                //////////////////////////////////////////////

                // Nếu định dạng tệp hợp lệ, lấy thông tin của video
                // Thực hiện xử lý với thông tin của video ở đây

                // thực hiện xử lí lấy thông tin
                String title = cursor.getString(titleColumn);
                long dateAdded = cursor.getLong(dateAddedColumn);
                String stringId = cursor.getString(idColumn);
                //String name = cursor.getString(nameColumn);
                /////////////////////////////////

                Log.d("Downloaded", "success to get title, dateAdded, stringId");

                /////// get parentFolder ////////////
                int index = path.lastIndexOf("/");
                String videoPath = path.substring(0, index);
                index = videoPath.lastIndexOf("/");
                String parentFolder = videoPath.substring(index + 1, videoPath.length());
                ///////////////////////////////////
                return new ShortsVideo(stringId, title, path, parentFolder, dateAdded, 0, 0);
            } else  {
                Log.d("Downloaded", "Cursor is null - media path: " + MediaPath);
            }
        } catch (Exception e) {
            Log.d("Downloaded", "exception: " +  e.getMessage());
            return null;
        }
        return null;
    }



        public static List<Media> filterByKey (List < Map < String, Media>> results, String key){
            List<Media> mediaList = new ArrayList<>();

            for (Map<String, Media> map : results) {
                if (key != null && map.containsKey(key)) {
                    Media media = map.get(key);
                    mediaList.add(media);
                } else {
                    mediaList.addAll(map.values());
                }
            }

            return mediaList;
        }

        public static List<Media> filterByMediaType (List <Media> results, int key){
            List<Media> mediaList = new ArrayList<>();

            for (Media media : results) {
                if(key == Media.MediaType.TYPE_SHORTS_VIDEO){
                    if(media instanceof ShortsVideo && media.getSocialMediaType() == Media.MediaType.TYPE_SHORTS_VIDEO){
                        mediaList.add(media);
                        continue;
                    }
                }

                if(key == Media.MediaType.TYPE_EXTERNAL_STORAGE && media.getSocialMediaType() == Media.MediaType.TYPE_EXTERNAL_STORAGE){
                    if(media instanceof FxMediaVideo){
                        mediaList.add(media);
                        continue;
                    }
                }
            }

            return mediaList;
        }

    }
