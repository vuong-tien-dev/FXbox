package com.vtstudio.fxbox.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.database.dao.PlaylistDao;
import com.vtstudio.fxbox.database.dao.ShortsVideoDao;
import com.vtstudio.fxbox.database.dao.YTVideoDao;
import com.vtstudio.fxbox.databinding.AnchorPosLabelLayoutBinding;
import com.vtstudio.fxbox.databinding.CapcutLabelLayoutBinding;
import com.vtstudio.fxbox.databinding.LimitContentLayBinding;
import com.vtstudio.fxbox.databinding.LoadMoreLayoutBinding;
import com.vtstudio.fxbox.databinding.MediaTypePhotoLayoutBinding;
import com.vtstudio.fxbox.databinding.ShortsContentsLayoutBinding;
import com.vtstudio.fxbox.databinding.ShortsForImagesItemLayoutBinding;
import com.vtstudio.fxbox.databinding.ShortsItemLayoutBinding;
import com.vtstudio.fxbox.databinding.ShortsSyncLayoutBinding;
import com.vtstudio.fxbox.databinding.ShortsWarningLayoutBinding;
import com.vtstudio.fxbox.fxviews.FxLottieView;
import com.vtstudio.fxbox.fxviews.dialog.FxVideoSelectionDialog;
import com.vtstudio.fxbox.fxviews.listview.ImagePagerView;
import com.vtstudio.fxbox.fxviews.textview.FxExpandableTextView;
import com.vtstudio.fxbox.listeners.OnEmptyListener;
import com.vtstudio.fxbox.listeners.OnLoadMoreListener;
import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.models.tiktok.Playlist;
import com.vtstudio.fxbox.media.models.tiktok.ShortsMusic;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.media.models.youtube.YTVideo;
import com.vtstudio.fxbox.media.player.FxPlayer;
import com.vtstudio.fxbox.media.sources.DataSourceBuilder;
import com.vtstudio.fxbox.utils.FormatUtils;
import com.vtstudio.fxbox.utils.ListUtils;
import com.vtstudio.fxbox.utils.TimeUtils;
import com.vtstudio.fxbox.utils.ViewsUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import xyz.hasnat.sweettoast.SweetToast;

