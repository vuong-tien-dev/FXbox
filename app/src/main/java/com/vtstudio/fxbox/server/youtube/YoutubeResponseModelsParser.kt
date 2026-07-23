package com.vtstudio.fxbox.server.youtube

import android.annotation.SuppressLint
import android.util.Log
import com.vtstudio.fxbox.media.models.SocialUserType
import com.vtstudio.fxbox.media.models.youtube.YTUser
import com.vtstudio.fxbox.media.models.youtube.YTVideoInfo
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object YoutubeResponseModelsParser {
    @JvmStatic
    fun parseUser(server: String, body: String): YTUser? {
        when (server) {
            YoutubeApi.API_YOUTUBE_V3 -> return parseUserYTV3(body)
        }
        return null
    }

    @JvmStatic
    fun parseInfo(server: String, body: String): YTVideoInfo? {
        when (server) {
            YoutubeApi.API_YOUTUBE_V3 -> {
                Log.d("Youtube", "calling ")
                return parseInfoYTV3(body)
            }
        }
        return null
    }

    @SuppressLint("SuspiciousIndentation")
    private fun parseInfoYTV3(body: String): YTVideoInfo? {
        return try {
            val result = JSONObject(body)

            // "items" là một mảng

            val isArray = result.optJSONArray("items") != null

            var item: JSONObject? = null
            if(isArray) {
                val items: JSONArray = result.getJSONArray("items")
                if (items.length() == 0) {
                    Log.e("Youtube", "No items found in the JSON response.")
                    return null
                }
                item = items.getJSONObject(0)
            } else {
                item = result.getJSONObject("items")
            }


            if(item == null) return null

            val snippet = item.getJSONObject("snippet")
            val contentDetails = item.getJSONObject("contentDetails")
            val statistics = item.getJSONObject("statistics")
            val videoId = item.getString("id")
            val title = snippet.getString("title")
            val description = snippet.optString("description")
            val channelId = snippet.getString("channelId")
            val channelTitle = snippet.getString("channelTitle")
            val publishedAt = snippet.getString("publishedAt")

            // Lấy thumbnail URL
            var thumbnailUrl = ""
                snippet.optJSONObject("thumbnails")?.optJSONObject("standard")?.let { thumbnailUrl = it.getString("url") }
            val viewCount = statistics.getLong("viewCount")
            val likeCount = statistics.getLong("likeCount")
            val favoriteCount = statistics.optLong("favoriteCount", 0)
            val commentCount = statistics.getLong("commentCount")
            val duration = contentDetails.getString("duration")
            val definition = contentDetails.getString("definition")
            val projection = contentDetails.getString("projection")
            YTVideoInfo(
                videoId, title, description, channelId, channelTitle, publishedAt,
                thumbnailUrl, viewCount, likeCount, favoriteCount, commentCount,
                duration, definition, projection
            )
        } catch (e: JSONException) {
            Log.e(
                "Youtube", """
     Failed to parse JSON YTInfo response: ${e.message}
     $body
     """.trimIndent()
            )
            null
        }
    }

    private fun parseUserYTV3(body: String): YTUser? {
        var result: JSONObject? = null
        return try {
            result = JSONObject(body)
            // "items" là một mảng
            val isArray = result.optJSONArray("items") != null

            var item: JSONObject? = null
            if(isArray) {
                val items: JSONArray = result.getJSONArray("items")
                if (items.length() == 0) {
                    Log.e("Youtube", "No items found in the JSON response.")
                    return null
                }
                item = items.getJSONObject(0)
            } else {
                item = result.getJSONObject("items")
            }


            if(item == null) return null

            val snippet = item.getJSONObject("snippet")
            val statistics = item.getJSONObject("statistics")
            val channelId: String
            val channelName: String
            val uniqueId: String
            val description: String
            val country: String
            var thumbnailUrl: String = ""
            val totalViewCount: Long
            val subscriberCount: Long
            val videoCount: Long
            channelId = item.getString("id")
            channelName = snippet.getString("title")
            description = snippet.optString("description")
            uniqueId = snippet.optString("customUrl")
            country = snippet.optString("country")

            snippet.optJSONObject("thumbnails")?.optJSONObject("default")?.let {
                  thumbnailUrl = it.getString("url")
            }
            totalViewCount = statistics.optLong("viewCount")
            subscriberCount = statistics.optLong("subscriberCount")
            videoCount = statistics.optLong("videoCount")
            val fxId = System.currentTimeMillis()
            val user = YTUser(
                fxId, channelId, uniqueId, channelName,
                description, thumbnailUrl, SocialUserType.USER_YOUTUBE, country,
                subscriberCount, totalViewCount, videoCount, false
            )
            user.rawResponse = body
            user
        } catch (e: JSONException) {
            Log.e(
                "Youtube", """
     Failed to parse JSON response: ${e.message}
     $body
     """.trimIndent()
            )
            null
        }
    }
}