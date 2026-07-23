package com.vtstudio.fxbox.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class MediaStoreUtils {

    public static void updateFileMimeType(Context context, Uri uri, String newMimeType) {
        ContentResolver contentResolver = context.getContentResolver();

        // Tạo ContentValues mới với thông tin mimeType mới
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.MIME_TYPE, newMimeType);

        // Thực hiện cập nhật thông tin
        int updatedRows = contentResolver.update(uri, values, null, null);

        // Kiểm tra xem cập nhật có thành công không
        if (updatedRows > 0) {
            Log.d("MediaStore", "failed to update");
        } else {
            // Cập nhật không thành công
            Log.d("MediaStore", "success to update");
        }
    }
}
