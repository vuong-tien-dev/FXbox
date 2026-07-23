package com.vtstudio.fxbox.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClipboardUtils {
    public static String getFirstText(@NonNull Context context) {
        // Lấy đối tượng ClipboardManager
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        // Kiểm tra xem clipboard có dữ liệu không
        if (!clipboardManager.hasPrimaryClip()) {
            return null; // Trả về null nếu clipboard trống
        }

        // Lấy dữ liệu từ clipboard
        ClipData clipData = clipboardManager.getPrimaryClip();
        if (clipData == null || clipData.getItemCount() == 0) {
            return null; // Trả về null nếu không có dữ liệu trong clipboard
        }

        // Lấy văn bản từ mục đầu tiên trong clipboard
        ClipData.Item item = clipData.getItemAt(0);
        CharSequence text = item.getText();
        if (text != null) {
            return text.toString();
        }

        return null;
    }

    public static List<String> getAllTextsFromClipboard(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        List<String> texts = new ArrayList<>();

        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData clipData = clipboard.getPrimaryClip();
            for (int i = 0; i < clipData.getItemCount(); i++) {
                CharSequence text = clipData.getItemAt(i).getText();
                if (text != null) {
                    texts.add(text.toString());
                }
            }
        }

        return texts;
    }
    /**
     * Sao chép đoạn văn bản vào Clipboard.
     *
     * @param context  Context của ứng dụng.
     * @param label    Nhãn cho ClipData.
     * @param textToCopy Đoạn văn bản muốn copy.
     */
    public static void copyToClipboard(Context context, CharSequence label, CharSequence textToCopy) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            ClipData clip = ClipData.newPlainText(label, textToCopy);
            clipboardManager.setPrimaryClip(clip);
        }
    }

    /**
     * Sao chép đoạn văn bản vào Clipboard.
     *
     * @param context     Context của ứng dụng.
     * @param textToCopy  Đoạn văn bản muốn copy.
     */
    public static void copyToClipboard(Context context, CharSequence textToCopy) {
        copyToClipboard(context, "text", textToCopy);
    }

}