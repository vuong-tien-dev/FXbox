package com.vtstudio.fxbox.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;

import com.vtstudio.fxbox.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    public static String getTimeAgo(Long dataDate, Context context) {
// Khai báo SimpleDateFormat là một biến tĩnh
        String convTime = null;

        String suffix = context.getResources().getString(R.string.ago);

        Date pasTime = new Date(dataDate);

        Date nowTime = new Date();

        long dateDiff = 0;
        dateDiff = nowTime.getTime() - pasTime.getTime();

        long second = TimeUnit.MILLISECONDS.toSeconds(dateDiff);
        long minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff);
        long hour = TimeUnit.MILLISECONDS.toHours(dateDiff);
        long day = TimeUnit.MILLISECONDS.toDays(dateDiff);

        if (second < 60) {
            convTime = second + " " + context.getResources().getString(R.string.seconds) + " " + suffix;
        } else if (minute < 60) {
            convTime = minute + " " + context.getResources().getString(R.string.minute) + " " + suffix;
        } else if (hour < 24) {
            convTime = hour + " " + context.getResources().getString(R.string.hours) + " " + suffix;
        } else if (day >= 7) {
            if (day > 360) {
                convTime = (day / 360) + " " + context.getResources().getString(R.string.years) + " " + suffix;
            } else if (day > 30) {
                if(day/30 > 1){
                    convTime = SimpleDateFormat.getDateInstance().format(new Date(dataDate));
                } else {
                    convTime = (day / 30) + " " + context.getResources().getString(R.string.months) + " " + suffix;
                }
            } else {
                convTime = (day / 7) + " " + context.getResources().getString(R.string.weeks) + " " + suffix;
            }
        } else if (day < 7) {
            convTime = day + " " + context.getResources().getString(R.string.days) + " " + suffix;
        }

        return convTime;
    }

    public static String getTimeText (long time, Context context) {
        String convTime = null;

        long second = TimeUnit.MILLISECONDS.toSeconds(time);
        long minute = TimeUnit.MILLISECONDS.toMinutes(time);
        long hour = TimeUnit.MILLISECONDS.toHours(time);
        long day = TimeUnit.MILLISECONDS.toDays(time);

        if (second < 60) {
            convTime = second + " " + context.getResources().getString(R.string.seconds);
        } else if (minute < 60) {
            convTime = minute + " " + context.getResources().getString(R.string.minute);
        } else if (hour < 24) {
            convTime = hour + " " + context.getResources().getString(R.string.hours) ;
        } else if (day >= 7) {
            if (day > 360) {
                convTime = (day / 360) + " " + context.getResources().getString(R.string.years);
            } else if (day > 30) {
                if(day/30 > 1){
                    convTime = SimpleDateFormat.getDateInstance().format(new Date(time));
                } else {
                    convTime = (day / 30) + " " + context.getResources().getString(R.string.months);
                }
            } else {
                convTime = (day / 7) + " " + context.getResources().getString(R.string.weeks);
            }
        } else if (day < 7) {
            convTime = day + " " + context.getResources().getString(R.string.days) + " ";
        }

        return convTime;
    }

    @SuppressLint("SimpleDateFormat")
    public static String formatDuration(long time) {
        return new SimpleDateFormat("mm:ss").format(time);
    }
}