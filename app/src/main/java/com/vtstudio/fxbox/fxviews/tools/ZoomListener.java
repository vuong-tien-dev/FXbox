package com.vtstudio.fxbox.fxviews.tools;

import android.view.View;

/**
 * Created by Álvaro Blanco Cabrero on 12/02/2017.
 * Zoomy.
 */

public interface ZoomListener {
    void onViewStartedZooming(View view);
    void onViewEndedZooming(View view);
}
