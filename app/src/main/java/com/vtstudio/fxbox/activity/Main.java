package com.vtstudio.fxbox.activity;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.tuanhav95.drag.DragView;
import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.adapters.MainPagerAdapter;
import com.vtstudio.fxbox.databinding.ActivityMainBinding;
import com.vtstudio.fxbox.databinding.ActivitySplashBinding;
import com.vtstudio.fxbox.fragments.PlayerDragView;
import com.vtstudio.fxbox.fragments.Shorts;
import com.vtstudio.fxbox.fragments.YoutubePlayerTop;
import com.vtstudio.fxbox.fxviews.progressbar.FxExoSeekBar;
import com.vtstudio.fxbox.fxviews.FxNavigationView;
import com.vtstudio.fxbox.helpers.NotificationHelper;
import com.vtstudio.fxbox.listeners.ActionListener;
import com.vtstudio.fxbox.listeners.OnServiceConnectionListener;
import com.vtstudio.fxbox.media.player.FxPlayer;
import com.vtstudio.fxbox.media.player.MediaRequest;
import com.vtstudio.fxbox.utils.ViewsUtils;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.lang.ref.WeakReference;


public class Main extends FxBaseActivity {
    private int BOTTOM_NAVIGATION_MARGIN = 0;
    private ActivityMainBinding mainBinding;
    private boolean FAG_INIT_VIEWS_LAYOUT; // Biến kiểm tra xem đã khởi tạo giao diện chưa
    // Biến kiểm tra xem dịch vụ đã được kết nối hay chưa

//    // key để giao tiếp với fragment
//    public static final int REQUEST_BOUND_PLAYER_SERVICE = 0;
//    public static final int REQUEST_BOUND_FXPLAYER = 1;
//    public static final int REQUEST_BOTTOM_NAV_MARGIN = 2;

    // key để giao tiếp với activity khác
    public static final String RESULT_VIEWS_OK = "views_ok";

    // Kết nối với dịch vụ PlayerService
    private MainPagerAdapter pagerAdapter;
    private FxExoSeekBar exoSeekBar;
    private ViewPager2.OnPageChangeCallback mCallback;
    private final WeakReference<Main> mainRef = new WeakReference<>(Main.this);
    private int navigationHeight = 0;
    private int currentPlayerDragViewVisibility = View.VISIBLE;
    private boolean isDragPlayerInitialized;
    private boolean windowHasFocusCalled;
    private boolean canInitDragPlayer;
    //private int pagerHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //NotificationHelper.pushMediaNotification(this);
        // Tạo đối tượng binding từ layout xml
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

        initViews();

        KeyboardVisibilityEvent.setEventListener(this, b -> {
            if (b) {
                mainNavigation.setVisibility(View.INVISIBLE);
                mainBinding.seekBar.setVisibility(View.INVISIBLE);
            } else {
                mainNavigation.setVisibility(View.VISIBLE);
                mainBinding.seekBar.setVisibility(View.VISIBLE);
            }
        });

        requestBluetoothPermissions();

        boundFxPlayer(new OnServiceConnectionListener() {
            @Override
            public void onServiceConnected() {
                Main main = mainRef.get();

                if (main == null) throw new NullPointerException("Main activity is null");

                FxPlayer fxPlayer = getFxPlayer();
                fxPlayer.startBackground();
                int id = fxPlayer.getCurrentRequestId();
                if ((id == Shorts.BASE_MEDIA_REQUEST_ID + Shorts.TYPE_VIDEO_FROM_CLOUD ) || (id == Shorts.BASE_MEDIA_REQUEST_ID + Shorts.TYPE_VIDEO_FOR_YOU)) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (FAG_INIT_VIEWS_LAYOUT && isFxPlayerBound() && fxPlayer.isFirstPlayed()) {
                                mainBinding.mainViewPager.setCurrentItem(1);
                            } else {
                                handler.postDelayed(this, 100);
                            }
                        }
                    }, 200);

