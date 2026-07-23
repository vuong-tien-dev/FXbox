package com.vtstudio.fxbox.media.models.tiktok;

import androidx.annotation.NonNull;

import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.database.dao.RawResponseDao;
import com.vtstudio.fxbox.downloader.FXDownloader;
import com.vtstudio.fxbox.downloader.OnSaveModelCallback;
import com.vtstudio.fxbox.downloader.RequestInfo;
import com.vtstudio.fxbox.media.models.RawResponse;
import com.vtstudio.fxbox.media.models.tiktok.Playlist;
import com.vtstudio.fxbox.media.models.tiktok.ShortsMusic;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.media.sources.DataSourceBuilder;
import com.vtstudio.fxbox.media.utils.ModelUtils;
import com.vtstudio.fxbox.media.utils.ModelsParser;
import com.vtstudio.fxbox.models.TiktokPackage;
import com.vtstudio.fxbox.server.ApiCaller;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class TiktokSaveModelCallback implements OnSaveModelCallback {
    @Override
    public boolean onSaveModel(@NonNull FXDownloader downloader, @NonNull FXDownloader.Request request) {
        RequestInfo info = request.getInfo();
        Object model = downloader.getSaveModelMap().get(info.getId());
        TiktokPackage shorts = null;

        if(model instanceof TiktokPackage) {
            shorts = (TiktokPackage) model;
        }

        if (shorts == null) {
            info.getFile().delete();
            return false;
        }

        boolean isImageList = shorts.isImageList();
        String parentPath = info.getFile().getParent();

        if (parentPath == null) {
            info.getFile().delete();
            throw new NullPointerException("parentPath is null");
        }

        ShortsVideo shortsVideo = new ShortsVideo("",
                info.getFileName().substring(0, info.getFileName().lastIndexOf("."))
                , info.getFile().getAbsolutePath()
                , parentPath.substring(parentPath.lastIndexOf("/") + 1)
                , System.currentTimeMillis()
                , (int) info.getContentLength(),
                0
        );

        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.get(downloader);

        shortsVideo.setFxId(ModelUtils.retrieveFxIdFromNameIfCan(shortsVideo.getMediaStoreName()));
        ModelsParser.parseTiktokToShortsVideo(shorts, shortsVideo);
        ShortsUser user = ModelsParser.createShortsUserFromTiktok(shorts);
        ShortsMusic music = ModelsParser.createShortsMusicFromTiktok(shorts);

        dataSourceBuilder.createMediaDuration(shortsVideo);
        dataSourceBuilder.createThumbnailSizeIfIsImageList(shortsVideo);

        if (!isImageList) {
            dataSourceBuilder.createFxThumbnail(shortsVideo);
            try {
                dataSourceBuilder.createFxNotificationLargeIcon(shortsVideo);
            } catch (ExecutionException | InterruptedException ignored) {

            }
        }

        FxRoomDB database = FxRoomDB.get(downloader);

        if (user == null) {
            info.getFile().delete();
            return false;
        }


        ApiCaller.with(downloader)
                .asTikTokApi()
                .asUser()
                .url(user.getUid())
                .callback(response -> {
                    if (response.isSuccessfully()) {
                        database.shortsUserDao().insert(response.getModel());
                    }
                })
                .get();


        Playlist playlist = database.playlistDao().getPlaylistById(parentPath);

        if (playlist == null) {
            playlist = new Playlist(parentPath, shortsVideo.getMediaStoreParent(), null, System.currentTimeMillis());
            database.playlistDao().insert(playlist);
        }

        List<String> idList = playlist.getVideoIdList();
        idList.add(0, String.valueOf(shortsVideo.getFxId()));
        database.playlistDao().update(playlist);

        shortsVideo.setFxMusicId(music.getFxId());
        shortsVideo.setFxAuthorId(user.getFxId());

        database.shortsVideoDao().insert(shortsVideo);
        database.shortsUserDao().insert(user);
        database.shortsMusicDao().insert(music);


        RawResponseDao rawResponseDao = database.rawResponseDao();
        rawResponseDao.insert(RawResponse.fromShortsVideo(shortsVideo));

        return true;
    }
}
