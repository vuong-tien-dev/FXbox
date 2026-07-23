package com.vtstudio.fxbox.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.adapters.ShortsVideoProfileAdapter;
import com.vtstudio.fxbox.api.TiktokDataSourceBuilder;
import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.database.dao.RawResponseDao;
import com.vtstudio.fxbox.database.dao.ShortsMusicDao;
import com.vtstudio.fxbox.database.dao.ShortsUserDao;
import com.vtstudio.fxbox.database.dao.ShortsVideoDao;
import com.vtstudio.fxbox.databinding.ActivityUserProfileBinding;
import com.vtstudio.fxbox.listeners.OnServiceConnectionListener;
import com.vtstudio.fxbox.media.models.Media;
import com.vtstudio.fxbox.media.models.RawResponse;
import com.vtstudio.fxbox.media.models.tiktok.ShortsMusic;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.media.player.MediaRequest;
import com.vtstudio.fxbox.server.ApiCaller;
import com.vtstudio.fxbox.server.OnResponseListener;
import com.vtstudio.fxbox.server.Response;
import com.vtstudio.fxbox.utils.ClipboardUtils;
import com.vtstudio.fxbox.utils.FormatUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by mertsimsek on 01/10/2017.
 */

public class UserProfile extends FxBaseActivity {

    private ActivityUserProfileBinding binding;
    public static final String INTENT_DATA_SHORTS_USER_ID = "shorts_uid";

    private String userId;
    private ShortsUser user;
    private List<ShortsVideo> shortsVideos;
    private int titleTextColor;
    private int oppositeTitleTextColor;
    private boolean flag_is_destroyed;

    private int mAlpha;
    private int mRed;
    private int mGreen;
    private int mBlue;