                    mainBinding.dragView.setVisibility(View.GONE);

                } else if (id != -1 && id != PlayerDragView.MEDIA_ID) {
                    Log.d("fx_player", "id in main: " + id);
                    PlayVideo.setCurrentPlayingRequestId(id);
                    MediaRequest request = fxPlayer.getRequestById(id);
                    String nameList = request.getName();
                    Intent intent = new Intent(main, PlayVideo.class);
                    intent.putExtra(PlayVideo.KEY_NAME_LIST, nameList);
                    intent.putExtra(PlayVideo.KEY_PLAYING_INDEX, request.getCurrentPlayingIndex());
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {

                    canInitDragPlayer = true;

                    if(windowHasFocusCalled && FAG_INIT_VIEWS_LAYOUT) {
                        initDragPlayer();
                    }
                }
            }
            @Override
            public void onServiceDisconnected() {

            }
        });

    }

    private void requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.BLUETOOTH_CONNECT},
                        100);
            }
        }
    }

    private void initDragPlayer() {
        if(isDragPlayerInitialized || getFxPlayer() == null || isFinishing() || isDestroyed() || mainBinding.dragView.getVisibility() == View.GONE) return;
        mainBinding.dragView.setFragmentManager(getSupportFragmentManager());
        mainBinding.dragView.setMMarginBottomWhenMin(navigationHeight);
        mainBinding.dragView.initFrame();
        mainBinding.dragView.setFxPlayer(getFxPlayer());
        mainBinding.dragView.attachFragments();
        isDragPlayerInitialized = true;
    }

    // list views in activity
    FxNavigationView mainNavigation;

    private void initViews() {

        exoSeekBar = mainBinding.seekBar;

        // Tạo đối tượng PagerAdapter với FragmentManager và Lifecycle của Activity
        pagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), getLifecycle());

        // Thiết lập PagerAdapter cho ViewPager2 để hiển thị nội dung
        ViewPager2 mainPager = mainBinding.mainViewPager;

        mainPager.setAdapter(pagerAdapter);

        // hủy tính năng vuốt
        mainPager.setUserInputEnabled(false);

        // khởi tạo hiệu ứng chạm cho fxNav
        mainNavigation = mainBinding.bottomNavigationView;
        mainNavigation.setItemsTouchEffect(true);

        // tạo hiệu ứng chuyển trang
        ViewPager2.PageTransformer transformer = (page, position) -> page.startAnimation(AnimationUtils.loadAnimation(Main.this, R.anim.fade_in));
        mainPager.setPageTransformer(transformer);

        // khởi tạo trình lắng nghe để xử lý chuyển page cho nav
        mainNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            //fadingTransform(itemId);

            exoSeekBar.setVisibility(View.INVISIBLE);

            if (itemId == R.id.home_nav) {
                mainPager.setCurrentItem(0);
            } else if (itemId == R.id.shorts_nav) {
                mainPager.setCurrentItem(1);
                hidePlayerDragView();
            } else if (itemId == R.id.add_nav) {
                mainPager.setCurrentItem(2);
            } else if (itemId == R.id.library_nav) {
                mainPager.setCurrentItem(3);
            } else if (itemId == R.id.account_nav) {

            }

            if(itemId != R.id.shorts_nav) {
                restoreCurrentPlayerDragViewVisibility();
            }

            return true;
        });


        // khởi tạo trình lắng nghe để xử lý chuyển page cho pager
        mCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                switch (position) {
                    case 0:
                        mainNavigation.getMenu().getItem(0).setChecked(true);
                        break;
                    case 1:
                        mainNavigation.getMenu().getItem(1).setChecked(true);
                        break;
                    case 2:
                        mainNavigation.getMenu().getItem(2).setChecked(true);
                        break;
                }
            }
        };

        mainPager.registerOnPageChangeCallback(mCallback);

        mainBinding.dragView.setDragListener(new DragView.DragListener() {
            @Override
            public void onExpanded() {

            }

            @Override
            public void onChangeState(@NonNull DragView.State state) {

            }

            @Override
            public void onChangePercent(float v) {
                setNavigationHide(1 - v);
            }
        });

    }

