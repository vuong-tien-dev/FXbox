package com.vtstudio.fxbox.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.vtstudio.fxbox.database.converter.Converters;
import com.vtstudio.fxbox.database.dao.CommentDao;
import com.vtstudio.fxbox.database.dao.FxMediaVideoDao;
import com.vtstudio.fxbox.database.dao.MediaSegmentDao;
import com.vtstudio.fxbox.database.dao.PlaylistDao;
import com.vtstudio.fxbox.database.dao.RawResponseDao;
import com.vtstudio.fxbox.database.dao.ShortsDetailsDao;
import com.vtstudio.fxbox.database.dao.ShortsMusicDao;
import com.vtstudio.fxbox.database.dao.ShortsUserDao;
import com.vtstudio.fxbox.database.dao.ShortsVideoDao;
import com.vtstudio.fxbox.database.dao.YTDetailsDao;
import com.vtstudio.fxbox.database.dao.YTUserDao;
import com.vtstudio.fxbox.database.dao.YTVideoDao;
import com.vtstudio.fxbox.media.models.*;
import com.vtstudio.fxbox.media.models.tiktok.Comment;
import com.vtstudio.fxbox.media.models.tiktok.Playlist;
import com.vtstudio.fxbox.media.models.tiktok.ShortsMusic;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.media.models.youtube.YTDetails;
import com.vtstudio.fxbox.media.models.youtube.YTUser;
import com.vtstudio.fxbox.media.models.youtube.YTVideo;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;


@Database(entities = {FxMediaVideo.class, ShortsUser.class, ShortsMusic.class, ShortsVideo.class, Comment.class, RawResponse.class, Playlist.class, YTVideo.class, YTUser.class, MediaSegment.class}, version = 16, exportSchema = true)
@TypeConverters(Converters.class)
public abstract class FxRoomDB extends RoomDatabase {
    public static final String DATABASE_NAME = "fx_db_3";
    public abstract FxMediaVideoDao fxMediaVideoDao();
    public abstract ShortsUserDao shortsUserDao();
    public abstract ShortsMusicDao shortsMusicDao();
    public abstract ShortsVideoDao shortsVideoDao();
    public abstract ShortsDetailsDao shortsDetailsDao();
    public abstract CommentDao commentDao();
    public abstract PlaylistDao playlistDao();
    public abstract RawResponseDao rawResponseDao();
    public abstract YTVideoDao ytvideoDao();
    public abstract YTUserDao ytUserDao();
    public abstract YTDetailsDao ytvDetailsDao();
    public abstract MediaSegmentDao mediaSegmentDao();

    private static FxRoomDB instance;
    public static synchronized FxRoomDB get (Context context){
        if(instance == null && context != null){
            instance = Room.databaseBuilder(context.getApplicationContext(), FxRoomDB.class, FxRoomDB.DATABASE_NAME)
                    .allowMainThreadQueries()
                    .addCallback(playListCallBack)
                    .addMigrations(MIGRATION_11_TO_12, MIGRATION_12_TO_13, MIGRATION_13_TO_14, MIGRATION_14_TO_15, MIGRATION_15_TO_16)
                    .build();
        }
        return instance;
    }

