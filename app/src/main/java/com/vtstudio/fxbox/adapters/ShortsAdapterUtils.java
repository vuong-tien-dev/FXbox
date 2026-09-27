package com.vtstudio.fxbox.adapters;

import org.json.JSONObject;
import java.io.File;
import java.util.concurrent.Executors;
import com.vtstudio.fxbox.network.FxDesktopUploader;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.saadahmedsoft.popupdialog.CreateDialog;
import com.saadahmedsoft.popupdialog.PopupDialog;
import com.saadahmedsoft.popupdialog.Styles;
import com.saadahmedsoft.popupdialog.listener.OnDialogButtonClickListener;
import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.activity.FxBaseActivity;
import com.vtstudio.fxbox.activity.ShortsUserProfile;
import com.vtstudio.fxbox.activity.UserProfile;
import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.database.dao.PlaylistDao;
import com.vtstudio.fxbox.databinding.ShortsItemLayoutBinding;
import com.vtstudio.fxbox.fxviews.dialog.AddVideoPlaylistDialog;
import com.vtstudio.fxbox.fxviews.dialog.ChangeMediaSegmentDialog;
import com.vtstudio.fxbox.fxviews.dialog.DialogHelper;
import com.vtstudio.fxbox.fxviews.dialog.DialogUtils;
import com.vtstudio.fxbox.fxviews.dialog.FxCommentDialog;
import com.vtstudio.fxbox.fxviews.dialog.FxMediaPropertiesDialog;
import com.vtstudio.fxbox.fxviews.dialog.FxShortsImageExportDialog;
import com.vtstudio.fxbox.fxviews.dialog.FxVideoSelectionDialog;
import com.vtstudio.fxbox.fxviews.textview.FxExpandableTextView;
import com.vtstudio.fxbox.listeners.OnSelectionItemListener;
import com.vtstudio.fxbox.media.MediaManger;
import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.models.Media;
import com.vtstudio.fxbox.media.models.tiktok.Playlist;
import com.vtstudio.fxbox.media.models.tiktok.ShortsMusic;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.media.models.youtube.YTUser;
import com.vtstudio.fxbox.media.models.youtube.YTVideo;
import com.vtstudio.fxbox.media.player.FxPlayer;
import com.vtstudio.fxbox.media.player.MediaRequest;
import com.vtstudio.fxbox.media.player.PlaybackBehavior;
import com.vtstudio.fxbox.ui.MyRequestOptions;
import com.vtstudio.fxbox.utils.FormatUtils;
import com.vtstudio.fxbox.utils.ListUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import xyz.hasnat.sweettoast.SweetToast;

public class ShortsAdapterUtils {

    public static void setDefaultOrUserAvatar(Object user, ImageView view) {
        if (view == null) return;

        Context context = view.getContext();

        if (user instanceof ShortsUser) {
            Glide.with(context).load(((ShortsUser) user).getAvatarPath())
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .override(view.getWidth(), view.getHeight())
                    .placeholder(R.drawable.default_avatar_user)
                    .into(view);
        } else if (user instanceof YTUser){
            Glide.with(context).load(((YTUser) user).getAvatarPath())
                    .apply(MyRequestOptions.getOptions())
                    .override(view.getWidth(), view.getHeight())
                    .placeholder(R.drawable.default_avatar_user)
                    .into(view);
        }else {
            Glide.with(context).load(R.drawable.default_avatar_user)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .override(view.getWidth(), view.getHeight())
                    .into(view);
        }
    }

    public static void setDefaultOrMusicAvatar(ShortsMusic music, String pathDefault, ImageView view) {
        if (view == null) return;

        Context context = view.getContext();

        String musicPath;
        if (music != null) {
            if (music.getId() != null && new File(music.getFxThumbnailPath()).exists())
                musicPath = music.getFxThumbnailPath();
            else musicPath = pathDefault;
        } else musicPath = pathDefault;

        Glide.with(context).load(musicPath)
                .apply(MyRequestOptions.getOptions())
                .placeholder(R.drawable.default_avatar_user)
                .override(view.getWidth(), view.getHeight())
                .into(view);
    }