//    private void fadingTransform(int id) {
//        int itemIndex = ViewsUtils.getItemIndexFromId(mainNavigation, id);
//        if (itemIndex == -1) return;
//        int menuSize = mainNavigation.getMenu().size();
//        int itemWidth = mainNavigation.getWidth() / menuSize;
//        int fadeLeft = itemIndex * itemWidth + mainNavigation.getPaddingStart();
//        int fadeRight = itemWidth * (menuSize - (itemIndex + 1)) + mainNavigation.getPaddingEnd();
//        int fadeBottom = (int) (mainNavigation.getHeight() * 0.2);
//        int fadeTop = 0;
//        mainBinding.mainFadingNavWrapper.setFadeSizes(fadeTop, fadeLeft, fadeBottom, fadeRight);
//    }

    public void setNavigationHide(@FloatRange(from = 0, to = 1) float hidePercent) {
        mainNavigation.setTranslationY(navigationHeight*hidePercent);
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Khởi tạo bố cục giao diện nếu chưa được khởi tạo
        if (!FAG_INIT_VIEWS_LAYOUT) {
            initViewsLayout();
            FAG_INIT_VIEWS_LAYOUT = true;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        windowHasFocusCalled = true;
        // Khởi tạo bố cục giao diện nếu chưa được khởi tạo
        if (hasFocus && FAG_INIT_VIEWS_LAYOUT && navigationHeight > 0 && canInitDragPlayer ) {
            initDragPlayer();
        }
    }

    private void initViewsLayout() {
        // Thiết lập vị trí SeekBar cho phù hợp với FadingNavigationLayout
        FrameLayout.LayoutParams wrapperParams = (FrameLayout.LayoutParams) mainBinding.bottomNavigationView.getLayoutParams();

        mainBinding.seekBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isFinishing() || isDestroyed() ) return;
                FrameLayout.LayoutParams exoSeekBarParams = (FrameLayout.LayoutParams) mainBinding.seekBar.getLayoutParams();
                exoSeekBarParams.bottomMargin = navigationHeight + wrapperParams.bottomMargin - exoSeekBar.getHeight() / 3;
                showSeekBarIfCan();
            }
        }, 1000);

        BOTTOM_NAVIGATION_MARGIN = wrapperParams.height;

        ViewPager2 mainPager = mainBinding.mainViewPager;

        FrameLayout.LayoutParams pagerParams = (FrameLayout.LayoutParams) mainPager.getLayoutParams();
        pagerParams.bottomMargin = wrapperParams.height + wrapperParams.bottomMargin;
        mainPager.requestLayout();

        //mainPager.post(() -> pagerHeight = mainPager.getHeight());

        mainBinding.bottomNavigationView.post(() -> {
            navigationHeight = mainBinding.bottomNavigationView.getHeight();
        });

    }

    public void showSeekBarIfCan() {
        if(mainBinding != null && mainBinding.mainViewPager.getCurrentItem() == 1) {
            exoSeekBar.setVisibility(View.VISIBLE);
        }
    }

    public void hidePlayerDragView() {
        if(mainBinding != null) {
            currentPlayerDragViewVisibility = mainBinding.dragView.getVisibility();
            mainBinding.dragView.setVisibility(View.GONE);
        }
    }

    private void restoreCurrentPlayerDragViewVisibility() {
        if(mainBinding != null) {
            mainBinding.dragView.setVisibility(currentPlayerDragViewVisibility);
        }
    }

    @Override
    protected void onDestroy() {
        mainNavigation.setItemsTouchEffect(false);
        mainNavigation.release();
        mainNavigation.setOnItemSelectedListener(null);
        mainBinding.mainViewPager.setAdapter(null);
        mainBinding.mainViewPager.unregisterOnPageChangeCallback(mCallback);
        mainBinding.seekBar.release();
        mainBinding = null;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (exoSeekBar == null || pagerAdapter == null) return;

        final int item = mainBinding.mainViewPager.getCurrentItem();

        //showAllViews();
    }


    public FxExoSeekBar getExoSeekBar() {
        return exoSeekBar;
    }

    public int getBottomMargin() {
        return BOTTOM_NAVIGATION_MARGIN;
    }


    /*
     *  notify to fragment methods
     */


}