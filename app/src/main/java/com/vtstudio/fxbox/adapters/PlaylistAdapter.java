package com.vtstudio.fxbox.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.database.dao.FxMediaVideoDao;
import com.vtstudio.fxbox.database.dao.ShortsVideoDao;
import com.vtstudio.fxbox.database.dao.YTVideoDao;
import com.vtstudio.fxbox.databinding.AddNewPlaylistBinding;
import com.vtstudio.fxbox.databinding.PlaylistItemLayoutBinding;
import com.vtstudio.fxbox.listeners.OnItemListClickListener;
import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.models.tiktok.Playlist;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.ui.MyRequestOptions;

import java.io.File;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Playlist> playlistList;
    public PlaylistAdapter(List<Playlist> playlistList) {
        this.playlistList = playlistList;
    }

    public List<Playlist> getPlaylistList() {
        return playlistList;
    }

    public OnItemListClickListener onItemClickListener;
    public OnItemListClickListener onCreatePlaylistClickListener;
    private int itemWidth;
    private int itemHeight;

    public void setOnCreatePlaylistClickListener(OnItemListClickListener onCreatePlaylistClickListener) {
        this.onCreatePlaylistClickListener = onCreatePlaylistClickListener;
    }

    public void setOnItemClickListener(OnItemListClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setPlaylistList(List<Playlist> playlistList) {
        this.playlistList = playlistList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            PlaylistItemLayoutBinding binding = PlaylistItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new PlaylistHolder(binding);
        } else {
            AddNewPlaylistBinding binding = AddNewPlaylistBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            binding.getRoot().setMinHeight(itemHeight);
            binding.getRoot().setMinWidth(itemWidth);
            return new CreatePlaylistHolder (binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder baseHolder, int position) {

        if (baseHolder instanceof PlaylistHolder) {
            PlaylistHolder holder = (PlaylistHolder) baseHolder;
            Playlist playlist = playlistList.get(position);

            if (playlist.getBackGroundId() != null && !playlist.getBackGroundId().isEmpty()) {
                Glide.with(holder.itemView).load(playlist.getBackgroundPath())
                        .apply(MyRequestOptions.getOptions())
                        .override(holder.binding.playlistThumbnail.getWidth(), holder.binding.playlistThumbnail.getHeight())
                        .into(holder.binding.playlistThumbnail);
            } else {
                playlist.getVideoIdList();
                if (playlist.getVideoIdList().size() > 0) {

                    long id = Long.parseLong(playlist.getVideoIdList().get(0));
                    FxRoomDB db = FxRoomDB.get(holder.itemView.getContext());
                    ShortsVideoDao shortsVideoDao = db.shortsVideoDao();
                    FxMediaVideoDao fxMediaVideoDao = db.fxMediaVideoDao();
                    YTVideoDao ytvideoDao = db.ytvideoDao();

                    Log.d("Playlist", "id: " + playlist.getId());
                    Log.d("Playlist", "count: " + playlist.getVideoIdList().size());
                    Log.d("Playlist", "bg: " + playlist.getVideoIdList().get(0));

                    FxMediaVideo media;
                    media = shortsVideoDao.getVideoByFxId(id);

                    if (media == null) {
                        media = fxMediaVideoDao.getVideoByFxId(id);
                    }

                    if(media == null) {
                        media = ytvideoDao.getVideoByFxId(id);
                    }

                    if (media != null) {

                        Log.d("Playlist", "Media not null");

                        int width = holder.binding.playlistThumbnail.getWidth();
                        int height = holder.binding.playlistThumbnail.getHeight();

                        width = width > 0 ? width : 150;
                        height = height > 0 ? height : 200;
                        boolean isImageLoaded = false;
                        if (media instanceof ShortsVideo) {
                            ShortsVideo shortsVideo3 = (ShortsVideo) media;
                            if (shortsVideo3.isImageList() && shortsVideo3.getImageListPath() != null) {
                                Glide.with(holder.itemView)
                                        .load(shortsVideo3.getImageListPath().get(0))
                                        .apply(MyRequestOptions.getOptions())
                                        .override(width, height)
                                        .into(holder.binding.playlistThumbnail);

                                isImageLoaded = true;
                            }
                        }

                        if (!isImageLoaded) {
                            File fileThumbnail = new File(media.getFxNotificationLargeIconPath());
                            if (fileThumbnail.exists()) {
                                Glide.with(holder.itemView).load(fileThumbnail)
                                        .override(width, height)
                                        .dontAnimate()
                                        .apply(MyRequestOptions.getOptions())
                                        .into(holder.binding.playlistThumbnail);
                                Log.d("MediaItemSearch", "is loading in notification thumbnail");
                                isImageLoaded = true;
                            }
                        }

                        if (!isImageLoaded) {
                            Glide.with(holder.itemView).load(media.getPlayUri())
                                    .override(width, height)
                                    .dontAnimate()
                                    .apply(MyRequestOptions.getOptions())
                                    .into(holder.binding.playlistThumbnail);
                            Log.d("MediaItemSearch", "is loading in playUri");
                        }
                    }

                }
            }

            holder.binding.playlistName.setText(playlist.getName());

            List<String> idList = playlist.getVideoIdList();

            holder.binding.playlistItemCount.setText(holder.itemView.getContext().getString(R.string.video_count, idList.size()));

            if(itemWidth == 0) {
                itemWidth = holder.itemView.getWidth();
            }

            if(itemHeight == 0) {
                itemHeight = holder.itemView.getHeight();
            }

        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        if (holder.getBindingAdapterPosition() < getItemCount() - 1) {
            holder.itemView.setOnClickListener(v -> {
                if (onItemClickListener != null)
                    onItemClickListener.onClick(holder.getLayoutPosition());
            });
        } else {
            holder.itemView.setOnClickListener(v -> {
                if (onCreatePlaylistClickListener != null)
                    onCreatePlaylistClickListener.onClick(0);
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return playlistList != null && position < playlistList.size() ? 0 : 1;
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        holder.itemView.setOnClickListener(null);
    }

    @Override
    public int getItemCount() {
        return playlistList == null ? 1 : playlistList.size() + 1;
    }

    public static class PlaylistHolder extends RecyclerView.ViewHolder {
        final PlaylistItemLayoutBinding binding;

        public PlaylistHolder(@NonNull PlaylistItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static class CreatePlaylistHolder extends RecyclerView.ViewHolder {

        public CreatePlaylistHolder(@NonNull AddNewPlaylistBinding binding) {
            super(binding.getRoot());
        }
    }
}
