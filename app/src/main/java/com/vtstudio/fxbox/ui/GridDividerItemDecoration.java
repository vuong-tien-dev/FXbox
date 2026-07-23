package com.vtstudio.fxbox.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GridDividerItemDecoration extends RecyclerView.ItemDecoration {

    private final Paint dividerPaint;
    private final int dividerSize;

    public GridDividerItemDecoration(Context context, int dividerColor, int dividerSize) {
        this.dividerPaint = new Paint();
        this.dividerPaint.setColor(dividerColor);
        this.dividerSize = dividerSize;
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        drawVerticalDividers(c, parent);
    }

    private void drawVerticalDividers(Canvas canvas, RecyclerView parent) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int left = child.getRight() + params.rightMargin;
            int right = left + dividerSize;

        }
    }


    private int getSpanCount(RecyclerView parent) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            return ((GridLayoutManager) layoutManager).getSpanCount();
        }
        return 1;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int spanCount = getSpanCount(parent);
        int itemCount = parent.getAdapter().getItemCount();

        int column = position % spanCount;

        outRect.top = 0;
        outRect.left = 0;
        outRect.right = dividerSize;
        outRect.bottom = dividerSize;

        // Only add top margin for the first row
        if (position < spanCount) {
            outRect.top = dividerSize;
        }

        // Only add left margin for the first column
        if (column == 0) {
            outRect.left = dividerSize;
        }

        // Only add right margin for the last column
        if ((position + 1) % spanCount == 0) {
            outRect.right = 0;
        }

        // Only add bottom margin for the last row
        if (position >= itemCount - spanCount) {
            outRect.bottom = 0;
        }
    }
}
