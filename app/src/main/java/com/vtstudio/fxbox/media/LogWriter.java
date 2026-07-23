package com.vtstudio.fxbox.media;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LogWriter {
    private final File logFile;
    private final List<String> logs;
    private boolean isFlushing = false;
    public LogWriter(@NonNull File logFile) {
        this.logFile = logFile;
        logs = new ArrayList<>();
    }

    public void writeToBuffer(@NonNull String log) {
        logs.add(log);
    }

    public void flush () {

        if(isFlushing) return;

        isFlushing = true;
        try {
            FileWriter writer = new FileWriter(logFile, true);

            for(String log : logs) {
                writer.append(log).append("\n");
            }

            writer.flush();
            writer.close();
            logs.clear();
        } catch (IOException e) {
            Log.e("LogWriter", "Error writing to file " + logFile + " with exception: " + e.getMessage());
        } finally {
            isFlushing = false;
        }
    }
}
