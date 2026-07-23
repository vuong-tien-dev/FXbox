package com.vtstudio.fxbox.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vtstudio.fxbox.activity.FxBaseActivity;
import com.vtstudio.fxbox.activity.Main;
import com.vtstudio.fxbox.adapters.ShortsListAdapter;
import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.database.dao.MediaSegmentDao;
import com.vtstudio.fxbox.database.dao.ShortsDetailsDao;
import com.vtstudio.fxbox.database.dao.YTDetailsDao;
import com.vtstudio.fxbox.databinding.CannotConnectLayoutBinding;
import com.vtstudio.fxbox.databinding.FragmentShortsBinding;
import com.vtstudio.fxbox.databinding.LoadingLayoutBinding;
import com.vtstudio.fxbox.fxviews.progressbar.FxExoSeekBar;
import com.vtstudio.fxbox.fxviews.listview.FxRecyclerView;

import com.vtstudio.fxbox.listeners.OnMediaNotificationPlaybackChanged;
import com.vtstudio.fxbox.listeners.OnServiceConnectionListener;
import com.vtstudio.fxbox.listeners.PipModeListener;
import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.models.Media;
import com.vtstudio.fxbox.media.models.MediaSegment;
import com.vtstudio.fxbox.media.models.tiktok.Playlist;
import com.vtstudio.fxbox.media.models.tiktok.ShortsDetails;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.media.models.youtube.YTDetails;
import com.vtstudio.fxbox.media.player.FxPlayer;
import com.vtstudio.fxbox.media.player.MediaRequest;
import com.vtstudio.fxbox.media.player.OnQueryMediaListener;
import com.vtstudio.fxbox.media.utils.ModelUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.random.Random;

public class Shorts extends FxBaseFragment {

    public static final int BASE_MEDIA_REQUEST_ID = 50;
    public static final String SOURCE_TYPE_KEY = "source_type";
    public static final int TYPE_VIDEO_FOR_YOU = 0;
    public static final int TYPE_VIDEO_FROM_CLOUD = 1;
    private FragmentShortsBinding shortsBinding;
    private FxRecyclerView shortsListView;
    private FxPlayer fxPlayer;
    private static final int requestCode = 200;
    private int bottomMarginNav;
    private ShortsListAdapter shortsListAdapter;
    private List<FxMediaVideo> shortsList;
    private OnMediaNotificationPlaybackChanged onMediaNotificationPlaybackChanged;
    private boolean FLAG_PLAYER_SERVICE_BOUND = false; // Biến kiểm tra xem dịch vụ đã được kết nối hay chưa
    private boolean isStop;
    private boolean playControl = true;
    private FxExoSeekBar exoBar;
    private TextView playingTimeText;
    private boolean initializedPlayer = false;
    private boolean isShouldPrepareAgain = false;
    private int sourceType = 0;
    private int MEDIA_REQUEST_ID;
    private View loadingView;
    private boolean isEnterVideoWindowMode;
    private boolean isLoadMore;
    private boolean canQueryMore;
    private boolean shouldScrollToNext;
    private MediaRequest request;

    public Shorts() {
        // Required empty public constructor
    }

    public static Shorts newInstance(int sourceType) {
        Bundle args = new Bundle();
        args.putInt(SOURCE_TYPE_KEY, sourceType);
        Shorts shorts = new Shorts();
        shorts.setArguments(args);
        return shorts;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Fragment", "shorts is fxOnCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("Fragment", "shorts is onCreateView");

        Bundle bundle = getArguments();

        if (bundle != null) {
            sourceType = bundle.getInt(SOURCE_TYPE_KEY);
            MEDIA_REQUEST_ID = BASE_MEDIA_REQUEST_ID + sourceType;
        }

        // Bind view
        shortsBinding = FragmentShortsBinding.inflate(inflater, container, false);

        // allways callthisis funcion first - luôn gọi hàm này trước
        initViews();

        return shortsBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LoadingLayoutBinding loadingLayoutBinding = LoadingLayoutBinding.inflate(getLayoutInflater());
        loadingView = loadingLayoutBinding.getRoot();
        shortsBinding.getRoot().addView(loadingView);
    }