public class ShortsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public List<FxMediaVideo> getShortsDataList() {
        return shortsDataList;
    }

    private List<FxMediaVideo> shortsDataList;
    private SimpleExoPlayer player;
    private int currentPlayingIndex = -1;

    public ShortsItem getCurrentPlayingItem() {
        return currentPlayingItem;
    }

    private boolean canLoadMore;
    protected ShortsItem currentPlayingItem;
    protected FxPlayer fxPlayer;
    private RecyclerView recyclerView;
    private Player.Listener listener;
    public boolean isPlaying() {
        return isPlaying;
    }
    private boolean isPlaying;
    private boolean isAutoSwipe;
    private int bottomMargin;
    private boolean recyclable = true;
    private final ExecutorService executor;
    private FxRoomDB roomDB;
    protected PlaylistDao playlistDao;
    protected ShortsVideoDao shortsVideoDao;
    protected YTVideoDao ytvideoDao;
    protected Playlist historyList;
    protected Playlist favoritesList;
    protected Playlist playingPlaylist;

    public Playlist getPlayingPlaylist() {
        return playingPlaylist;
    }

    public void setPlayingPlaylist(Playlist playingPlaylist) {
        this.playingPlaylist = playingPlaylist;
    }

    private boolean isInPipMode;
    private boolean isRelease;
    protected boolean shouldRemoveWhenLimit = false;

    public boolean isShouldRemoveWhenLimit() {
        return shouldRemoveWhenLimit;
    }

    private OnLoadMoreListener onLoadMoreListener;

    public boolean isCanLoadMore() {
        return canLoadMore;
    }
    private OnEmptyListener onEmptyListener;
    private List<Integer> synchronizingIndexList;
    private List<Integer> uploadingIndexList;

    public void setShouldRemoveWhenLimit(boolean shouldRemoveWhenLimit) {
        this.shouldRemoveWhenLimit = shouldRemoveWhenLimit;
    }

    public ShortsListAdapter(Context context) {
        executor = Executors.newSingleThreadExecutor();
        roomDB = FxRoomDB.get(context);
        playlistDao = roomDB.playlistDao();
        shortsVideoDao = roomDB.shortsVideoDao();
        ytvideoDao = roomDB.ytvideoDao();
        historyList = playlistDao.getPlaylistById(Playlist.RECENTLY_VIEWED_PLAYLIST);
        favoritesList = playlistDao.getPlaylistById(Playlist.FAVORITE_PLAYLIST_ID);
    }

    public void setRecyclerView(RecyclerView parent) {
        this.recyclerView = parent;
    }

    public void setFxPlayer(@NonNull FxPlayer fxPlayer) {
        this.fxPlayer = fxPlayer;
        this.player = fxPlayer.getPlayer();
        this.listener = new Player.Listener() {
            @Override
            public void onRenderedFirstFrame() {
                Player.Listener.super.onRenderedFirstFrame();
                if (currentPlayingItem != null) {
                    currentPlayingItem.binding.shortsVideoThumbnail.setVisibility(View.INVISIBLE);
                }
            }
        };
        this.player.addListener(listener);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == 0) {
            ShortsItemLayoutBinding itemBinding = ShortsItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            itemBinding.shortsVideoContainer.setZ(0);
            itemBinding.shortsDescriptionFadingEdge.setZ(1);
            itemBinding.shortsContents.getRoot().setZ(2);
            return new ShortsItem(itemBinding, this.bottomMargin);
        }
        LoadMoreLayoutBinding binding = LoadMoreLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MediaItemAdapter.LoadMoreItemHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder baseHolder, int position) {


        if (baseHolder instanceof LoadMoreItemHolder) {
            return;
        }

        final ShortsItem holder = baseHolder instanceof ShortsItem ? ((ShortsItem) baseHolder) : null;

        if (isRelease || holder == null) return;

        holder.setShowingViewsSync(checkHolderIsSynchronizing(holder.getBindingAdapterPosition()));

        Glide.with(holder.itemView).clear(holder.binding.shortsVideoThumbnail);
        Glide.with(holder.itemView).clear(holder.binding.shortsContents.shortsReelPivotThumbnail);
        Glide.with(holder.itemView).clear(holder.binding.shortsContents.shortsAuthorAvatar);

        FxMediaVideo fxMedia = shortsDataList.get(position);
        Context context = holder.itemView.getContext();
        ShortsItemLayoutBinding binding = holder.binding;


        boolean isImageList = false;

        ShortsVideo shorts = null;

        if (fxMedia instanceof ShortsVideo) {
            shorts = (ShortsVideo) fxMedia;

            holder.checkAndInflateExtraViews(shorts);

            if (shorts.isImageList()) {
                binding.shortsImgListContainer.setVisibility(View.VISIBLE);
                binding.shortsPlayerView.setVisibility(View.GONE);


                ImagePagerView pagerView = new ImagePagerView(holder.itemView.getContext(), shorts.getImageListPath(), binding.shortsContents.dotsIndicator);
                pagerView.setOnClickListener(v -> {
                    if (fxPlayer.isPlaying()) {
                        fxPlayer.pause();
                        onPause();
                    } else {
                        fxPlayer.play();
                        onResume();
                    }
                });

                pagerView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        ArrayList<Integer> selectionsDefault = ShortsAdapterUtils.buildConfigSelections(ShortsListAdapter.this, fxMedia);

                        FxVideoSelectionDialog dialog = new FxVideoSelectionDialog(context, fxMedia, selectionsDefault);
                        ShortsAdapterUtils.bindSelectionListener(dialog, holder, ShortsListAdapter.this, playingPlaylist);
                        dialog.show();
                        return true;
                    }
                });


                // AppWatcher.INSTANCE.getObjectWatcher().watch(pagerView, " View đã bị gỡ khỏi cửa sổ");
                holder.checkAndInflateExtraViews(shorts, pagerView);
                isImageList = true;
            } else {
                binding.shortsPlayerView.setVisibility(View.VISIBLE);
                binding.shortsImgListContainer.setVisibility(View.GONE);
            }

        }

        String description = shorts != null ? shorts.getDescription() : fxMedia instanceof YTVideo ? ((YTVideo) fxMedia).getDescription() : null;
        String descriptionTitle = shorts != null ? shorts.getDescriptionTitle() : null;
        String finalDesc = null;


        if (descriptionTitle == null || descriptionTitle.trim().isEmpty()) {
            finalDesc = description;
            binding.shortsContents.shortsDescriptionTv.setMaxLines(2);
        } else {
            if (description.contains(descriptionTitle) && description.indexOf(descriptionTitle) == 0) {
                binding.shortsContents.shortsDescriptionTv.setMaxLines(4);
                finalDesc = description;
            } else {
                finalDesc = descriptionTitle + '\n' + description;
                binding.shortsContents.shortsDescriptionTv.setMaxLines(4);
            }
        }

        Log.d("Shorts", "Shorts: " + finalDesc);
        binding.shortsContents.shortsDescriptionTv.setTitle(descriptionTitle);
        binding.shortsContents.shortsDescriptionTv.setText(finalDesc);

        // get user and music ref

        ShortsUser user = null;
        if (shorts != null) {
            user = shorts.getShortsUser();
        }
        ShortsMusic music = null;
        if (shorts != null) {
            music = shorts.getShortsMusic();
        }

        Log.d("Shorts", "duration: " + fxMedia.getDuration());

        // solved data
        String authorName = user != null ? user.getNickName() : fxMedia.getMediaStoreParent();
        String musicName = music != null ? music.getTitle() : fxMedia.getMediaStoreName();
        if (musicName != null && !musicName.contains("original")) {
            musicName = context.getString(R.string.contains_music_from) + " " + musicName;
        }
        String dayAdded = TimeUtils.getTimeAgo(fxMedia.getMediaStoreDayAdded(), context);

        //count strings
        String likeCount = shorts != null ? FormatUtils.formatCount(shorts.getLikeCount()) : "0";
        String commentCount = shorts != null ? FormatUtils.formatCount(shorts.getCommentCount()) : "0";
        String shareCount = shorts != null ? FormatUtils.formatCount(shorts.getShareCount()) : "0";


        boolean isSetAuthorImg = false;

        if(fxMedia instanceof YTVideo) {
            YTVideo ytvideo = (YTVideo)fxMedia;
            if(ytvideo.getUser() != null) {
                authorName = ytvideo.getUser().getChannelName();
            }
            musicName = ytvideo.getTitle();
            likeCount = FormatUtils.formatCount(ytvideo.getLikeCount());
            ShortsAdapterUtils.setDefaultOrUserAvatar(ytvideo.getUser(), binding.shortsContents.shortsAuthorAvatar);
            isSetAuthorImg = true;

        }

        // set Text for shorts contents
        binding.shortsContents.shortsAuthorNameTv.setText(authorName);
        binding.shortsContents.shortsMusicNameTv.setText(musicName);
        binding.shortsContents.shortsDateAddedTv.setText(dayAdded);
        binding.shortsContents.shortsLikeCountTv.setText(likeCount);
        binding.shortsContents.shortsCommentCountTv.setText(commentCount);
        binding.shortsContents.shortsShareCountTv.setText(shareCount);

        // load ảnh thu nhỏ
        ShortsAdapterUtils.setDefaultOrMusicAvatar(music, fxMedia.getMediaStorePath(), binding.shortsContents.shortsReelPivotThumbnail);

        binding.shortsContents.shortsFavoriteClick.setLikeState(fxMedia.isFavorite());

        if(!isSetAuthorImg) ShortsAdapterUtils.setDefaultOrUserAvatar(user, binding.shortsContents.shortsAuthorAvatar);

        ShortsAdapterUtils.loadLeftMusicNameIcon(binding.shortsContents.shortsMusicNote, fxMedia);


        boolean verified = user != null && user.getVerified();
