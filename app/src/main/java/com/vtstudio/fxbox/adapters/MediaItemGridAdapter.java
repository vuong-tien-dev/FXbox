package com.vtstudio.fxbox.adapters;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.databinding.MediaItemGridLayoutBinding;
import com.vtstudio.fxbox.listeners.OnItemListClickListener;
import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.models.Media;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.ui.MyRequestOptions;
import com.vtstudio.fxbox.utils.FormatUtils;
import com.vtstudio.fxbox.utils.ViewsUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MediaItemGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public void setMediaList(List<Media> mediaList) {
        this.mediaList = mediaList;
    }

    private List<Media> mediaList;

    public MediaItemGridAdapter() {
        mediaList = new ArrayList<>();
    }

    private OnItemListClickListener onItemClickListener;
    private int selectedPosition = -1;

    public void setOnItemClickListener(OnItemListClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MediaItemGridLayoutBinding binding = MediaItemGridLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MediaSearchItemHolder(binding, parent.getWidth());
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder baseHolder, int position) {

        MediaSearchItemHolder holder = null;
        if(baseHolder instanceof MediaSearchItemHolder){
            holder = (MediaSearchItemHolder) baseHolder;
        } else {
            return;
        }

        holder.itemView.setSelected(position == selectedPosition);

        Media mediaItem = mediaList.get(position);
        if (mediaItem == null) return;

        int width = holder.itemThumbnail.getWidth();
        int height = holder.itemThumbnail.getHeight();

        width = width > 0 ? width : 120;
        height = height > 0 ? height : 200;
        boolean isImageLoaded = false;
        if (mediaItem instanceof ShortsVideo) {
            ShortsVideo shortsVideo3 = (ShortsVideo) mediaItem;
            if (shortsVideo3.isImageList() && shortsVideo3.getImageListPath() != null) {
                Glide.with(holder.itemView)
                        .load(shortsVideo3.getImageListPath().get(0))
                        .apply(MyRequestOptions.getOptions())
                        .transition(DrawableTransitionOptions.withCrossFade(800))
                        .override(width, height)
                        .into(holder.itemThumbnail);

                isImageLoaded = true;
            }
        }

        if (!isImageLoaded) {
            if(mediaItem instanceof FxMediaVideo) {
                File fileThumbnail = new File(((FxMediaVideo) mediaItem).getFxNotificationLargeIconPath());
                if (fileThumbnail.exists()) {
                    Glide.with(holder.itemView).load(fileThumbnail)
                            .override(width, height)
                            .apply(MyRequestOptions.getOptions())
                            .transition(DrawableTransitionOptions.withCrossFade(800))
                            .dontAnimate()
                            .into(holder.itemThumbnail);
                    isImageLoaded = true;
                }
            }
        }

        if(!isImageLoaded) {
            Glide.with(holder.itemView).load(mediaItem.getPlayUri())
                    .override(width, height)
                    .transition(DrawableTransitionOptions.withCrossFade(800))
                    .dontAnimate()
                    .apply(MyRequestOptions.getOptions())
                    .into(holder.itemThumbnail);
        }

        // solved data
        String name = mediaItem.getMediaStoreName();
        String authorName = mediaItem.getMediaStoreParent();

        long likeCount = 0;
        if (mediaItem instanceof ShortsVideo) {
            name = ((ShortsVideo) mediaItem).getDescription();
            ShortsVideo shortsVideo = (ShortsVideo) mediaItem;
            ShortsUser user = shortsVideo.getShortsUser();
            likeCount = shortsVideo.getLikeCount();
            if (user != null) {
                authorName = user.getNickName();
                Glide.with(holder.itemView).load(user.getAvatarPath()).skipMemoryCache(true)
                        .apply(MyRequestOptions.getOptions())
                        .override(holder.authorAvatar.getWidth()).placeholder(R.drawable.default_avatar_user).into(holder.authorAvatar);
            }
        }

        String formatLikeCount = FormatUtils.formatCount(likeCount);
        holder.itemName.setText(name);
        holder.itemAuthorName.setText(authorName);
        holder.likeCount.setText(formatLikeCount);
        MediaSearchItemHolder finalHolder = holder;
        holder.itemView.setOnClickListener(v -> {
            notifyOnItemClick(finalHolder.getBindingAdapterPosition());
        });
        Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.scale_in);
        animation.setDuration(50);
        holder.itemView.startAnimation(animation);

    }

    public void notifyItemSelectedChange(int newPosition) {
        if (newPosition == selectedPosition) return;

        int current = selectedPosition;
        selectedPosition = newPosition;
        notifyItemChanged(current);
        notifyItemChanged(selectedPosition);

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            int current1 = selectedPosition;
            selectedPosition = -1;
            notifyItemChanged(current1);
        }, 1000);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {

    }

    private void notifyOnItemClick(int position) {
        if (onItemClickListener != null) onItemClickListener.onClick(position);
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    public List<Media> getMediaList() {
        return mediaList;
    }

    public static class MediaSearchItemHolder extends RecyclerView.ViewHolder {
        private final MediaItemGridLayoutBinding binding;

        private ImageView itemThumbnail;
        private ImageView authorAvatar;
        private TextView itemName;
        private TextView itemAuthorName;
        private TextView likeCount;

        public MediaSearchItemHolder(@NonNull MediaItemGridLayoutBinding binding, int parentWidth) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setMaxWidth(parentWidth/2);
            itemThumbnail = binding.searchThumbnail;
            itemName = binding.searchDescription;
            itemAuthorName = binding.searchAuthorNameTv;
            authorAvatar = binding.searchAuthorAvt;
            likeCount = binding.searchLikeCountTv;
            Glide.with(itemView).load(R.drawable.like_outline).into(binding.searchFavoriteClick);
            ViewsUtils.setTouchScaleEffect(binding.getRoot(), 0.95f);
        }
    }
}
