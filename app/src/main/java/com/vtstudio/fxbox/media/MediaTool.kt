package com.vtstudio.fxbox.media

import android.content.Context
import com.vtstudio.fxbox.database.FxRoomDB
import com.vtstudio.fxbox.media.models.FxMediaVideo
import com.vtstudio.fxbox.media.models.Media
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo
import com.vtstudio.fxbox.media.models.youtube.YTVideo
import com.vtstudio.fxbox.media.utils.ModelUtils

class MediaTool {
    companion object {

        @JvmStatic
        fun getMediaByFxId (context: Context, fxId: Long): FxMediaVideo? {
            val database = FxRoomDB.get(context)
            val fxMediaVideoDao = database.fxMediaVideoDao()
            val shortsDetailsDao = database.shortsDetailsDao()
            val ytDetailsDao = database.ytvDetailsDao()

            var media: FxMediaVideo? = fxMediaVideoDao.getVideoByFxId(fxId)

            if(media == null) {
                media = ModelUtils.fromDetails(shortsDetailsDao.getDetailsByFxId(fxId))
            }

            if(media == null) {
                media = ModelUtils.fromDetails(ytDetailsDao.getDetailsByFxId(fxId))
            }

            return media
        }

        @JvmStatic
        fun getMediaTitle (media: Media): String {
            if(media is ShortsVideo) {
                return media.description
            } else if(media is YTVideo) {
                media.title
            }
            return media.mediaStoreName
        }
        @JvmStatic
        fun getVideoThumbnail (media: FxMediaVideo): String {
            if(media is ShortsVideo) {
                if(media.isImageList && media.imageListPath.isNotEmpty()) {
                    return media.imageListPath[0]
                }
            } else if(media is YTVideo) {
                return media.ytVideoThumbnailPath
            }

            return media.fxThumbnailPath
        }

        @JvmStatic
        fun getUserAvatar (media: FxMediaVideo): String {

            if(media is ShortsVideo) {
                media.shortsUser?.let { return it.avatarPath }
            } else if(media is YTVideo) {
                media.user?.let { return it.avatarPath }
            }

            return ""
        }

        @JvmStatic
        fun getUserName (media: FxMediaVideo): String {

            if(media is ShortsVideo) {
                media.shortsUser?.let { return it.nickName }
            } else if(media is YTVideo) {
                media.user?.let { return it.channelName }
            }

            return media.mediaStoreName
        }
    }
}