//        if (verified) {
//            Log.d("TiktokPackage", user.getChannelName() + " is verified");
//            @SuppressLint("UseCompatLoadingForDrawables") Drawable verifiedDrw = context.getDrawable(R.drawable.verify);
////            int height = binding.shortsContents.shortsAuthorNameTv.getHeight();
////            height = height == 0 ? 24 : height;
//            verifiedDrw.setBounds(0, 6, 36, 40);
//            binding.shortsContents.shortsAuthorNameTv.setCompoundDrawables(null, null, verifiedDrw, null);
//            binding.shortsContents.shortsAuthorNameTv.setCompoundDrawablePadding(10);
//        }
        binding.shortsContents.shortsAuthorNameTv.setVerified(verified);

        // solve events for views
        // 2. solve button view more for description

        // 1. solved resize mode

        float scale = (fxMedia.getHeight() * 1.f) / fxMedia.getWidth();

        boolean isVisible = description != null && !description.isEmpty();

        binding.shortsContents.shortsDescriptionTv.setVisibility(isVisible ? View.VISIBLE : View.GONE);

        // make edge length
        if (isVisible) {
            if (holder.expandRunnable != null) {
                binding.shortsContents.shortsDescriptionTv.removeCallbacks(holder.expandRunnable);
            }
            holder.expandRunnable = new Runnable() {
                @Override
                public void run() {
                    if (binding.shortsContents.shortsDescriptionTv.expandable()) {
                        binding.shortsContents.shortsDescriptionTv.addOnExpandListener(holder.createFadeListener());
                    }
                }
            };
            binding.shortsContents.shortsDescriptionTv.post(holder.expandRunnable);
        }

        // Nếu item đang đồnn bộ dữ liệu với tiktok
        if (holder.isShowingViewsSync()) {
            holder.setViewsSyncVisibility(true);
            if (isHolderUploading(position)) {
                holder.setViewsSyncText(context.getString(R.string.push_to_desktop_progress));
            }
        }

        // Kiểm tra video bị giới hạn
        if(!fxMedia.isCanPlayLimited()) {
            LimitContentLayBinding limitBinding = LimitContentLayBinding.inflate(LayoutInflater.from(context));
            View limitRootView = limitBinding.getRoot();
            limitRootView.setZ(3);
            limitRootView.setId(R.id.limit_layout);
            ViewsUtils.setTouchScaleEffect(limitBinding.viewButton, 0.95f);
            limitBinding.viewButton.setOnClickListener(v -> {
                if(fxPlayer != null) {
                    fxPlayer.play();
                    onResume();
                }
                holder.binding.getRoot().removeView(limitRootView);
                fxMedia.setUserIgnoredLimited(true);
            });
            holder.binding.getRoot().addView(limitRootView);
        }

        if (isImageList) return;

        final long bindTag = fxMedia.getFxId();
        binding.shortsVideoThumbnail.setTag(bindTag);

        executor.execute(() -> {
            if (isRelease) return;

            if (!new File(fxMedia.getFxThumbnailPath()).exists()) {
                Log.d("ShortsVideo", "is Not Found so creating " + fxMedia.getMediaStoreName());
                DataSourceBuilder builder = DataSourceBuilder.get(context);
                builder.createFxThumbnail(fxMedia);
                if (fxMedia instanceof ShortsVideo) shortsVideoDao.update((ShortsVideo) fxMedia);
                else roomDB.fxMediaVideoDao().update(fxMedia);
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(fxMedia.getFxThumbnailPath(), options);
            options.inSampleSize = calculateInSampleSize(options, 540, 960);
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(fxMedia.getFxThumbnailPath(), options);

            if (isRelease) {
                if (bitmap != null) bitmap.recycle();
                return;
            }

            ((Activity) holder.itemView.getContext()).runOnUiThread(() -> {
                Object currentTag = binding.shortsVideoThumbnail.getTag();
                if (!(currentTag instanceof Long) || (long) currentTag != bindTag) {
                    if (bitmap != null) bitmap.recycle();
                    return;
                }

                binding.shortsVideoThumbnail.setImageDrawable(null);

                if (scale > 1.41f) {
                    binding.shortsVideoThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    binding.shortsVideoThumbnail.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }

                if (scale > 1.41f) {
                    holder.binding.shortsPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
                } else {
                    holder.binding.shortsPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
                }

                binding.shortsVideoThumbnail.setImageBitmap(bitmap);
            });
        });
    }


    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder baseHolder) {

        final ShortsItem holder = baseHolder instanceof ShortsItem ? ((ShortsItem) baseHolder) : null;

        if (!recyclable || holder == null) return;

        holder.binding.shortsVideoThumbnail.setTag(null);
        holder.binding.shortsVideoThumbnail.setImageBitmap(null);

        android.content.Context context = holder.itemView.getContext();
        boolean canGlide = true;
        if (context instanceof android.app.Activity) {
            android.app.Activity activity = (android.app.Activity) context;
            if (activity.isFinishing() || activity.isDestroyed()) {
                canGlide = false;
            }
        }
        if (canGlide) {
            try {
                Glide.with(context).clear(holder.binding.shortsVideoThumbnail);
            } catch (Exception e) {
                // Ignore glide errors during recycle/destroy
            }
        }

        if (holder.expandRunnable != null) {
            holder.binding.shortsContents.shortsDescriptionTv.removeCallbacks(holder.expandRunnable);
            holder.expandRunnable = null;
        }

        holder.binding.shortsContents.shortsFavoriteClick.clearAnimation();

        holder.binding.shortsIcPlay.setVisibility(View.GONE);
        if (holder.isImageTypeLabelShow()) {
            holder.binding.shortsImgListContainer.removeAllViews();
        }

        holder.binding.shortsContents.shortsMusicNameTv.setText(null);
        holder.binding.shortsContents.shortsDescriptionTv.setMaxLines(2);
        holder.binding.shortsContents.shortsDescriptionTv.notifyOnViewRecycled();
        holder.binding.shortsContents.dotsIndicator.setVisibility(View.GONE);
        holder.binding.shortsContents.shortsAuthorNameTv.setVerified(false);


        if (holder.isShowingViewsSync()) {
            holder.setShowingViewsSync(false);
            holder.setViewsSyncVisibility(false);
        }

        View limitedView = holder.binding.getRoot().findViewById(R.id.limit_layout);
        if(limitedView != null) {
            holder.binding.getRoot().removeView(limitedView);
        }

        holder.onRecycled();

//        holder.binding.shortsContents.shortsAuthorNameTv.setCompoundDrawables(null, null, null, null);
//        holder.binding.shortsContents.shortsAuthorNameTv.setCompoundDrawablePadding(0);
//        if (parent != null) {
//            parent.getRecycledViewPool().clear();
//        }
    }

    protected void requestLoadMoreIfNeeded() {
        if (currentPlayingItem != null && shortsDataList != null
                && currentPlayingItem.getBindingAdapterPosition() >= shortsDataList.size() - 2
                && canLoadMore && onLoadMoreListener != null) {
            onLoadMoreListener.onLoadMoreEvent(true, isAutoSwipe);
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        if ((holder instanceof MediaItemAdapter.LoadMoreItemHolder
                || (isAutoSwipe && shortsDataList != null && holder.getBindingAdapterPosition() >= shortsDataList.size() - 2))
                && onLoadMoreListener != null && canLoadMore) {
            onLoadMoreListener.onLoadMoreEvent(true, isAutoSwipe);
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof MediaItemAdapter.LoadMoreItemHolder && onLoadMoreListener != null) {
            onLoadMoreListener.onLoadMoreEvent(false, false);
        }
    }

    /*
     * Những phương thức dùng đẻ nhận sự kiện trình phát
     */


    @SuppressLint("ClickableViewAccessibility")
    public void onPlay(ShortsItem item, int position, boolean override) {

        if (position == currentPlayingIndex && !override) return;

        Context context = item.itemView.getContext();
        FxMediaVideo fxMedia = shortsDataList.get(position);
        ShortsItemLayoutBinding binding = item.binding;
        ShortsContentsLayoutBinding contentsView = binding.shortsContents;
        TextView musicNameTv = contentsView.shortsMusicNameTv;

        if (currentPlayingItem != null && currentPlayingItem.isScrolling) {
            ViewsUtils.startFadeAnimWithFillAfter(currentPlayingItem.binding.shortsContents.getRoot(), true);
            currentPlayingItem.isScrolling = false;
        }


        GestureDetector detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(@NonNull MotionEvent e) {
                ArrayList<Integer> selectionsDefault = ShortsAdapterUtils.buildConfigSelections(ShortsListAdapter.this, fxMedia);
                FxVideoSelectionDialog dialog = new FxVideoSelectionDialog(context, fxMedia, selectionsDefault);
                ShortsAdapterUtils.bindSelectionListener(dialog, item, ShortsListAdapter.this, playingPlaylist);
                dialog.show();
                super.onLongPress(e);
            }

            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                if (fxPlayer.isPlaying()) {
                    fxPlayer.pause();
                    onPause();
                } else {
                    fxPlayer.play();
                    onResume();
                }
                return true;
            }
        });

