package com.vtstudio.fxbox.media;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.adapters.ShortsAdapterUtils;
import com.vtstudio.fxbox.adapters.ShortsListAdapter;
import com.vtstudio.fxbox.api.TiktokDataSourceBuilder;
import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.database.dao.CommentDao;
import com.vtstudio.fxbox.database.dao.FxMediaVideoDao;
import com.vtstudio.fxbox.database.dao.PlaylistDao;
import com.vtstudio.fxbox.database.dao.RawResponseDao;
import com.vtstudio.fxbox.database.dao.ShortsMusicDao;
import com.vtstudio.fxbox.database.dao.ShortsUserDao;
import com.vtstudio.fxbox.database.dao.ShortsVideoDao;
import com.vtstudio.fxbox.database.dao.YTUserDao;
import com.vtstudio.fxbox.database.dao.YTVideoDao;
import com.vtstudio.fxbox.helpers.PreferenceHelper;
import com.vtstudio.fxbox.listeners.OnDeletionResultListener;
import com.vtstudio.fxbox.listeners.OnProgressUpdateListener;
import com.vtstudio.fxbox.media.models.tiktok.Comment;
import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.models.Media;
import com.vtstudio.fxbox.media.models.tiktok.Playlist;
import com.vtstudio.fxbox.media.models.RawResponse;
import com.vtstudio.fxbox.media.models.tiktok.ShortsMusic;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.media.models.youtube.YTUser;
import com.vtstudio.fxbox.media.models.youtube.YTVideo;
import com.vtstudio.fxbox.media.utils.ModelsParser;
import com.vtstudio.fxbox.models.TiktokPackage;
import com.vtstudio.fxbox.server.ApiCaller;
import com.vtstudio.fxbox.server.tiktok.TikTokApi;
import com.vtstudio.fxbox.utils.FileUtils;
import com.vtstudio.fxbox.utils.ListUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import xyz.hasnat.sweettoast.SweetToast;

public class MediaManger {
    public static void syncShortsTiktokData(@NonNull Context context, @NonNull ShortsVideo mShortsItem, @Nullable ShortsListAdapter adapter, @NonNull ShortsVideoDao shortsVideoDao, @Nullable ShortsListAdapter.ShortsItem holder) {

        if (mShortsItem.getAwemeId() == null) {
            // bắt đầu lệnh gọi lấy dữ liệu từ servers
            return;
        }

        final int finalIndex = holder != null ? holder.getBindingAdapterPosition() : -1;

        if (holder != null) {
            holder.setViewsSyncVisibility(true);
            holder.setShowingViewsSync(true);
        }

        if (adapter != null) {
            adapter.addHolderSyncIndex(finalIndex);
        }

        ApiCaller.with(context).asTikTokApi().asShorts()
                .server(TikTokApi.API_TIKTOK_MOBILE_VERSION, TikTokApi.API_TIKTOK_PRIVATE)
                .url(mShortsItem.getAwemeId())
                .callback(response -> {

                    // xử lý khi thành công
                    if (response.isSuccessfully()) {
                        RawResponseDao rawResponseDao = FxRoomDB.get(context).rawResponseDao();
                        TiktokPackage model = response.getModel();

                        TiktokDataSourceBuilder dataSource = new TiktokDataSourceBuilder(context);
                        dataSource.doOnFailed(() -> {
                            SweetToast.warning(context, context.getString(R.string.failed_get_music_thumb));
                        }).createMusicThumbnail(model, 30);

                        mShortsItem.setDescriptionTitle(model.getDescriptionTitle());
                        ModelsParser.setServerFromResponse(mShortsItem, response.getServer());
                        ModelsParser.parseInformation(mShortsItem, model);
                        mShortsItem.setRawResponse(model.getRawResponse());
                        rawResponseDao.insert(RawResponse.fromShortsVideo(mShortsItem));
                        mShortsItem.setRawResponse(null);
                        shortsVideoDao.update(mShortsItem);
                        SweetToast.success(context, context.getString(R.string.get_data_success));

                        if (adapter != null && holder != null) {

                            if (adapter.getCurrentPlayingIndex() == finalIndex) {
                                ShortsAdapterUtils.updateInformation(holder, mShortsItem);
                            }

                            adapter.removeHolderSyncIndex(finalIndex);

                            holder.setShowingViewsSync(false);
                            holder.setViewsSyncVisibility(false);

                        }
                    } else {

                        if (holder != null) {
                            holder.setViewsSyncVisibility(false);
                            holder.setShowingViewsSync(false);
                        }
                        // Nếu xảy ra sự cố đường truyền
                        Log.d("MediaManager", "failed downloaded for " + mShortsItem.getDescription());
                        SweetToast.error(context, context.getString(R.string.get_data_api_failed));
                    }
                }).get();
    }

