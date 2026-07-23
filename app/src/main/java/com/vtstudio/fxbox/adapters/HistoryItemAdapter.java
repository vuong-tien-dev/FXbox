package com.vtstudio.fxbox.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vtstudio.fxbox.databinding.LibHistoryItemLayBinding;
import com.vtstudio.fxbox.listeners.OnItemListClickListener;
import com.vtstudio.fxbox.media.MediaTool;
import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.models.Media;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.media.models.youtube.YTUser;
import com.vtstudio.fxbox.media.models.youtube.YTVideo;
import com.vtstudio.fxbox.ui.MyRequestOptions;
import com.vtstudio.fxbox.utils.ViewsUtils;

import java.util.List;

public class HistoryItemAdapter extends RecyclerView.Adapter<HistoryItemAdapter.HistoryViewHolder> {

    private List<Media> historyItemList;
    private OnItemListClickListener onItemListClickListener;

    public HistoryItemAdapter(List<Media> historyItemList) {
        this.historyItemList = historyItemList;
    }

    public void setHistoryItemList(List<Media> historyItemList) {
        this.historyItemList = historyItemList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng ViewBinding để inflate layout
        LibHistoryItemLayBinding binding = LibHistoryItemLayBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HistoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        LibHistoryItemLayBinding binding = holder.binding;
        Context context = binding.getRoot().getContext();
        Media media3 = historyItemList.get(position);

        String content, author;
        author = media3.getMediaStoreParent();
        content = media3.getMediaStoreName();

        int w = binding.historyItemImg.getWidth();
        int h = binding.historyItemImg.getHeight();

        if(media3 instanceof ShortsVideo){
            ShortsVideo shortsVideo = (ShortsVideo) media3;
            ShortsUser shortsUser = shortsVideo.getShortsUser();
            content = shortsVideo.getDescription();
            author = shortsUser != null ? shortsUser.getNickName() : author;
        } else if(media3 instanceof YTVideo) {
            YTVideo video = (YTVideo) media3;
            YTUser user = video.getUser();
            content = video.getTitle();
            author = user != null ? user.getChannelName() : author;
        }

        binding.historyItemAuthor.setText(author);
        binding.historyItemContent.setText(content);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemListClickListener != null) {
                    onItemListClickListener.onClick(position);
                }
            }
        });

        if(media3 instanceof FxMediaVideo) {
            Glide.with(context).load(MediaTool.getVideoThumbnail((FxMediaVideo) media3))
                    .override(w, h)
                    .dontAnimate()
                    .dontTransform()
                    .apply(MyRequestOptions.getOptions())
                    .into(binding.historyItemImg)
                    .clearOnDetach();
        }
    }

    @Override
    public int getItemCount() {
        return historyItemList == null ? 0 : historyItemList.size();
    }

    public void setOnItemListClickListener(OnItemListClickListener onItemListClickListener) {
        this.onItemListClickListener = onItemListClickListener;
    }

    // ViewHolder sử dụng ViewBinding
    public static class HistoryViewHolder extends RecyclerView.ViewHolder {

        private final LibHistoryItemLayBinding binding;

        public HistoryViewHolder(LibHistoryItemLayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            ViewsUtils.setTouchScaleEffect(binding.getRoot(), 0.95f);
        }
    }
}
