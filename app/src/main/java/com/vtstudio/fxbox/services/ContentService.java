package com.vtstudio.fxbox.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.YoutubeCallback;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.database.dao.FxMediaVideoDao;
import com.vtstudio.fxbox.database.dao.ShortsVideoDao;
import com.vtstudio.fxbox.helpers.PreferenceHelper;
import com.vtstudio.fxbox.listeners.OnFailedListener;
import com.vtstudio.fxbox.listeners.OnSuccessListener;
import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.models.tiktok.ShortsMusic;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.media.models.youtube.YTVideo;
import com.vtstudio.fxbox.media.models.youtube.YTVideoInfo;
import com.vtstudio.fxbox.media.models.youtube.YoutubeDataSourceBuilder;
import com.vtstudio.fxbox.media.sources.DataSourceBuilder;
import com.vtstudio.fxbox.server.ApiCaller;
import com.vtstudio.fxbox.server.OnResponseListener;
import com.vtstudio.fxbox.server.Response;
import com.vtstudio.fxbox.utils.ContentUtils;
import com.vtstudio.fxbox.utils.ZipUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class ContentService extends Service {

    /////// key /////////////
    public static final String ACTION_KEY = "content_action_key";
    ////////////// Actions /////////////
    public static final String ACTION_GET_VIDEO = "get_video";
    public static final String ACTION_GET_IMAGE = "get_image";

    public static final String ACTION_STOP_SERVICE = "stop";
    public static final String ACTION_SYNC_DATASTORE = "sync_datastore";
    public static final String CONTENT_ACTION = "content_action";
    ///////////////////////////////////

    //////// Events /////////////

    public static final String ON_LOADED_VIDEO_FROM_STORAGE = "on_loaded_video_from_storage";
    public static final String ON_DATASTORE_SYNCHRONIZED = "on_datastore_synchronized";

    ////////////////////////////

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getStringExtra(ACTION_KEY);
        /////////// solve action ///////
        switch (action) {
            case ACTION_SYNC_DATASTORE:
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        synchronizeData();
                    }
                });
                executor.shutdown();
                break;
            case ACTION_STOP_SERVICE:
                break;
            default:
                intent = new Intent(CONTENT_ACTION).putExtra(ACTION_KEY, "");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
        ///////////////////////////////////
        return START_NOT_STICKY;
    }

    private void synchronizeData() {
//
//
//        FxRoomDB database = FxRoomDB.get(this);
//        Handler handler = new Handler(Looper.getMainLooper());
//        YoutubeDownloader downloader = new YoutubeDownloader();
//        for (YTVideo video : database.ytvideoDao().getAllVideos()) {
//
////            RequestVideoInfo info = new RequestVideoInfo(video.getId())
////                    .callback(new YoutubeCallback<VideoInfo>() {
////                        @Override
////                        public void onFinished(VideoInfo videoInfo) {
////                            Log.d("ContentService", "onFinished: " + video.getDuration());
////                            handler.post(new Runnable() {
////                                @Override
////                                public void run() {
////                                    long duration = Math.max(videoInfo.videoFormats().get(0).duration(), video.getDuration());
////                                    video.setDuration(duration);
////                                    database.ytvideoDao().update(video);
//////                                    new YoutubeDataSourceBuilder(ContentService.this)
//////                                            .doOnSuccess(new OnSuccessListener() {
//////                                                @Override
//////                                                public void onSuccess() {
//////                                                    Log.d("ContentService", "onSuccess");
//////                                                }
//////                                            })
//////                                            .doOnFailed(new OnFailedListener() {
//////                                                @Override
//////                                                public void onFailed() {
//////                                                    Log.d("ContentService", "onFailed");
//////                                                }
//////                                            })
//////                                            .createVideoThumbnail(videoInfo.details().thumbnails().get(videoInfo.details().thumbnails().size() - 1), video.getId(), 80);
////                                }
////                            });
////
////                        }
////
////                        @Override
////                        public void onError(Throwable throwable) {
////                            // Xử lý lỗi nếu cần
////                        }
////                    })
////                    .async();
////            downloader.getVideoInfo(info);
//            if(video.getLikeCount() > 0 && video.getDescription() != null) continue;
//            ApiCaller.with(this).asYTApi()
//                    .asDetails()
//                    .url(video.getId())
//                    .callback(new OnResponseListener<YTVideoInfo>() {
//                        @Override
//                        public void onResponse(Response<YTVideoInfo> response) {
//                            if (response.isSuccessfully()) {
//                                YTVideoInfo info =  response.getModel();
//                                video.setDescription(info.getDescription());
//                                video.setCommentCount(info.getCommentCount());
//                                video.setLikeCount(info.getLikeCount());
//                                Log.d("ContentService", "successfully: " + video.getDescription());
//                                database.ytvideoDao().update(video);
//                            }
//                        }
//                    }).get();
//            Thread.sleep(2000);
//        }

//        ShortsVideoDao shortsVideoDao = database.shortsVideoDao();
//        ShortsUserDao shortsUserDao = database.shortsUserDao();
//        ShortsMusicDao shortsMusicDao = database.shortsMusicDao();
//        CommentDao commentDao = database.commentDao();

//
//        int count = 0;
//        for (ShortsVideo video : videos) {
//            if(video.getResponseServer() != null && (video.getResponseServer().equals(TikTokApi.API_TIKTOK_MOBILE_VERSION) || video.getResponseServer().equals(TikTokApi.API_TIKTOK_PRIVATE))) {
//                TiktokPackage tiktokPackage = TiktokResponseModelsParser.parseTiktokApi(video.getResponseServer(), video.getRawResponse());
//                if(tiktokPackage != null) {
//                    if(tiktokPackage.getAnchorPosId() != null && tiktokPackage.getAnchorPosKeyword() != null) {
//                        count++;
//                        video.setAnchorPosId(tiktokPackage.getAnchorPosId());
//                        video.setAnchorPosKeyword(tiktokPackage.getAnchorPosKeyword());
//                        shortsVideoDao.update(video);
//                        Log.d("ContentService", "Video has been update anchor, the video has desc: " + video.getDescription());
//                    }
//                }
//            }
//        }
//
//        Log.d("ContentService", "In summary, we have " + count + " videos have anchor position !");

//        RawResponseDao responseDao = database.rawResponseDao();
//        List<ShortsVideo> videos = shortsVideoDao.getAllVideos();
//
//        for(ShortsVideo video : videos) {
//            String raw = video.getRawResponse();
//            if(raw != null) {
//                video.setRawResponse(null);
//                shortsVideoDao.update(video);
//            }
//        }
//
//        videos = null;
//
//        List<ShortsMusic> musics = shortsMusicDao.getAll();
//
//        for(ShortsMusic music : musics) {
//            String raw = music.getRawResponse();
//            if(raw != null) {
//                music.setRawResponse(null);
//                shortsMusicDao.update(music);
//            }
//        }
//
//        musics = null;
//
//        List<ShortsUser> users = shortsUserDao.getAll();
//
//        for(ShortsUser user : users) {
//            String raw = user.getRawResponse();
//            if(raw != null) {
//                user.setRawResponse(null);
//                shortsUserDao.update(user);
//            }
//        }
//
//        users = null;
//
//        List<Comment> comments = commentDao.getAllComments();
//
//        for(Comment comment : comments) {
//            String raw = comment.getRawResponse();
//            if(raw != null) {
//                comment.setRawResponse(null);
//                commentDao.update(comment);
//            }
//        }


//        List<String> list_videos_path = new ArrayList<String>();
//        for (ShortsVideo shortsVideo : videos) {
//            if(!shortsVideo.isImageList()) {
//                list_videos_path.add(shortsVideo.getMediaStorePath());
//            }
//        }
//        ShortsVideo video = videos.get(0);
//
//        File file = new File(video.getMediaStorePath()).getParentFile();
//        if (file != null && file.exists()) {
//            Log.d("ContentService", file.getAbsolutePath());
//            File[] files = file.listFiles();
//            if (files != null) {
//                for (File f : files) {
//                    String path = f.getAbsolutePath();
//                    if (!list_videos_path.contains(path)) {
//                        Log.d("ContentService", "The file is not in database, path: " + path);
//                    }
//                }
//            }
//        }


//        PreferenceHelper.putTimeSavedToStorage(this, System.currentTimeMillis() - 10*60*60*1000);

//        PlaylistVideo playlist = database.playlistDao().getPlaylistById(PlaylistVideo.FAVORITE_PLAYLIST_ID);
//        List<ShortsVideo> videos = shortsVideoDao.getByFxIds(playlist.getVideoIdList());
//        Amplituda amplituda = new Amplituda(this);
//        for(ShortsVideo video : videos) {
//
//        }
        //checkDeltaTimeAndDeleteIfNeed();

//        boolean isVideoExists = FxRoomDB.get(this).shortsVideoDao().isExistsByAwemeId("7140829111517220123");
//        boolean isUserExists = FxRoomDB.get(this).shortsUserDao().isExistsByUserId("7016213449856680965");
//        boolean isMusicExists = FxRoomDB.get(this).shortsMusicDao().isExistsById("7133552011732142875");
//        Log.d("ContentService", "video exists: " + isVideoExists + ", user exists: " + isUserExists + ",music exists:  " + isMusicExists);

//        String dirPath = PreferenceHelper.getSaveStoragePath(this);
//        File videoJson = new File(dirPath, "shorts_video.json");
//        File userJson = new File(dirPath, "shorts_user.json");
//        File musicJson = new File(dirPath, "shorts_music.json");
//
//
//        JSONArray musicArray = readJsonArray(musicJson.getAbsolutePath());
//
//        try {
//            for (int i = 0; i < musicArray.length(); i++) {
//                JSONObject musicObject = musicArray.getJSONObject(i);
//                String id = musicObject.optString("id");
//                String url = musicObject.optString("url");
//                String title = musicObject.optString("title");
//                String author = musicObject.optString("author");
//                String authorId = musicObject.optString("authorId");
//                long duration = musicObject.optLong("duration");
//
//                ShortsMusic music = new ShortsMusic(id, url, title, author, duration, authorId);
//
//                // Lưu vào Room Database
//                FxRoomDB.get(this).shortsMusicDao().insert(music);
//            }
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
//
//        JSONArray userArray = readJsonArray(userJson.getAbsolutePath());
//
//        try {
//            for (int i = 0; i < userArray.length(); i++) {
//                JSONObject userObject = userArray.getJSONObject(i);
//                String uid = userObject.optString("uid");
//                String uniqueId = userObject.optString("uniqueId");
//                String nickName = userObject.optString("nickName");
//                String shareUrl = userObject.optString("shareUrl");
//                String signature = userObject.optString("signature");
//                String avatarUrl = userObject.optString("avatarUrl");
//                String socialUserType = userObject.optString("socialUserType");
//                String country = userObject.optString("country");
//                long followerCount = userObject.getLong("followerCount");
//                long followingCount = userObject.getLong("followingCount");
//                long totalFavorite = userObject.optLong("totalFavorite");
//                boolean verified = userObject.optInt("verified") == 1;
//
//                ShortsUser user = new ShortsUser(uid, uniqueId, nickName, shareUrl, signature, avatarUrl, socialUserType, followerCount, followingCount, totalFavorite);
//                user.setVerified(verified);
//                user.setCountry(country);
//
//                // Lưu vào Room Database
//                FxRoomDB.get(this).shortsUserDao().insert(user);
//                Log.d("ContentService", "insert new shorts user from json: " + user.getNickName());
//            }
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
//
//        JSONArray videoArray = readJsonArray((videoJson.getAbsolutePath()));
//
//        try {
//            for (int i = 0; i < videoArray.length(); i++) {
//                JSONObject videoObject = videoArray.getJSONObject(i);
//                long fxId = videoObject.optLong("fxId");
//                String mediaStoreId = videoObject.getString("mediaStoreId");
//                String mediaStoreName = videoObject.optString("mediaStoreName");
//                String mediaStorePath = videoObject.optString("mediaStorePath");
//                String mediaStoreParent = videoObject.optString("mediaStoreParent");
//                int size = videoObject.optInt("size");
//                long mediaStoreDayAdded = videoObject.optLong("mediaStoreDayAdded");
//                boolean isFavorite = videoObject.optInt("isFavorite") == 1;
//                boolean isPrivate = videoObject.optInt("isPrivate") == 1;
//                boolean isLimited = videoObject.optInt("isLimited") == 1;
//                int socialMediaType = videoObject.getInt("socialMediaType");
//                String fxThumbnailId = videoObject.getString("fxThumbnailId");
//                String fxNotificationLargeIconId = videoObject.getString("fxNotificationLargeIconId");
//                String fxDescription = videoObject.getString("fxDescription");
//                long fxAuthorId = videoObject.getLong("fxAuthorId");
//                long fxMusicId = videoObject.getLong("fxMusicId");
//                long duration = videoObject.getLong("duration");
//                int width = videoObject.getInt("width");
//                int height = videoObject.getInt("height");
//                String awemeId = videoObject.optString("awemeId");
//                String url = videoObject.optString("url");
//                String shareUrl = videoObject.optString("shareUrl");
//                String fxCloudDatabaseUrl = videoObject.optString("fxCloudDatabaseUrl");
//                String dynamicCover = videoObject.optString("dynamicCover");
//                String description = videoObject.optString("description");
//                String descriptionTitle = videoObject.optString("descriptionTitle");
//                String authorId = videoObject.optString("authorId");
//                String musicId = videoObject.optString("musicId");
//                long likeCount = videoObject.optLong("likeCount");
//                long commentCount = videoObject.optLong("commentCount");
//                long shareCount = videoObject.optLong("shareCount");
//                long playCount = videoObject.optLong("playCount");
//                long shortsCreateTime = videoObject.optLong("shortsCreateTime");
//                boolean isImageList = videoObject.optInt("isImageList") == 1;
//                String region = videoObject.optString("region");
//                boolean isWarnAttempt = videoObject.optInt("isWarnAttempt") == 1;
//                int warningType = videoObject.optInt("warningType");
//                String capcutTemplateId = videoObject.optString("capcutTemplateId");
//                String capcutSchema = videoObject.optString("capcutSchema");
//                String anchorPosId = videoObject.optString("anchorPosId");
//                String anchorPosKeyword = videoObject.optString("anchorPosKeyword");
//
//                ShortsVideo shorts = new ShortsVideo(mediaStoreId, mediaStoreName, mediaStorePath, mediaStoreParent, mediaStoreDayAdded, size, duration);
//                // set other fields here
//                shorts.setFxId(fxId);
//                shorts.setFavorite(isFavorite);
//                shorts.setPrivate(isPrivate);
//                shorts.setLimited(isLimited);
//                shorts.setSocialMediaType(socialMediaType);
//                shorts.setFxThumbnailId(fxThumbnailId);
//                shorts.setFxNotificationLargeIconId(fxNotificationLargeIconId);
//                shorts.setFxDescription(fxDescription);
//                shorts.setFxAuthorId(fxAuthorId);
//                shorts.setFxMusicId(fxMusicId);
//                shorts.setWidth(width);
//                shorts.setHeight(height);
//                shorts.setAwemeId(awemeId);
//                shorts.setUrl(url);
//                shorts.setShareUrl(shareUrl);
//                shorts.setFxCloudDatabaseUrl(fxCloudDatabaseUrl);
//                shorts.setDynamicCover(dynamicCover);
//                shorts.setDescription(description);
//                shorts.setDescriptionTitle(null);
//                shorts.setCapcutSchema(null);
//                shorts.setCapcutTemplateId(null);
//                shorts.setAnchorPosId(null);
//                shorts.setAnchorPosKeyword(null);
//                shorts.setAuthorId(authorId);
//                shorts.setMusicId(musicId);
//                shorts.setLikeCount(likeCount);
//                shorts.setCommentCount(commentCount);
//                shorts.setShareCount(shareCount);
//                shorts.setPlayCount(playCount);
//                shorts.setShortsCreateTime(shortsCreateTime);
//                shorts.setIsImageList(isImageList);
//                shorts.setRegion(region);
//                shorts.setWarnAttempt(isWarnAttempt);
//                shorts.setWarningType(warningType);
//                shorts.setCapcutTemplateId(capcutTemplateId);
//                shorts.setCapcutSchema(capcutSchema);
//                shorts.setAnchorPosId(anchorPosId);
//                shorts.setAnchorPosKeyword(anchorPosKeyword);
//
//                FxRoomDB.get(this).shortsVideoDao().insert(shorts);
//                Log.d("ContentService", "insert new shorts video from json: " + shorts.getTrackTitle());
//            }
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }


