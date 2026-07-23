package com.vtstudio.fxbox.media.models.youtube;

import androidx.annotation.NonNull;

import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.database.dao.RawResponseDao;
import com.vtstudio.fxbox.downloader.FXDownloader;
import com.vtstudio.fxbox.downloader.OnSaveModelCallback;
import com.vtstudio.fxbox.downloader.RequestInfo;
import com.vtstudio.fxbox.media.models.RawResponse;
import com.vtstudio.fxbox.media.models.tiktok.Playlist;
import com.vtstudio.fxbox.media.sources.DataSourceBuilder;
import com.vtstudio.fxbox.media.utils.ModelUtils;

import java.util.List;

public class YTSaveModelCallback implements OnSaveModelCallback {
    @Override
    public boolean onSaveModel(@NonNull FXDownloader downloader, @NonNull FXDownloader.Request request) {
        RequestInfo info = request.getInfo();
        Object model = downloader.getSaveModelMap().get(info.getId());
        YTDetails details = null;

        if(model instanceof YTDetails) {
            details = (YTDetails) model;
        }

        if (details == null) {
            info.getFile().delete();
            return false;
        }

        String parentPath = info.getFile().getParent();

        if (parentPath == null) {
            info.getFile().delete();
            throw new NullPointerException("parentPath is null");
        }

        YTVideo video = details.getVideo();
        video.setMediaStoreName(info.getFileName().substring(0, info.getFileName().lastIndexOf(".")));
        video.setMediaStoreDayAdded(System.currentTimeMillis());
        video.setMediaStoreParent(parentPath.substring(parentPath.lastIndexOf("/") + 1));
        video.setSize(Math.toIntExact(info.getContentLength()));
        video.setMediaStorePath(info.getFile().getAbsolutePath());
        video.setFxId(ModelUtils.retrieveFxIdFromNameIfCan(video.getMediaStoreName()));
        video.setChannelId(details.getUser().getChannelId());

        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.get(downloader);

        video.setFxId(ModelUtils.retrieveFxIdFromNameIfCan(video.getMediaStoreName()));

        dataSourceBuilder.createMediaDuration(video);
        dataSourceBuilder.createThumbnailSizeIfIsImageList(video);


        FxRoomDB database = FxRoomDB.get(downloader);

        Playlist playlist = database.playlistDao().getPlaylistById(parentPath);

        if (playlist == null) {
            playlist = new Playlist(parentPath, video.getMediaStoreParent(), null, System.currentTimeMillis());
            database.playlistDao().insert(playlist);
        }

        List<String> idList = playlist.getVideoIdList();
        idList.add(0, String.valueOf(video.getFxId()));
        database.playlistDao().update(playlist);

        database.ytUserDao().insert(details.getUser());
        database.ytvideoDao().insert(video);

//        RawResponseDao rawResponseDao = database.rawResponseDao();
//        rawResponseDao.insert(RawResponse.fromShortsVideo(video));

        return true;
    }
}
