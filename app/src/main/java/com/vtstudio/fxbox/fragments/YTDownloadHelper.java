package com.vtstudio.fxbox.fragments;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.YoutubeCallback;
import com.github.kiulian.downloader.downloader.client.ClientType;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.model.subtitles.SubtitlesInfo;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.VideoFormat;
import com.github.kiulian.downloader.model.videos.formats.VideoWithAudioFormat;
import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.downloader.FXDownloader;
import com.vtstudio.fxbox.downloader.MineType;
import com.vtstudio.fxbox.downloader.ModelDownload;
import com.vtstudio.fxbox.downloader.RequestInfo;
import com.vtstudio.fxbox.fxviews.listview.VideoQualityListView;
import com.vtstudio.fxbox.helpers.PreferenceHelper;
import com.vtstudio.fxbox.media.models.youtube.YTDetails;
import com.vtstudio.fxbox.media.models.youtube.YTSaveModelCallback;
import com.vtstudio.fxbox.media.models.youtube.YTUser;
import com.vtstudio.fxbox.media.models.youtube.YTVideo;
import com.vtstudio.fxbox.media.models.youtube.YTVideoInfo;
import com.vtstudio.fxbox.media.models.youtube.YoutubeDataSourceBuilder;
import com.vtstudio.fxbox.media.utils.ModelsParser;
import com.vtstudio.fxbox.server.ApiCaller;
import com.vtstudio.fxbox.server.OnResponseListener;
import com.vtstudio.fxbox.server.Response;
import com.vtstudio.fxbox.server.UrlExtractor;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class YTDownloadHelper {
    public static void callAndDownloadYT(@NonNull Download fragment, @NonNull String url) {
        Log.d("Download", "Extracting");
        String id = UrlExtractor.getYouTubeId(url);

        YoutubeDownloader downloader = new YoutubeDownloader();
        RequestVideoInfo request = createVideoInfoRequest(id, url, fragment);
        downloader.getVideoInfo(request);
    }

    private static RequestVideoInfo createVideoInfoRequest(String id, String url, Download fragment) {
        return new RequestVideoInfo(id)
                .callback(new YoutubeCallback<VideoInfo>() {
                    @Override
                    public void onFinished(VideoInfo videoInfo) {
                        Log.d("Download", "Finish: " + id);
                        fragment.requireActivity().runOnUiThread(() -> handleVideoInfo(videoInfo, url, fragment));
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.d("Download", "Failed: " + id);
                        fragment.requireActivity().runOnUiThread(() -> {
                            fragment.getStatusDesc().setText(R.string.get_server_data_failed);
                            fragment.recallEnable(url);
                            fragment.showFailedViews();
                            fragment.enableViews();
                        });
                    }
                })
                .clientType(ClientType.MWEB)
                .async();
    }

    private static void handleVideoInfo(VideoInfo videoInfo, String url, Download fragment) {

        ApiCaller.with(fragment.requireActivity()).asYTApi()
                .asDetails()
                .url(videoInfo.details().videoId())
                .callback(new OnResponseListener<YTVideoInfo>() {
                    @Override
                    public void onResponse(Response<YTVideoInfo> response) {
                        if (response.isSuccessfully()) {
                            Log.d("Download", "Successfully get channel id");
                            handleVideoDetailsResponse(videoInfo, response.getModel(), url, fragment);
                        } else {
                            Log.d("Download", "Failed get channel id");
                            fragment.getStatusDesc().setText(R.string.get_server_data_failed);
                            fragment.recallEnable(url);
                            fragment.showFailedViews();
                            fragment.enableViews();
                        }
                    }
                })
                .get();
    }

    private static void handleVideoDetailsResponse(VideoInfo info, YTVideoInfo apiInfo, String url, Download fragment) {

        if (isExists(fragment.getDownloader(), info.details().videoId())) {
            if (!fragment.isViewsDestroyed()) {
                // Nếu video đã tải trước đó, hiển thị thành công
                fragment.showSuccessViews();
                fragment.enableViews();
                fragment.getStatusDesc().setText(R.string.is_in_downloading);
            }
            return;
        }

        ApiCaller.with(fragment.requireActivity()).asYTApi()
                .asUser()
                .url(apiInfo.getChannelId())
                .callback(new OnResponseListener<YTUser>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onResponse(Response<YTUser> response) {
                        if (response.isSuccessfully()) {
                            YTUser user = response.getModel();
                            YTVideo video = ModelsParser.fromVideoInfo(info);
                            video.setLikeCount(apiInfo.getLikeCount());
                            video.setCommentCount(apiInfo.getCommentCount());
                            video.setDescription(apiInfo.getDescription());

                            FXDownloader downloader = fragment.getDownloader();
                            if (downloader != null && user != null) {
                                YTDetails details = new YTDetails();
                                details.setUser(user);
                                details.setVideo(video);

                                YoutubeDataSourceBuilder builder = new YoutubeDataSourceBuilder(downloader, details);

                                fragment.getStatusDesc().setText(R.string.getting_data_img);

                                builder.doOnFailed(() -> {
                                    Log.d("Youtube", "Error downloading video: " + video.getThumbnailUrl());
                                    fragment.getStatusDesc().setText(fragment.getString(R.string.get_data_img_failed));
                                    fragment.showFailedViews();
                                    fragment.recallEnable(url);
                                    fragment.enableViews();
                                }).doOnSuccess(() -> {

                                    FXDownloader.Request request = downloader.newRequest();
                                    request.with(new File(PreferenceHelper.
                                                    getYoutubeVideoPath(downloader, 0), "youtube_" + System.currentTimeMillis() + ".mp4"))
                                            .mineType(MineType.VIDEO_MP4)
                                            .title(video.getTitle())
                                            .thumbnail(video.getThumbnailUrl())
                                            .withModels(details, new YTSaveModelCallback());

                                    if (!fragment.isViewsDestroyed()) {
                                        fragment.showSuccessViews();
                                        fragment.enableViews();
                                    }


                                    if (!fragment.isViewsDestroyed()) {

                                        List<String> qualities = info.videoWithAudioFormats().stream().map(VideoFormat::qualityLabel).collect(Collectors.toList());

                                        fragment.showQualityList(qualities, qualityList -> {

                                            List<VideoFormat> videoFormats =
                                                    info.videoWithAudioFormats().
                                                            stream().filter(f -> Objects.equals(f.qualityLabel(), qualityList)).collect(Collectors.toList());

                                            if (!fragment.isViewsDestroyed()) {
                                                fragment.setEmptyViewsVisible(false);
                                                fragment.getAdapter().notifyDataSetChanged();
                                            }

                                            if(!videoFormats.isEmpty()) {

                                                VideoFormat match = videoFormats.get(0);
                                                video.setWidth(match.width());
                                                video.setHeight(match.height());
                                                video.setQuality(match.qualityLabel());
                                                video.setDuration(match.duration());
                                                request.url(match.url())
                                                        .enqueue();
                                            } else {
                                                if (!fragment.isViewsDestroyed()) {
                                                    fragment.showFailedViews();
                                                    fragment.recallEnable(url);
                                                }
                                            }
                                            if(!fragment.isViewsDestroyed()) {
                                                fragment.hideQualityListWithAnimation();
                                            }
                                        });
                                    }

                                    builder.release();

                                }).createDataSource(true);
                            } else {
                                if (!fragment.isViewsDestroyed()) {
                                    fragment.getStatusDesc().setText(fragment.getString(R.string.get_server_data_failed));
                                    fragment.showFailedViews();
                                    fragment.recallEnable(url);
                                    fragment.enableViews();
                                }
                            }
                        } else {
                            if (!fragment.isViewsDestroyed()) {
                                fragment.getStatusDesc().setText(fragment.getString(R.string.get_server_data_failed));
                                fragment.showFailedViews();
                                fragment.recallEnable(url);
                                fragment.enableViews();
                            }
                        }
                    }
                }).get();
    }

    public static boolean isExists(@NonNull FXDownloader manager, String id) {
        for (RequestInfo info : manager.getRequestInfoList()) {
            ModelDownload model = manager.getSaveModelMap().get(info.getId());
            if (model instanceof YTDetails) {
                if (Objects.equals(((YTDetails) model).getVideo().getId(),id)) {
                    return true;
                }
            }
        }
        return false;
    }
}