    private synchronized void initializePlayer(FragmentActivity activity, FxBaseActivity baseActivity) {

        if (initializedPlayer) return;

        fxPlayer = baseActivity.getFxPlayer();
        init();
        FLAG_PLAYER_SERVICE_BOUND = true;

        if (baseActivity.isPipSupported()) {
            baseActivity.addOnEnterPipMode(new PipModeListener() {
                @Override
                public void onStateChange(boolean isEnter) {
                    if (shortsListAdapter != null) {
                        shortsListAdapter.setInPipMode(isEnter);
                        if (!isEnter) {
                            shortsListAdapter.refreshPlayerView();
                        }
                    }
                }
            });
        }

        if (activity instanceof Main) {
            bottomMarginNav = ((Main) activity).getBottomMargin();
            exoBar = ((Main) activity).getExoSeekBar();

            initLayout();
            setUpExoBar();
            shortsListView.setAdapter(shortsListAdapter);
            shortsListAdapter.setRecyclerView(shortsListView);
            ((Main) activity).showSeekBarIfCan();
        }

        if (loadingView != null) {
            shortsBinding.getRoot().removeView(loadingView);
            loadingView = null;
        }

        initializedPlayer = true;
    }

    private void initLayout() {
        shortsListAdapter.setBottomMargin(bottomMarginNav);
        FrameLayout.LayoutParams timeTextParams = (FrameLayout.LayoutParams) shortsBinding.shortsCurrentPlayingPositionTv.getLayoutParams();
        timeTextParams.bottomMargin = exoBar.getHeight();
    }

    private void setUpExoBar() {
        if (exoBar != null && fxPlayer != null) {
            exoBar.setUpWithExoPlayer(fxPlayer.getPlayer(), 500);
            initTimeTextEvent();
        }
    }