    public static void bindInteractiveViews(ShortsItemLayoutBinding binding) {
        Context context = binding.getRoot().getContext();
        ImageView comment = binding.shortsContents.shortsCommentButton;
        Glide.with(context)
                .load(R.drawable.comment)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .override(comment.getWidth(), comment.getHeight())
                .into(comment);

        ImageView send = binding.shortsContents.shortsShareButton;

        Glide.with(context)
                .load(R.drawable.shorts_share)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .override(send.getWidth(), send.getHeight())
                .into(send);
    }

    public static void setLikeCountIfShortsVideo(FxMediaVideo shorts, TextView textView, boolean isFavorite) {
        if (shorts instanceof ShortsVideo) {
            ShortsVideo shortsVideo = (ShortsVideo) shorts;
            shortsVideo.setLikeCount(shortsVideo.getLikeCount() + ((isFavorite) ? 1 : -1));
            textView.setText(FormatUtils.formatCount(shortsVideo.getLikeCount()));
        }
    }

    public static void setListenerOfShareButton(@NonNull View view, FxMediaVideo media) {
        view.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("video/mp4");
            File file = new File(media.getMediaStorePath());
            Uri fileUri = FileProvider.getUriForFile(v.getContext()
                    , v.getContext().getApplicationContext().getPackageName() + ".fileprovider"
                    , file);
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            view.getContext().startActivity(Intent.createChooser(intent, "Share"));
        });
    }

    public static void setCommentButtonIfNeed(@NonNull View view, FxMediaVideo shorts, Context context) {
        if (shorts instanceof ShortsVideo && context instanceof FragmentActivity) {
            view.setOnClickListener(v -> {
                FxCommentDialog commentDialog = new FxCommentDialog(context, (ShortsVideo) shorts);
                commentDialog.show();
            });
        }
    }

    public static void setUserProfileClickIfNeed(@NonNull View view, FxMediaVideo shorts, Context context) {

        if (shorts instanceof ShortsVideo) {
            view.setOnClickListener(v -> {
                Intent intent = new Intent(context, UserProfile.class);
                intent.putExtra(ShortsUserProfile.INTENT_DATA_SHORTS_USER_ID, ((ShortsVideo) shorts).getAuthorId());
                context.startActivity(intent);
                ((FragmentActivity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }
    }

    public static void loadLeftMusicNameIcon(@NonNull ImageView view, FxMediaVideo shorts) {
        long differenceDay = (System.currentTimeMillis() - shorts.getMediaStoreDayAdded()) / (24 * 3600 * 1000);
        if (differenceDay < 3) {
            Glide.with(view).load(R.drawable.new_sym).override(view.getWidth(), view.getHeight())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .dontAnimate()
                    .into(view);
            view.setScaleX(1.3f);
            view.setScaleY(1.3f);
        } else {
            int drwId = R.drawable.device;

            if(shorts instanceof ShortsVideo) {
                drwId = R.drawable.music_note;
            }

            if(shorts instanceof YTVideo) {
                drwId = R.drawable.youtube;
            }

            Glide.with(view).load(drwId).override(view.getWidth(), view.getHeight())
                    .apply(MyRequestOptions.getOptions())
                    .dontAnimate()
                    .into(view);
            view.setScaleX(1);
            view.setScaleY(1);
        }

    }


    @SuppressLint("ClickableViewAccessibility")
    public static void disableListenerOf(ShortsItemLayoutBinding binding) {

        if (binding == null) return;

        binding.shortsPlayerView.setPlayer(null);
        binding.shortsVideoContainer.setOnClickListener(null);
        binding.shortsContents.shortsFavoriteClick.setOnClickListener(null);
        binding.shortsContents.shortsCommentContainer.setOnClickListener(null);
        binding.shortsContents.shortsAuthorAvatar.setOnClickListener(null);
        binding.shortsContents.shortsShareButton.setOnClickListener(null);
        binding.shortsVideoContainer.setOnTouchListener(null);

    }

    public static void bindSelectionListener(@NonNull FxVideoSelectionDialog dialog, @NonNull ShortsListAdapter.ShortsItem holder, @NonNull ShortsListAdapter adapter, @Nullable Playlist playlist) {
        dialog.setOnSelectionClickListener((OnSelectionItemListener) action -> {
            Context context = dialog.getContext();
            List<FxMediaVideo> mediaVideos = adapter.getShortsDataList();
            FxMediaVideo media = null;
            final int index = holder.getBindingAdapterPosition();
            if (mediaVideos != null && index >= 0 && index < mediaVideos.size()) {
                media = mediaVideos.get(index);
            }

            if (media == null) return true;

            switch (action) {
                case FxVideoSelectionDialog.ACTION_MUTE:
                    handleMuteAction(adapter);
                    break;
                case FxVideoSelectionDialog.ACTION_ADJUST_VOLUME:
                    handleAdjustVolumeAction(context, adapter, media);
                    break;
                case FxVideoSelectionDialog.ACTION_DISABLE_MUTE:
                    handleDisableMuteAction(adapter);
                    break;
                case FxVideoSelectionDialog.ACTION_LIMIT:
                    handleLimitAction(context, adapter, holder, media, playlist);
                    break;
                case FxVideoSelectionDialog.ACTION_REMOVE_LIMIT:
                    handleRemoveLimitAction(context, adapter, holder, media, playlist);
                    break;
                case FxVideoSelectionDialog.ACTION_MODE_AUTO_SWIPE:
                case FxVideoSelectionDialog.ACTION_DISABLE_MODE_AUTO_SWIPE:
                    handlePlaybackBehaviorAction(adapter, action);
                    break;
                case FxVideoSelectionDialog.ACTION_ENTER_WINDOW_VIDEO_MODE:
                    handleEnterWindowVideoModeAction(dialog, holder);
                    break;
                case FxVideoSelectionDialog.ACTION_SYNC_DATA:
                    handleSyncDataAction(context, media, adapter, holder);
                    break;
                case FxVideoSelectionDialog.ACTION_ADD_TO_PLAYLIST:
                    handleAddToPlaylistAction(context, media);
                    break;
                case FxVideoSelectionDialog.ACTION_CHANGE_VIDEO_SEGMENT:
                    handleChangeMediaSegmentAction(context, media);
                    break;
                case FxVideoSelectionDialog.ACTION_DELETE_MEDIA:
                    FxMediaVideo finalMedia = media;
                    dialog.setOnDismissListener(dialog1 -> handleDeleteMediaAction(context, finalMedia, adapter, holder, playlist));
                    break;
                case FxVideoSelectionDialog.ACTION_SEE_PROPERTIES:
                    FxMediaVideo finalMedia1 = media;
                    dialog.setOnDismissListener(dialog1 -> handleSeePropertiesAction(context, finalMedia1));
                    break;
                case FxVideoSelectionDialog.ACTION_EXPORT:
                    handleExportAction(context, media);
                    break;
                case FxVideoSelectionDialog.ACTION_PUSH_TO_DESKTOP:
                    handlePushToDesktopAction(context, media);
                    break;
            }
            return true;
        });
    }


    private static void handleMuteAction(ShortsListAdapter adapter) {
        if (adapter.getFxPlayer().getPlayer().getVolume() > 0) {
            adapter.getFxPlayer().getPlayer().setVolume(0f);
        }
    }

    private static void handleAdjustVolumeAction(@NonNull Context context, ShortsListAdapter adapter, FxMediaVideo media) {
        FxPlayer player = adapter.getFxPlayer();
        if ( player != null) {
            DialogHelper.showAdjustVolumeDialog(context, player, media);
        }
    }

    private static void handleDisableMuteAction(ShortsListAdapter adapter) {
        if (adapter.getFxPlayer().getPlayer().getVolume() < 1) {
            adapter.getFxPlayer().getPlayer().setVolume(1f);
        }
    }

    private static void handleLimitAction(@NonNull Context context, ShortsListAdapter adapter, ShortsListAdapter.ShortsItem holder, FxMediaVideo media, Playlist playlist) {
        showLimitWarningDialog(context, adapter, holder, media, playlist);
    }

    private static void showLimitWarningDialog(@NonNull Context context, ShortsListAdapter adapter, ShortsListAdapter.ShortsItem holder, FxMediaVideo media, Playlist playlist) {
        buildTemplate(context, context.getString(R.string.action_limit), context.getString(R.string.warn_limit_action))
                .showDialog(new OnDialogButtonClickListener() {
                    @Override
                    public void onPositiveClicked(Dialog dialog) {
                        super.onPositiveClicked(dialog);
                        handleLimitActionConfirmation(context, adapter, holder, media, playlist);
                    }
                });
    }

    private static CreateDialog buildTemplate(@NonNull Context context, String heading, String content) {
        return DialogUtils.buildTemplate(context, heading, content);
    }

    private static void handleLimitActionConfirmation(Context context, ShortsListAdapter adapter, ShortsListAdapter.ShortsItem holder, FxMediaVideo media, @Nullable Playlist playlist) {
        //xóa khỏi recyclerview nếu danh sách hiện tại là danh sách hạn chế
        if (adapter.shouldRemoveWhenLimit) {
            adapter.getShortsDataList().remove(media);
            adapter.getFxPlayer().getCurrentMediaList().remove(media);
            int pos = holder.getBindingAdapterPosition();

            if (playlist != null) {
                List<String> idList = playlist.getVideoIdList();
                ListUtils.removeAllOccurrences(idList, String.valueOf(media.getFxId()));
            }

            adapter.notifyItemRemoved(pos);
            if (adapter.getShortsDataList().isEmpty()) {
                adapter.notifyOnEmpty();
            }
            adapter.onStop();
            adapter.getRecyclerView().post(() -> {
                ShortsListAdapter.ShortsItem shortsItem = ShortsListAdapter.getItemAt(adapter.getRecyclerView(), pos);
                if (shortsItem != null) {
                    Log.d("ShortsListAdapter", "position: " + pos);
                    Log.d("ShortsListAdapter", "shorts position: " + shortsItem.getBindingAdapterPosition());
                    adapter.onPlay(shortsItem, pos, true);
                    adapter.getFxPlayer().prepare(pos, true);
                    adapter.getFxPlayer().play();
                }
            });
        }

        media.setLimited(true);
        // cập nhật cơ sở dữ liệu
        FxRoomDB database = FxRoomDB.get(context);
        if (media instanceof ShortsVideo) {
            database.shortsVideoDao().update((ShortsVideo) media);
        }

        PlaylistDao playlistDao = database.playlistDao();
        Playlist limitList = playlistDao.getPlaylistById(Playlist.LIMITED_PLAYLIST);

        if (limitList != null) {
            List<String> idList = limitList.getVideoIdList();
            ListUtils.addUnique(idList, String.valueOf(media.getFxId()));
            playlistDao.update(limitList);
        }

        SweetToast.success(context,
                context.getString(R.string.limited_success), 2000);
    }

    private static void handleRemoveLimitAction(@NonNull Context context, ShortsListAdapter adapter, ShortsListAdapter.ShortsItem holder, FxMediaVideo media, Playlist playlist) {
        // xóa khỏi recycler view nếu danh sách hiện tại là danh sách hạn chế
        if (adapter.shouldRemoveWhenLimit) {
            adapter.getShortsDataList().remove(media);
            adapter.getFxPlayer().getCurrentMediaList().remove(media);
            int pos = holder.getBindingAdapterPosition();

            if (playlist != null) {
                List<String> idList = playlist.getVideoIdList();
                ListUtils.removeAllOccurrences(idList, String.valueOf(media.getFxId()));
            }

            adapter.notifyItemRemoved(pos);
            if (adapter.getShortsDataList().isEmpty()) {
                adapter.notifyOnEmpty();
            }

            adapter.onStop();
            adapter.getRecyclerView().post(() -> {
                ShortsListAdapter.ShortsItem shortsItem = ShortsListAdapter.getItemAt(adapter.getRecyclerView(), pos);
                if (shortsItem != null) {
                    Log.d("ShortsListAdapter", "position: " + pos);
                    Log.d("ShortsListAdapter", "shorts position: " + shortsItem.getBindingAdapterPosition());
                    adapter.onPlay(shortsItem, pos, true);
                    adapter.getFxPlayer().prepare(pos, true);
                    adapter.getFxPlayer().play();
                }
            });
        }

        media.setLimited(false);
        // cập nhật database
        FxRoomDB database = FxRoomDB.get(context);
        if (media instanceof ShortsVideo) {
            database.shortsVideoDao().update((ShortsVideo) media);
        }

        PlaylistDao playlistDao = database.playlistDao();
        Playlist limitList = playlistDao.getPlaylistById(Playlist.LIMITED_PLAYLIST);
        if (limitList != null) {
            List<String> idList = limitList.getVideoIdList();
            ListUtils.removeAllOccurrences(idList, String.valueOf(media.getFxId()));
            playlistDao.update(limitList);
        }

        SweetToast.success(context,
                context.getString(R.string.remove_limited_success),
                2000);
    }

    private static void handlePlaybackBehaviorAction(ShortsListAdapter adapter, int action) {
        MediaRequest request = adapter.getFxPlayer().getCurrentRequest();
        if (request != null) {
            if (action == FxVideoSelectionDialog.ACTION_MODE_AUTO_SWIPE) {
                adapter.setAutoSwipe(true);
                adapter.requestLoadMoreIfNeeded();
                request.setPlayBackBehavior(PlaybackBehavior.BEHAVIOR_NEXT);
            } else {
                adapter.setAutoSwipe(false);
                request.setPlayBackBehavior(PlaybackBehavior.BEHAVIOR_REPEAT);
            }
        }
    }

    private static void handleEnterWindowVideoModeAction(@NonNull Dialog dialog, ShortsListAdapter.ShortsItem holder) {
        Context context = holder.itemView.getContext();
        if (context instanceof FxBaseActivity) {
            dialog.cancel();
            FxBaseActivity baseActivity = (FxBaseActivity) context;
            handleEnterVideoWindowMode(baseActivity);
        }
    }

    private static void handleEnterVideoWindowMode(FxBaseActivity baseActivity) {
        boolean canEnter = baseActivity.getFxPlayer() != null && baseActivity.getFxPlayer().setVideoWindowModeEnabled(true, baseActivity.getClass());
        if (canEnter) {
            baseActivity.moveTaskToBack(true);
        } else {
            Toast.makeText(baseActivity, R.string.cannot_enter_video_window_mode, Toast.LENGTH_LONG).show();
            baseActivity.requestOverlayPermission();
        }
    }

    private static void handleSyncDataAction(Context context, FxMediaVideo media, ShortsListAdapter adapter, ShortsListAdapter.ShortsItem holder) {
        if (media instanceof ShortsVideo) {
            MediaManger.syncShortsTiktokData(context, (ShortsVideo) media, adapter, holder);
        }
    }

    private static void handleAddToPlaylistAction(Context context, FxMediaVideo media) {
        showAddToPlaylistDialog(context, media);
    }

    private static void handleChangeMediaSegmentAction(Context context, FxMediaVideo media) {
        showChangeMediaSegmentDialog(context, media);
    }

    private static void showAddToPlaylistDialog(Context context, FxMediaVideo media) {
        AddVideoPlaylistDialog dialog = new AddVideoPlaylistDialog(context, media);
        dialog.show();
    }

    private static void showChangeMediaSegmentDialog(Context context, FxMediaVideo media) {
        ChangeMediaSegmentDialog dialog = new ChangeMediaSegmentDialog(context, media);
        dialog.show();
    }

    private static void handleDeleteMediaAction(Context context, FxMediaVideo media, ShortsListAdapter adapter, ShortsListAdapter.ShortsItem holder, Playlist playlist) {
        showDeleteConfirmationDialog(context, media, adapter, holder, playlist);
    }

    private static void showDeleteConfirmationDialog(Context context, FxMediaVideo media, ShortsListAdapter adapter, ShortsListAdapter.ShortsItem holder, Playlist playlist) {
        buildTemplate(context, context.getString(R.string.action_delete), context.getString(R.string.warn_delete_action))
                .showDialog(new OnDialogButtonClickListener() {
                    @Override
                    public void onPositiveClicked(Dialog dialog) {
                        super.onPositiveClicked(dialog);
                        handleDeleteMediaConfirmation(context, media, adapter, holder, playlist);
                    }
                });
    }

    private static void handleDeleteMediaConfirmation(Context context, FxMediaVideo media, ShortsListAdapter adapter, ShortsListAdapter.ShortsItem holder, Playlist playlist) {
        Dialog loadDialog = new Dialog(context);
        loadDialog.setContentView(R.layout.dialog_loading_layout);
        loadDialog.setCancelable(false);
        loadDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        loadDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadDialog.show();
        MediaManger.deleteMedia(media, context, deleted -> {
            loadDialog.dismiss();
            if (deleted) {
                // Xóa khỏi RecyclerView và cập nhật dữ liệu
                handleItemRemoved(adapter, media, holder, playlist);
            } else {
                SweetToast.error(context, context.getString(R.string.delete_failed));
            }
        });
    }

    private static void handleItemRemoved(ShortsListAdapter adapter, FxMediaVideo media, ShortsListAdapter.ShortsItem holder, Playlist playlist) {
        adapter.getShortsDataList().remove(media);
        adapter.getFxPlayer().getCurrentMediaList().remove(media);
        int pos = holder.getBindingAdapterPosition();
        adapter.notifyItemRemoved(pos);
        if (adapter.getShortsDataList().isEmpty()) {
            adapter.notifyOnEmpty();
        }

        if (playlist != null) {
            List<String> idList = playlist.getVideoIdList();
            ListUtils.removeAllOccurrences(idList, String.valueOf(media.getFxId()));
            FxRoomDB.get(holder.itemView.getContext()).playlistDao().update(playlist);
        }

        adapter.onStop();
        adapter.getRecyclerView().post(() -> {
            ShortsListAdapter.ShortsItem shortsItem = ShortsListAdapter.getItemAt(adapter.getRecyclerView(), pos);
            if (shortsItem != null) {
                adapter.onPlay(shortsItem, pos, true);
                adapter.getFxPlayer().prepare(pos, true);
                adapter.getFxPlayer().play();
            }
        });
    }

    private static void handleExportAction(Context context, Media media) {
        if (media instanceof ShortsVideo && ((ShortsVideo) media).isImageList()) {
            new FxShortsImageExportDialog(context, (ShortsVideo) media).show();
        } else {
            MediaManger.exportMediaToPublicFolder(context, media);
        }
    }

    private static void handlePushToDesktopAction(Context context, Media media) {
        if (media instanceof ShortsVideo) {
            ShortsVideo shorts = (ShortsVideo) media;
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    FxRoomDB db = FxRoomDB.get(context);
                    ShortsUser user = db.shortsUserDao().getUserById(shorts.getAuthorId());
                    ShortsMusic music = db.shortsMusicDao().getById(shorts.getMusicId());

                    File videoFile = new File(shorts.getMediaStorePath());
                    File thumbFile = shorts.getFxThumbnailPath() != null ? new File(shorts.getFxThumbnailPath()) : null;
                    File avatarFile = user != null && user.getAvatarPath() != null ? new File(user.getAvatarPath()) : null;
                    File musicThumbFile = music != null && music.getFxThumbnailPath() != null ? new File(music.getFxThumbnailPath()) : null;

                    JSONObject meta = new JSONObject();
                    JSONObject vObj = new JSONObject();
                    vObj.put("fxId", shorts.getFxId());
                    vObj.put("awemeId", shorts.getAwemeId());
                    vObj.put("description", shorts.getDescription());
                    vObj.put("authorId", shorts.getAuthorId());
                    vObj.put("musicId", shorts.getMusicId());
                    vObj.put("likeCount", shorts.getLikeCount());
                    vObj.put("commentCount", shorts.getCommentCount());
                    vObj.put("shareCount", shorts.getShareCount());
                    vObj.put("playCount", shorts.getPlayCount());
                    vObj.put("shortsCreateTime", shorts.getShortsCreateTime());
                    vObj.put("duration", shorts.getDuration());
                    vObj.put("width", shorts.getWidth());
                    vObj.put("height", shorts.getHeight());
                    vObj.put("shareUrl", shorts.getShareUrl());
                    vObj.put("volume", shorts.getVolume());
                    vObj.put("mediaSegmentId", shorts.getMediaSegmentId());
                    vObj.put("isFavorite", shorts.isFavorite());
                    vObj.put("isImageList", shorts.isImageList());
                    if (shorts.getImageListPath() != null) {
                        vObj.put("imageListPath", new org.json.JSONArray(shorts.getImageListPath()).toString());
                    }
                    meta.put("video", vObj);

                    if (user != null) {
                        JSONObject uObj = new JSONObject();
                        uObj.put("fxId", user.getFxId());
                        uObj.put("uid", user.getUid());
                        uObj.put("uniqueId", user.getUniqueId());
                        uObj.put("nickName", user.getNickName());
                        uObj.put("signature", user.getSignature());
                        uObj.put("followerCount", user.getFollowerCount());
                        uObj.put("followingCount", user.getFollowingCount());
                        uObj.put("verified", user.getVerified());
                        meta.put("user", uObj);
                    }

                    if (music != null) {
                        JSONObject mObj = new JSONObject();
                        mObj.put("fxId", music.getFxId());
                        mObj.put("id", music.getId());
                        mObj.put("title", music.getTitle());
                        mObj.put("author", music.getAuthor());
                        mObj.put("duration", music.getDuration());
                        meta.put("music", mObj);
                    }

                    if (shorts.isImageList()) {
                        java.util.List<File> imageFiles = new java.util.ArrayList<>();
                        if (shorts.getImageListPath() != null) {
                            for (String imgPath : shorts.getImageListPath()) {
                                if (imgPath != null && !imgPath.trim().isEmpty()) {
                                    imageFiles.add(new File(imgPath));
                                }
                            }
                        }
                        File audioFile = shorts.getMediaStorePath() != null ? new File(shorts.getMediaStorePath()) : null;
                        FxDesktopUploader.pushImageListToDesktop(
                                context,
                                imageFiles,
                                audioFile,
                                meta.toString(),
                                thumbFile,
                                avatarFile,
                                musicThumbFile,
                                new FxDesktopUploader.UploadCallback() {
                                    @Override
                                    public void onSuccess(String response) {
                                        Log.d("FxDesktop", "Push ImageList success: " + response);
                                    }

                                    @Override
                                    public void onError(String error) {
                                        Log.e("FxDesktop", "Push ImageList error: " + error);
                                    }
                                }
                        );
                    } else {
                        FxDesktopUploader.pushToDesktop(
                                context,
                                videoFile,
                                meta.toString(),
                                thumbFile,
                                avatarFile,
                                musicThumbFile,
                                new FxDesktopUploader.UploadCallback() {
                                    @Override
                                    public void onSuccess(String response) {
                                        Log.d("FxDesktop", "Push success: " + response);
                                    }

                                    @Override
                                    public void onError(String error) {
                                        Log.e("FxDesktop", "Push error: " + error);
                                    }
                                }
                        );
                    }
                } catch (Exception e) {
                    Log.e("FxDesktop", "Error pushing to desktop", e);
                }
            });
        } else if (media instanceof com.vtstudio.fxbox.media.models.youtube.YTVideo) {
            com.vtstudio.fxbox.media.models.youtube.YTVideo yt = (com.vtstudio.fxbox.media.models.youtube.YTVideo) media;
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    FxRoomDB db = FxRoomDB.get(context);
                    YTUser ytUser = db.ytUserDao().getUserByChannelId(yt.getChannelId());
                    File videoFile = new File(yt.getMediaStorePath());
                    File thumbFile = yt.getFxThumbnailPath() != null ? new File(yt.getFxThumbnailPath()) : null;
                    File avatarFile = ytUser != null && ytUser.getAvatarPath() != null ? new File(ytUser.getAvatarPath()) : null;

                    JSONObject meta = new JSONObject();
                    JSONObject vObj = new JSONObject();
                    vObj.put("fxId", yt.getFxId());
                    vObj.put("awemeId", yt.getId());
                    vObj.put("description", yt.getTitle() != null ? yt.getTitle() : yt.getDescription());
                    vObj.put("authorId", yt.getChannelId());
                    vObj.put("musicId", "music_" + yt.getId());
                    vObj.put("likeCount", yt.getLikeCount());
                    vObj.put("commentCount", yt.getCommentCount());
                    vObj.put("shareCount", 0);
                    vObj.put("playCount", yt.getPlayCount());
                    vObj.put("duration", yt.getDuration());
                    vObj.put("width", yt.getWidth());
                    vObj.put("height", yt.getHeight());
                    vObj.put("shareUrl", yt.getShareUrl());
                    vObj.put("isFavorite", yt.isFavorite());
                    meta.put("video", vObj);

                    if (ytUser != null) {
                        JSONObject uObj = new JSONObject();
                        uObj.put("fxId", ytUser.getFxId());
                        uObj.put("uid", ytUser.getChannelId());
                        uObj.put("uniqueId", ytUser.getUniqueId());
                        uObj.put("nickName", ytUser.getChannelName());
                        uObj.put("verified", ytUser.isVerified());
                        meta.put("user", uObj);
                    }

                    FxDesktopUploader.pushToDesktop(
                            context,
                            videoFile,
                            meta.toString(),
                            thumbFile,
                            avatarFile,
                            null,
                            null
                    );
                } catch (Exception e) {
                    Log.e("FxDesktop", "Error pushing YT to desktop", e);
                }
            });
        }
    }

    private static void handleSeePropertiesAction(Context context, Media media) {
        showMediaPropertiesDialog(context, media);
    }

    private static void showMediaPropertiesDialog(Context context, Media media) {
        FxMediaPropertiesDialog dialog = new FxMediaPropertiesDialog(context, media);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    public static ArrayList<Integer> buildConfigSelections(@NonNull ShortsListAdapter adapter, @NonNull Media fxMedia) {

        ArrayList<Integer> selectionsDefault = FxVideoSelectionDialog.getDefaultSelections();
        FxPlayer fxPlayer = adapter.getFxPlayer();

        if (fxPlayer.getPlayer().getVolume() == 0 && selectionsDefault.contains(FxVideoSelectionDialog.ACTION_MUTE)) {
            selectionsDefault.set(selectionsDefault.indexOf(FxVideoSelectionDialog.ACTION_MUTE), FxVideoSelectionDialog.ACTION_DISABLE_MUTE);
        }

        MediaRequest request = fxPlayer.getCurrentRequest();
        if (request != null
                && request.getPlayBackBehavior() == PlaybackBehavior.BEHAVIOR_NEXT
                && selectionsDefault.contains(FxVideoSelectionDialog.ACTION_MODE_AUTO_SWIPE)) {
            selectionsDefault.set(selectionsDefault.indexOf(FxVideoSelectionDialog.ACTION_MODE_AUTO_SWIPE), FxVideoSelectionDialog.ACTION_DISABLE_MODE_AUTO_SWIPE);
        }

        if (fxMedia.isLimited()) {
            selectionsDefault.set(selectionsDefault.indexOf(FxVideoSelectionDialog.ACTION_LIMIT), FxVideoSelectionDialog.ACTION_REMOVE_LIMIT);
        }

        return selectionsDefault;
    }

    public static void updateInformation(@NonNull ShortsListAdapter.ShortsItem item, @NonNull ShortsVideo shorts) {
        ShortsItemLayoutBinding binding = item.binding;
        //binding.shortsContents.shortsDescriptionTv.notifyOnViewRecycled();
        binding.shortsContents.shortsDescriptionTv.collapse();
        item.checkAndInflateExtraViews(shorts);
        //count strings
        String likeCount = FormatUtils.formatCount(shorts.getLikeCount());
        String commentCount = FormatUtils.formatCount(shorts.getCommentCount());
        String shareCount = FormatUtils.formatCount(shorts.getShareCount());

        binding.shortsContents.shortsLikeCountTv.setText(likeCount);
        binding.shortsContents.shortsCommentCountTv.setText(commentCount);
        binding.shortsContents.shortsShareCountTv.setText(shareCount);
    }
}
