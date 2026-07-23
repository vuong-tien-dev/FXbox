package com.vtstudio.fxbox.utils;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class FileUtils {
    public static boolean copyDirectoryToExternal(File sourceDir, File destinationDir) {
        if (sourceDir == null || destinationDir == null || !sourceDir.isDirectory()) return false;

        File[] files = sourceDir.listFiles();

        if (files == null) return false;

        boolean created = destinationDir.exists() || destinationDir.mkdirs();

        if (created) {
            for (File file : files) {
                File destinationFile = new File(destinationDir, file.getName());
                if (file.isDirectory()) {
                    created = copyDirectoryToExternal(file, destinationFile);
                } else {
                    created = copyFileToExternal(file, destinationFile);
                }

                if (!created) {
                    // Handle the case where copying fails
                    return false;
                }
            }
        } else {
            return false;
        }

        return true;
    }

    public static boolean copyFileToExternal(File sourceFile, File destinationFile) {
        try (InputStream inputStream = Files.newInputStream(sourceFile.toPath());
             OutputStream outputStream = Files.newOutputStream(destinationFile.toPath());) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            return true;
        } catch (IOException e) {
            Log.e("FileUtils", "errol copying " + e.getMessage());
            return false;
        }
    }

}