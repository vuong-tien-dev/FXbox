package com.vtstudio.fxbox.listeners;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public interface OnSnapVideoListener {
    boolean onSnap(@NonNull RecyclerView.ViewHolder holder, int videoIndex);
}