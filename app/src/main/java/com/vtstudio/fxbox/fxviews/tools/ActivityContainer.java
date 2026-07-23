package com.vtstudio.fxbox.fxviews.tools;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Created by Álvaro Blanco Cabrero on 01/05/2017.
 * Zoomy.
 */

public class ActivityContainer implements TargetContainer {

    private Activity mActivity;
    private boolean isNavigationBarShow = false;
    private boolean isStatusBarShow = false;

    ActivityContainer(Activity activity) {
        this.mActivity = activity;
    }

    @Override
    public ViewGroup getDecorView() {
        return (ViewGroup) mActivity.getWindow().getDecorView();
    }

    @Override
    public void requestFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsets insets = mActivity.getWindow().getDecorView().getRootWindowInsets();
            WindowInsetsController controller = mActivity.getWindow().getInsetsController();
            if (insets != null && controller != null) {
                isNavigationBarShow = insets.isVisible(WindowInsetsCompat.Type.navigationBars());
                isStatusBarShow = insets.isVisible(WindowInsetsCompat.Type.statusBars());
                controller.hide(WindowInsetsCompat.Type.systemBars());
               }
            WindowCompat.setDecorFitsSystemWindows(mActivity.getWindow(), true);
        } else {
            getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    @Override
    public void restoreNonFullscreenState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(mActivity.getWindow(), false);
            WindowInsetsController controller = mActivity.getWindow().getInsetsController();

            if (controller != null) {
                controller.show(WindowInsetsCompat.Type.systemBars());
                if (isStatusBarShow) {
                    controller.show(WindowInsetsCompat.Type.statusBars());
                } else {
                    controller.hide(WindowInsetsCompat.Type.statusBars());
                }
                if (isNavigationBarShow) {
                    controller.show(WindowInsetsCompat.Type.navigationBars());
                } else {
                    controller.hide(WindowInsetsCompat.Type.navigationBars());
                }

                getDecorView().setOnApplyWindowInsetsListener((v, insets) -> {
                    View contentView = mActivity.findViewById(android.R.id.content);
                    contentView.setPadding(0, !isStatusBarShow ?  0 : insets.getInsets(WindowInsetsCompat.Type.statusBars()).top, 0, !isNavigationBarShow ? 0 : insets.getInsets(WindowInsets.Type.navigationBars()).bottom);
                    return insets;
                });
            }
        } else {
            getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }
}
