package com.vtstudio.fxbox.activity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.adapters.ShortsVideoProfileAdapter;
import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.database.dao.ShortsMusicDao;
import com.vtstudio.fxbox.database.dao.ShortsUserDao;
import com.vtstudio.fxbox.database.dao.ShortsVideoDao;
import com.vtstudio.fxbox.databinding.ActivityShortsUserProfileBinding;
import com.vtstudio.fxbox.listeners.OnServiceConnectionListener;
import com.vtstudio.fxbox.media.models.SocialUserType;
import com.vtstudio.fxbox.media.models.Media;
import com.vtstudio.fxbox.media.models.tiktok.ShortsMusic;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.media.player.MediaRequest;
import com.vtstudio.fxbox.utils.FormatUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShortsUserProfile extends FxBaseActivity {
    private ActivityShortsUserProfileBinding binding;
    public static final String INTENT_DATA_SHORTS_USER_ID = "shorts_uid";

    private String userId;
    private ShortsUser user;
    private List<ShortsVideo> shortsVideos;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityShortsUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if(getDataIntent() && initData()){
            initViews();
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

    private void initViews() {
        if(new File(user.getAvatarPath()).exists()) {
            Glide.with(this).asBitmap().load(user.getAvatarPath()).diskCacheStrategy(DiskCacheStrategy.NONE)
                    .override(binding.userAvatar.getWidth(), binding.userAvatar.getHeight())
                    .placeholder(R.drawable.default_avatar_user)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(@Nullable Palette palette) {
                                    if (palette != null) {
                                        // Lấy màu chủ đạo
                                        int dominantColor = palette.getDominantColor(ShortsUserProfile.this.getColor(R.color.rgb_15));

                                        int modifiedColor = Color.argb(100, Color.red(dominantColor), Color.green(dominantColor), Color.blue(dominantColor));
                                        // Thiết lập màu chủ đạo vào tệp gradient_background.xml
                                        @SuppressLint("UseCompatLoadingForDrawables") GradientDrawable gradientDrawable = new GradientDrawable();
                                        gradientDrawable.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
                                        gradientDrawable.setColors(new int[]{modifiedColor, ShortsUserProfile.this.getColor(R.color.rgb_15)});
                                        binding.getRoot().setBackground(gradientDrawable);
                                    }
                                }
                            });

                            binding.userAvatar.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        } else {
            Glide.with(this).asBitmap().load(user.getAvatarPath()).diskCacheStrategy(DiskCacheStrategy.NONE)
                    .override(binding.userAvatar.getWidth(), binding.userAvatar.getHeight())
                    .placeholder(R.drawable.default_avatar_user)
                    .into(binding.userAvatar);
        }
        if(Objects.equals(user.getSocialUserType(), SocialUserType.USER_TIKTOK)) {
            Glide.with(this).asBitmap().load(R.drawable.ssm_tiktok_icon).diskCacheStrategy(DiskCacheStrategy.NONE)
                    .override(binding.socialMediaIcon.getWidth(), binding.socialMediaIcon.getHeight())
                    .into(binding.socialMediaIcon);
        }
        binding.userNickname.setText(getString(R.string.user_name, user.getNickName()));
        binding.userUniqueId.setText(getString(R.string.user_unique_id, user.getUniqueId()));
        binding.userFollowingCount.setText(getString(R.string.following_count, FormatUtils.formatCount(user.getFollowingCount())));
        binding.userFollowerCount.setText(getString(R.string.follower_count, FormatUtils.formatCount(user.getFollowerCount())));
        binding.userHeartCount.setText(getString(R.string.total_favorited, FormatUtils.formatCount(user.getTotalFavorite())));
        binding.userSignature.setText(getString(R.string.signature, user.getSignature()));

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 3);
        ShortsVideoProfileAdapter adapter = new ShortsVideoProfileAdapter(shortsVideos);

        boundFxPlayer(new OnServiceConnectionListener() {
            @Override
            public void onServiceConnected() {
                adapter.setOnItemClickListener(position -> {
                    String nameList = getString(R.string.user_video_list_of, user.getNickName());
                    Intent intent = new Intent(ShortsUserProfile.this, PlayVideo.class);

                    List<Media> mediaList = new ArrayList<>(shortsVideos);

                    MediaRequest request = new MediaRequest(PlayVideo.newPlayingRequestId());
                    Log.d("fx_player", "id: " + request.getId());
                    request.setName(nameList);
                    request.setMediaList(mediaList);
                    request.setCurrentPlayingIndex(position);
                    ShortsUserProfile.this.getFxPlayer().addRequest(request);
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

        binding.userProfileVideos.setLayoutManager(layoutManager);
        binding.userProfileVideos.setAdapter(adapter);


    }

    private boolean getDataIntent() {
        userId = getIntent().getStringExtra(INTENT_DATA_SHORTS_USER_ID);
        return userId != null;
    }

    @Override
    protected void onDestroy() {
        try {
            Glide.get(this).clearMemory();
            Glide.get(this).clearDiskCache();
        } catch (Exception ignored) {}
        super.onDestroy();
    }
}
