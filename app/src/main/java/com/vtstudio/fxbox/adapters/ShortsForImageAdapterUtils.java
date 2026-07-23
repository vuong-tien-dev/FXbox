package com.vtstudio.fxbox.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.activity.ShortsUserProfile;
import com.vtstudio.fxbox.activity.UserProfile;
import com.vtstudio.fxbox.databinding.ShortsForImagesItemLayoutBinding;
import com.vtstudio.fxbox.fxviews.dialog.FxCommentDialog;
import com.vtstudio.fxbox.fxviews.textview.FxExpandableTextView;
import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.models.tiktok.ShortsMusic;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.utils.FormatUtils;

import java.io.File;

public class ShortsForImageAdapterUtils {
    public static void makeExpandEdgeEffect(@NonNull ShortsForImagesItemLayoutBinding binding) {
        binding.shortsContents.shortsDescriptionTv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (binding.shortsContents.shortsDescriptionTv.expandable()) {
                    binding.shortsContents.shortsDescriptionTv.addOnExpandListener(new FxExpandableTextView.SimpleOnExpandListener() {
                        @Override
                        public void onExpanding(@NonNull FxExpandableTextView view) {
                            binding.shortsDescriptionFadingEdge.getLayoutParams().height =
                                    (int) (binding.shortsContents.getRoot().getHeight() -
                                            binding.shortsContents.shortsDescriptionTv.getTop() * 0.7f);

                            binding.shortsDescriptionFadingEdge.requestLayout();
                        }

                        @Override
                        public void onCollapsing(@NonNull FxExpandableTextView view) {
                            binding.shortsDescriptionFadingEdge.getLayoutParams().height =
                                    (int) (binding.shortsContents.getRoot().getHeight() -
                                            binding.shortsContents.shortsDescriptionTv.getTop() * 0.7f);

                            binding.shortsDescriptionFadingEdge.requestLayout();
                        }


                        @Override
                        public void onCollapsed(@NonNull FxExpandableTextView view) {
                            binding.shortsDescriptionFadingEdge.getLayoutParams().height = 0;
                            binding.shortsDescriptionFadingEdge.requestLayout();
                        }
                    });
                }

                binding.shortsContents.shortsDescriptionTv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    public static void setDefaultOrUserAvatar(ShortsUser user, ImageView view) {
        if (view == null) return;

        Context context = view.getContext();

        if (user != null) {
            Glide.with(context).load(user.getAvatarPath())
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .override(view.getWidth(), view.getHeight())
                    .placeholder(R.drawable.default_avatar_user)
                    .into(view);
        } else {
            Glide.with(context).load(R.drawable.default_avatar_user)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .override(view.getWidth(), view.getHeight())
                    .into(view);
        }
    }

    public static void setDefaultOrMusicAvatar(ShortsMusic music, String pathDefault, ImageView view) {
        if (view == null) return;

        Context context = view.getContext();

        String musicPath;
        if (music != null) {
            if (music.getId() != null && new File(music.getFxThumbnailPath()).exists())
                musicPath = music.getFxThumbnailPath();
            else musicPath = pathDefault;
        } else musicPath = pathDefault;

        Glide.with(context).load(musicPath)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.default_avatar_user)
                .override(view.getWidth(), view.getHeight())
                .into(view);
    }

    public static void bindInteractiveViews(ShortsForImagesItemLayoutBinding binding) {
        Context context = binding.getRoot().getContext();
        ImageView comment = binding.shortsContents.shortsCommentButton;
        Glide.with(context)
                .load(R.drawable.comment)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .override(comment.getWidth(), comment.getHeight())
                .into(comment);

        ImageView send = binding.shortsContents.shortsShareButton;

        Glide.with(context)
                .load(R.drawable.shorts_share)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .override(send.getWidth(), send.getHeight())
                .into(send);
    }

    public static void setLikeCountIfShortsVideo(FxMediaVideo shorts, TextView textView, boolean isFavorite) {
        if (shorts instanceof ShortsVideo) {
            ShortsVideo shortsVideo = (ShortsVideo) shorts;
            shortsVideo.setLikeCount(shortsVideo.getLikeCount() + ((isFavorite) ? 1 : -1));
            textView.setText(FormatUtils.formatCount(shortsVideo.getLikeCount()));
        }
    }

    public static void setListenerOfShareButton(@NonNull View view, FxMediaVideo media){
        view.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("video/mp4");
            intent.putExtra(Intent.EXTRA_STREAM, media.getPlayUri());
            view.getContext().startActivity(Intent.createChooser(intent, "Share"));
        });
    }

    public static void setCommentButtonIfNeed (@NonNull View view, FxMediaVideo shorts, Context context){
        if (shorts instanceof ShortsVideo && context instanceof FragmentActivity) {
            view.setOnClickListener(v -> {
                FxCommentDialog commentDialog = new FxCommentDialog(context, (ShortsVideo) shorts);
                commentDialog.show();
            });
        }
    }

    public static void setUserProfileClickIfNeed (@NonNull View view, FxMediaVideo shorts, Context context) {

        if(shorts instanceof ShortsVideo) {
            view.setOnClickListener(v -> {
                Intent intent = new Intent(context, UserProfile.class);
                intent.putExtra(ShortsUserProfile.INTENT_DATA_SHORTS_USER_ID, ((ShortsVideo) shorts).getAuthorId());
                context.startActivity(intent);
                ((FragmentActivity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }
    }


    public static void disableListenerOf(ShortsForImagesItemLayoutBinding binding) {
        if (binding == null) return;

        binding.shortsVideoContainer.setOnClickListener(null);
        binding.shortsContents.shortsFavoriteClick.setOnClickListener(null);
        binding.shortsContents.shortsCommentContainer.setOnClickListener(null);
        binding.shortsContents.shortsAuthorAvatar.setOnClickListener(null);
        binding.shortsContents.shortsShareButton.setOnClickListener(null);
    }

}
