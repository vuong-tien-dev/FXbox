package com.vtstudio.fxbox.utils;

import android.annotation.SuppressLint;

public class FormatUtils {
    private static final int THOUSAND = 1000;
    private static final int TEN_THOUSAND = 10000;
    private static final long MILLION = 1000000L;
    private static final String KILO_SUFFIX = "N";
    private static final String MILLION_SUFFIX = "Tr";
    @SuppressLint("DefaultLocale")
    public static String formatCount(long count) {
        if (count < TEN_THOUSAND) {
            return String.valueOf(count);
        } else if (count < MILLION) {
            double formattedCount = count / (double) THOUSAND;
            return String.format("%.1f %s", formattedCount, KILO_SUFFIX);
        } else {
            double formattedCount = count / (double) MILLION;
            return String.format("%.1f %s", formattedCount, MILLION_SUFFIX);
        }
    }

    @SuppressLint("DefaultLocale")
    public static String formatCountThousand(long count) {
        if (count < THOUSAND) {
            double formattedCount = count / (double) THOUSAND;
            return String.format("%.1f %s", formattedCount, KILO_SUFFIX);
        } else if (count < MILLION) {
            double formattedCount = count / (double) THOUSAND;
            return String.format("%.1f %s", formattedCount, KILO_SUFFIX);
        } else {
            double formattedCount = count / (double) MILLION;
            return String.format("%.1f %s", formattedCount, MILLION_SUFFIX);
        }
    }
}