    private static final Migration MIGRATION_15_TO_16 = new Migration (15, 16) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

        }
    };

    private static final Migration MIGRATION_14_TO_15 = new Migration (14, 15) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `media_segment` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`title` TEXT, " +
                            "`startTime` INTEGER NOT NULL, " +
                            "`endTime` INTEGER NOT NULL, " +
                            "`mediaId` INTEGER NOT NULL )"
            );
            database.execSQL("ALTER TABLE fx_media_video ADD COLUMN mediaSegmentId INTEGER NOT NULL DEFAULT -1");
            database.execSQL("ALTER TABLE shorts_video ADD COLUMN mediaSegmentId INTEGER NOT NULL DEFAULT -1");
            database.execSQL("ALTER TABLE yt_video ADD COLUMN mediaSegmentId INTEGER NOT NULL DEFAULT -1");
        }
    };

    private static final Migration MIGRATION_13_TO_14 = new Migration (13, 14) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String[] tables = new String[]  {"fx_media_video", "shorts_video", "yt_video"};
            for(String table : tables){
                database.execSQL("ALTER TABLE " +  table + " ADD volume REAL NOT NULL DEFAULT 1");
            }
        }
    };

    private static final Migration MIGRATION_11_TO_12 = new Migration (11, 12) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE RawResponse (responseId TEXT NOT NULL PRIMARY KEY, mediaType INTEGER NOT NULL, stringResponse TEXT)");
        }
    };

    private static final Migration MIGRATION_12_TO_13 = new Migration (12, 13) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE yt_video (\n" +
                    "    id TEXT NOT NULL PRIMARY KEY,\n" +
                    "    title TEXT,\n" +
                    "    description TEXT,\n" +
                    "    channelId TEXT,\n" +
                    "    shareUrl TEXT,\n" +
                    "    quality TEXT,\n" +
                    "    likeCount INTEGER NOT NULL,\n" +
                    "    commentCount INTEGER NOT NULL,\n" +
                    "    playCount INTEGER NOT NULL,\n" +
                    "    createTime INTEGER NOT NULL,\n" +
                    "    fxId INTEGER NOT NULL,\n" +
                    "    mediaStoreId TEXT,\n" +
                    "    mediaStoreName TEXT,\n" +
                    "    mediaStorePath TEXT,\n" +
                    "    mediaStoreParent TEXT,\n" +
                    "    mediaStoreDayAdded INTEGER NOT NULL,\n" +
                    "    size INTEGER NOT NULL,\n" +
                    "    duration INTEGER NOT NULL,\n" +
                    "    fxThumbnailId TEXT,\n" +
                    "    fxNotificationLargeIconId TEXT,\n" +
                    "    fxDescription TEXT,\n" +
                    "    fxAuthorId INTEGER NOT NULL,\n" +
                    "    fxMusicId INTEGER NOT NULL,\n" +
                    "    width INTEGER NOT NULL,\n" +
                    "    height INTEGER NOT NULL,\n" +
                    "    isFavorite INTEGER NOT NULL,\n" +
                    "    isPrivate INTEGER NOT NULL,\n" +
                    "    isLimited INTEGER NOT NULL,\n" +
                    "    socialMediaType INTEGER NOT NULL\n" +
                    ");\n");
            database.execSQL("CREATE TABLE IF NOT EXISTS yt_user (\n" +
                    "    fxId INTEGER NOT NULL,\n" +
                    "    channelId TEXT NOT NULL PRIMARY KEY,\n" +
                    "    uniqueId TEXT,\n" +
                    "    channelName TEXT,\n" +
                    "    description TEXT,\n" +
                    "    avatarUrl TEXT,\n" +
                    "    socialUserType TEXT,\n" +
                    "    country TEXT,\n" +
                    "    subscriberCount INTEGER NOT NULL,\n" +
                    "    totalViewCount INTEGER NOT NULL,\n" +
                    "    videoCount INTEGER NOT NULL,\n" +
                    "    verified INTEGER NOT NULL\n" +
                    ");\n");
        }
    };

    private static final RoomDatabase.Callback playListCallBack = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Thực hiện khởi tạo danh sách Playlist nếu chúng không tồn tại trước đó
            Executors.newSingleThreadScheduledExecutor().execute(() -> {
                // Đối với mỗi Playlist, kiểm tra xem nó có tồn tại trong cơ sở dữ liệu hay không
                // Nếu không tồn tại, hãy khởi tạo và thêm vào cơ sở dữ liệu
                Playlist favoritePlaylist = new Playlist(Playlist.FAVORITE_PLAYLIST_ID, "Favorite Playlist", "favorite_background", System.currentTimeMillis());
                Playlist recentlyViewedPlaylist = new Playlist(Playlist.RECENTLY_VIEWED_PLAYLIST, "Recently Viewed Playlist", "recent_background", System.currentTimeMillis());
                Playlist limitedPlaylist = new Playlist(Playlist.LIMITED_PLAYLIST, "Limited Playlist", "limited_background", System.currentTimeMillis());
                List<Playlist> playlists = Arrays.asList(favoritePlaylist, recentlyViewedPlaylist, limitedPlaylist);

                // Kiểm tra từng playlist và thêm vào cơ sở dữ liệu nếu chưa tồn tại
                for (Playlist playlist : playlists) {
                    if (instance.playlistDao().getPlaylistById(playlist.getId()) == null) {
                        instance.playlistDao().insert(playlist);
                    }
                }
            });
        }
    };
}

