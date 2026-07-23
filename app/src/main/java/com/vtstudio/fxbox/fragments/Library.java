package com.vtstudio.fxbox.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.saadahmedsoft.popupdialog.CreateDialog;
import com.saadahmedsoft.popupdialog.listener.OnDialogButtonClickListener;
import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.activity.FxBaseActivity;
import com.vtstudio.fxbox.activity.PlayVideo;
import com.vtstudio.fxbox.activity.PlaylistList;
import com.vtstudio.fxbox.activity.PlaylistVideo;
import com.vtstudio.fxbox.activity.SettingActivity;
import com.vtstudio.fxbox.adapters.HistoryItemAdapter;
import com.vtstudio.fxbox.adapters.PlaylistAdapter;
import com.vtstudio.fxbox.adapters.SpecialITemAdapter;
import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.database.dao.FxMediaVideoDao;
import com.vtstudio.fxbox.database.dao.ShortsDetailsDao;
import com.vtstudio.fxbox.database.dao.YTDetailsDao;
import com.vtstudio.fxbox.databinding.FragmentLibraryBinding;
import com.vtstudio.fxbox.fxviews.dialog.BackupDialog;
import com.vtstudio.fxbox.fxviews.dialog.DialogHelper;
import com.vtstudio.fxbox.fxviews.dialog.DialogUtils;
import com.vtstudio.fxbox.listeners.OnItemListClickListener;
import com.vtstudio.fxbox.media.models.Media;
import com.vtstudio.fxbox.media.models.tiktok.Playlist;
import com.vtstudio.fxbox.media.player.FxPlayer;
import com.vtstudio.fxbox.media.player.MediaRequest;
import com.vtstudio.fxbox.media.utils.ModelUtils;
import com.vtstudio.fxbox.ui.SpaceItemDecoration;
import com.vtstudio.fxbox.utils.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Library extends FxBaseFragment {
    public Library() {
        // Required empty public constructor
    }

    public static Library newInstance() {
        return new Library();
    }

    private FragmentLibraryBinding binding;

    private RecyclerView historyItems;
    private RecyclerView playlistView;
    private RecyclerView specialListView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView historyText;
    private PlaylistAdapter playlistAdapter;
    private HistoryItemAdapter historyItemAdapter;
    private SpecialITemAdapter specialItemAdapter;
    private List<Playlist> playlistList;
    private List<Media> historyMediaItems;

    private boolean isLayoutInitialized = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLibraryBinding.inflate(inflater, container, false);

        initData();

        initViews();

        updateHistory();

        return binding.getRoot();
    }

    private void initLayout(int bottomMargin) {
        int currentHeight = binding.getRoot().getHeight();
        int newHeight = currentHeight - bottomMargin;

        View root = binding.getRoot();

        ViewGroup.LayoutParams layoutParams = root.getLayoutParams();
        if (layoutParams == null)
            return;

        layoutParams.height = newHeight;
        root.requestLayout();

        isLayoutInitialized = true;
    }

    private void initData() {

        FxRoomDB database = FxRoomDB.get(getContext());
        // get playlist

        playlistList = database.playlistDao().getAllPlaylists();

        playlistList = playlistList.stream().sorted((o1, o2) -> {

            boolean first = Playlist.isShortsList(requireActivity(), o1.getId());
            boolean second = Playlist.isShortsList(requireActivity(), o2.getId());

            if (first && second)
                return 0;

            return first ? -1 : 1;
        }).limit(15).collect(Collectors.toList());

        initHistoryList(database);

    }

    private void initHistoryList(FxRoomDB database) {

        ShortsDetailsDao shortsDetailsDao = database.shortsDetailsDao();
        FxMediaVideoDao fxMediaVideoDao = database.fxMediaVideoDao();
        YTDetailsDao ytDetailsDao = database.ytvDetailsDao();

        boolean isHistoryExist = false;
        Playlist historyList = null;

        historyList = database.playlistDao().getPlaylistById(Playlist.RECENTLY_VIEWED_PLAYLIST);

        if (historyList != null) {
            historyMediaItems = new ArrayList<>();

            List<String> idList = historyList.getVideoIdList();

            for (int i = 0; i < idList.size(); ++i) {
                String id = idList.get(i);
                if (id == null || id.isEmpty() || id.matches("[^0-9]"))
                    continue;

                Log.d("Library", id);
                Media media;
                long fxId = Long.parseLong(id);

                media = ModelUtils.fromDetails(shortsDetailsDao.getDetailsByFxId(fxId));

                if (media == null) {
                    media = ModelUtils.fromDetails(ytDetailsDao.getDetailsByFxId(fxId));
                }

                if (media == null) {
                    media = fxMediaVideoDao.getVideoByFxId(fxId);
                }

                if (media != null) {
                    historyMediaItems.add(media);
                } else {
                    ListUtils.removeAllOccurrences(idList, id);
                    --i;
                }
            }
            database.playlistDao().update(historyList);
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private void initViews() {
        // ref of views
        playlistView = binding.libPlaylistList;
        historyItems = binding.libHistoryList;
        specialListView = binding.libSpecialList;
        historyText = binding.libHistoryText;

        swipeRefreshLayout = binding.libSwipeRefreshLayout;
        swipeRefreshLayout.setOnRefreshListener(() -> {
            initData();
            updateHistory();
            historyItemAdapter.setHistoryItemList(historyMediaItems);
            playlistAdapter.setPlaylistList(playlistList);
            historyItemAdapter.notifyDataSetChanged();
            playlistAdapter.notifyDataSetChanged();
            playlistView.scrollToPosition(0);
            historyItems.scrollToPosition(0);
            swipeRefreshLayout.setRefreshing(false);
        });

        // adapters
        playlistAdapter = new PlaylistAdapter(playlistList);
        historyItemAdapter = new HistoryItemAdapter(historyMediaItems);
        specialItemAdapter = new SpecialITemAdapter();

        playlistView.addItemDecoration(new SpaceItemDecoration(10));
        historyItems.addItemDecoration(new SpaceItemDecoration(5));
        specialListView.addItemDecoration(new SpaceItemDecoration(5));

        // layout managers
        LinearLayoutManager playListLM = new LinearLayoutManager(getContext());
        LinearLayoutManager historyItemsLayoutManager = new LinearLayoutManager(getContext());
        historyItemsLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        playListLM.setOrientation(RecyclerView.HORIZONTAL);
        historyItems.setLayoutManager(historyItemsLayoutManager);
        playlistView.setLayoutManager(playListLM);
        specialListView.setLayoutManager(new LinearLayoutManager(getActivity()));

        historyItems.setAdapter(historyItemAdapter);
        playlistView.setAdapter(playlistAdapter);
        specialListView.setAdapter(specialItemAdapter);

        binding.libViewAllPlaylist.setOnClickListener(v -> {
            Activity activity = getActivity();
            if (activity != null) {
                Intent intent = new Intent(activity, PlaylistList.class);
                startActivity(intent);
                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        historyItemAdapter.setOnItemListClickListener(new OnItemListClickListener() {
            @Override
            public void onClick(int position) {
                MediaRequest request = new MediaRequest(PlayVideo.newPlayingRequestId());
                request.setQueryMore(false);
                request.setName(getString(R.string.history_text));
                request.setPlaylistId(Playlist.RECENTLY_VIEWED_PLAYLIST);
                request.setCurrentPlayingIndex(position);
                request.setMediaList(historyMediaItems);
                Activity activity = getActivity();
                if (activity instanceof FxBaseActivity) {
                    FxPlayer fxPlayer = ((FxBaseActivity) activity).getFxPlayer();
                    if (fxPlayer != null) {
                        fxPlayer.addRequest(request);
                        Intent intent = new Intent(activity, PlayVideo.class);
                        intent.putExtra(PlayVideo.KEY_PLAYLIST_ID_TO_LOAD_MORE, Playlist.RECENTLY_VIEWED_PLAYLIST);
                        intent.putExtra(PlayVideo.KEY_NAME_LIST, request.getName());
                        activity.startActivity(intent);
                    }
                }
            }
        });

        binding.libSettingButton.setOnClickListener(v -> {
            Activity activity = getActivity();
            if (activity != null) {
                activity.startActivity(new Intent(activity, SettingActivity.class));
                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        playlistAdapter.setOnItemClickListener(new OnItemListClickListener() {
            @Override
            public void onClick(int position) {
                Playlist playlist = playlistAdapter.getPlaylistList().get(position);
                Intent intent = new Intent(getActivity(), PlaylistVideo.class);
                intent.putExtra(PlaylistVideo.KEY_PLAYLIST_ID, playlist.getId());
                startActivity(intent);
                requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        playlistAdapter.setOnCreatePlaylistClickListener(new OnItemListClickListener() {
            @Override
            public void onClick(int position) {
                Context context = getActivity();
                if (context != null) {
                    DialogHelper.showCreatePlaylistDialog(context);
                }
            }
        });

        specialItemAdapter.setOnItemClickListener(new OnItemListClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(getActivity(), PlaylistVideo.class);
                if (position == 0) {
                    intent.putExtra(PlaylistVideo.KEY_PLAYLIST_ID, Playlist.FAVORITE_PLAYLIST_ID);
                } else {
                    intent.putExtra(PlaylistVideo.KEY_PLAYLIST_ID, Playlist.LIMITED_PLAYLIST);
                }
                startActivity(intent);
                requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        binding.libStoreDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getActivity();
                if (context != null) {
                    CreateDialog confirmDialog = DialogUtils.buildTemplate(context, getString(R.string.backup),
                            getString(R.string.backup_content));
                    confirmDialog.showDialog(new OnDialogButtonClickListener() {
                        @Override
                        public void onPositiveClicked(Dialog dialog) {
                            super.onPositiveClicked(dialog);
                            BackupDialog backupDialog = new BackupDialog(context);
                            backupDialog.show();
                            backupDialog.startBackup();
                        }
                    });
                }
            }
        });
    }

    private void updateHistory() {
        if (historyItemAdapter.getItemCount() > 0) {
            showHistoryViews();
        } else {
            hideHistoryViews();
        }
    }

    private void hideHistoryViews() {
        historyItems.setVisibility(View.GONE);
        historyText.setVisibility(View.GONE);
    }

    private void showHistoryViews() {
        historyItems.setVisibility(View.VISIBLE);
        historyText.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();

        // if(isLayoutInitialized) return;
        //
        // Activity activity = getActivity();
        // if(activity instanceof Main){
        // initLayout(((Main) activity).getBottomMargin());
        // }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Glide.get(getActivity()).clearMemory();
        isLayoutInitialized = false;
        playlistView.setAdapter(null);
        historyItems.setAdapter(null);
        specialListView.setAdapter(null);
        specialItemAdapter = null;
        historyMediaItems = null;
        historyText = null;
        swipeRefreshLayout = null;
        historyItems = null;
        playlistAdapter = null;
        playlistList = null;
        binding = null;
    }
}