// Sử dụng onTouchListener để gọi detector
        binding.shortsVideoContainer.setOnTouchListener((v, event) -> {
            return detector.onTouchEvent(event);
        });


        // đặt sự kiện và hiệu ứng nút like
        contentsView.shortsFavoriteClick.setOnClickListener(v -> {
            Log.d("FxLottie", "click");
            if (item.getBindingAdapterPosition() >= 0 && item.getBindingAdapterPosition() < shortsDataList.size()) {
                FxLottieView like = contentsView.shortsFavoriteClick;
                boolean newState = !fxMedia.isFavorite();
                like.setLikeState(newState);
                fxMedia.setFavorite(newState);
                ShortsAdapterUtils.setLikeCountIfShortsVideo(fxMedia, contentsView.shortsLikeCountTv, newState);
                if (newState) {
                    like.playAnimation();
                    ListUtils.addUnique(favoritesList.getVideoIdList(), String.valueOf(fxMedia.getFxId()));
                } else {
                    ListUtils.removeAllOccurrences(favoritesList.getVideoIdList(), String.valueOf(fxMedia.getFxId()));
                }
                executor.execute(() -> {
                    if (fxMedia instanceof ShortsVideo)
                        shortsVideoDao.update((ShortsVideo) fxMedia);
                    if (fxMedia instanceof YTVideo)
                        ytvideoDao.update((YTVideo) fxMedia);

                    playlistDao.update(favoritesList);
                });
            }
        });

        Glide.with(contentsView.shortsReelPivotAnim)
                .load(R.drawable.reel_pivot_anim)
                .override(contentsView.shortsReelPivotAnim.getWidth(), contentsView.shortsReelPivotAnim.getHeight())
                .into(contentsView.shortsReelPivotAnim);

        // sự kiện nút chia sẻ
        ShortsAdapterUtils.setListenerOfShareButton(contentsView.shortsShareButton, fxMedia);

        ShortsAdapterUtils.setCommentButtonIfNeed(binding.shortsContents.shortsCommentContainer, fxMedia, context);
        ShortsAdapterUtils.setUserProfileClickIfNeed(binding.shortsContents.shortsAuthorAvatar, fxMedia, context);

        musicNameTv.setSelected(true);

        binding.shortsContents.shortsReelPivotContainer.startAnimation(AnimationUtils.loadAnimation(context, R.anim.rotate));

        if (!isInPipMode) {
            binding.shortsPlayerView.setPlayer(player);
        }

        ListUtils.addUnique(historyList.getVideoIdList(), String.valueOf(fxMedia.getFxId()));
        playlistDao.update(historyList);

        currentPlayingIndex = position;
        currentPlayingItem = item;
    }


    public void onPause() {
        if (currentPlayingItem != null) {
            currentPlayingItem.binding.shortsIcPlay.setVisibility(View.VISIBLE);
            currentPlayingItem.binding.shortsContents.shortsMusicNameTv.setSelected(false);
            currentPlayingItem.binding.shortsContents.shortsReelPivotContainer.setAnimation(null);
            isPlaying = false;
        }
    }

    public void onResume() {
        if (currentPlayingItem != null) {
            currentPlayingItem.binding.shortsIcPlay.setVisibility(View.GONE);
            currentPlayingItem.binding.shortsContents.shortsMusicNameTv.setSelected(true);
            currentPlayingItem.binding.shortsContents.shortsReelPivotContainer.startAnimation(AnimationUtils.loadAnimation(currentPlayingItem.itemView.getContext(), R.anim.rotate));
            isPlaying = true;
        }
    }

    public void onStop() {
        if (currentPlayingItem != null) {

            try {
                Glide.with(currentPlayingItem.itemView).clear(currentPlayingItem.binding.shortsContents.shortsReelPivotAnim);
            } catch (Exception ignored) {

            }

            ShortsItemLayoutBinding binding = currentPlayingItem.binding;
            ShortsContentsLayoutBinding contentsView = binding.shortsContents;
            TextView musicNameTv = contentsView.shortsMusicNameTv;

            ShortsAdapterUtils.disableListenerOf(binding);

            binding.shortsIcPlay.setVisibility(View.GONE);
            binding.shortsVideoThumbnail.setVisibility(View.VISIBLE);
            binding.shortsContents.shortsReelPivotContainer.setAnimation(null);
            binding.shortsPlayerView.setPlayer(null);

            contentsView.getRoot().setAlpha(1f);
            musicNameTv.setSelected(false);

        }
    }

    public void onScrolling() {
        if (currentPlayingItem != null && !currentPlayingItem.isScrolling) {
            currentPlayingItem.binding.shortsContents.shortsMusicNameTv.setSelected(false);
            currentPlayingItem.binding.shortsContents.shortsReelPivotContainer.setAnimation(null);
            setContentEffect(false);
            currentPlayingItem.isScrolling = true;
        }
    }

    public void onScrollIdle() {
        if (currentPlayingItem != null && currentPlayingItem.isScrolling) {
            if (fxPlayer != null && fxPlayer.isPlaying()) {
                currentPlayingItem.binding.shortsContents.shortsMusicNameTv.setSelected(true);
                currentPlayingItem.binding.shortsContents.shortsReelPivotContainer.startAnimation(AnimationUtils.loadAnimation(currentPlayingItem.itemView.getContext(), R.anim.rotate));
            }
            currentPlayingItem.isScrolling = false;
            setContentEffect(true);
        }
    }

    public void setContentEffect(boolean effectIn) {
        if (currentPlayingItem != null) {
            ViewsUtils.startFadeAnimWithFillAfter(currentPlayingItem.binding.shortsContents.getRoot(), effectIn);
        }
    }

    public void setContentVisibilityEffect(boolean visible) {
        if (currentPlayingItem != null) {
            ViewsUtils.makeVisibilityWithAnim(currentPlayingItem.binding.shortsContents.getRoot(), visible);
        }
    }

    public void setContentVisibility(boolean visible) {
        if (currentPlayingItem != null) {
            currentPlayingItem.binding.shortsContents.getRoot().setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void setHolderUploadingIndex(int index, boolean uploading) {
        if (uploading) {
            if (uploadingIndexList == null) uploadingIndexList = new ArrayList<>();
            if (!uploadingIndexList.contains(index)) uploadingIndexList.add(index);
        } else if (uploadingIndexList != null) {
            uploadingIndexList.remove(Integer.valueOf(index));
            if (uploadingIndexList.isEmpty()) uploadingIndexList = null;
        }
    }

    private boolean isHolderUploading(int index) {
        return uploadingIndexList != null && uploadingIndexList.contains(index);
    }

    public void addHolderSyncIndex(int index) {
        if (synchronizingIndexList == null) {
            synchronizingIndexList = new ArrayList<>();
        }

        if (!synchronizingIndexList.contains(index)) synchronizingIndexList.add(index);
    }

    public void removeHolderSyncIndex(int index) {
        if(synchronizingIndexList != null) {
            synchronizingIndexList.remove(Integer.valueOf(index));

            if(synchronizingIndexList.isEmpty()) {
                synchronizingIndexList = null;
            }
        }
    }

    private boolean checkHolderIsSynchronizing(int index) {
        if(synchronizingIndexList != null) {
            return synchronizingIndexList.contains(index);
        }
        return false;
    }

    @Override
    public int getItemCount() {
        if (shortsDataList == null) return 0;
        return shortsDataList.size() + (canLoadMore ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (shortsDataList == null) return 0;
        return position >= shortsDataList.size() ? 1 : 0;
    }

    public void setShortsDataList(List<FxMediaVideo> shortsDataList) {
        this.shortsDataList = shortsDataList;
    }

    public int getCurrentPlayingIndex() {
        return currentPlayingIndex;
    }

    public void setCurrentPlayingIndex(int currentPlayingIndex) {
        this.currentPlayingIndex = currentPlayingIndex;
    }

    public FxPlayer getFxPlayer() {
        return fxPlayer;
    }

    public void setBottomMargin(int bottomMargin) {
        this.bottomMargin = bottomMargin;
    }

    public void refreshPlayerView() {
        if (currentPlayingItem != null) {
            currentPlayingItem.binding.shortsPlayerView.setPlayer(null);
            currentPlayingItem.binding.shortsPlayerView.setPlayer(player);
        }
    }

    public void release() {
        if (currentPlayingItem != null) {
            onStop();
        }
        if (listener != null && player != null) {
            player.removeListener(listener);
        }

        listener = null;
        player = null;
        recyclerView = null;
        currentPlayingItem = null;
        shortsDataList = null;
        fxPlayer = null;
        recyclable = false;
        playlistDao = null;
        roomDB = null;
        historyList = null;
        favoritesList = null;
        playingPlaylist = null;
        synchronizingIndexList = null;
        uploadingIndexList = null;
        onLoadMoreListener = null;
        onEmptyListener = null;
        shortsVideoDao = null;
        ytvideoDao = null;

        if (!executor.isShutdown()) {
            executor.shutdown();
        }

        isRelease = true;
    }

    public static ShortsItem getItemAt(@NonNull RecyclerView parent, int index) {
        RecyclerView.ViewHolder viewHolder = parent.findViewHolderForAdapterPosition(index);

        if (viewHolder instanceof ShortsListAdapter.ShortsItem) {
            return (ShortsItem) viewHolder;
        }
        return null;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public boolean isInPipMode() {
        return isInPipMode;
    }

    public void setInPipMode(boolean inPipMode) {
        isInPipMode = inPipMode;
    }

    public void setCanLoadMore(boolean canLoadMore) {
        this.canLoadMore = canLoadMore;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public void setOnEmptyListener(OnEmptyListener onEmptyListener) {
        this.onEmptyListener = onEmptyListener;
    }

    public void notifyOnEmpty() {
        if (onEmptyListener != null) {
            onEmptyListener.onEmpty();
        }
    }

    public boolean isAutoSwipe() {
        return isAutoSwipe;
    }

    public void setAutoSwipe(boolean autoSwipe) {
        isAutoSwipe = autoSwipe;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static class ShortsItem extends RecyclerView.ViewHolder {
        public final ShortsItemLayoutBinding binding;
        public static final int FLAG_SYNC_DATA = 1;
        public static final int FLAG_IMAGE_TYPE = 2;
        public static final int FLAG_CAPCUT_TEMPLATE = 4;
        public static final int FLAG_ADVISE_VIEWER = 8;
        public static final int FLAG_DO_NOT_ATTEMPT = 16;
        public static final int FLAG_PLACE = 32;
        private int views_states_flag = 0;
        private boolean isScrolling;
        private ShortsSyncLayoutBinding syncLayoutBinding;
        private FxExpandableTextView.OnExpandListener listener;
        private Runnable expandRunnable;

        public ShortsItem(@NonNull ShortsItemLayoutBinding binding, int bottomMargin) {
            super(binding.getRoot());
            this.binding = binding;

            binding.shortsContents.shortsDescriptionTv.setSelected(true);

            //  solved layout content
//            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) binding.shortsContents.getRoot().getLayoutParams();
//            params.bottomMargin = bottomMargin;

            ShortsAdapterUtils.bindInteractiveViews(binding);
        }

        public void checkAndInflateExtraViews(@NonNull ShortsVideo shorts, @Nullable ImagePagerView pagerView) {
            Context context = itemView.getContext();

            if(pagerView != null && shorts.isImageList() && !isImageTypeLabelShow()) {
                View view = MediaTypePhotoLayoutBinding.inflate(LayoutInflater.from(context)).getRoot();
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                view.setLayoutParams(layoutParams);
                binding.shortsContents.shortsLabelTopView.addView(view);
                binding.shortsImgListContainer.addView(pagerView);
                views_states_flag |= FLAG_IMAGE_TYPE;
            }

            if(!isPlaceLabelShow()) {
                String anchorPosId = shorts.getAnchorPosId();
                String anchorPosKeyword = shorts.getAnchorPosKeyword();
                if (anchorPosId != null && anchorPosKeyword != null && !anchorPosId.isEmpty() && !anchorPosKeyword.isEmpty()) {
                    AnchorPosLabelLayoutBinding anchorPosLabelLayoutBinding = AnchorPosLabelLayoutBinding.inflate(LayoutInflater.from(context));
                    anchorPosLabelLayoutBinding.anchorText.setText(anchorPosKeyword);
                    View view = anchorPosLabelLayoutBinding.getRoot();
                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    view.setLayoutParams(layoutParams);
                    view.setOnClickListener(v -> {
                        try {
                            Uri uri = Uri.parse("https://www.tiktok.com/place/" + anchorPosKeyword + "-" + anchorPosId);
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            context.startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            SweetToast.error(context, context.getString(R.string.cannot_launch_acitivity));
                        }
                    });
                    binding.shortsContents.shortsLabelTopView.addView(view);
                    views_states_flag |= FLAG_PLACE;
                }
            }

            if (shorts.isWarnAttempt() && !isAdvisedViewerShow()) {
                ShortsWarningLayoutBinding warningBinding = ShortsWarningLayoutBinding.inflate(LayoutInflater.from(context));
                if (shorts.getWarningType() == ShortsVideo.WARNING_TYPE_BEFORE_VIEWING) {
                    warningBinding.warnText.setText(R.string.warn_before_viewing);
                    views_states_flag |= FLAG_ADVISE_VIEWER;
                }

                binding.shortsContents.shortsDescriptionTv.post(new Runnable() {
                    @Override
                    public void run() {
                        if (binding.shortsContents.shortsDescriptionTv.expandable()) {
                            binding.shortsContents.shortsBottomExtraView.addView(warningBinding.getRoot());
                        } else {
                            warningBinding.warnText.setMaxLines(Integer.MAX_VALUE);
                            warningBinding.warnText.setTextColor(itemView.getContext().getColor(R.color.white_a85));
                            warningBinding.getRoot().setBackgroundColor(itemView.getContext().getColor(R.color.rgb_48_a30));
                            binding.shortsContents.shortsExtraView.addView(warningBinding.getRoot());
                        }
                    }
                });
            }

            String schema = shorts.getCapcutSchema();
            String templateId = shorts.getCapcutTemplateId();

            if (!isCapcutLabelShow() && schema != null && templateId != null && !schema.isEmpty() && !templateId.isEmpty()) {
                View view = CapcutLabelLayoutBinding.inflate(LayoutInflater.from(context)).getRoot();
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                view.setLayoutParams(layoutParams);
                view.setOnClickListener(v -> {
                    try {
                        Uri uri = Uri.parse("https://www.capcut.com/template-detail/" + templateId);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        context.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        SweetToast.error(context, context.getString(R.string.cannot_launch_acitivity));
                    }
                });
                binding.shortsContents.shortsLabelTopView.addView(view);
                views_states_flag |= FLAG_CAPCUT_TEMPLATE;
            }
        }


        public void checkAndInflateExtraViews (@NonNull ShortsVideo shorts) {
            checkAndInflateExtraViews(shorts, null);
        }

        public void setViewsSyncText(String text) {
            if (syncLayoutBinding != null) syncLayoutBinding.syncText.setText(text);
        }

        public void setViewsSyncVisibility(boolean visible) {
            if (visible && syncLayoutBinding == null) {
                syncLayoutBinding = ShortsSyncLayoutBinding.inflate(LayoutInflater.from(itemView.getContext()));
                binding.shortsContents.shortsBottomExtraView.addView(syncLayoutBinding.getRoot());
            } else if (!visible && syncLayoutBinding != null) {
                binding.shortsContents.shortsBottomExtraView.removeView(syncLayoutBinding.getRoot());
                syncLayoutBinding = null;
            }
        }

        public FxExpandableTextView.OnExpandListener createFadeListener() {
            if (listener == null) {
                listener = new FxExpandableTextView.SimpleOnExpandListener() {
                    @Override
                    public void onExpanding(@NonNull FxExpandableTextView view) {
                        Log.d("ShortsAdapter", "onExpanding ");
                        binding.shortsDescriptionFadingEdge.getLayoutParams().height =
                                (int) (binding.shortsContents.getRoot().getHeight() -
                                        binding.shortsContents.shortsDescriptionTv.getTop() * 0.6f);

                        binding.shortsDescriptionFadingEdge.requestLayout();
                    }

                    @Override
                    public void onCollapsing(@NonNull FxExpandableTextView view) {
                        binding.shortsDescriptionFadingEdge.getLayoutParams().height =
                                (int) (binding.shortsContents.getRoot().getHeight() -
                                        binding.shortsContents.shortsDescriptionTv.getTop() * 0.6f);

                        binding.shortsDescriptionFadingEdge.requestLayout();
                    }


                    @Override
                    public void onCollapsed(@NonNull FxExpandableTextView view) {
                        binding.shortsDescriptionFadingEdge.getLayoutParams().height = 0;
                        binding.shortsDescriptionFadingEdge.requestLayout();
                    }
                };
            }
            return listener;
        }

        public void onRecycled () {
            if(binding != null) {
                binding.shortsContents.shortsExtraView.removeAllViews();
                binding.shortsContents.shortsLabelTopView.removeAllViews();
                binding.shortsContents.shortsBottomExtraView.removeAllViews();
                views_states_flag &= FLAG_SYNC_DATA;
            }
        }

        public boolean isShowingViewsSync() {
            return (views_states_flag & FLAG_SYNC_DATA) == FLAG_SYNC_DATA;
        }

        public void setShowingViewsSync(boolean showingViewsSync) {
            if(showingViewsSync)
                views_states_flag |= FLAG_SYNC_DATA;
            else
                views_states_flag &= ~FLAG_SYNC_DATA;
        }

        public boolean isCapcutLabelShow () {
            return (views_states_flag & FLAG_CAPCUT_TEMPLATE) == FLAG_CAPCUT_TEMPLATE;
        }

        public boolean isAdvisedViewerShow () {
            return (views_states_flag & FLAG_ADVISE_VIEWER) == FLAG_ADVISE_VIEWER;
        }

        public boolean isDoNotAttemptShow () {
            return (views_states_flag & FLAG_DO_NOT_ATTEMPT) == FLAG_DO_NOT_ATTEMPT;
        }

        public boolean isPlaceLabelShow () {
            return (views_states_flag & FLAG_PLACE) == FLAG_PLACE;
        }

        public boolean isImageTypeLabelShow () {
            return (views_states_flag & FLAG_IMAGE_TYPE) == FLAG_IMAGE_TYPE;
        }
    }

    public static class LoadMoreItemHolder extends RecyclerView.ViewHolder {
        private final LoadMoreLayoutBinding binding;

        public LoadMoreItemHolder(@NonNull LoadMoreLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    public class ShortsImageItem extends RecyclerView.ViewHolder {
        public final ShortsForImagesItemLayoutBinding itemBinding;
        private boolean isScrolling;

        public ShortsImageItem(@NonNull ShortsForImagesItemLayoutBinding binding) {
            super(binding.getRoot());
            itemBinding = binding;

            //binding.shortsContents.shortsDescriptionTv.setSelected(true);

            //  solved layout content
//            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) binding.shortsContents.getRoot().getLayoutParams();
//            params.bottomMargin = bottomMargin;

            //ShortsForImageAdapterUtils.bindInteractiveViews(binding);
        }
    }

}
