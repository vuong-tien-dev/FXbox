package com.vtstudio.fxbox.fragments;


import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.activity.FxBaseActivity;
import com.vtstudio.fxbox.adapters.DownloadItemAdapter;
import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.database.dao.ShortsVideoDao;
import com.vtstudio.fxbox.databinding.FragmentDownloadBinding;
import com.vtstudio.fxbox.downloader.FXDownloader;
import com.vtstudio.fxbox.downloader.ModelDownload;
import com.vtstudio.fxbox.downloader.RequestInfo;
import com.vtstudio.fxbox.fxviews.VirtualAssistantView;
import com.vtstudio.fxbox.fxviews.listview.VideoQualityListView;
import com.vtstudio.fxbox.listeners.OnServiceConnectionListener;
import com.vtstudio.fxbox.models.TiktokPackage;
import com.vtstudio.fxbox.ui.DownloadItemAnimator;
import com.vtstudio.fxbox.utils.ClipboardUtils;
import com.vtstudio.fxbox.utils.URLValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Download extends FxBaseFragment {
    private FragmentDownloadBinding downloadBinding;
    private static boolean isInitialized = false;
    // view refs
    private EditText linkEdt;
    private TextView paste;
    private TextView statusDesc;
    private ImageView leftEditorIcon;
    private ProgressBar loadBar;
    private List<String> urlList;
    // private Queue<String> urlQueue;
    private FXDownloader manager;
    private List<RequestInfo> requestInfoList;
    private Map<Integer, ModelDownload> saveModelMap;
    private DownloadItemAdapter adapter;
    private RecyclerView downloadItemList;
    private RecyclerView.LayoutManager layoutManager;
    private VirtualAssistantView bot;
    private ShortsVideoDao shortsVideoDao;
    private boolean isRecallEnabled;
    private boolean bound;
    private boolean isViewsDestroyed;
    private boolean isChoosingQuality;

    public Download() {
    }

    public static Download newInstance() {
        return new Download();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        isViewsDestroyed = false;
        downloadBinding = FragmentDownloadBinding.inflate(inflater, container, false);
        initVars();
        initViews();
        shortsVideoDao = FxRoomDB.get(getActivity()).shortsVideoDao();
        return downloadBinding.getRoot();
    }

    private void initVars() {
        urlList = new ArrayList<>();
        layoutManager = new LinearLayoutManager(getActivity());
    }


    private void initViews() {

        linkEdt = downloadBinding.downloadLinkEdittext;
        paste = downloadBinding.downloadPaste;
        statusDesc = downloadBinding.downloadStatusDesc;
        bot = downloadBinding.downloadStatusBot;
        leftEditorIcon = downloadBinding.leftEditorIcon;
        loadBar = downloadBinding.downloadLoading;
        downloadItemList = downloadBinding.downloadItemList;
        downloadItemList.setLayoutManager(layoutManager);
        downloadItemList.setItemAnimator(new DownloadItemAnimator());
        adapter = new DownloadItemAdapter(layoutManager, downloadItemList, manager);

        statusDesc.setSelected(true);

        // listener
        paste.setOnClickListener(v -> {
            handlePasteClick();
        });

        // unEnable listener until bind downloader
        unEnableViews();

        //
    }

    private void handlePasteClick() {
        Context context = getActivity();
        if (context == null) return;

        String url = ClipboardUtils.getFirstText(context);
        if (url == null) return;
        startCallApi(url);

    }

    private void startCallApi(String url) {
        if(Patterns.WEB_URL.matcher(url).matches() && (!url.equals(linkEdt.getText().toString()) || isRecallEnabled)) {
            if (URLValidator.isTikTokURL(url)) {
                // bắt đầu trạng thái loading cho views

                linkEdt.setText(url);
                showLoadingViews();
                unEnableViews();

                isRecallEnabled = false;

                ShortsDownloadHelper.callAndDownloadShorts(this, url);
            } else {

                linkEdt.setText(url);
                showLoadingViews();
                unEnableViews();

                isRecallEnabled = false;

                YTDownloadHelper.callAndDownloadYT(this, url);
            }
        }

    }

    @Override
    public void onStop() {
        if (manager != null) manager.setDownloadListener(null);
        bot.autoMakeStatus(false);
        super.onStop();
    }

    @Override
    public void onResume() {
        // Liên kết với FXDownloader
        if (!bound) {
            FragmentActivity activity = getActivity();
            if (activity instanceof FxBaseActivity) {
                ((FxBaseActivity) activity).boundDownloadManager(new OnServiceConnectionListener() {
                    @Override
                    public void onServiceConnected() {

                        manager = ((FxBaseActivity) activity).getDownloadManager();

                        if (manager == null) return;

                        saveModelMap = manager.getSaveModelMap();
                        requestInfoList = manager.getRequestInfoList();
                        adapter.setRequestInfoList(requestInfoList);
                        adapter.setSaveModelMap(saveModelMap);
                        adapter.setManager(manager);
                        downloadItemList.setAdapter(adapter);
                        manager.setDownloadListener(adapter);
                        if (!requestInfoList.isEmpty())
                            downloadBinding.downloadStatusNoneItem.setVisibility(View.GONE);
                        enableViews();
                        bot.autoMakeStatus(true);
                        bot.showWakeUpStatus();
                        bound = true;
                    }

                    @Override
                    public void onServiceDisconnected() {
                        manager = null;
                        bound = false;
                    }
                });
            }
        }

        if (manager != null && adapter != null) {
            // Làm mới danh sách hiển thị tải xuống mỗi khi người dùng thoát ra, vào lại
            downloadItemList.setAdapter(null);
            adapter.setRequestInfoList(manager.getRequestInfoList());
            downloadItemList.setAdapter(adapter);
            downloadItemList.post(() -> manager.setDownloadListener(adapter));
        }

        super.onResume();
    }

    public void hideQualityListWithAnimation() {
        @SuppressLint("Recycle") ValueAnimator valueAnimator = ValueAnimator.ofInt(downloadBinding.videoQualityList.getHeight(), 0);
        valueAnimator.setDuration(400);
        valueAnimator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = downloadBinding.videoQualityList.getLayoutParams();
            params.height = (int) valueAnimator.getAnimatedValue();
            downloadBinding.videoQualityList.requestLayout();
            if(params.height ==0) {
                downloadBinding.videoQualityList.setVisibility(View.GONE);
            }
        });
        valueAnimator.start();
    }

    public void showQualityList() {
        ViewGroup.LayoutParams params = downloadBinding.videoQualityList.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        downloadBinding.videoQualityList.requestLayout();
        downloadBinding.videoQualityList.setVisibility(View.VISIBLE);
    }


    /*
     * Hàm này cho phép tải lại bằng cách nhấn vào nút tải lại bên trái link edit text
     */
    void recallEnable(String url) {

        if(isViewsDestroyed) return;

        leftEditorIcon.setOnClickListener((v) -> {
            isRecallEnabled = true;
            startCallApi(url);
        });
    }

    /*
     * Hàm này ngăn chặn người dùng bấm vào nút paste, thường dùng trong khi request api
     */
    void unEnableViews() {

        if(isViewsDestroyed) return;

        paste.setClickable(false);
        paste.setAlpha(0.6f);
    }

    /*
     * Hàm này cho phép người dùng bấm vào nút paste
     */
    void enableViews() {

        if(isViewsDestroyed) return;

        paste.setClickable(true);
        paste.setAlpha(1f);
    }

    /*
     * Hàm này sẽ hiển thị trang thái loading của các views
     */
    void showLoadingViews() {

        if(isViewsDestroyed) return;

        bot.showLoadingStatus();
        statusDesc.setText(R.string.is_get_data_from_server);
        statusDesc.setSelected(true);
        loadBar.setVisibility(View.VISIBLE);
        leftEditorIcon.setVisibility(View.GONE);

    }

    /*
     * Hàm này sẽ hiển thị trang thái tải thành công của các views
     */
    void showSuccessViews() {

        if(isViewsDestroyed) return;

        bot.showSuccessStatus();
        statusDesc.setText(R.string.get_data_success);
        leftEditorIcon.setImageResource(R.drawable.success_icon);
        leftEditorIcon.setVisibility(View.VISIBLE);
        loadBar.setVisibility(View.GONE);
    }

    /*
     * Hàm này sẽ hiển thị trang thái tải thất bại của các views
     */
    void showFailedViews() {

        if(isViewsDestroyed) return;

        bot.showFailedStatus();
        leftEditorIcon.setImageResource(R.drawable.recall_download);
        leftEditorIcon.setVisibility(View.VISIBLE);
        loadBar.setVisibility(View.GONE);
    }

    void setEmptyViewsVisible (boolean visible) {
        if (visible) {
            downloadBinding.downloadStatusNoneItem.setVisibility(View.VISIBLE);
        } else {
            downloadBinding.downloadStatusNoneItem.setVisibility(View.GONE);
        }
    }

    public void showQualityList (@NonNull List<String> qualityList, VideoQualityListView.OnQualitySubmitListener callback) {
        showQualityList();
        downloadBinding.videoQualityList.setQualityList(qualityList);
        downloadBinding.videoQualityList.setOnQualitySubmit(callback);
    }

    @Override
    public void onDestroyView() {

        // Release resources
        if (adapter != null) adapter.release();

        bot.clearAnimation();
        // Release the adapter
        if (adapter != null) {
            adapter.release();
            adapter = null;
        }

        // Set views to null
        setAllRefsNull();

        shortsVideoDao = null;
        isViewsDestroyed = true;
        super.onDestroyView();
    }

    private void setAllRefsNull() {
        linkEdt = null;
        paste = null;
        leftEditorIcon = null;
        loadBar = null;
        urlList = null;
        requestInfoList = null;
        manager = null;
        downloadItemList = null;
        bot = null;
        downloadBinding = null;
    }

    EditText getLinkEdt() {
        return linkEdt;
    }

    TextView getPaste() {
        return paste;
    }

    TextView getStatusDesc() {
        return statusDesc;
    }

    ImageView getLeftEditorIcon() {
        return leftEditorIcon;
    }

    ProgressBar getLoadBar() {
        return loadBar;
    }

    FXDownloader getDownloader() {
        return manager;
    }

    List<RequestInfo> getRequestInfoList() {
        return requestInfoList;
    }

    Map<Integer, ModelDownload> getSaveModelMap() {
        return saveModelMap;
    }

    DownloadItemAdapter getAdapter() {
        return adapter;
    }

    RecyclerView getDownloadItemList() {
        return downloadItemList;
    }

    RecyclerView.LayoutManager getLayoutManager() {
        return layoutManager;
    }

    VirtualAssistantView getBot() {
        return bot;
    }

    ShortsVideoDao getShortsVideoDao() {
        return shortsVideoDao;
    }

    public boolean isViewsDestroyed() {
        return isViewsDestroyed;
    }

    boolean isRecallEnabled() {
        return isRecallEnabled;
    }

    boolean isBound() {
        return bound;
    }

    List<String> getUrlList() {
        return urlList;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}