//        reFetchMedia();
//        String baseDir = getExternalFilesDir(null).getAbsolutePath().replace("files", "");
//        File zipFile = new File(baseDir, "files.zip");
//
//       try {
//           ZipUtils.unzip(zipFile.getAbsolutePath(), baseDir, false);
//       } catch (IOException e) {
//           try {
//               throw new IOException("Lỗi khi unzip: " + e.getMessage());
//           } catch (IOException ex) {
//               throw new RuntimeException(ex);
//           }
//       }

        notifyOnContentEvent(ON_DATASTORE_SYNCHRONIZED);
    }

    public void reFetchMedia() {
        FxRoomDB db = FxRoomDB.get(this);
        FxMediaVideoDao dao = db.fxMediaVideoDao();
        List<FxMediaVideo> videos = ContentUtils.getFxMediaVideoFromStorage(this);

        for (FxMediaVideo video : videos) {
            if (!dao.isExistsByMediaStoreId(video.getMediaStoreId())) {
                dao.insert(video);
                Log.d("ContentService", "Inserted video: " + video.getMediaStoreName());
            }
        }
    }

    public JSONArray readJsonArray(String filePath) {
        try {
            // Đọc toàn bộ nội dung file JSON thành chuỗi
            String content = new String(Files.readAllBytes(Paths.get(filePath)));

            // Chuyển chuỗi thành JSONArray
            return new JSONArray(content);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private void checkDeltaTimeAndDeleteIfNeed() {
        SharedPreferences contentPref = getSharedPreferences("preferences", MODE_PRIVATE);
        long deletionLastTimeMillis = contentPref.getLong("DELETION_LAST_TIME", 0);
        Log.d("ContentService", "deletionLastTimeMillis: " + deletionLastTimeMillis);

        LocalDateTime deletionLastDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(deletionLastTimeMillis), ZoneId.systemDefault());
        LocalDateTime currentDateTime = LocalDateTime.now();

        Duration duration = Duration.between(deletionLastDateTime, currentDateTime);

        Log.d("ContentService", "Duration in minutes: " + duration.toMinutes());

        if (duration.toHours() > 8) {
            Log.d("ContentService", "Deleting media...");
            deleteMediaNotExists();
            contentPref.edit().putLong("DELETION_LAST_TIME", currentDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()).apply();
        }
    }

    private void deleteMediaNotExists() {
        FxRoomDB database = FxRoomDB.get(this);
        ShortsVideoDao shortsVideoDao = database.shortsVideoDao();
        List<ShortsVideo> videos = shortsVideoDao.getAllVideos();
        DataSourceBuilder ds = DataSourceBuilder.get(this);

        ExecutorService executor = Executors.newFixedThreadPool(5); // Số luồng có thể điều chỉnh

        for (ShortsVideo v : videos) {
            executor.execute(() -> {
                File file = new File(v.getMediaStorePath());
                if (!file.exists()) {
                    // Log đường dẫn của tệp tin thumbnail
                    File thumbnail = new File(v.getFxThumbnailPath());
                    thumbnail.delete();
                    Log.d("ContentService", "Thumbnail path: " + thumbnail.getPath());

                    // Log đường dẫn của tệp tin largeIcon
                    File largeIcon = new File(v.getFxNotificationLargeIconPath());
                    largeIcon.delete();
                    Log.d("ContentService", "LargeIcon path: " + largeIcon.getPath());

                    if (v.isImageList() && v.getImageListPath() != null) {
                        for (String image : v.getImageListPath()) {
                            File imageFile = new File(image);
                            imageFile.delete();
                            Log.d("ContentService", "Image path: " + imageFile.getPath());
                        }
                    }

                    // Log đường dẫn của tệp tin musicAvatar
//                File musicAvatar = new File(v.getMusicAvatarPath());
//                musicAvatar.delete();
//                Log.d("FileDeletion", "MusicAvatar path: " + musicAvatar.getPath());
//
//                // Log đường dẫn của tệp tin authorAvatar
//                File authorAvatar = new File(v.getAuthorAvatarPath());
//                authorAvatar.delete();
//                Log.d("FileDeletion", "AuthorAvatar path: " + authorAvatar.getPath());

                    shortsVideoDao.delete(v);
                } else {
                    boolean isNeedToUpdate = false;
                    if (v.getDuration() == 0) {
                        ds.createMediaDuration(v);
                        isNeedToUpdate = true;
                    }
                    if (v.getHeight() == 0 || v.getWidth() == 0) {
                        ds.createThumbnailSizeIfIsImageList(v);
                        isNeedToUpdate = true;
                    }
                    if (isNeedToUpdate) shortsVideoDao.update(v);
                }
            });
        }

        FxMediaVideoDao mediaVideoDao3 = database.fxMediaVideoDao();

        for (FxMediaVideo v : mediaVideoDao3.getAll()) {
            executor.execute(() -> {
                File file = new File(v.getMediaStorePath());
                if (!file.exists()) {
                    // Log đường dẫn của tệp tin thumbnail
                    File thumbnail = new File(v.getFxThumbnailPath());
                    thumbnail.delete();
                    Log.d("ContentService", "Thumbnail path: " + thumbnail.getPath());

                    // Log đường dẫn của tệp tin largeIcon
                    File largeIcon = new File(v.getFxNotificationLargeIconPath());
                    largeIcon.delete();
                    Log.d("ContentService", "LargeIcon path: " + largeIcon.getPath());
                    mediaVideoDao3.delete(v);
                }
            });
        }

        executor.shutdown(); // Đợi tất cả các luồng kết thúc
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS); // Đợi tới khi tất cả các luồng đã kết thúc
        } catch (InterruptedException e) {
            Log.e("ContentService", "Error waiting for thread termination", e);
            Thread.currentThread().interrupt();
        }

        try {
            DataSourceBuilder.killInstance();
        } catch (IOException ignored) {

        }
    }

    private void notifyOnContentEvent(String event) {
        Intent intent = new Intent(CONTENT_ACTION).putExtra(ACTION_KEY, event);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}
