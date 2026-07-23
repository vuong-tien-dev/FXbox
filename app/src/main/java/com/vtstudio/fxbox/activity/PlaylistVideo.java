package com.vtstudio.fxbox.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.adapters.MediaItemAdapter;
import com.vtstudio.fxbox.database.dao.YTDetailsDao;
import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.database.dao.FxMediaVideoDao;
import com.vtstudio.fxbox.database.dao.PlaylistDao;
import com.vtstudio.fxbox.database.dao.ShortsDetailsDao;
import com.vtstudio.fxbox.databinding.ActivityVideoPlaylistBinding;
import com.vtstudio.fxbox.databinding.LoadingLayoutBinding;
import com.vtstudio.fxbox.listeners.OnItemListClickListener;
import com.vtstudio.fxbox.listeners.OnLoadMoreListener;
import com.vtstudio.fxbox.listeners.OnServiceConnectionListener;
import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.models.Media;
import com.vtstudio.fxbox.media.player.FxPlayer;
import com.vtstudio.fxbox.media.player.MediaRequest;
import com.vtstudio.fxbox.media.utils.ModelUtils;
import com.vtstudio.fxbox.utils.ListUtils;
import com.vtstudio.fxbox.utils.ParserUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.text.Regex;

public class PlaylistVideo extends FxBaseActivity {
    public static final String KEY_PLAYLIST_ID = "playlist_id";
    private ActivityVideoPlaylistBinding mBinding;
    private PlaylistDao mPlaylistDao;
    private com.vtstudio.fxbox.media.models.tiktok.Playlist mPlaylist;
    private MediaItemAdapter mAdapter;
    private FxRoomDB database;
    private FxMediaVideoDao mediaVideoDao;
    private ShortsDetailsDao shortDetailsDao;
    private YTDetailsDao ytDetailsDao;
    private boolean canLoadMoreTemp;
    private boolean isLoadMore;
    private int requestId = -1;
    private boolean isAppCreatedPlaylist = false;
    private boolean isMovingItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = ActivityVideoPlaylistBinding.inflate(getLayoutInflater());
        ViewGroup root = mBinding.getRoot();
        setContentView(root);

        LoadingLayoutBinding loadingLayoutBinding = LoadingLayoutBinding.inflate(getLayoutInflater());
        View loadingView = loadingLayoutBinding.getRoot();
        loadingView.setBackgroundColor(getColor(R.color.theme_black_red_dark));

