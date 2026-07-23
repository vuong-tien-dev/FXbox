package com.vtstudio.fxbox.fxviews;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

public class LoadMoreSnapHelper extends PagerSnapHelper {
    private boolean canLoadMore = false;
    private OnSnapLoadMoreHolder onLoadMoreHolder;
    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        final int mTarget = super.findTargetSnapPosition(layoutManager, velocityX, velocityY);
        final int finalIndex = canLoadMore && mTarget == layoutManager.getItemCount() - 1 ? RecyclerView.NO_POSITION : mTarget;
        if(finalIndex == RecyclerView.NO_POSITION && onLoadMoreHolder != null) {
            onLoadMoreHolder.onSnap();
        }
        return finalIndex;
    }

    @Nullable
    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        return super.findSnapView(layoutManager);
    }

    public boolean isCanLoadMore() {
        return canLoadMore;
    }

    public void setCanLoadMore(boolean canLoadMore) {
        this.canLoadMore = canLoadMore;
    }

    public void setOnLoadMoreHolder(OnSnapLoadMoreHolder onLoadMoreHolder) {
        this.onLoadMoreHolder = onLoadMoreHolder;
    }

    public interface OnSnapLoadMoreHolder {
        void onSnap ();
    }
}