    private void initTimeTextEvent() {
        exoBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (playingTimeText != null)
                    playingTimeText.setText(new SimpleDateFormat("mm:ss").format(new Date(progress)) + "  /  " + new SimpleDateFormat("mm:ss").format(new Date(seekBar.getMax())));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (shortsListAdapter != null) shortsListAdapter.setContentVisibilityEffect(false);
                if (playingTimeText != null) playingTimeText.setVisibility(View.VISIBLE);
                if (exoBar != null) exoBar.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (shortsListAdapter != null) shortsListAdapter.setContentVisibilityEffect(true);
                if (playingTimeText != null) playingTimeText.setVisibility(View.GONE);
                if (exoBar != null) exoBar.resume();
            }
        });
    }

    private void stopSeekBar() {
        if (exoBar != null) {
            Log.d("FxExoSeekBar", "stopSeekBar");
            exoBar.pause();
            exoBar.setOnSeekBarChangeListener(null);
        }
    }

    private void resumeSeekBar() {
        if (exoBar != null) {
            initTimeTextEvent();
            if (request != null) {
                if (request.getId() != MEDIA_REQUEST_ID) {
                    exoBar.unEnable();
                    return;
                }
            }
            exoBar.resume();
            showOrHideExoBar();
        }

    }

    private void showOrHideExoBar() {
        Activity activity = getActivity();
        if (activity != null && shortsList != null && !shortsList.isEmpty()) {
            ((Main) activity).showSeekBarIfCan();
        } else {
            if (exoBar != null) {
                exoBar.setVisibility(View.GONE);
            }
        }
    }

    private void initViews() {

        //
        playingTimeText = shortsBinding.shortsCurrentPlayingPositionTv;

        // khởi tạo recyler, adapter và layoutManager
        shortsListView = shortsBinding.shortsList;
        shortsListAdapter = new ShortsListAdapter(getActivity());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        shortsListView.setLayoutManager(layoutManager);


        // add snapHelper
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(shortsListView);

        // cấu hinh onscroll
        shortsListView.setOnSnapVideoListener((holder, videoIndex) -> {

            if (fxPlayer == null) return false;

            shortsListView.cancelDisableNextScrollBy();

            // get item holder
            ShortsListAdapter.ShortsItem shortsItem = (ShortsListAdapter.ShortsItem) holder;

            // hủy player view trước đó
            shortsListAdapter.onStop();

            // phát video mới
            fxPlayer.prepare(videoIndex, MEDIA_REQUEST_ID, isShouldPrepareAgain, false);

            // Play the video at the calculated index
            shortsListAdapter.onPlay(shortsItem, videoIndex, false);

            if (!fxPlayer.isPlaying() && !playControl) {
                shortsListAdapter.onPause();
                playControl = true;
            } else  {
                fxPlayer.play();
            }

            if (!shortsListAdapter.getShortsDataList().get(videoIndex).isCanPlayLimited()) {
                fxPlayer.pause();
            }

            return true;
        });


        //
        FragmentActivity activity = getActivity();
        if (activity instanceof Main) {
            exoBar = ((Main) activity).getExoSeekBar();
        }


        if (exoBar == null) return;

        shortsListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (exoBar != null) exoBar.enable();
                    shortsListAdapter.onScrollIdle();
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    if (exoBar != null) exoBar.unEnable();
                    shortsListAdapter.onScrolling();
                }
            }
        });
    }

    // this method will being called when bound service
    private void init() {

        if (sourceType == TYPE_VIDEO_FROM_CLOUD) {
            ViewGroup rootView = shortsBinding.getRoot();
            CannotConnectLayoutBinding binding = CannotConnectLayoutBinding.inflate(getLayoutInflater());
            rootView.removeAllViews();
            rootView.addView(binding.getRoot());
            return;
        }

        // kiểm tra empty không
        if (fxPlayer == null) return;
        request = fxPlayer.getRequestById(MEDIA_REQUEST_ID);
        MediaRequest currentRequest = fxPlayer.getCurrentRequest();
        isShouldPrepareAgain = currentRequest != null && currentRequest.getId() != MEDIA_REQUEST_ID;
        if (request == null) {
            request = new MediaRequest(MEDIA_REQUEST_ID);
            shortsList = new ArrayList<>();
            FxRoomDB database = FxRoomDB.get(getActivity());
            ShortsDetailsDao detailsDao = database.shortsDetailsDao();
            YTDetailsDao ytDetailsDao = database.ytvDetailsDao();
            MediaSegmentDao mediaSegmentDao = database.mediaSegmentDao();
            List<ShortsDetails> detailsList = detailsDao.getWithRandomLimit(10);

            if (detailsList == null) return;

            List<ShortsVideo> shortsVideos = ModelUtils.fromDetailsList(detailsList);

            List<YTDetails> ytDetails = null;

            if (Random.Default.nextInt(5) < 2 || shortsVideos.isEmpty()) {
                ytDetails = ytDetailsDao.generateShorts(shortsVideos.isEmpty() ? 10 : Random.Default.nextInt(2), new ArrayList<>());
            }

            List<FxMediaVideo> mediaList = new ArrayList<>(shortsVideos);
            if (ytDetails != null && !ytDetails.isEmpty()) {
                mediaList.addAll(ModelUtils.fromYTDetailsList(ytDetails));
                Collections.shuffle(mediaList);
            }

            // binding video segment
            for(FxMediaVideo v : mediaList) {
                if(v.getMediaSegmentId() > 0) {
                    MediaSegment segment = mediaSegmentDao.getById(v.getMediaSegmentId());
                    if(segment != null) {
                        v.setCurrentSegment(segment);
                    }
                }
            }

            shortsList.addAll(mediaList);
            request.setMediaList(new ArrayList<>(mediaList));
            request.setQueryMore(true);
            fxPlayer.addRequest(request);

            Playlist playlist = database.playlistDao().getPlaylistById(Playlist.LIMITED_PLAYLIST);
            List<String> playlistId = playlist.getVideoIdList();

//            for(FxMediaVideo video : shortsList) {
//                if(video instanceof ShortsVideo && playlistId.contains(String.valueOf(video.getFxId()))) {
//                    Log.d("TiktokPackage", "shorts is limited " + ((ShortsVideo) video).getDescription());
//                }
//            }
        } else {
            List<Media> mediaList = request.getMediaList();
            shortsList = new ArrayList<>();
            for (Media media : mediaList) {
                if (media instanceof FxMediaVideo) {
                    shortsList.add((FxMediaVideo) media);
                }
            }
            fxPlayer.cancelRemoved(MEDIA_REQUEST_ID);
            playControl = request.isPlaying() && fxPlayer.getCurrentRequestId() == MEDIA_REQUEST_ID;
            shortsListView.scrollToPosition(request.getCurrentPlayingIndex());
        }

        request.setOnQueryMediaListener(null);

        // set dữ liệu từ service
        canQueryMore = request.canQueryMore();
        shortsListAdapter.setCanLoadMore(request.canQueryMore());
        if (request.canQueryMore()) {
            shortsListAdapter.setOnLoadMoreListener((holderAttached, fromAdapter) -> {
                if (holderAttached) {
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.execute(Shorts.this::loadMore);
                    executor.shutdown();
                } else {
                    shouldScrollToNext = false;
                }
            });
        }
        shortsListAdapter.setShortsDataList(shortsList);
        shortsListAdapter.setFxPlayer(fxPlayer);
        shortsListAdapter.setShouldRemoveWhenLimit(sourceType == TYPE_VIDEO_FOR_YOU);
        Log.d("Shorts", "shorts have " + shortsListAdapter.getItemCount() + " media");
        // listener

        onMediaNotificationPlaybackChanged = new OnMediaNotificationPlaybackChanged() {
            @Override
            public void onPlay(int pos, boolean fromNotification) {
                if (fromNotification && shortsListView != null) {
                    if (Math.abs(shortsListView.getCurrentSnapIndex() - pos) < 4) {
                        shortsListView.smoothScrollToPosition(pos);
                    } else {
                        shortsListView.scrollToPosition(pos);
                    }
                }
            }

            @Override
            public void onResume() {
                shortsListAdapter.onResume();
            }

            @Override
            public void onPause() {
                shortsListAdapter.onPause();
            }
        };

        // đăng ký trình lắng nghe để đồng bộ với fxPlayer
        fxPlayer.addListener(onMediaNotificationPlaybackChanged);
    }

    private void loadMore() {
        shortsListView.disableNextScrollBy();
        if (isLoadMore) return;
        isLoadMore = true;
        shouldScrollToNext = true;
        Log.d("Shorts", "isLoadingMore");
        List<String> fxIds = ModelUtils.fxIdsFromFxMediaVideo(new ArrayList<>(shortsListAdapter.getShortsDataList()));
        int currentItemCount = shortsListAdapter.getItemCount();
        ShortsDetailsDao shortsDetailsDao = FxRoomDB.get(getActivity()).shortsDetailsDao();
        YTDetailsDao ytDetailsDao = FxRoomDB.get(getActivity()).ytvDetailsDao();
        MediaSegmentDao mediaSegmentDao =  FxRoomDB.get(getActivity()).mediaSegmentDao();
        List<ShortsVideo> videos = new ArrayList<>(ModelUtils.fromDetailsList(shortsDetailsDao.getRemainingRecords(fxIds, 20)));

        List<YTDetails> ytDetails = null;

        if (Random.Default.nextInt(5) < 2 || videos.isEmpty()) {
            ytDetails = ytDetailsDao.generateShorts(videos.isEmpty() ? 10 : Random.Default.nextInt(4), fxIds);
        }

        List<FxMediaVideo> mediaList = new ArrayList<>(videos);

        if (ytDetails != null && !ytDetails.isEmpty()) {
            mediaList.addAll(ModelUtils.fromYTDetailsList(ytDetails));
            Collections.shuffle(mediaList);
        }

        // binding video segment
        for(FxMediaVideo v : mediaList) {
            if(v.getMediaSegmentId() > 0) {
                MediaSegment segment = mediaSegmentDao.getById(v.getMediaSegmentId());
                if(segment != null) {
                    v.setCurrentSegment(segment);
                }
            }
        }

        boolean hasVideo = !mediaList.isEmpty();
        if (hasVideo) {
            shortsBinding.shortsList.postDelayed(() -> {
                canQueryMore = true;
                shortsListAdapter.getShortsDataList().addAll(mediaList);
                request.getMediaList().addAll(mediaList);
                shortsListAdapter.notifyItemRangeChanged(currentItemCount - 1, shortsListAdapter.getItemCount() - currentItemCount);
                if (shouldScrollToNext)
                    shortsListView.post(() -> shortsListView.smoothScrollToPosition(currentItemCount - 1));
            }, 200);
        } else {
            shortsListView.postDelayed(() -> {
                canQueryMore = false;
                request.setQueryMore(false);
                shortsListAdapter.setCanLoadMore(false);
                shortsListAdapter.notifyItemRemoved(currentItemCount - 1); // xóa load more holder
            }, 500);
        }
        isLoadMore = false;
    }


    private void requestPlayer() {

        if (shortsListView == null || shortsListAdapter == null || shortsList == null) return;

        MediaRequest request = fxPlayer.getRequestById(MEDIA_REQUEST_ID);

        if (request == null) return;

        isShouldPrepareAgain = fxPlayer.getCurrentRequestId() != MEDIA_REQUEST_ID;

        if (isShouldPrepareAgain || isEnterVideoWindowMode) {
            shortsListAdapter.refreshPlayerView();
        }

        boolean isPlaying = request.isPlaying();
        boolean isPlayingIndexNotChanged = request.getCurrentPlayingIndex() == shortsListAdapter.getCurrentPlayingIndex();


        if (isShouldPrepareAgain) {
            fxPlayer.prepare(request.getCurrentPlayingIndex(), MEDIA_REQUEST_ID, true, false);
            fxPlayer.play();
        }

        if (isPlayingIndexNotChanged) {
            if (isPlaying) {
                // Nếu dịch vụ đang phát và giao diện đang ở trạng thái paused thì làm mới
                if (!shortsListAdapter.isPlaying()) shortsListAdapter.onResume();
            } else {
                // Nếu dịch vụ đang dừng phát và giao diện đang ở trạng thái resume thì làm mới
                if (shortsListAdapter.isPlaying()) shortsListAdapter.onPause();
            }
        } else {
            // Nếu vị trí phát thay đổi kể từ lúc thoát và ứng dụng chạy nền thì cấu hình lại
            playControl = request.isPlaying(); // chặn phát khi cuộn

            // cuộn xuống và kiểm tra nếu được phép phát thì phát không thì cập nhật giao diện dừng (trong onSnap )
            shortsListView.scrollToPosition(request.getCurrentPlayingIndex());
        }

        fxPlayer.addListener(onMediaNotificationPlaybackChanged);

    }

    private boolean isStopByUser(MediaRequest request) {
        return (!request.isPlaying() && request.getId() == MEDIA_REQUEST_ID);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {

        if (request != null) {
            if (request.isHasBeenLoadedMore() && request.getMediaList() != null && shortsBinding != null && shortsListAdapter != null) {
                shortsBinding.shortsList.post(() -> {
                    shortsListAdapter.setShortsDataList(ModelUtils.filterFrom(request.getMediaList()));
                    shortsListAdapter.notifyDataSetChanged();
                    if (request.getCurrentPlayingIndex() > 0)
                        shortsBinding.shortsList.post(() -> shortsBinding.shortsList.scrollToPosition(request.getCurrentPlayingIndex()));
                    request.hasBeenLoadedMoreSolved();
                });
            }
            request.setOnQueryMediaListener(null);
        }

        if (isStop) {
            requestPlayer();
            isStop = false;
        }

        resumeSeekBar();
        super.onResume();

        if (initializedPlayer) return;

        FragmentActivity activity = getActivity();
        if (activity instanceof FxBaseActivity) {
            FxBaseActivity baseActivity = (FxBaseActivity) activity;
            if (!baseActivity.isFxPlayerBound()) {
                baseActivity.boundFxPlayer(new OnServiceConnectionListener() {
                    @Override
                    public void onServiceConnected() {
                        initializePlayer(activity, baseActivity);
                    }

                    @Override
                    public void onServiceDisconnected() {
                    }
                });
            } else if (!initializedPlayer) {
                initializePlayer(activity, baseActivity);
            }
        }
    }

    @Override
    public void onStop() {

        if (fxPlayer != null && shortsListView != null) {
            fxPlayer.removeListener(onMediaNotificationPlaybackChanged);
            isEnterVideoWindowMode = fxPlayer.isVideoWindowModeEnabled();
            isStop = true;
        }

        Log.d("Shorts", "CanQueryMore: " + canQueryMore);
        if (request != null && canQueryMore) {
            Log.d("Shorts", "OnQueryMore Setted");
            request.setOnQueryMediaListener(new OnQueryMediaListener() {
                @Override
                public List<Media> onQueryMedia(@NonNull FxRoomDB db, @NonNull MediaRequest request, @Nullable String playlistId, int currentSize) {
                    Log.d("Shorts", "onQueryMedia called");
                    if (request.getMediaList() != null && request.getMediaList().size() > 0) {
                        List<String> awemeIds = ModelUtils.awemeIdsFromFxMediaVideo(ModelUtils.filterFrom(request.getMediaList()));
                        ShortsDetailsDao shortsDetailsDao = db.shortsDetailsDao();

                        List<Media> appendMedia = new ArrayList<>(ModelUtils.fromDetailsList(shortsDetailsDao.getRemainingRecords(awemeIds, 20)));

                        boolean hasVideo = !appendMedia.isEmpty();
                        if (hasVideo) {
                            return appendMedia;
                        }
                    }
                    return null;
                }
            });
        }

        if (shortsListAdapter != null)
            shortsListAdapter.onPause(); // đóng hiệu animation và đưa giao diện vào trạng thái pause tạm thơi, sẽ khôi phục lại trên fxOnResume

        stopSeekBar();
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            Glide.get(getActivity()).clearMemory();
        } catch (Exception ignored) {

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onDestroyView() {
        Log.d("Fragment", "shorts is onDestroyView");

        if (shortsListAdapter != null) {
            shortsListAdapter.release();
        }


        shortsListView.setOnSnapVideoListener(null);
        shortsListView.setAdapter(null);

        exoBar = null;
        playingTimeText = null;
        shortsBinding = null;
        shortsListView = null;

        if (request != null) {
            request.setOnQueryMediaListener(null);
            request = null;
        }

        if (FLAG_PLAYER_SERVICE_BOUND) {
            if (fxPlayer != null) {
                fxPlayer.removeListener(onMediaNotificationPlaybackChanged);
                fxPlayer.removeRequest(MEDIA_REQUEST_ID);
            }
            fxPlayer = null;
            shortsList = null;
            onMediaNotificationPlaybackChanged = null;
            FLAG_PLAYER_SERVICE_BOUND = false;
            initializedPlayer = false;
        }

        super.onDestroyView();
    }
}