package com.vtstudio.fxbox.database;

import com.vtstudio.fxbox.media.models.tiktok.Playlist;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

public class DatabaseInitializer {

    public static void initializeData(FxRoomDB database) {
        // Thực hiện khởi tạo danh sách Playlist nếu chúng không tồn tại trong cơ sở dữ liệu
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            Playlist favoritePlaylist = new Playlist(Playlist.FAVORITE_PLAYLIST_ID, "Favorite Playlist", null, System.currentTimeMillis());
            Playlist recentlyViewedPlaylist = new Playlist(Playlist.RECENTLY_VIEWED_PLAYLIST, "Recently Viewed Playlist", null,  System.currentTimeMillis());
            Playlist limitedPlaylist = new Playlist(Playlist.LIMITED_PLAYLIST, "Limited Playlist", null,  System.currentTimeMillis());
            
            List<Playlist> playlists = Arrays.asList(favoritePlaylist, recentlyViewedPlaylist, limitedPlaylist);

            // Kiểm tra từng playlist và thêm vào cơ sở dữ liệu nếu chưa tồn tại
            for (Playlist playlist : playlists) {
                if (database.playlistDao().getPlaylistById(playlist.getId()) == null) {
                    database.playlistDao().insert(playlist);
                }
            }
        });
    }
}
