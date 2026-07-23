package com.vtstudio.fxbox;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.vtstudio.fxbox.helpers.PreferenceHelper;
import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.models.tiktok.ShortsMusic;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.media.models.youtube.YTUser;
import com.vtstudio.fxbox.media.models.youtube.YTVideo;

import java.io.File;

import xyz.hasnat.sweettoast.SweetToast;


public class FXApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        File directory = getDir(PreferenceHelper.MEDIA_THUMBNAIL_DIRECTORY, Context.MODE_PRIVATE);
        FxMediaVideo.setFxThumbnailDirectoryPath(directory.getAbsolutePath());
        directory = getDir(PreferenceHelper.MEDIA_LARGE_ICON_DIRECTORY, Context.MODE_PRIVATE);
        FxMediaVideo.setFxNotificationLargeIconDirectoryPath(directory.getAbsolutePath());
        ShortsUser.setAvatarDirectoryPath(PreferenceHelper.getShortsAuthorImagePath(this, 0));
        ShortsMusic.setAvatarDirectoryPath(PreferenceHelper.getShortsAudioImagePath(this, 0));
        ShortsVideo.setImgListDirectoryPath(PreferenceHelper.getShortsImagePath(this, 0));

        // youtube
        YTUser.setUserAvatarDirectoryPath(PreferenceHelper.getYoutubeAuthorImagePath(this, 0));
        YTVideo.setYtVideoThumbnailDirPath(PreferenceHelper.getYoutubeVideoThumbnailPath(this, 0));

        forcePortraitOrientation();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        SweetToast.error(this, getString(R.string.low_memory));
        try {
            Glide.get(this).getBitmapPool().clearMemory();
        } catch (Exception e) {

        }
    }

    private void forcePortraitOrientation() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @SuppressLint("SourceLockedOrientationActivity")
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }
}
