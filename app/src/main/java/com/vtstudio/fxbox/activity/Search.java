package com.vtstudio.fxbox.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.adapters.MediaSearchCategoryAdapter;
import com.vtstudio.fxbox.adapters.MediaItemGridAdapter;
import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.databinding.ActitvitySearchBinding;
import com.vtstudio.fxbox.fxviews.progressbar.FxLoadingView;
import com.vtstudio.fxbox.listeners.OnServiceConnectionListener;
import com.vtstudio.fxbox.media.models.Media;
import com.vtstudio.fxbox.media.models.tiktok.ShortsDetails;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.media.player.FxPlayer;
import com.vtstudio.fxbox.media.player.MediaRequest;
import com.vtstudio.fxbox.media.utils.ModelUtils;
import com.vtstudio.fxbox.ui.SpaceItemDecoration;
import com.vtstudio.fxbox.utils.ContentUtils;
import com.vtstudio.fxbox.utils.ViewsUtils;
import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.models.tiktok.Playlist;
import com.vtstudio.fxbox.utils.ParserUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class Search extends FxBaseActivity implements TextView.OnEditorActionListener {
    //
    public static final int CATEGORY_ALL = -1;
    public static final String DATA_KEY = "search_data_key";
    public static final int DATA_FROM_PLAYER_SERVICE = 0;
    public static final String FILTER_NAME = "name";
    public static final String FILTER_PARENT_FOLDER = "parent_folder";
    public static final String FILTER_DESCRIPTION = "description";
    public static final String FILTER_MUSIC_TITLE = "music_title";
    public static final String FILTER_AUTHOR_NICKNAME = "author_nickname";
    // binding var
    private ActitvitySearchBinding searchBinding;

    // view variables

    private RecyclerView resultsView;
    private ViewGroup resultsContainer;
    private RecyclerView categoriesView;
    private ConstraintLayout searchBar;
    private ImageView backImgButton;
    private ImageView deleteOrSearchButton;
    private EditText searchEditor;
    private FxLoadingView searchLoad;

    // data
    private List<Media> videoData;
    private List<Media> results;

    private String keyword;
    private MediaItemGridAdapter resultsAdapter;
    private MediaSearchCategoryAdapter categoryAdapter;
    private GridLayoutManager resultLayoutManager;

    private LinearLayoutManager categoryLayoutManager;
    private FxRoomDB database;


    // field vars
    private boolean isEnabled;
    private int currenRequestId = -1;
    public static final int DEFAULT_ANIM_DURATION = 300;
    private boolean isPlaySearchList;
    private int oldPosition = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchBinding = ActitvitySearchBinding.inflate(getLayoutInflater());

        setContentView(searchBinding.getRoot());

        initViews();

        initData();

    }

    private void initData() {
        database = FxRoomDB.get(this);
        boundFxPlayer(new OnServiceConnectionListener() {
            @Override
            public void onServiceConnected() {
                //getData();
                onDataInitialized();
            }

            @Override
            public void onServiceDisconnected() {

            }
        });
    }

    private void initViews() {

        resultsContainer = searchBinding.mediaSearchResultContainer;

        resultsView = searchBinding.mediaResultList;

        categoriesView = searchBinding.mediaSearchCategory;

        searchBar = searchBinding.searchBar.searchBarContainer;

        backImgButton = searchBinding.searchBar.searchBarBackIcon;

        deleteOrSearchButton = searchBinding.searchBar.searchBarDeleteOrSearchIcon;

        searchEditor = searchBinding.searchBar.searchBarEditor;

        searchLoad = searchBinding.mediaSearchLoad;

        // khởi tạo logic hoạt động
        // 1. Các view back và editor sẽ ẩn khi activity khởi tạo
        // đã khởi tạo trong xml layout

        // 2. khởi tạo thuộc tính cần thiết
        searchEditor.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        // 3. khởi tạo listener cho các view


        searchBar.setOnClickListener(v -> {
            if (!isEnabled) updateSearchBarState(true);
        });

        backImgButton.setOnClickListener(v -> updateSearchBarState(false));
        ViewsUtils.setTouchScaleEffect(backImgButton, 0.95f);
        ViewsUtils.setTouchScaleEffect(deleteOrSearchButton, 0.95f);

        resultsView.setNestedScrollingEnabled(false);
        categoriesView.setNestedScrollingEnabled(false);
    }

    private void updateSearchBarState(boolean isEnabled) {
        if (isEnabled) makeSearchBarViewEnabled();
        else makeSearchBarViewUnEnabled();
    }

    private void makeSearchBarViewUnEnabled() {
        // update state for local var
        isEnabled = false;

        // create anim
        Animation out = AnimationUtils.loadAnimation(Search.this, R.anim.fade_out);
        out.setDuration(DEFAULT_ANIM_DURATION);
        out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // hide views
                backImgButton.setVisibility(View.INVISIBLE);
                searchEditor.setVisibility(View.INVISIBLE);

                // change icon
                deleteOrSearchButton.setImageResource(R.drawable.shorts_search_bar_drw);

                // set listener for delete button
                deleteOrSearchButton.setOnClickListener(null);

                // editor listener
                searchEditor.setOnEditorActionListener(null);

                // hide keyboard
                toggleKeyboardVisibility(false, searchEditor);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // start Animation
        searchBar.startAnimation(out);
    }

    private void makeSearchBarViewEnabled() {
        // update state for local var
        isEnabled = true;

        // create anim
        Animation in = AnimationUtils.loadAnimation(Search.this, R.anim.fade_in);
        in.setDuration(DEFAULT_ANIM_DURATION);
        in.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

                // show views
                backImgButton.setVisibility(View.VISIBLE);
                searchEditor.setVisibility(View.VISIBLE);

                // change icon
                deleteOrSearchButton.setImageResource(R.drawable.search_bar_view_delete_icon);

                // set listener for delete button
                deleteOrSearchButton.setOnClickListener(v -> searchEditor.setText(null));

                // editor listener
                searchEditor.setOnEditorActionListener(Search.this);

                // focus
                searchEditor.requestFocus();
                toggleKeyboardVisibility(true, searchEditor);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // start Animation
        searchBar.startAnimation(in);

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        Log.d("debug", "action called");
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {

            toggleKeyboardVisibility(false, searchEditor);

            startSearch(v.getText());

            return true;
        }
        return false;
    }

    public void toggleKeyboardVisibility(boolean isVisible, EditText editText) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (isVisible) {
            // Hiển thị bàn phím
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        } else {
            // Ẩn bàn phím
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void onDataInitialized() {

        resultsAdapter = new MediaItemGridAdapter();
        categoryAdapter = new MediaSearchCategoryAdapter();

        resultLayoutManager = new GridLayoutManager(this, 2);
        resultLayoutManager.setOrientation(GridLayoutManager.VERTICAL);

        categoryLayoutManager = new LinearLayoutManager(Search.this);
        categoryLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        resultsView.setLayoutManager(resultLayoutManager);
        categoriesView.setLayoutManager(categoryLayoutManager);

        resultsView.setAdapter(resultsAdapter);
        categoriesView.setAdapter(categoryAdapter);

        categoriesView.addItemDecoration(new SpaceItemDecoration(20));

        // on click listener for items
        categoryAdapter.setOnItemClickListener((key, position) -> {
            if (key == CATEGORY_ALL) {
                resultsAdapter.setMediaList(results);
            } else {
                resultsAdapter.setMediaList(ContentUtils.filterByMediaType(results, key));
            }
            resultsAdapter.notifyDataSetChanged();
            resultsView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
            resultsView.scrollToPosition(0);
            categoriesView.scrollToPosition(position);
        });

        resultsAdapter.setOnItemClickListener(position -> {

            String nameList = getString(R.string.search_results_found) + " " + " \"" + keyword + "\"";
            Intent intent = new Intent(Search.this, PlayVideo.class);

            currenRequestId = PlayVideo.newPlayingRequestId();
            MediaRequest request = new MediaRequest(currenRequestId);
            request.setName(nameList);
            request.setMediaList(new ArrayList<>(resultsAdapter.getMediaList()));
            request.setCurrentPlayingIndex(position);
            Search.this.getFxPlayer().addRequest(request);

            intent.putExtra(PlayVideo.KEY_NAME_LIST, nameList);
            intent.putExtra(PlayVideo.KEY_PLAYING_INDEX, position);
            //intent.putExtra(PlayVideo.DATA_RETRIEVAL_METHOD, PlayVideo.METHOD_DATA_FROM_REQUEST);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in_250, 0);
            isPlaySearchList = true;
            oldPosition = position;
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        FxPlayer fxPlayer = getFxPlayer();
        if (fxPlayer != null && isPlaySearchList && fxPlayer.getCurrentRequestId() == currenRequestId
                && resultsAdapter != null && resultsView != null) {
            int current = getFxPlayer().getCurrentPlayingIndex();
            // scroll to display current in position ( hiển thị view ở giữa )
//            int targetToScroll = current > oldPosition ? Math.min(itemCount  - 1, current + 4) : Math.max(current - 4, 0);
            MediaRequest mediaRequest = fxPlayer.getRequestById(currenRequestId);
            if (mediaRequest != null && resultsAdapter != null
                    && mediaRequest.getMediaList() != null
                    && resultsAdapter.getMediaList() != null
                    && resultsAdapter.getMediaList().size() != mediaRequest.getMediaList().size()) {
                Log.d("Search", "is reloading media");
                resultsAdapter.setMediaList(mediaRequest.getMediaList());
                resultsAdapter.notifyDataSetChanged();
                searchBinding.mediaResultList.post(() -> {
                    searchBinding.mediaResultList.scrollToPosition(mediaRequest.getCurrentPlayingIndex());
                    resultsAdapter.notifyItemSelectedChange(mediaRequest.getCurrentPlayingIndex());
                });
            } else {
                resultsView.scrollToPosition(current);
                resultsAdapter.notifyItemSelectedChange(current);
            }
            isPlaySearchList = false;
        }
    }



    @SuppressLint("NotifyDataSetChanged")
    private void startSearch(@NonNull CharSequence text) {

        keyword = text.toString();//List<Map<String, Media>> resultsMap = ContentUtils.filterAndSearch(videoData, selections, text.toString());

        setResultViewsStatus(false);

        Executors.newSingleThreadExecutor().execute(() -> {
            String query = keyword.trim();
            List<Media> commandResults = com.vtstudio.fxbox.utils.SearchCommandHelper.tryExecuteCommand(query, database);
            if (commandResults != null) {
                videoData = commandResults;
                results = new ArrayList<>(videoData);
                Map<Integer, Integer> categoryCountMap = new HashMap<>();
                categoryCountMap.put(CATEGORY_ALL, 1);
                for (Media media : results) {
                    int category = Media.MediaType.TYPE_EXTERNAL_STORAGE;
                    if (media instanceof ShortsVideo) {
                        category = Media.MediaType.TYPE_SHORTS_VIDEO;
                    }
                    if (categoryCountMap.containsKey(category)) {
                        int count = categoryCountMap.get(category);
                        categoryCountMap.put(category, count + 1);
                    } else {
                        categoryCountMap.put(category, 1);
                    }
                }

                Search.this.runOnUiThread(() -> onDataSearchComplete(categoryCountMap));
                return;
            }

            List<ShortsDetails> detailsList = database.shortsDetailsDao().searchVideosByCriteria(keyword);
            videoData = ModelUtils.mediaFromFxMediaVideo(database.fxMediaVideoDao().searchVideosByCriteria(keyword));
            videoData.addAll(ModelUtils.mediaFromDetails(detailsList));
            results = new ArrayList<>(videoData);
            Map<Integer, Integer> categoryCountMap = new HashMap<>();

            categoryCountMap.put(CATEGORY_ALL, 1);

            for (Media media : results) {
                int category = Media.MediaType.TYPE_EXTERNAL_STORAGE;
                if (media instanceof ShortsVideo) {
                    category = Media.MediaType.TYPE_SHORTS_VIDEO;
                }

                if (categoryCountMap.containsKey(category)) {
                    int count = categoryCountMap.get(category);
                    categoryCountMap.put(category, count + 1);
                } else {
                    categoryCountMap.put(category, 1);
                }
            }

            Search.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onDataSearchComplete(categoryCountMap);
                }
            });
        });

    }

    @SuppressLint("NotifyDataSetChanged")
    private void onDataSearchComplete(Map<Integer, Integer> categoryCountMap) {

        categoryAdapter.setCategory(categoryCountMap);
        categoryAdapter.notifyDataSetChanged();
//
        resultsAdapter.setMediaList(results);
        resultsAdapter.notifyDataSetChanged();

        setResultViewsStatus(true);

    }

    private void setResultViewsStatus(boolean show) {
        if (show) {
            if (results.isEmpty()) {
                Log.d("MediaSearch", "isEmpty");
                searchLoad.setVisibility(View.GONE);
            } else {
                categoriesView.setVisibility(View.VISIBLE);
                resultsView.setVisibility(View.VISIBLE);
                searchLoad.setVisibility(View.GONE);
            }
        } else {
            categoriesView.setVisibility(View.INVISIBLE);
            resultsView.scrollToPosition(0);
            resultsView.setVisibility(View.INVISIBLE);
            searchLoad.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected void onDestroy() {

        resultsAdapter.setOnItemClickListener(null);
        resultsAdapter.setMediaList(null);
        categoryAdapter.setOnItemClickListener(null);
        resultsView.setAdapter(null);
        categoriesView.setAdapter(null);
        resultsView.getRecycledViewPool().clear();
        results = null;
        resultLayoutManager = null;
        categoryLayoutManager = null;
        searchEditor = null;
        searchLoad = null;
        searchBar = null;
        backImgButton = null;
        deleteOrSearchButton = null;
        resultsAdapter = null;
        categoryAdapter = null;
        resultsContainer = null;
        searchBinding = null;
        super.onDestroy();
    }
}
