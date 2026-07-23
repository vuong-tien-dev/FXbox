package com.vtstudio.fxbox.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.databinding.ShortsVideoProfileItemBinding;
import com.vtstudio.fxbox.listeners.OnItemListClickListener;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.ui.MyRequestOptions;
import com.vtstudio.fxbox.utils.FormatUtils;

import java.util.List;

public class ShortsVideoProfileAdapter extends RecyclerView.Adapter<ShortsVideoProfileAdapter.ShortsProfileHolder> {
    public static final int THUMBNAIL_THRESHOLD = 60;
    private List<ShortsVideo> shortsVideos;
    private OnItemListClickListener onItemClickListener;

    public ShortsVideoProfileAdapter(List<ShortsVideo> shortsVideos) {
        this.shortsVideos = shortsVideos;
    }

    @NonNull
    @Override
    public ShortsProfileHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ShortsVideoProfileItemBinding binding = ShortsVideoProfileItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ShortsProfileHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ShortsProfileHolder holder, int position) {
        if(shortsVideos == null) return;

        ShortsVideo video = shortsVideos.get(position);
        ShortsVideoProfileItemBinding binding = holder.binding;

        if(shortsVideos.size() > THUMBNAIL_THRESHOLD) {

        } else if (video.isImageList()) {
            Glide.with(holder.itemView).load(video.getImageListPath().get(0))
                    .override(binding.shortsVideoProfileThumbnail.getWidth(), binding.shortsVideoProfileThumbnail.getHeight())
                    .apply(MyRequestOptions.getOptions())
                    .into(binding.shortsVideoProfileThumbnail).clearOnDetach();
        } else {
            Glide.with(holder.itemView).load(video.getMediaStorePath())
                    .override(binding.shortsVideoProfileThumbnail.getWidth(), binding.shortsVideoProfileThumbnail.getHeight())
                    .apply(MyRequestOptions.getOptions())
                    .into(binding.shortsVideoProfileThumbnail).clearOnDetach();
        }

        binding.shortsVideoProfilePlayCount.setText(FormatUtils.formatCount(video.getPlayCount()));

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) onItemClickListener.onClick(position);
        });

    }

    @Override
    public int getItemCount() {
        return shortsVideos.size();
    }

    public void setOnItemClickListener(OnItemListClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public static class ShortsProfileHolder extends RecyclerView.ViewHolder {
        private final ShortsVideoProfileItemBinding binding;

        public ShortsProfileHolder(@NonNull ShortsVideoProfileItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
