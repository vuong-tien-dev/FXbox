package com.vtstudio.fxbox.fragments;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.api.TiktokDataSourceBuilder;
import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.database.dao.ShortsVideoDao;
import com.vtstudio.fxbox.downloader.FXDownloader;
import com.vtstudio.fxbox.downloader.MineType;
import com.vtstudio.fxbox.downloader.RequestInfo;
import com.vtstudio.fxbox.media.models.tiktok.TiktokSaveModelCallback;
import com.vtstudio.fxbox.helpers.PreferenceHelper;
import com.vtstudio.fxbox.models.TiktokPackage;
import com.vtstudio.fxbox.server.ApiCaller;

import java.util.Objects;

public class ShortsDownloadHelper {

    public static void callAndDownloadShorts(@NonNull Download fragment, @NonNull String url) {
        // Kiểm tra nếu là link rút gọn TikTok
        if (isShortUrl(url)) {
            // Expand link rút gọn trước khi gọi API
            expandShortUrl(fragment, url, expandedUrl -> {
                if (expandedUrl != null && !fragment.isViewsDestroyed()) {
                    callTikTokApi(fragment, expandedUrl);
                } else if (!fragment.isViewsDestroyed()) {
                    fragment.getStatusDesc().setText(R.string.get_server_data_failed);
                    fragment.recallEnable(url);
                    fragment.showFailedViews();
                    fragment.enableViews();
                }
            });
        } else {
            // Link đầy đủ, gọi API trực tiếp
            callTikTokApi(fragment, url);
        }
    }

    /**
     * Kiểm tra xem URL có phải là link rút gọn TikTok không
     */
    private static boolean isShortUrl(String url) {
        return url.contains("vm.tiktok.com") 
            || url.contains("vt.tiktok.com")
            || url.contains("tiktok.com/t/");
    }

    /**
     * Expand link rút gọn bằng cách follow HTTP redirect
     */
    private static void expandShortUrl(@NonNull Download fragment, String shortUrl, OnExpandUrlCallback callback) {
        new Thread(() -> {
            try {
                // Tạo OkHttpClient không tự động follow redirect
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                        .followRedirects(false)
                        .followSslRedirects(false)
                        .build();

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(shortUrl)
                        .head() // Chỉ lấy header, không cần body
                        .build();

                okhttp3.Response response = client.newCall(request).execute();
                
                String expandedUrl = null;
                if (response.isRedirect()) {
                    expandedUrl = response.header("Location");
                }
                response.close();

                // Callback trên main thread
                final String finalUrl = expandedUrl;
                if (fragment.getActivity() != null) {
                    fragment.getActivity().runOnUiThread(() -> callback.onExpanded(finalUrl));
                }
            } catch (Exception e) {
                if (fragment.getActivity() != null) {
                    fragment.getActivity().runOnUiThread(() -> callback.onExpanded(null));
                }
            }
        }).start();
    }

    /**
     * Interface callback cho expand URL
     */
    private interface OnExpandUrlCallback {
        void onExpanded(String expandedUrl);
    }

    /**
     * Gọi TikTok API với URL đã được expand
     */
    private static void callTikTokApi(@NonNull Download fragment, @NonNull String url) {
        ApiCaller.with(fragment.getActivity()).asTikTokApi().asShorts().url(url).callback(response -> {
            if (response.isSuccessfully()) {
                TiktokPackage shorts = response.getModel();
                handleShortsApiResponse(fragment, shorts, url);
            } else {
                fragment.getStatusDesc().setText(R.string.get_server_data_failed);
                fragment.recallEnable(url);
                fragment.showFailedViews();
                fragment.enableViews();
            }
        }).get();
    }

