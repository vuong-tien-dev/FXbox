package com.vtstudio.fxbox.fxviews.tools;

import android.view.ViewGroup;

/**
 * Created by Álvaro Blanco Cabrero on 01/05/2017.
 * Zoomy.
 */

public interface TargetContainer {

    ViewGroup getDecorView();
    void requestFullscreen();
    void restoreNonFullscreenState();
}
