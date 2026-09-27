package com.vtstudio.fxbox.adapters;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.vtstudio.fxbox.databinding.ShortsListImageItemLayBinding;
import com.vtstudio.fxbox.fxviews.tools.LongPressListener;
import com.vtstudio.fxbox.fxviews.tools.OnZoomChangeListener;
import com.vtstudio.fxbox.fxviews.tools.ZoomListener;
import com.vtstudio.fxbox.fxviews.tools.Zoomy;

import java.util.List;

public class ShortsImageAdapter extends RecyclerView.Adapter<ShortsImageAdapter.ImageHolder> {
    private List<String> imgList;
    private int current = -1;
    private View.OnClickListener onClickListener;
    private View.OnLongClickListener onLongClickListener;
    private int fHeight;
    private int fBottomMargin;
    private int fTop;
    private boolean canScale = true;

    public ShortsImageAdapter(List<String> imgList) {
        this.imgList = imgList;
    }

    @NonNull
    @Override
    public ShortsImageAdapter.ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImageHolder(ShortsListImageItemLayBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ShortsImageAdapter.ImageHolder holder, int position) {
        ImageView photoView = holder.binding.shortsImageItemView;
        Glide.with(holder.itemView).load(imgList.get(position))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        float w = resource.getIntrinsicWidth();
                        float h = resource.getIntrinsicHeight();

                        float ratio = w / h;
                        if (ratio >= 1 && (!canScale || holder.getBindingAdapterPosition() == 0)) {

                            photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) photoView.getLayoutParams();
                            layoutParams.bottomMargin = 0;
                            layoutParams.gravity = Gravity.CENTER_VERTICAL;
                            photoView.setLayoutParams(layoutParams);
                            if (holder.getBindingAdapterPosition() == 0) canScale = false;
                        } else if (ratio <= 9f / 16f && (!canScale || holder.getBindingAdapterPosition() == 0)) {

                            photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) photoView.getLayoutParams();
                            layoutParams.bottomMargin = 0;
                            layoutParams.gravity = Gravity.CENTER;
                            photoView.setLayoutParams(layoutParams);
                            if (holder.getBindingAdapterPosition() == 0) canScale = false;
                        } else  {

                            photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) photoView.getLayoutParams();
                            final int parentWidth = holder.binding.getRoot().getWidth();
                            final int parentHeight = holder.binding.getRoot().getHeight();
                            final int guessHeight = (int) (h * (parentWidth / w));
                            layoutParams.gravity = Gravity.CENTER;

                            if (holder.getBindingAdapterPosition() == 0) {
                                final float invertedRatio = 1 / ratio;
                                fBottomMargin = (int) (((parentHeight - guessHeight)) * (invertedRatio - 1));
                                fHeight = guessHeight;
                                layoutParams.bottomMargin = fBottomMargin;
                                fTop = Math.max((parentHeight - guessHeight) / 2 - layoutParams.bottomMargin, 0);
                                fTop = fTop < parentHeight*0.05f ? 0 : fTop;

                                photoView.setLayoutParams(layoutParams);
                            } else if(canScale) {

                                Handler handler = new Handler();
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (fHeight > 0) {

                                            int differenceHeight = 0;

                                            if(guessHeight == fHeight) {
                                                layoutParams.topMargin = fTop;
                                            } else if (guessHeight < fHeight) {
                                                differenceHeight = fHeight - guessHeight;
                                                layoutParams.topMargin = differenceHeight / 2 + fTop;
                                            } else {
                                                final int total = fHeight + 2*fTop;
                                                if(guessHeight > total) {
                                                    layoutParams.topMargin = 0;
                                                } else  {
                                                    differenceHeight = fHeight - guessHeight; // cho ra diff âm
                                                    layoutParams.topMargin = fTop + differenceHeight/2;
                                                    if (layoutParams.topMargin + guessHeight > parentHeight) {
                                                        layoutParams.topMargin = 0;
                                                    }
                                                }
                                            }

                                           layoutParams.gravity = Gravity.TOP;

                                            photoView.setLayoutParams(layoutParams);
                                        } else {
                                            handler.postDelayed(this, 500);
                                        }
                                    }
                                });
                            } else {
                                photoView.setLayoutParams(layoutParams);
                            }
                        }

                        return false;
                    }
                })
                .into(photoView).clearOnDetach();
    }


    @Override
    public int getItemCount() {
        return imgList == null ? 0 : imgList.size();
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ImageHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.setOnClickListener(null);
        holder.binding.shortsImageItemView.setOnClickListener(null);
        Zoomy.unregister(holder.binding.shortsImageItemView);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ImageHolder holder) {
        super.onViewAttachedToWindow(holder);

        ImageView photoView = holder.binding.shortsImageItemView;
        new Zoomy.Builder((Activity) holder.itemView.getContext())
                .enableImmersiveMode(false)
                .longPressListener(new LongPressListener() {
                    @Override
                    public void onLongPress(View v) {
                        if (onLongClickListener != null) onLongClickListener.onLongClick(v);
                    }
                })
                .target(photoView).register();

        if (onClickListener == null) return;
        photoView.setOnClickListener(onClickListener);
    }

    @Override
    public void onViewRecycled(@NonNull ImageHolder holder) {
        try {
            Glide.with(holder.itemView.getContext()).clear(holder.binding.shortsImageItemView);
        } catch (NullPointerException ignored) {

        }
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    public static class ImageHolder extends RecyclerView.ViewHolder {
        final ShortsListImageItemLayBinding binding;

        public ImageHolder(@NonNull ShortsListImageItemLayBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }
    }
}