    private static void handleShortsApiResponse(@NonNull Download fragment, @NonNull TiktokPackage shorts, String url) {

        ShortsVideoDao shortsVideoDao = FxRoomDB.get(fragment.getActivity()).shortsVideoDao();

        if (!shortsVideoDao.isExistsByAwemeId(shorts.getAwemeId())) {
            createShortsDataSourceThenDownload(fragment, shorts, url);
        } else {
            if (!fragment.isViewsDestroyed()) {
                fragment.showSuccessViews();
                fragment.enableViews();
                fragment.getStatusDesc().setText(R.string.data_exists);
            }
        }
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private static void createShortsDataSourceThenDownload(@NonNull Download fragment, @NonNull TiktokPackage api, @NonNull String url) {

        // thay đổi văn bản icon bot
        fragment.getStatusDesc().setText(R.string.getting_data_img);

        Context context = fragment.getActivity();
        FXDownloader manager = fragment.getDownloader();
        if (context == null) {
            return;
        }

        if (isExists(manager, api.getAwemeId())) {
            if (!fragment.isViewsDestroyed()) {
                // Nếu video đã tải trước đó, hiển thị thành công
                fragment.showSuccessViews();
                fragment.enableViews();
                fragment.getStatusDesc().setText(R.string.is_in_downloading);
            }
            return;
        }

        // Nếu thành công kiểm tra xem video có đang trong danh sách tải không
        String extension = api.isImageList() ? ".mp3" : ".mp4"; // đuôi mở rộng của tệp, nếu loại aweme là photos thì chỉ cần tải nhạc
        String fileName = "FxShorts_" + System.currentTimeMillis() + extension; // tên của tệp lưu trong bộ nhớ external


        // Tải hình ảnh từ tiktok
        TiktokDataSourceBuilder dataSource = new TiktokDataSourceBuilder(context, api);

        // tạo một yêu cầu tải
        FXDownloader.Request request = manager.newRequest()
                .with(PreferenceHelper.getShortsVideoPath(context, 0), fileName)
                .url(api.getUrl())
                .title(api.getDescription())
                .withModels(api, new TiktokSaveModelCallback());

        Runnable showFailedViews = () -> {
            // hiển thị trạng thái views khi tải ảnh thất bại
            if (!fragment.isViewsDestroyed()) {
                fragment.enableViews();
                fragment.showFailedViews();
                fragment.recallEnable(url);
                fragment.getStatusDesc().setText(context.getString(R.string.get_data_img_failed) + "(photos: " + api.getResponseServer() + " )");
            }

            dataSource.release();
        };


        dataSource.doOnSuccess(() -> {

            if (api.isImageList()) {
                // Nếu là danh sách ảnh
                dataSource.doOnSuccess(() -> {
                    fragment.setEmptyViewsVisible(false);
                    fragment.getAdapter().notifyDataSetChanged();
                    request.mineType(MineType.AUDIO_MP3);
                    request.thumbnail(dataSource.getVideoThumbnailPath()); // ảnh thu nhỏ trong cache (có thể bị xóa))
                    request.with(PreferenceHelper.getShortsAudioPath(context, 0), fileName); // lấy đường dẫn lưu trong thư mục Android
                    request.enqueue();
                    fragment.showSuccessViews();
                    fragment.enableViews();
                    dataSource.release();
                }).doOnFailed(showFailedViews::run).createImageList(api);

            } else {

                // Nếu không phải danh sách ảnh, tải video
                request.thumbnail(dataSource.getVideoThumbnailPath()); // ảnh thu nhỏ trong cache (có thể bị xóa))

                if (!fragment.isViewsDestroyed()) {
                    fragment.setEmptyViewsVisible(false);
                    fragment.getAdapter().notifyDataSetChanged();
                    fragment.showSuccessViews();
                    fragment.enableViews();
                }

                request.enqueue();
                dataSource.release();
            }

        }).doOnFailed(showFailedViews::run).createDataSource(false);


    }

    public static boolean isExists(@NonNull FXDownloader manager, String awemeId) {
        for (RequestInfo info : manager.getRequestInfoList()) {
            Object model = manager.getSaveModelMap().get(info.getId());
            if (model instanceof TiktokPackage) {
                TiktokPackage shorts = (TiktokPackage) model;
                if (Objects.equals(shorts.getAwemeId(), awemeId)) {
                    return true;
                }
            }
        }
        return false;
    }
}