    public static void syncShortsTiktokData(@NonNull Context context, @NonNull ShortsVideo mShortsItem, @NonNull ShortsListAdapter adapter, @NonNull ShortsListAdapter.ShortsItem holder) {
        syncShortsTiktokData(context, mShortsItem, adapter, FxRoomDB.get(context).shortsVideoDao(), holder);
    }

    public static void deleteMedia(@NonNull Media media, @NonNull Context context, @Nullable OnDeletionResultListener listener) {
        String mediaPath = media.getMediaStorePath();
        if (mediaPath != null) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                boolean deleted;
                if (media instanceof ShortsVideo) {
                    deleted = deleteShortsVideo((ShortsVideo) media, context);
                } else if (media instanceof YTVideo) {
                    deleted = deleteYTVideo((YTVideo) media, context);
                } else if (media instanceof FxMediaVideo) {
                    deleted = deleteFxMediaVideo((FxMediaVideo) media, context);
                } else {
                    deleted = new File(media.getMediaStorePath()).delete();
                }
                if (listener != null) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> listener.onDeletion(deleted));
                }
            });
            executor.shutdown();
        }
    }

    private static boolean deleteYTVideo(@NonNull YTVideo ytVideo, @NonNull Context context) {
        File file = new File(ytVideo.getMediaStorePath());
        boolean success = file.delete() || !file.exists();

        if (success) {
            FxRoomDB database = FxRoomDB.get(context);
            YTVideoDao ytVideoDao = database.ytvideoDao();

            LogWriter writer = new LogWriter(new File(PreferenceHelper.getLogFilePath(context), "log.txt"));
            writer.writeToBuffer("@Start");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy ss:mm:hh", Locale.getDefault());
            writer.writeToBuffer("-> @DateTime: " + sdf.format(new Date(System.currentTimeMillis())));
            writer.writeToBuffer("-> @Header delete youtube video, id is: " + ytVideo.getId());
            sdf = null;
            writer.writeToBuffer("-> @YTVideo: " + file.getName());

            File fxThumbnail = new File(ytVideo.getFxThumbnailPath());
            fxThumbnail.delete();

            File thumbnail = new File(ytVideo.getYTVideoThumbnailPath());
            thumbnail.delete();

            if (ytVideoDao.countVideosByChannelId(ytVideo.getChannelId()) < 2) {
                deleteYTUser(ytVideo.getChannelId(), database.ytUserDao(), writer);
            }

            // Log đường dẫn của tệp tin largeIcon
            File largeIcon = new File(ytVideo.getFxNotificationLargeIconPath());
            largeIcon.delete();
            Log.d("MediaManager", "LargeIcon path: " + largeIcon.getPath());
            writer.writeToBuffer("@End");
            writer.writeToBuffer("-----------------------------");
            writer.flush();
            ytVideoDao.delete(ytVideo);
        }

        return success;
    }

    private static void deleteYTUser(@NonNull String channelId, @NonNull YTUserDao ytUserDao, LogWriter writer) {
        YTUser user = ytUserDao.getUserByChannelId(channelId);
        if (user != null) {
            if (user.getAvatarPath() != null) {
                File file = new File(user.getAvatarPath());
                file.delete();
                if (writer != null) {
                    writer.writeToBuffer("-> @YTUser: " + file.getName());
                }
                ytUserDao.delete(user);
            }
        }
    }

    private static boolean deleteFxMediaVideo(@NonNull FxMediaVideo fxMediaVideo, @NonNull Context context) {
        FxRoomDB database = FxRoomDB.get(context);
        FxMediaVideoDao fxMediaVideoDao = database.fxMediaVideoDao();
        File file = new File(fxMediaVideo.getMediaStorePath());
        boolean success = file.delete() || !file.exists();

        if (success) {
            File thumbnail = new File(fxMediaVideo.getFxThumbnailPath());
            thumbnail.delete();
            Log.d("MediaManager", "Thumbnail path: " + thumbnail.getPath());

            // Log đường dẫn của tệp tin largeIcon
            File largeIcon = new File(fxMediaVideo.getFxNotificationLargeIconPath());
            largeIcon.delete();
            Log.d("MediaManager", "LargeIcon path: " + largeIcon.getPath());
            fxMediaVideoDao.delete(fxMediaVideo);
        }

        return success;
    }

    private static boolean deleteShortsVideo(@NonNull ShortsVideo shortsVideo, @NonNull Context context) {

        FxRoomDB database = FxRoomDB.get(context);
        ShortsVideoDao shortsVideoDao = database.shortsVideoDao();
        File file = new File(shortsVideo.getMediaStorePath());

        boolean success = file.delete() || !file.exists();

        if (success) {
            LogWriter writer = new LogWriter(new File(PreferenceHelper.getLogFilePath(context), "log.txt"));
            writer.writeToBuffer("@Start");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy ss:mm:hh", Locale.getDefault());
            writer.writeToBuffer("-> @DateTime: " + sdf.format(new Date(System.currentTimeMillis())));
            writer.writeToBuffer("-> @Header delete shorts video, shorts (awemeid) is: " + shortsVideo.getAwemeId());
            sdf = null;
            writer.writeToBuffer("-> @ShortsVideo: " + file.getName());
            // Log đường dẫn của tệp tin thumbnail
            File thumbnail = new File(shortsVideo.getFxThumbnailPath());
            thumbnail.delete();

            // Log đường dẫn của tệp tin largeIcon
            File largeIcon = new File(shortsVideo.getFxNotificationLargeIconPath());
            largeIcon.delete();

            if (shortsVideo.isImageList() && shortsVideo.getImageListPath() != null) {
                for (String image : shortsVideo.getImageListPath()) {
                    File imageFile = new File(image);
                    imageFile.delete();
                    writer.writeToBuffer("-> @ShortsPhoto: " + imageFile.getName());
                }
            }

            CommentDao commentDao = database.commentDao();
            ShortsUserDao userDao = database.shortsUserDao();
            List<Comment> comments;
            do {
                comments = commentDao.getCommentsByVideoId(shortsVideo.getAwemeId(), 10, 0);
                if (comments != null && !comments.isEmpty()) {
                    for (Comment comment : comments) {
                        deleteCommentAndRepliesAndRelatedUsers(comment, shortsVideoDao, userDao, commentDao, writer);
                    }
                }
            } while (comments != null && !comments.isEmpty());


            if (shortsVideoDao.countVideosWithMusicId(shortsVideo.getMusicId()) < 2) {
                ShortsMusicDao musicDao = database.shortsMusicDao();
                ShortsMusic music = musicDao.getById(shortsVideo.getMusicId());
                if (music != null) {
                    File musicAvatar = new File(music.getFxThumbnailPath());
                    musicAvatar.delete();
                    writer.writeToBuffer("-> @ShortsMusic: " + musicAvatar.getName());
                }
            } else {
                writer.writeToBuffer("-> @Message: Not sure delete MusicAvatar path, because it is used by others, id = " + shortsVideo.getMusicId());
            }

            if (!hasRelatedDataByUser(shortsVideo.getAuthorId(), shortsVideoDao, commentDao)) {
                deleteShortsUser(shortsVideo.getAuthorId(), database.shortsUserDao(), writer);
            } else {
                writer.writeToBuffer("-> @Message: Not sure delete AuthorAvatar path of the media, because it has related, uid = " + shortsVideo.getAuthorId());
            }

            PlaylistDao playlistDao = database.playlistDao();
            String parentPath = new File(shortsVideo.getMediaStorePath()).getParent();
            Playlist mPlaylist = playlistDao.getPlaylistById(parentPath);

            if (mPlaylist != null) {
                ListUtils.removeAllOccurrences(mPlaylist.getVideoIdList(), String.valueOf(shortsVideo.getFxId()));
                playlistDao.update(mPlaylist);
            }

            writer.writeToBuffer("@End");
            writer.writeToBuffer("-----------------------------");
            writer.flush();
            shortsVideoDao.delete(shortsVideo);
        }
        return success;
    }

    public static void deleteShortsUser(@NonNull ShortsUser user, @NonNull ShortsUserDao shortsUserDao, LogWriter writer) {
        String avatarPath = user.getAvatarPath();
        if (avatarPath != null) {
            File file = new File(avatarPath);
            if (file.delete()) {
                if (writer != null) {
                    writer.writeToBuffer("-> @ShortsUser: " + file.getName());
                }
            }
        }
        shortsUserDao.delete(user);
    }

    public static void deleteShortsUser(String userId, @NonNull ShortsUserDao shortsUserDao, LogWriter writer) {
        if (userId != null) {
            ShortsUser user = shortsUserDao.getUserById(userId);
            if (user != null) {
                deleteShortsUser(user, shortsUserDao, writer);
            }
        }
    }

    // Hàm xử lý việc xóa các phản hồi và người dùng liên quan
    private static void deleteCommentAndRepliesAndRelatedUsers(Comment comment, ShortsVideoDao shortsVideoDao, ShortsUserDao userDao, CommentDao commentDao, LogWriter writer) {

        List<Comment> replies;
        do {
            replies = commentDao.getReplyById(comment.getId(), 10, 0);
            if (replies != null && !replies.isEmpty()) {
                for (Comment reply : replies) {
                    commentDao.delete(reply);
                    if (!hasRelatedDataByUser(reply.getUid(), shortsVideoDao, commentDao)) {
                        deleteShortsUser(reply.getUid(), userDao, writer);
                    } else {
                        Log.d("MediaManager", "Not sure to delete user of reply with content: \"" + reply.getContent() + "\" because it be used by other");
                    }
                }
            }
        } while (replies != null && !replies.isEmpty());

        commentDao.delete(comment);
        if (!hasRelatedDataByUser(comment.getUid(), shortsVideoDao, commentDao)) {
            deleteShortsUser(comment.getUid(), userDao, writer);
        } else {
            Log.d("MediaManager", "Not sure to delete user of comment with content: \"" + comment.getContent() + "\" because it be used by other");
        }
    }

    // Kiểm tra xem người dùng có dữ liệu liên quan không
    private static boolean hasRelatedDataByUser(String uid, ShortsVideoDao shortsVideoDao, CommentDao commentDao) {
        return shortsVideoDao.hasVideoByUserId(uid) || commentDao.hasCommentByUserId(uid);
    }

    // backup data
    public static synchronized boolean storeData(@NonNull Context context, @Nullable OnProgressUpdateListener updateListener) {
        boolean allSaved = true;
        int v;
        FxRoomDB database = FxRoomDB.get(context);
        ShortsVideoDao shortsVideoDao = database.shortsVideoDao();
        ShortsUserDao shortsUserDao = database.shortsUserDao();
        ShortsMusicDao shortsMusicDao = database.shortsMusicDao();

        long lastSavedTime = PreferenceHelper.getTimeSavedToStorage(context);

        int total = 0;
        int current = 0;

        List<ShortsVideo> videos = shortsVideoDao.getAllVideosSince(lastSavedTime);
        List<ShortsMusic> musics = shortsMusicDao.getAllSince(lastSavedTime);
        List<ShortsUser> users = shortsUserDao.getAllSince(lastSavedTime);

        if (videos != null) total += videos.size();
        if (musics != null) total += musics.size();
        if (users != null) total += users.size();

        Log.d("MediaManager", "Total: " + total + " data need to be saved");

        if (total == 0) {
            if (updateListener != null) {
                updateListener.onUpdate(0, 0);
            }
            return true;
        }

        String musicAvtSavePath = PreferenceHelper.getShortsAudioImagePath(context, 1);
        String photoAudioPath = PreferenceHelper.getShortsAudioPath(context, 1);
        String authorImagePath = PreferenceHelper.getShortsAuthorImagePath(context, 1);

        if (videos != null) {
            for (v = 0; v < videos.size(); ++v) {
                current++;
                if (updateListener != null) {
                    updateListener.onUpdate(current, total);
                }

                ShortsVideo video = videos.get(v);
                if (video.getTimeDownloaded() > PreferenceHelper.getTimeSavedToStorage(context)) {
                    String mediaPath = video.getMediaStorePath();
                    String destinationDir = null;
                    // sao lưu media type photo list
                    if (video.isImageList()) {
                        String imgListPath = PreferenceHelper.getShortsImagePath(context, 1);
                        destinationDir = photoAudioPath;

                        Log.d("MediaManager", "Copying: " + video.getTrackTitle());
                        List<String> imageList = video.getImageListPath();
                        if (imageList != null && !imageList.isEmpty()) {
                            for (int i = 0; i < imageList.size(); ++i) {
                                String imagePath = imageList.get(i);
                                File srcFile = new File(imagePath);
                                boolean saveSuccess = FileUtils.copyFileToExternal(new File(imagePath), new File(imgListPath, srcFile.getName()));
                                if (!saveSuccess) {
                                    Log.e("MediaManager", "cannot copy file shorts image: " + srcFile.getAbsolutePath() + "\t" + video.getDescription());
                                }
                            }
                        }
                    } else {
                        destinationDir = PreferenceHelper.getShortsVideoPath(context, 1);
                    }


                    // copy the media video or audio files
                    File mediaFile = new File(mediaPath);
                    File copyFile = new File(destinationDir, mediaFile.getName());

                    boolean copySuccess = FileUtils.copyFileToExternal(mediaFile, copyFile);

                    if (!copySuccess) {
                        Log.e("MediaManager", "cannot copy file shorts: " + mediaFile.getAbsolutePath() + "\t" + video.getDescription());
                        allSaved = false;
                        break;
                    }
                }
            }
        }

        if (users != null && !users.isEmpty()) {
            for (int i = 0; i < users.size(); i++) {

                current++;
                if (updateListener != null) {
                    updateListener.onUpdate(current, total);
                }

                ShortsUser user = users.get(i);
                if (user != null) {
                    boolean isSuccess = copyAuthorAvatar(user, shortsUserDao, authorImagePath);
                    if (!isSuccess) {
                        Log.e("MediaManager", "cannot copy file author Avt in user !" + user.getNickName());
                    }
                }
            }
        }

        if (musics != null && !musics.isEmpty()) {
            for (int i = 0; i < musics.size(); i++) {
                current++;
                if (updateListener != null) {
                    updateListener.onUpdate(current, total);
                }
                // copy the video music information
                ShortsMusic music = musics.get(i);
                if (music != null) {
                    File musicAvtFile = new File(music.getFxThumbnailPath());
                    File desAvtMusicFile = new File(musicAvtSavePath, musicAvtFile.getName());

                    boolean isSuccess = FileUtils.copyFileToExternal(musicAvtFile, desAvtMusicFile);
                    if (!isSuccess) {
                        Log.e("MediaManager", "cannot copy file music Avt: " + musicAvtFile.getAbsolutePath() + "\t" + music.getTitle());
                        break;
                    }
                }
            }
        }


        if (allSaved) {
            Log.d("MediaManager", "Success to save all data");
            PreferenceHelper.putTimeSavedToStorage(context, System.currentTimeMillis());
        } else if (updateListener != null) {
            updateListener.onUpdate(0, 0);
        }

        return allSaved;
    }

    public static boolean copyAuthorAvatar(@NonNull String userId, @NonNull ShortsUserDao userDao, @NonNull String desDirectoryPath) {
        ShortsUser user = userDao.getUserById(userId);
        boolean isSuccess = false;
        if (user != null) {
            isSuccess = copyAuthorAvatar(user, userDao, desDirectoryPath);
        }
        return isSuccess;
    }

    public static boolean copyAuthorAvatar(@NonNull ShortsUser user, @NonNull ShortsUserDao userDao, @NonNull String desDirectoryPath) {
        File userAvtFile = new File(user.getAvatarPath());
        File desUserAvtFile = new File(desDirectoryPath, userAvtFile.getName());
        return FileUtils.copyFileToExternal(userAvtFile, desUserAvtFile);
    }

    public static boolean copyReply(@NonNull String commentId, @NonNull CommentDao commentDao, @NonNull ShortsUserDao shortsUserDao, @NonNull String desDirectory) {
        boolean isSuccess = true;
        List<Comment> comments = commentDao.getReplyById(commentId, Integer.MAX_VALUE, 0);

        for (int j = 0; j < comments.size(); ++j) {
            Comment comment = comments.get(j);
            if (comment != null) {
                String cmUserId = comment.getUid();
                if (cmUserId != null) {
                    if (!copyAuthorAvatar(cmUserId, shortsUserDao, desDirectory)) {
                        isSuccess = false;
                        break;
                    }
                }
            }
        }

        return isSuccess;
    }
}
