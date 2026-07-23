package com.vtstudio.fxbox.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.adapters.ShortsListAdapter;
import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.database.dao.FxMediaVideoDao;
import com.vtstudio.fxbox.database.dao.MediaSegmentDao;
import com.vtstudio.fxbox.database.dao.ShortsDetailsDao;
import com.vtstudio.fxbox.database.dao.YTDetailsDao;
import com.vtstudio.fxbox.databinding.ActivityPlayVideoBinding;
import com.vtstudio.fxbox.databinding.EmptyListLayoutBinding;
import com.vtstudio.fxbox.fxviews.progressbar.FxExoSeekBar;
import com.vtstudio.fxbox.fxviews.listview.FxRecyclerView;
import com.vtstudio.fxbox.listeners.OnMediaNotificationPlaybackChanged;
import com.vtstudio.fxbox.listeners.OnServiceConnectionListener;
import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.models.Media;
import com.vtstudio.fxbox.media.models.MediaSegment;
import com.vtstudio.fxbox.media.models.tiktok.Playlist;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.media.player.FxPlayer;
import com.vtstudio.fxbox.media.player.MediaRequest;
import com.vtstudio.fxbox.media.player.OnQueryMediaListener;
import com.vtstudio.fxbox.media.utils.ModelUtils;
import com.vtstudio.fxbox.utils.ListUtils;
import com.vtstudio.fxbox.utils.ParserUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayVideo extends FxBaseActivity {

    // keys data
    public static final String DATA_RETRIEVAL_METHOD = "data_retrieval_method";
    public static final String METHOD_DATA_FROM_FLAG_PLAYER_SERVICE = "data_from_flag_player_service";
    public static final String METHOD_DATA_FROM_REQUEST = "data_from_request";
    public static final String KEY_NAME_LIST = "name_list";
    public static final String KEY_PLAYING_INDEX = "playing_index";
    public static final String KEY_ADAPTER_ACTION_LIMIT_CONTROL = "adapter_action_limit";
    public static final String KEY_PLAYLIST_ID_TO_LOAD_MORE = "playlist_id_to_load_more";
    private static int PLAYING_REQUEST_ID = 1977;
    //flags
    private boolean FAG_INIT_VIEWS_LAYOUT;
    // bind var
    private ActivityPlayVideoBinding playVideoBinding;

    // views refs
    private ImageView back;
    private TextView nameList;
    private TextView fractions;
    private TextView playingTimeText;
    private FxRecyclerView videosView;
    private FxPlayer fxPlayer;
    private FxExoSeekBar exoSeekBar;
    // ob
    private ShortsListAdapter videosAdapter;
    // data
    private List<FxMediaVideo> videos;
    // local
    private int currentPlayingIndex = -1;
    private int instanceRequestId;
    private boolean isStop;
    private boolean canQueryMore;
    private boolean isLoadMore;
    private boolean shouldScrollToNext;
    private OnMediaNotificationPlaybackChanged onMediaNotificationPlaybackChanged;
    //    private boolean hide;
    private boolean playControl = true;
    private boolean isShouldPrepareAgain;
    private boolean isEnterVideoWindowMode;
    private boolean shouldIgnoredFirst = false;
    private Playlist mPlaylist;
    private ShortsDetailsDao mShortsDetailsDao;
    private FxMediaVideoDao mFxVideoDao;
    private YTDetailsDao mYTDetailsDao;
    private MediaRequest request;
    //private boolean destroyed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //destroyed = false;
        instanceRequestId = PLAYING_REQUEST_ID;
        Log.d("PlayVideo", "fxOnCreate with id: " + instanceRequestId);

        // bind layout
        playVideoBinding = ActivityPlayVideoBinding.inflate(getLayoutInflater());

        // setContentViews
        setContentView(playVideoBinding.getRoot());

        //init Views
        initViews();

        // init Data
        initData();
    }

    private void initData() {
        String getDataMethod = getIntent().getStringExtra(DATA_RETRIEVAL_METHOD);
        if (getDataMethod == null || Objects.equals(getDataMethod, METHOD_DATA_FROM_REQUEST)) {
            boundFxPlayerAndInitData();
        }
    }

    private void boundFxPlayerAndInitData() {
        boundFxPlayer(new OnServiceConnectionListener() {
            @Override
            public void onServiceConnected() {
                fxPlayer = getFxPlayer();
                fxPlayer.startBackground();
                getData();
                setData();
            }

            @Override
            public void onServiceDisconnected() {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void setData() {

        // bind exo bar
        exoSeekBar.setUpWithExoPlayer(fxPlayer.getPlayer(), 500);

        // khởi tạo
        videosAdapter = new ShortsListAdapter(this);

        // set data for adapter
        videosAdapter.setShortsDataList(videos);
        videosAdapter.setFxPlayer(fxPlayer);
        videosAdapter.setCanLoadMore(canQueryMore);
        videosAdapter.setOnEmptyListener(() -> {
            fractions.setText(" • " + getText(R.string.empty));
            playVideoBinding.acPlayVideoListContainer.addView(EmptyListLayoutBinding.inflate(getLayoutInflater()).getRoot());
        });

        if (canQueryMore) {
            Log.d("PlayVideo", "Can load more");
            videosAdapter.setOnLoadMoreListener((holderAttached, fromAdapter) -> {
                if (holderAttached) {
                    shouldScrollToNext = !fromAdapter;
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.execute(PlayVideo.this::loadMore);
                    executor.shutdown();
                } else {
                    shouldScrollToNext = false;
                }
            });
        }

        onMediaNotificationPlaybackChanged = buildPlaybackChangedListener();

        // đăng ký trình lắng nghe để đồng bộ với playerService
        fxPlayer.addListener(onMediaNotificationPlaybackChanged);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        videosView.setLayoutManager(layoutManager);
        videosView.setAdapter(videosAdapter);
        videosAdapter.setRecyclerView(videosView);
        videosAdapter.setShouldRemoveWhenLimit(getIntent().getBooleanExtra(KEY_ADAPTER_ACTION_LIMIT_CONTROL, false));
        videosAdapter.setPlayingPlaylist(mPlaylist);
        //videosView.setNestedScrollingEnabled(true);

        // add snapHelper
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(videosView);

        // cấu hinh onscroll
        videosView.setOnSnapVideoListener((holder, videoIndex) -> {
            Log.d("PlayVideo", "snap video at " + videoIndex);
            /*
            nếu lần đầu tiên phát với request id instance này thì bỏ qua để ngăn chặn việc phát lại video từ đầu khi
            người dùng remove task và vào lại, khi đó fxOnCreate của PlayVideo được gọi lại vì thể, onSnap cũng được gọi lại.
            Còn khi người dùng thoát khỏi Playvideo bằng backpress và nhấn vào item video khác trên playlist hoặc một
            danh sách bất kỳ, một instanceRequestId mới và request mới được tạo ra thông qua newRequestId, vì vậy thuộc tính
            isFirstPlayed sẽ là false cho đến khi phát một item bất khi trong list video của instanceRequest hiện tại
            do khi activity mới khởi tạo lại, hàm này luôn được gọi với videoIndex là 0 khiến playing index bị thay đổi trước khi kiếm tra,
            nhưng vì shouldIgnoredFirst sẽ bỏ qua lần đầu gọi này nên khi đang phát index 0, ta remove task và vào lại thì nó bị
            vấn đề là playerview không được vẽ do chưa gọi adapter.onPlay để notify vì vậy videoIndex != 0 sẽ khắc phục điều này.
            */
            videosView.cancelDisableNextScrollBy();

            if (shouldIgnoredFirst) {
                Log.d("PlayVideoDebug", "onSnap has been ignored and return false");
                shouldIgnoredFirst = false;
                if (videoIndex == 0 && videosAdapter != null) {
                    videosAdapter.onPlay((ShortsListAdapter.ShortsItem) holder, 0, false);
                }

                return false;
            }

            // get item holder
            ShortsListAdapter.ShortsItem shortsItem = (ShortsListAdapter.ShortsItem) holder;

            // hủy player view trước đó
            videosAdapter.onStop();

            //videosView.setScrollableText(shortsItem.itemView.findViewById(R.id.shorts_description_tv));

            // phát video mới
            Log.d("PlayVideoDebug", "Play new video position at " + videoIndex);
            fxPlayer.prepare(videoIndex, instanceRequestId, isShouldPrepareAgain, false);

            // Play the video at the calculated index
            videosAdapter.onPlay(shortsItem, videoIndex, false);

            // cập nhật bottom
            updateFractions();

//            if (!fxPlayer.isPlaying() && !firstPlayingState) {
//                videosAdapter.fxOnPause();
//                firstPlayingState = true;
//            }
//            else {
//                firstPlayingState = true;
//                fxPlayer.play();
//            }

            if (playControl) {
                fxPlayer.play();
            } else {
                videosAdapter.onPause();
            }

            if (!videosAdapter.getShortsDataList().get(videoIndex).isCanPlayLimited()) {
                Log.d("PlayVideoDebug", "stop caused limited video " + videoIndex);
                fxPlayer.pause();
            }

            return true;
        });

        videosView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (exoSeekBar != null) exoSeekBar.enable();
                    videosAdapter.onScrollIdle();
                    Log.d("state", "SCROLL_STATE_IDLE");
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    if (exoSeekBar != null) exoSeekBar.unEnable();
                    videosAdapter.onScrolling();
                    Log.d("state", "SCROLL_STATE_DRAGGING");
                } else {
                    Log.d("state", "fling");
                }
            }
        });

        // play video và scroll to target position
        if (currentPlayingIndex >= 0) {
            videosView.post(() -> {
                isShouldPrepareAgain = false;
                //fxPlayer.prepare(currentPlayingIndex, instanceRequestId, false, false);
                //fxPlayer.play();
                videosView.scrollToPosition(currentPlayingIndex);
            });
        }

    }

    private OnMediaNotificationPlaybackChanged buildPlaybackChangedListener() {
        return new OnMediaNotificationPlaybackChanged() {
            @Override
            public void onPlay(int pos, boolean fromNotification) {
                playControl = true;
                if (fromNotification && videosView != null) {
                    if (Math.abs(videosView.getCurrentSnapIndex() - pos) < 4) {
                        videosView.smoothScrollToPosition(pos);
                    } else {
                        videosView.scrollToPosition(pos);
                    }
                }
            }

            @Override
            public void onResume() {
                videosAdapter.onResume();
            }

            @Override
            public void onPause() {
                if (videosAdapter != null) {
                    videosAdapter.onPause();
                }
            }
        };
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void getData() {

        request = fxPlayer.getRequestById(instanceRequestId);
        MediaRequest currentRequest = fxPlayer.getCurrentRequest();
        isShouldPrepareAgain = currentRequest != null && currentRequest.getId() != instanceRequestId;

        if (request == null) return;

        fxPlayer.cancelRemoved(request.getId());
        canQueryMore = request.canQueryMore();
        request.setOnQueryMediaListener(null);

        if (canQueryMore) {
            Log.d("PlayVideo", "Can query more video");
        }

        List<Media> mediaList = fxPlayer.getRequestById(instanceRequestId).getMediaList();

        if (mediaList == null || mediaList.size() < 1) return;

        MediaSegmentDao mediaSegmentDao =  FxRoomDB.get(this).mediaSegmentDao();

        videos = new ArrayList<>();
        for (Media media : mediaList) {
            if (media instanceof ShortsVideo) {
                videos.add((ShortsVideo) media);
            } else if (media instanceof FxMediaVideo) {
                videos.add((FxMediaVideo) media);
            }

            // binding video segment
            if(media instanceof FxMediaVideo) {
                final FxMediaVideo fxMediaVideo = (FxMediaVideo) media;
                if(fxMediaVideo.getMediaSegmentId() > 0) {
                    MediaSegment segment = mediaSegmentDao.getById(fxMediaVideo.getMediaSegmentId());
                    if(segment != null) {
                        fxMediaVideo.setCurrentSegment(segment);
                    }
                }
            }
        }

        final String requestPlaylistId = request.getPlaylistId();
        final String playlistId = requestPlaylistId != null ? requestPlaylistId : getIntent().getStringExtra(KEY_PLAYLIST_ID_TO_LOAD_MORE);

        if (canQueryMore && playlistId != null) {
            mPlaylist = FxRoomDB.get(this).playlistDao().getPlaylistById(playlistId);
        }

        if (mPlaylist != null) {
            FxRoomDB db = FxRoomDB.get(this);
            mShortsDetailsDao = db.shortsDetailsDao();
            mFxVideoDao = db.fxMediaVideoDao();
            mYTDetailsDao = db.ytvDetailsDao();
        }

        String name = getIntent().getStringExtra(KEY_NAME_LIST);
        nameList.setText(name);
        currentPlayingIndex = request.getCurrentPlayingIndex();
        shouldIgnoredFirst = request.isFirstPlayed();
        String frac = " • " + (currentPlayingIndex + 1) + "/" + videos.size();
        fractions.setText(frac);

    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadMore() {
        videosView.disableNextScrollBy();
        if (isLoadMore || mPlaylist == null) return;
        isLoadMore = true;
        Log.d("PlaylistActivity", "isLoadingMore");
        List<Long> fxIdList = ParserUtils.fromList(mPlaylist.getVideoIdList());
        List<Long> currentFxIdList = ParserUtils.fromListMedia(videosAdapter.getShortsDataList());
        int currentItemCount = videosAdapter.getItemCount();
        List<FxMediaVideo> videos = new ArrayList<>();
        MediaSegmentDao mediaSegmentDao =  FxRoomDB.get(this).mediaSegmentDao();
        for (int i = currentItemCount - 1; i < Math.min(currentItemCount + 15, fxIdList.size()); ++i) {
            Long fxId = fxIdList.get(i);

            FxMediaVideo video = mFxVideoDao.getVideoByFxId(fxId);

            if (video == null) {
                video = ModelUtils.fromDetails(mShortsDetailsDao.getDetailsByFxId(fxId));
            }

            if(video == null) {
                video = ModelUtils.fromDetails(mYTDetailsDao.getDetailsByFxId(fxId));
            }

            if (video != null && !currentFxIdList.contains(video.getFxId())) {
                videos.add(video);
            }

            if (video == null) {
                ListUtils.removeAllOccurrences(mPlaylist.getVideoIdList(), String.valueOf(fxId));
                Log.d("PlaylistActivity", "Null fxId: " + fxId);
            } else {
                // binding video segment
                if(video.getMediaSegmentId() > 0) {
                    MediaSegment segment = mediaSegmentDao.getById(video.getMediaSegmentId());
                    if(segment != null) {
                        video.setCurrentSegment(segment);
                    }
                }
            }
        }

        boolean hasVideo = !videos.isEmpty();
        if (hasVideo) {
            playVideoBinding.acPlayVideoList.post(() -> {
                canQueryMore = true;
                videosAdapter.getShortsDataList().addAll(videos);
                request.getMediaList().addAll(videos);
                videosAdapter.notifyItemRangeChanged(currentItemCount - 1, videosAdapter.getItemCount() - currentItemCount);
                updateFractions();
                if (shouldScrollToNext)
                    videosView.post(() -> videosView.smoothScrollToPosition(currentItemCount - 1));
            });
        } else {
            playVideoBinding.acPlayVideoList.postDelayed(() -> {
                canQueryMore = false;
                request.setQueryMore(false);
                videosAdapter.setCanLoadMore(false);
                videosAdapter.notifyItemRemoved(currentItemCount - 1); // xóa load more holder
            }, 500);
        }
        isLoadMore = false;
    }

    private void initViews() {
        back = playVideoBinding.acPlayVideoBackIc;
        nameList = playVideoBinding.acPlayVideoNameList;
        fractions = playVideoBinding.acPlayVideoPercentTv;
        playingTimeText = playVideoBinding.acCurrentPlayingPositionTv;
        videosView = playVideoBinding.acPlayVideoList;
        exoSeekBar = playVideoBinding.acPlayVideoExoBar;

        back.setOnClickListener(v -> {
            finish();
        });
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // Khởi tạo bố cục giao diện nếu chưa được khởi tạo
        if (!FAG_INIT_VIEWS_LAYOUT) {
            initViewsLayout();
            FAG_INIT_VIEWS_LAYOUT = true;
        }
    }

    private void initViewsLayout() {
        // Thiết lập vị trí SeekBar cho phù hợp với FadingNavigationLayout
        FrameLayout.LayoutParams bottomParams = (FrameLayout.LayoutParams) playVideoBinding.acPlayVideoBottomSheetWrapper.getLayoutParams();

        FrameLayout.LayoutParams videoContainerParams = (FrameLayout.LayoutParams) playVideoBinding.acPlayVideoListContainer.getLayoutParams();

        FrameLayout.LayoutParams exoBarParams = (FrameLayout.LayoutParams) playVideoBinding.acPlayVideoExoBar.getLayoutParams();
        Log.d("PlayVideo", "bottomParams: " + bottomParams.height + ", exoParams: " + exoSeekBar.getHeight());
        exoBarParams.bottomMargin = bottomParams.height - ((exoSeekBar.getHeight() - exoSeekBar.getPaddingBottom() - exoSeekBar.getPaddingTop()) / 2);

        exoSeekBar.requestLayout();

        videoContainerParams.bottomMargin = bottomParams.height;

        FrameLayout.LayoutParams timeTextParams = (FrameLayout.LayoutParams) playVideoBinding.acCurrentPlayingPositionTv.getLayoutParams();
        timeTextParams.bottomMargin = bottomParams.height + exoSeekBar.getHeight();

        playVideoBinding.acPlayVideoListContainer.requestLayout();

        playVideoBinding.acPlayVideoNameList.setMaxWidth((int) (playVideoBinding.acPlayVideoBottomSheetLay.getWidth() * 0.6));
        playVideoBinding.acPlayVideoNameList.setSelected(true);
        playVideoBinding.acPlayVideoNameList.setFadingEdgeLength(0);
    }

    private void initTimeTextEvent() {
        exoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                playingTimeText.setText(new SimpleDateFormat("mm:ss").format(new Date(progress)) + "  /  " + new SimpleDateFormat("mm:ss").format(new Date(seekBar.getMax())));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                videosAdapter.setContentVisibilityEffect(false);
                playingTimeText.setVisibility(View.VISIBLE);
                exoSeekBar.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (fxPlayer != null) fxPlayer.seekTo(seekBar.getProgress());
                videosAdapter.setContentVisibilityEffect(true);
                playingTimeText.setVisibility(View.GONE);
                exoSeekBar.resume();
            }
        });
    }

    private void updateFractions () {
        if (videosAdapter != null && fractions != null) {
            if (videosAdapter.getShortsDataList().size() > 0) {
                String fractionText = " • " + (videosAdapter.getCurrentPlayingIndex() + 1) + "/" + videosAdapter.getShortsDataList().size();
                fractions.setText(fractionText);
            } else {
                fractions.setText(getText(R.string.empty));
            }
        }
    }

    private void stopSeekBar() {
        if (exoSeekBar != null) {
            exoSeekBar.pause();
            exoSeekBar.setOnSeekBarChangeListener(null);
        }
    }

    private void resumeSeekBar() {
        if (exoSeekBar != null) {
            initTimeTextEvent();
            exoSeekBar.resume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopSeekBar();
        isStop = true;
        if (fxPlayer != null) {
            fxPlayer.removeListener(onMediaNotificationPlaybackChanged);
            isEnterVideoWindowMode = fxPlayer.isVideoWindowModeEnabled();
        }

        if (request != null && canQueryMore) {
            request.setOnQueryMediaListener(new OnQueryMediaListener() {
                @Override
                public List<Media> onQueryMedia(@NonNull FxRoomDB db, @NonNull MediaRequest request, @Nullable String playlistId, int currentSize) {
                    if (playlistId != null) {
                        Playlist mPlaylist = db.playlistDao().getPlaylistById(playlistId);
                        if (mPlaylist != null) {
                            List<Long> fxIdList = ParserUtils.fromList(mPlaylist.getVideoIdList());
                            List<Media> appendMedia = new ArrayList<>();
                            ShortsDetailsDao detailsDao = db.shortsDetailsDao();
                            FxMediaVideoDao fxMediaVideoDao = db.fxMediaVideoDao();
                            for (int i = currentSize; i < Math.min(currentSize + 15, fxIdList.size()); ++i) {
                                Long fxId = fxIdList.get(i);
                                FxMediaVideo video = fxMediaVideoDao.getVideoByFxId(fxId);
                                if (video == null) {
                                    video = ModelUtils.fromDetails(detailsDao.getDetailsByFxId(fxId));
                                }
                                if (video != null) {
                                    appendMedia.add(video);
                                }
                            }

                            boolean hasVideo = !appendMedia.isEmpty();
                            if (hasVideo) {
                                return appendMedia;
                            }
                        }
                    }

                    return null;
                }
            });
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onRestart() {
        super.onRestart();
        if (request != null) {
            if (request.isHasBeenLoadedMore() && request.getMediaList() != null && playVideoBinding != null && videosAdapter != null) {
                playVideoBinding.acPlayVideoList.post(() -> {
                    videosAdapter.setShortsDataList(ModelUtils.filterFrom(request.getMediaList()));
                    videosAdapter.notifyDataSetChanged();
                    if (request.getCurrentPlayingIndex() > 0)
                        videosView.post(() -> videosView.scrollToPosition(request.getCurrentPlayingIndex()));
                    request.hasBeenLoadedMoreSolved();
                    updateFractions();
                });
            }
            request.setOnQueryMediaListener(null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isStop) {
            Log.d("PlayVideo", "refresh after stop");
            requestPlayer();
            isStop = false;
        }

        if (request != null) {
            request.setOnQueryMediaListener(null);
        }
        resumeSeekBar();
    }

    private boolean isStopByUser(MediaRequest request) {
        return (!request.isPlaying() && request.getId() == instanceRequestId);
    }

    private void requestPlayer() {

        if (fxPlayer == null || videos == null || videosView == null) return;

        MediaRequest request = fxPlayer.getRequestById(instanceRequestId);

        if (request == null) return;

        Log.d("PlayVideo", "id: " + instanceRequestId);

        isShouldPrepareAgain = fxPlayer.getCurrentRequestId() != instanceRequestId;

        if (isShouldPrepareAgain || isEnterVideoWindowMode) {
            videosAdapter.refreshPlayerView();
        }

        boolean isPlaying = request.isPlaying();
        boolean isPlayingIndexNotChanged = request.getCurrentPlayingIndex() == videosAdapter.getCurrentPlayingIndex();

        if (isPlayingIndexNotChanged) {
            Log.d("PlayVideo", "isPlayingIndexNotChanged so update");
            fxPlayer.prepare(request.getCurrentPlayingIndex(), instanceRequestId, isShouldPrepareAgain, false);
            if (isPlaying) {
                fxPlayer.play();
                Log.d("PlayVideo", "video is playing");
                // Nếu dịch vụ đang phát và giao diện đang ở trạng thái paused thì làm mới
                if (videosAdapter != null) videosAdapter.onResume();
            } else {
                Log.d("PlayVideo", "video is paused");
                // Nếu dịch vụ đang dừng phát và giao diện đang ở trạng thái resume thì làm mới
                if (videosAdapter != null) videosAdapter.onPause();
            }
        } else {
            Log.d("PlayVideo", "isPlayingIndexChanged so scroll");
            // Nếu vị trí phát thay đổi kể từ lúc thoát và ứng dụng chạy nền thì cấu hình lại
            playControl = request.isPlaying(); // chặn phát khi cuộn

            // cuộn xuống và kiểm tra nếu được phép phát thì phát không thì cập nhật giao diện dừng (trong onSnap )
            videosView.scrollToPosition(request.getCurrentPlayingIndex());
        }

        if (onMediaNotificationPlaybackChanged == null) {
            onMediaNotificationPlaybackChanged = buildPlaybackChangedListener();
        }

        fxPlayer.addListener(onMediaNotificationPlaybackChanged);
    }

    public static int newPlayingRequestId() {
        return ++PLAYING_REQUEST_ID;
    }

    public static int getPlayingRequestId() {
        return PLAYING_REQUEST_ID;
    }

    public static void setCurrentPlayingRequestId(int playingRequestId) {
        PLAYING_REQUEST_ID = playingRequestId;
    }

    @Override
    protected void onDestroy() {

        if (request != null) {
            request.setOnQueryMediaListener(null);
            request = null;
        }

        if (fxPlayer != null) {
            fxPlayer.removeRequest(instanceRequestId);
            fxPlayer.removeListener(onMediaNotificationPlaybackChanged);
            fxPlayer.addListener(null);
            fxPlayer = null;
        }


        if (exoSeekBar != null) {
            exoSeekBar.release();
            exoSeekBar = null;
        }

        if (videosAdapter != null) {
            videosView.setOnSnapVideoListener(null);
            videosView.setAdapter(null);
            videosAdapter.release();
        }


        // Set views to null
        back = null;
        nameList = null;
        fractions = null;
        playingTimeText = null;
        videosView = null;

        onMediaNotificationPlaybackChanged = null;
        videosAdapter = null;
        playVideoBinding = null;
        //destroyed = true;
        super.onDestroy();
    }

}
