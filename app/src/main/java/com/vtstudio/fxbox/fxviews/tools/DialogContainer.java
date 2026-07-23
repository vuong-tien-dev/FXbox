package com.vtstudio.fxbox.fxviews.tools;

import android.app.Dialog;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Objects;

/**
 * Created by Álvaro Blanco Cabrero on 01/05/2017.
 * Zoomy.
 */

public class DialogContainer implements TargetContainer {
    private boolean isNavigationBarShow = false;
    private boolean isStatusBarShow = false;
    private Dialog mDialog;

    DialogContainer(Dialog dialog) {
        this.mDialog = dialog;
    }

    @Override
    public final ViewGroup getDecorView() {
        return mDialog.getWindow() != null ? (ViewGroup) mDialog.getWindow().getDecorView() : null;
    }

    @Override
    public void requestFullscreen() {
        if(mDialog.getWindow() == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsets insets = mDialog.getWindow().getDecorView().getRootWindowInsets();
            WindowInsetsController controller = mDialog.getWindow().getInsetsController();
            if (insets != null && controller != null) {
                isNavigationBarShow = insets.isVisible(WindowInsetsCompat.Type.navigationBars());
                isStatusBarShow = insets.isVisible(WindowInsetsCompat.Type.statusBars());
                controller.hide(WindowInsetsCompat.Type.systemBars());
            }
            WindowCompat.setDecorFitsSystemWindows(mDialog.getWindow(), true);
        } else {
            getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    @Override
    public void restoreNonFullscreenState() {
        if(mDialog.getWindow() == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(mDialog.getWindow(), false);
            WindowInsetsController controller = mDialog.getWindow().getInsetsController();

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
                    View contentView = mDialog.findViewById(android.R.id.content);
                    contentView.setPadding(0, !isStatusBarShow ?  0 : insets.getInsets(WindowInsetsCompat.Type.statusBars()).top, 0, !isNavigationBarShow ? 0 : insets.getInsets(WindowInsets.Type.navigationBars()).bottom);
                    return insets;
                });
            }
        } else {
            getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }
}