        root.addView(loadingView);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            checkAndInitDataViewIfValid();
            executorService.shutdown();
            runOnUiThread(() -> root.removeView(loadingView));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViews() {

        setSupportActionBar(mBinding.acPlaylistToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mBinding.acPlaylistToolbar.setNavigationOnClickListener(v -> onBackPressed());

        String name = mPlaylist.getName();

        if (mPlaylist.getId().equals(com.vtstudio.fxbox.media.models.tiktok.Playlist.FAVORITE_PLAYLIST_ID)) {
            name = getString(R.string.favorite_list);
        } else if (mPlaylist.getId().equals(com.vtstudio.fxbox.media.models.tiktok.Playlist.LIMITED_PLAYLIST)) {
            name = getString(R.string.limit_list);
        }
        mPlaylist.setName(name);
        getSupportActionBar().setTitle(name);
        List<FxMediaVideo> playlistMedia = mPlaylist.getVideoList();

        // mBinding.acPlaylistIntroduction.acPlaylistName.setText(name);
        // mBinding.acPlaylistIntroduction.acPlaylistVideoCount.setText(getString(R.string.video_count,
        // idList.size()));
        // if (mPlaylist.getBackGroundId() != null &&
        // !mPlaylist.getBackGroundId().isEmpty()) {
        // Glide.with(mBinding.acPlaylistIntroduction.acPlaylistThumbnail).load(mPlaylist.getBackgroundPath())
        // .diskCacheStrategy(DiskCacheStrategy.NONE)
        // .override(mBinding.acPlaylistIntroduction.acPlaylistThumbnail.getWidth(),
        // mBinding.acPlaylistIntroduction.acPlaylistThumbnail.getHeight())
        // .into(mBinding.acPlaylistIntroduction.acPlaylistThumbnail);
        // } else if (playlistMedia != null && !playlistMedia.isEmpty()){
        // FxMediaVideo media = mPlaylist.getVideoList().get(0);
        // MediaViewUtils.loadImageFromMedia(media,
        // mBinding.acPlaylistIntroduction.acPlaylistThumbnail, 400, 300);
        // }

        mAdapter = new MediaItemAdapter();
        if (playlistMedia != null) {
            List<FxMediaVideo> videos = mPlaylist.getVideoList();
            mAdapter.setMediaList(videos);
            mAdapter.setCanLoadMore(videos.size() != mPlaylist.getVideoIdList().size());
            mBinding.acPlaylistListview.setAdapter(mAdapter);
            boundFxPlayer(new OnServiceConnectionListener() {
                @Override
                public void onServiceConnected() {
                    mAdapter.setOnItemClickListener(new OnItemListClickListener() {
                        @Override
                        public void onClick(int position) {
                            Log.d("PlaylistActivity", "onClick " + position);
                            Intent intent = new Intent(PlaylistVideo.this, PlayVideo.class);
                            requestId = PlayVideo.newPlayingRequestId();
                            MediaRequest request = new MediaRequest(requestId);
                            request.setName(mPlaylist.getName());
                            request.setCurrentPlayingIndex(position);
                            request.setMediaList(new ArrayList<>(mAdapter.getMediaList()));
                            request.setQueryMore(mAdapter.isCanLoadMore());
                            request.setPlaylistId(mPlaylist.getId());
                            Log.d("PlayVideo", "Can query more = " + mAdapter.isCanLoadMore());
                            getFxPlayer().addRequest(request);
                            intent.putExtra(PlayVideo.KEY_NAME_LIST, request.getName());
                            intent.putExtra(PlayVideo.KEY_PLAYING_INDEX, position);
                            intent.putExtra(PlayVideo.KEY_ADAPTER_ACTION_LIMIT_CONTROL, mPlaylist.getId()
                                    .equals(com.vtstudio.fxbox.media.models.tiktok.Playlist.LIMITED_PLAYLIST));
                            intent.putExtra(PlayVideo.KEY_PLAYLIST_ID_TO_LOAD_MORE, mPlaylist.getId());
                            startActivity(intent);
                            overridePendingTransition(R.anim.fade_in_250, 0);
                        }
                    });
                }

                @Override
                public void onServiceDisconnected() {

                }
            });

            Regex regex = new Regex("[a-zA-Z]+");
            isAppCreatedPlaylist = regex.find(mPlaylist.getId(), 0) != null;

            if (!isAppCreatedPlaylist) {
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
                    @Override
                    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder) {
                        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                        return makeMovementFlags(dragFlags, 0);
                    }

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {

                        int currentPosition = viewHolder.getBindingAdapterPosition();
                        int targetPosition = target.getBindingAdapterPosition();

                        if (currentPosition == RecyclerView.NO_POSITION || targetPosition == RecyclerView.NO_POSITION) {
                            // Handle the case where positions are invalid.
                            return false;
                        }

                        // Log.d("PlaylistActivity", "Moving item from position " + currentPosition + "
                        // to position " + targetPosition);

                        List<FxMediaVideo> mediaList = mAdapter.getMediaList();
                        FxMediaVideo media = mediaList.get(currentPosition);

                        mediaList.remove(currentPosition);
                        mediaList.add(targetPosition, media);

                        List<String> idList = mPlaylist.getVideoIdList();
                        String currentId = idList.get(currentPosition);

                        idList.remove(currentId);
                        idList.add(targetPosition, currentId);

                        mPlaylistDao.update(mPlaylist);

                        mAdapter.notifyItemMoved(currentPosition, targetPosition);

                        if (!isMovingItem) {
                            isMovingItem = true;
                            canLoadMoreTemp = mAdapter.isCanLoadMore();
                            mAdapter.setCanLoadMore(false);
                        }

                        return true;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                    }
                });

                itemTouchHelper.attachToRecyclerView(mBinding.acPlaylistListview);
                mBinding.acPlaylistListview.setOnTouchListener(new View.OnTouchListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_CANCEL:
                            case MotionEvent.ACTION_UP:
                                if (!isMovingItem)
                                    break;
                                mAdapter.setCanLoadMore(canLoadMoreTemp);
                                isMovingItem = false;
                                break;

                        }
                        return false;
                    }
                });
            } else {
                // String id = mPlaylist.getId();
                // if(Objects.equals(id,
                // com.vtstudio.fxbox.media.models.tiktok.Playlist.FAVORITE_PLAYLIST_ID)) {
                // // final int maxLines =
                // mBinding.acPlaylistIntroduction.acPlaylistDescription.getHeight() /
                // mBinding.acPlaylistIntroduction.acPlaylistDescription.getBaseline();
                // mBinding.acPlaylistIntroduction.acPlaylistDescription.setMaxLines(Integer.MAX_VALUE);
                // mBinding.acPlaylistIntroduction.acPlaylistDescription.setTitle(getString(R.string.favorite_list)
                // + "\n");
                // mBinding.acPlaylistIntroduction.acPlaylistDescription.setText(getString(R.string.favorite_playlist_desc));
                // }
            }
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mBinding.acPlaylistListview.setLayoutManager(layoutManager);
        if (mAdapter.isCanLoadMore()) {
            mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMoreEvent(boolean canLoadMore, boolean fromAdapter) {
                    if (canLoadMore) {
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.execute(PlaylistVideo.this::loadMore);
                        executor.shutdown();
                    }
                }
            });
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("PlaylistVideo", "onRestart");
        if (requestId != -1) {
            FxPlayer fxPlayer = getFxPlayer();
            if (fxPlayer != null) {
                Log.d("PlaylistVideo", "getFxPlayer success");
                MediaRequest mediaRequest = fxPlayer.getRequestById(requestId);
                if (mediaRequest != null && mAdapter != null
                        && mediaRequest.getMediaList() != null
                        && mAdapter.getMediaList() != null) {
                    mAdapter.setCanLoadMore(mediaRequest.canQueryMore());
                    List<FxMediaVideo> mediaVideos = new ArrayList<>();
                    Log.d("PlaylistVideo", "sync data");
                    mPlaylist = database.playlistDao().getPlaylistById(mPlaylist.getId());

                    if (mPlaylist == null)
                        return;

                    for (Media media : mediaRequest.getMediaList()) {
                        if (media instanceof FxMediaVideo) {
                            mediaVideos.add((FxMediaVideo) media);
                        }
                    }

                    Log.d("PlaylistVideo", "id count: " + (mPlaylist.getVideoIdList().size()));
                    Log.d("PlaylistVideo", "Video count: " + (mediaVideos.size()));

                    mAdapter.setMediaList(mediaVideos);
                    mPlaylist.setVideoList(mediaVideos);
                    mAdapter.notifyDataSetChanged();
                    mBinding.acPlaylistListview.post(() -> {
                        mBinding.acPlaylistListview.scrollToPosition(mediaRequest.getCurrentPlayingIndex());
                        mAdapter.notifyItemSelectedChange(mediaRequest.getCurrentPlayingIndex());
                    });
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("PlaylistVideo", "fxOnStop");
        Log.d("PlaylistVideo", "id count: " + (mPlaylist.getVideoIdList().size()));
        Log.d("PlaylistVideo", "Video count: " + (mAdapter.getMediaList().size()));
    }

    private void checkAndInitDataViewIfValid() {
        Intent intent = getIntent();

        if (intent != null) {
            String playlistId = intent.getStringExtra(KEY_PLAYLIST_ID);
            if (playlistId != null) {
                database = FxRoomDB.get(this);
                mediaVideoDao = database.fxMediaVideoDao();
                shortDetailsDao = database.shortsDetailsDao();
                mPlaylistDao = database.playlistDao();
                mPlaylist = mPlaylistDao.getPlaylistById(playlistId);
                ytDetailsDao = database.ytvDetailsDao();
                if (mPlaylist != null) {
                    mPlaylist.getVideoIdList();
                    List<Long> fxIdList = ParserUtils.fromList(mPlaylist.getVideoIdList());
                    if (!fxIdList.isEmpty()) {
                        List<FxMediaVideo> videos = new ArrayList<>();
                        for (int i = 0; i < Math.min(fxIdList.size(), 50); ++i) {
                            Long fxId = fxIdList.get(i);
                            FxMediaVideo video = mediaVideoDao.getVideoByFxId(fxId);

                            if (video == null) {
                                video = ModelUtils.fromDetails(shortDetailsDao.getDetailsByFxId(fxId));
                            }

                            if (video == null) {
                                video = ModelUtils.fromDetails(ytDetailsDao.getDetailsByFxId(fxId));
                            }

                            if (video != null)
                                videos.add(video);

                            if (video == null) {
                                ListUtils.removeAllOccurrences(mPlaylist.getVideoIdList(), String.valueOf(fxId));
                            }
                        }
                        mPlaylist.setVideoList(videos);
                        mPlaylistDao.update(mPlaylist);
                    }
                    runOnUiThread(this::initViews);
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadMore() {
        if (isLoadMore || isFinishing() || mBinding == null || mAdapter == null)
            return;

        isLoadMore = true;
        Log.d("PlaylistActivity", "isLoadingMore");
        List<Long> fxIdList = ParserUtils.fromList(mPlaylist.getVideoIdList());
        List<Long> currentFxIdList = ParserUtils.fromListMedia(mAdapter.getMediaList());
        int currentItemCount = mAdapter.getItemCount();
        List<FxMediaVideo> videos = new ArrayList<>();
        for (int i = currentItemCount - 1; i < Math.min(currentItemCount + 15, fxIdList.size()); ++i) {
            Long fxId = fxIdList.get(i);

            if (currentFxIdList.contains(fxId))
                continue;

            FxMediaVideo video = mediaVideoDao.getVideoByFxId(fxId);

            if (video == null) {
                video = ModelUtils.fromDetails(shortDetailsDao.getDetailsByFxId(fxId));
            }

            if (video == null) {
                video = ModelUtils.fromDetails(ytDetailsDao.getDetailsByFxId(fxId));
            }

            if (video != null) {
                videos.add(video);
            }

            if (video == null) {
                ListUtils.removeAllOccurrences(mPlaylist.getVideoIdList(), String.valueOf(fxId));
                Log.d("PlaylistActivity", "Null fxId: " + fxId);
            }
        }

        if (!isAppCreatedPlaylist) {
            mPlaylistDao.update(mPlaylist);
        }

        if (!videos.isEmpty()) {
            mBinding.acPlaylistListview.post(() -> {
                mAdapter.getMediaList().addAll(videos);
                mAdapter.notifyItemRangeChanged(currentItemCount - 1, mAdapter.getItemCount() - currentItemCount);
            });
        } else {
            mBinding.acPlaylistListview.post(() -> {
                mAdapter.setCanLoadMore(false);
                mAdapter.notifyItemRemoved(currentItemCount - 1);
            });
        }
        isLoadMore = false;
    }

    @Override
    protected void onDestroy() {

        if (mBinding != null) {
            mBinding.acPlaylistListview.setAdapter(null);
        }
        if (mAdapter != null) {
            mAdapter.setOnLoadMoreListener(null);
            mAdapter.setOnItemClickListener(null);
            mAdapter = null;
        }

        database = null;
        mPlaylistDao = null;
        mediaVideoDao = null;
        mPlaylist = null;
        shortDetailsDao = null;
        mBinding = null;
        requestId = -1;
        super.onDestroy();
    }

}