    private int dAlpha;
    private int dRed;
    private int dGreen;
    private int dBlue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        if(getDataIntent() && initData()){
            initViews();
        }

    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    private void initViews() {

        oppositeTitleTextColor = titleTextColor = getColor(R.color.white);

        mAlpha = Color.alpha(titleTextColor);
        mRed = Color.red(titleTextColor);
        mGreen = Color.green(titleTextColor);
        mBlue = Color.blue(titleTextColor);

        Toolbar toolbar = binding.toolbar;
        toolbar.setTitle("Người dùng");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setNavigationIconColor(titleTextColor);

        binding.appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {

            final int scrollRange = appBarLayout.getTotalScrollRange();
            if(oppositeTitleTextColor == titleTextColor || scrollRange == 0) return;

            final int ver = Math.abs(verticalOffset);
            final float ratio = (float) ver/scrollRange;

            final int colorTransition = Color.argb((mAlpha - (int) (dAlpha * ratio)), (mRed - (int) (dRed * ratio)), (mGreen - (int) (dGreen * ratio)), (mBlue - (int) (dBlue * ratio)));

            toolbar.setTitleTextColor(colorTransition);
            setNavigationIconColor(colorTransition);

        });

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 3);
        ShortsVideoProfileAdapter adapter = new ShortsVideoProfileAdapter(shortsVideos);
        binding.userProfileVideos.setLayoutManager(layoutManager);
        binding.userProfileVideos.setAdapter(adapter);

        // get contentScrim color
        initThumbnailAttributes();

        initInformation();

        Log.d("ShortsUser", "from profile" + user.getAvatarPath());

        binding.userInformation.profileUserUniqueId.setVerified(user.getVerified());
        binding.userInformation.profileUserUniqueId.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    binding.userInformation.profileUserUniqueId.setTextColor(getColor(R.color.rgb_180));
                    return true;
                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    ClipboardUtils.copyToClipboard(UserProfile.this,  binding.userInformation.profileUserUniqueId.getText().toString());
                    Toast.makeText(UserProfile.this, getString(R.string.copy_to_clip_successfully), Toast.LENGTH_SHORT).show();
                }

                if(event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP){
                    binding.userInformation.profileUserUniqueId.setTextColor(getColor(R.color.rgb_240));
                    return true;
                }

                return false;
            }
        });

        boundFxPlayer(new OnServiceConnectionListener() {
            @Override
            public void onServiceConnected() {
                adapter.setOnItemClickListener(position -> {
                    String nameList = getString(R.string.user_video_list_of, user.getNickName());
                    Intent intent = new Intent(UserProfile.this, PlayVideo.class);

                    List<Media> mediaList = new ArrayList<>(shortsVideos);

                    MediaRequest request = new MediaRequest(PlayVideo.newPlayingRequestId());
                    Log.d("fx_player", "id: " + request.getId());
                    request.setName(nameList);
                    request.setMediaList(mediaList);
                    request.setCurrentPlayingIndex(position);
                    UserProfile.this.getFxPlayer().addRequest(request);
                    intent.putExtra(PlayVideo.KEY_NAME_LIST, nameList);
                    intent.putExtra(PlayVideo.KEY_PLAYING_INDEX, position);
                    //intent.putExtra(PlayVideo.DATA_RETRIEVAL_METHOD, PlayVideo.METHOD_DATA_FROM_REQUEST);
                    startActivity(intent);
                    overridePendingTransition(R.anim.scale_in_bot_right, 0);
                });
            }

            @Override
            public void onServiceDisconnected() {

            }
        });

        binding.userInformation.profileSyncDataWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.userInformation.profileUserDataLoading.setVisibility(View.VISIBLE);
                v.setClickable(false);
                v.setAlpha(0.8f);
                startSyncAuthorData();
            }
        });

    }

    private void initInformation() {
        Toolbar toolbar = binding.toolbar;
        toolbar.setTitle(user.getNickName());
        binding.userInformation.profileUserUniqueId.setText("@" + user.getUniqueId());
        binding.userInformation.userFollowingCount.setText(FormatUtils.formatCount(user.getFollowingCount()));
        binding.userInformation.userFollowerCount.setText(FormatUtils.formatCount(user.getFollowerCount()));
        binding.userInformation.userHeartCount.setText(FormatUtils.formatCount(user.getTotalFavorite()));
        binding.userInformation.profileUserSignature.setText(user.getSignature());
    }

    private void startSyncAuthorData() {
        ApiCaller.with(this).asTikTokApi().asUser().url(userId).callback(new OnResponseListener<ShortsUser>() {
            @Override
            public void onResponse(Response<ShortsUser> response) {
                if(response.isSuccessfully()){
                    ShortsUserDao dao = FxRoomDB.get(UserProfile.this).shortsUserDao();
                    ShortsUser data = response.getModel();
                    if(user != null && data != null){

                        if(data.getFollowingCount() != 0){
                            user.setFollowingCount(data.getFollowingCount());
                        }
                        if(data.getFollowerCount() != 0){
                            user.setFollowerCount(data.getFollowerCount());
                        }
                        if(data.getTotalFavorite() != 0){
                            user.setTotalFavorite(data.getTotalFavorite());
                        }
                        if(data.getNickName() != null) user.setNickName(data.getNickName());
                        if (data.getSignature() == null) user.setSignature(data.getSignature());
                        if (data.getShareUrl() != null) user.setShareUrl(data.getShareUrl());
                        if (data.getUniqueId() != null) user.setUniqueId(data.getUniqueId());
                        if (data.getAvatarUrl() != null) user.setAvatarUrl(data.getAvatarUrl());

                        if(!user.getVerified()) user.setVerified(data.getVerified());

                        RawResponseDao rawResponseDao = FxRoomDB.get(UserProfile.this).rawResponseDao();
                        rawResponseDao.insert(new RawResponse(user.getUid(), Media.MediaType.TYPE_SHORTS_TIKTOK, data.getRawResponse()));

                        TiktokDataSourceBuilder builder = new TiktokDataSourceBuilder(UserProfile.this);
                        builder.doOnSuccess(() -> {
                            if(!flag_is_destroyed) {
                                initThumbnailAttributes();
                                initInformation();
                                showSuccessStatus();
                            }
                            dao.update(user);
                        })
                                .doOnFailed(() -> {
                                    if(flag_is_destroyed) return;

                                    Toast.makeText(UserProfile.this, getString(R.string.get_data_img_failed), Toast.LENGTH_LONG).show();
                                    showFailedStatus();
                                })
                                .createAuthorAvatar(data.getAvatarUrl(), user.getUid(), 80, true);
                    }
                } else {
                    if(!flag_is_destroyed) showFailedStatus();
                }
            }
        }).get();
    }

    private void showSuccessStatus() {
        binding.userInformation.profileUserDataLoading.setVisibility(View.GONE);
        binding.userInformation.profileSyncData.setText(getString(R.string.sync_data));
        binding.userInformation.profileSyncDataWrapper.setClickable(true);
        binding.userInformation.profileSyncDataWrapper.setAlpha(1);
    }

    private void showFailedStatus(){
        binding.userInformation.profileUserDataLoading.setVisibility(View.GONE);
        binding.userInformation.profileSyncData.setText(getString(R.string.failed));
        binding.userInformation.profileSyncDataWrapper.setClickable(true);
        binding.userInformation.profileSyncDataWrapper.setAlpha(1);
    }

    private void initThumbnailAttributes(){

        if(new File(user.getAvatarPath()).exists()) {
            Glide.with(this).asBitmap().load(user.getAvatarPath()).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
                    .override(binding.userInformation.profileUserAvatar.getWidth(), binding.userInformation.profileUserAvatar.getHeight())
                    .placeholder(R.drawable.default_avatar_user)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Palette.from(resource).generate(palette -> {
                                if (palette != null) {
                                    // Lấy màu chủ đạo
                                    final int dominantColor = palette.getDominantColor(UserProfile.this.getColor(R.color.rgb_15));

                                    int red = Color.red(dominantColor);
                                    int green = Color.green(dominantColor);
                                    int blue = Color.blue(dominantColor);

                                    int threshold = 60;
                                    boolean isNearWhite = (red > 255 - threshold) && (green > 255 - threshold) && (blue > 255 - threshold);

                                    @SuppressLint("UseCompatLoadingForDrawables") GradientDrawable gradientDrawable = new GradientDrawable();
                                    gradientDrawable.setOrientation(GradientDrawable.Orientation.BOTTOM_TOP);
                                    gradientDrawable.setColors(new int[]{dominantColor, UserProfile.this.getColor(R.color.rgb_15)});
                                    binding.userInformation.getRoot().setBackground(gradientDrawable);

                                    // tìm thành phần màu chính
                                    final int mainMaxColor = Math.max(Math.max(red, green), blue);

                                    // làm tối màu chính
                                    if(mainMaxColor == red) red *= 0.9f;
                                    else if(mainMaxColor == green) green *= 0.9f;
                                    else blue *= 0.9f;

                                    // tạo màu chỉnh sửa
                                    final int modifiedColor = Color.argb(100, red, green, blue);

                                    if (isNearWhite) {

                                        oppositeTitleTextColor = getColor(R.color.rgb_15);

                                        final int oAlpha = Color.alpha(oppositeTitleTextColor);
                                        final int oRed = Color.red(oppositeTitleTextColor);
                                        final int oGreen = Color.green(oppositeTitleTextColor);
                                        final int oBlue = Color.blue(oppositeTitleTextColor);

                                        dAlpha = Math.abs(UserProfile.this.mAlpha - oAlpha);
                                        dRed = Math.abs(UserProfile.this.mRed -oRed);
                                        dGreen = Math.abs(UserProfile.this.mGreen -oGreen);
                                        dBlue = Math.abs(UserProfile.this.mBlue -oBlue);

                                        Log.d("UserProfile", "isNearWhite");
                                    }

                                    @SuppressLint("UseCompatLoadingForDrawables") GradientDrawable backgroundDrawable = (GradientDrawable) getDrawable(R.drawable.corner_180_shape);
                                    backgroundDrawable.setColor(modifiedColor);

                                    binding.userInformation.profileSyncDataWrapper.setBackground(backgroundDrawable);
                                    binding.userInformation.profileSyncData.setTextColor(oppositeTitleTextColor);
                                    binding.userInformation.profileAddCollection.setBackground(backgroundDrawable);
                                    binding.userInformation.profileAddCollection.setTextColor(oppositeTitleTextColor);

                                    binding.userInformation.profileUserDataLoading.getIndeterminateDrawable().setColorFilter(new LightingColorFilter(Color.TRANSPARENT, oppositeTitleTextColor));
                                    binding.collapsingToolbarLayout.setContentScrimColor(dominantColor);
                                }
                            });

                            binding.userInformation.profileUserAvatar.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        } else {
            Glide.with(this).asBitmap().load(user.getAvatarPath()).diskCacheStrategy(DiskCacheStrategy.NONE)
                    .override(binding.userInformation.profileUserAvatar.getWidth(), binding.userInformation.profileUserAvatar.getHeight())
                    .placeholder(R.drawable.default_avatar_user)
                    .into(binding.userInformation.profileUserAvatar);
        }
    }

    public void setNavigationIconColor(@ColorInt int color) {
        Drawable navigationIcon = binding.toolbar.getNavigationIcon();
        if (navigationIcon != null) {
            navigationIcon = navigationIcon.mutate();
            navigationIcon.setTint(color);
            binding.toolbar.setNavigationIcon(navigationIcon);
        }
    }


    private boolean initData() {

        FxRoomDB database = FxRoomDB.get(this);
        ShortsUserDao userDao = database.shortsUserDao();
        ShortsVideoDao videoDao = database.shortsVideoDao();
        ShortsMusicDao musicDao = database.shortsMusicDao();

        user = userDao.getUserById(userId);

        if(user == null) return false;

        shortsVideos = videoDao.getVideoByUserId(userId);

        if(shortsVideos == null || shortsVideos.isEmpty()) return false;

        for(ShortsVideo video : shortsVideos) {
            video.setShortsUser(user);
            ShortsMusic music3 = musicDao.getById(video.getMusicId());
            video.setShortsMusic(music3);
        }

        return true;
    }

    private boolean getDataIntent() {
        userId = getIntent().getStringExtra(INTENT_DATA_SHORTS_USER_ID);
        return userId != null;
    }

    @Override
    protected void onDestroy() {
        binding.userProfileVideos.setAdapter(null);
        user = null;
        shortsVideos = null;
        binding = null;
        super.onDestroy();
    }
}
