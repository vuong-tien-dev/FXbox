package com.vtstudio.fxbox.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import android.util.Log;

public class ZipUtils {
    private static final String TAG = "ZipUtils";

    /**
     * Giải nén file ZIP từ đường dẫn zipFilePath vào thư mục đích destDirectory.
     * Nếu trong file ZIP có cấu trúc lặp (ví dụ: files/files/abc.txt), sẽ loại bỏ một cấp folder trùng lặp.
     * Nếu tham số replace là false, khi file đã tồn tại sẽ không giải nén file đó.
     *
     * @param zipFilePath   Đường dẫn đến file ZIP.
     * @param destDirectory Thư mục đích giải nén.
     * @param replace       Nếu true thì ghi đè file đã tồn tại, nếu false thì bỏ qua.
     * @throws IOException  Nếu có lỗi đọc/ghi file.
     */
    public static void unzip(String zipFilePath, String destDirectory, boolean replace) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        int totalFiles = 0;     // Tổng số file (không tính thư mục) có trong ZIP
        int extractedFiles = 0; // Số file đã được giải nén

        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();

        while (entry != null) {
            String entryName = entry.getName();

            // Nếu entry có cấu trúc lặp thư mục (ví dụ: files/files/abc.txt),
            // loại bỏ một cấp folder thừa.
            String[] parts = entryName.split("/");
            if (parts.length > 1 && parts[0].equals(parts[1])) {
                StringBuilder sb = new StringBuilder();
                // Bỏ phần folder lặp đầu tiên (parts[0])
                for (int i = 1; i < parts.length; i++) {
                    if (!parts[i].isEmpty()) {
                        sb.append(parts[i]);
                        if (i < parts.length - 1) {
                            sb.append("/");
                        }
                    }
                }
                entryName = sb.toString();
            }

            String filePath = destDirectory + File.separator + entryName;

            if (entry.isDirectory()) {
                // Nếu là thư mục thì tạo thư mục (nếu chưa tồn tại)
                File dir = new File(filePath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            } else {
                totalFiles++;  // Đếm thêm 1 file trong ZIP

                File file = new File(filePath);
                // Đảm bảo thư mục cha đã tồn tại
                File parent = file.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }

                // Nếu file đã tồn tại và không cho phép ghi đè, bỏ qua file này.
                if (file.exists() && !replace) {
                    Log.d(TAG, "Bỏ qua file đã tồn tại: " + filePath);
                } else {
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = zipIn.read(buffer)) != -1) {
                        bos.write(buffer, 0, read);
                    }
                    bos.close();
                    extractedFiles++;
                }
            }

            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();

        Log.d(TAG, "Đã giải nén " + extractedFiles + "/" + totalFiles + " file.");
    }
}
