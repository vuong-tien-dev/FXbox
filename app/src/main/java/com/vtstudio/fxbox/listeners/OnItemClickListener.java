package com.vtstudio.fxbox.listeners;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public interface OnItemClickListener {
    public void onItemClick(RecyclerView.Adapter<?> parent, View view, int position, long id);
}
