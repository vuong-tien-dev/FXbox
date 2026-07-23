package com.vtstudio.fxbox.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.adapters.CommentAdapter;
import com.vtstudio.fxbox.api.CommentDetails;
import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.database.dao.CommentDao;
import com.vtstudio.fxbox.database.dao.ShortsUserDao;
import com.vtstudio.fxbox.databinding.BottomSheetCommentBinding;
import com.vtstudio.fxbox.databinding.CommentItemLayoutBinding;
import com.vtstudio.fxbox.databinding.ContentCommentLayoutBinding;
import com.vtstudio.fxbox.databinding.ContentReplyLayoutBinding;
import com.vtstudio.fxbox.api.TiktokDataSourceBuilder;
import com.vtstudio.fxbox.media.models.tiktok.Comment;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.server.ApiCaller;
import com.vtstudio.fxbox.utils.TimeUtils;
import com.vtstudio.fxbox.utils.ViewsUtils;

import java.util.ArrayList;
import java.util.List;

public class BottomSheetDialogHelper {
    public static int COMMENT_LIMIT = 15;
    public static void showCommentBottomSheetDialog(Context context, ShortsVideo shorts) {
        // inflate layout
        BottomSheetCommentBinding binding = BottomSheetCommentBinding.inflate(LayoutInflater.from(context));
        FxRoomDB database = FxRoomDB.get(context);
        ShortsUserDao userDao = database.shortsUserDao();
        CommentAdapter adapter = new CommentAdapter(new ArrayList<>(), shorts.getAwemeId());

        //
        setupCommentList(context, binding.contentComment.bottomSheetListComment, adapter);

        List<Comment> commentList = database.commentDao().getCommentsByVideoId(shorts.getAwemeId(), COMMENT_LIMIT, 0);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);

        // bind user to comment
        if (commentList != null && !commentList.isEmpty()) {
            for(Comment comment : commentList){
                ShortsUser user = userDao.getUserById(comment.getUid());
                comment.setShortsUser(user);
            }
            adapter.setCommentList(commentList);
        } else {
            // if is empty show empty state
            showEmptyState(binding.contentComment);

            // set click when click on tải xuống button
            binding.contentComment.bottomSheetCommentDownloadBtn.setOnClickListener(v -> {
                showLoadingState(binding.contentComment);
                downloadComments(context, shorts, binding, adapter);
            });
        }

        adapter.setAuthorId(shorts.getAuthorId());

        /*
        Phần cấu hình cho nút xem câu trả lời
        */
        adapter.setItemClickListener((parent, view, position, id) -> {
            // inflate layout
            ContentReplyLayoutBinding replyBinding = ContentReplyLayoutBinding.inflate(LayoutInflater.from(view.getContext()));
            View bottomRootView = binding.getRoot();
            // resize layout
            replyBinding.getRoot().setMinHeight(bottomRootView.getHeight() + bottomRootView.getPaddingBottom() + bottomRootView.getPaddingTop());
            replyBinding.getRoot().setMaxHeight(bottomRootView.getHeight() + bottomRootView.getPaddingBottom() + bottomRootView.getPaddingTop());

            // get reply of comment at clicked position
            Comment comment = ((CommentAdapter) parent).getCommentList().get(position);
            ShortsUser user = comment.getShortsUser();
//
            // bind information to comment was clicked on reply button
            CommentItemLayoutBinding item = replyBinding.commentWasRepliedLayout;
            ImageView userAvatar = item.commentUserAvatar;
//
            item.commentContent.setText(comment.getContent().trim());
            item.commentCreateTime.setText(TimeUtils.getTimeAgo(comment.getCreateTime()*1000L, replyBinding.getRoot().getContext()));
            item.commentDiggCount.setText(String.valueOf(comment.getDiggCount()));
//
            if(comment.getShortsUser() != null){
                Glide.with(replyBinding.getRoot().getContext()).load(user.getAvatarPath()).placeholder(R.drawable.default_avatar_user)
                        .skipMemoryCache(true).override(userAvatar.getWidth(), userAvatar.getHeight())
                        .into(userAvatar);
                item.commentUserName.setText(user.getNickName().trim());
            }

            // create reply adapter
            CommentAdapter replyAdapter = new CommentAdapter(new ArrayList<>(), comment.getMediaVideoId());
            replyAdapter.setViewMode(CommentAdapter.MODE_REPLY);
            replyAdapter.setCommentId(comment.getId());
            replyAdapter.setAuthorId(((CommentAdapter) parent).getAuthorId());
            List<Comment> replyList = database.commentDao().getReplyById(comment.getId(), COMMENT_LIMIT, 0);

            replyBinding.bottomSheetListComment.setAdapter(replyAdapter);
            replyBinding.bottomSheetListComment.setLayoutManager(new LinearLayoutManager(replyBinding.getRoot().getContext()));

            // fetch data replies from database if is empty show empty state
            if (replyList != null && !replyList.isEmpty()) {
                for(Comment reply : replyList){
                    ShortsUser userReply = userDao.getUserById(reply.getUid());
                    reply.setShortsUser(userReply);
                }
                requestLayout(replyBinding.bottomSheetListComment);
                replyAdapter.setCommentList(replyList);
            } else {
                showEmptyState(replyBinding);
                replyBinding.bottomSheetCommentDownloadBtn.setOnClickListener(v -> {
                    showLoadingState(replyBinding);
                    downloadReplies(context, comment, replyBinding, replyAdapter);
                });
            }

            Animation animOut = AnimationUtils.loadAnimation(context, R.anim.slide_out_left);
            animOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    binding.contentComment.getRoot().setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            Animation animIn = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
            binding.getRoot().addView(replyBinding.getRoot());
            binding.contentComment.getRoot().startAnimation(animOut);
            replyBinding.getRoot().startAnimation(animIn);

            // make touch effect on back button
            ViewsUtils.setTouchScaleEffect(replyBinding.bottomSheetBack, 0.95f);
            replyBinding.bottomSheetBack.setOnClickListener(v -> {
                Animation out = AnimationUtils.loadAnimation(v.getContext(), R.anim.fade_out);
                out.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        binding.getRoot().removeView(replyBinding.getRoot());
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                replyBinding.getRoot().startAnimation(out);
                binding.contentComment.getRoot().setVisibility(View.VISIBLE);
            });
        });
        /////////////////////////////////////////////////////////////////////////////////////

        bottomSheetDialog.setContentView(binding.getRoot());
        bottomSheetDialog.setOnCancelListener(dialog -> {
            binding.contentComment.bottomSheetListComment.setAdapter(null);
            adapter.release();
        });
        bottomSheetDialog.show();
    }

    public static void requestLayout(RecyclerView recyclerView){
        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        recyclerView.requestLayout();
    }

    public static void setupCommentList(Context context, RecyclerView recyclerView, CommentAdapter adapter) {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
    }

    public static void showEmptyState(ContentCommentLayoutBinding binding) {
        binding.bottomSheetCommentIsNoneWrapper.setVisibility(View.VISIBLE);
        binding.bottomSheetLoadingComment.setVisibility(View.GONE);
    }

    public static void showLoadingState(ContentCommentLayoutBinding binding) {
        binding.bottomSheetCommentIsNoneWrapper.setVisibility(View.GONE);
        binding.bottomSheetLoadingComment.setVisibility(View.VISIBLE);
    }

    public static void showErrorState(ContentCommentLayoutBinding binding) {
        binding.bottomSheetCommentIsNoneWrapper.setVisibility(View.VISIBLE);
        binding.bottomSheetCommentDownloadBtn.setVisibility(View.GONE);
        binding.bottomSheetLoadingComment.setVisibility(View.GONE);
        binding.bottomSheetLoadingComment.clearAnimation();
    }

    public static void showEmptyState(ContentReplyLayoutBinding binding) {
        binding.bottomSheetCommentIsNoneWrapper.setVisibility(View.VISIBLE);
        binding.bottomSheetLoadingComment.setVisibility(View.GONE);
    }

    public static void showLoadingState(ContentReplyLayoutBinding binding) {
        binding.bottomSheetCommentIsNoneWrapper.setVisibility(View.GONE);
        binding.bottomSheetLoadingComment.setVisibility(View.VISIBLE);
    }

    public static void showErrorState(ContentReplyLayoutBinding binding) {
        binding.bottomSheetCommentIsNoneWrapper.setVisibility(View.VISIBLE);
        binding.bottomSheetLoadingComment.setVisibility(View.GONE);
        binding.bottomSheetCommentDownloadBtn.setVisibility(View.GONE);
        binding.bottomSheetLoadingComment.clearAnimation();
        binding.bottomSheetLoadingComment.setVisibility(View.GONE);
    }

    private static void downloadComments(Context context, ShortsVideo shorts, BottomSheetCommentBinding binding, CommentAdapter adapter) {
        FxRoomDB newDb = FxRoomDB.get(context);
        ShortsUserDao shortsUserDao = newDb.shortsUserDao();
        CommentDao commentDao2 = newDb.commentDao();

        if(shorts.getAwemeId() == null) {
            Toast.makeText(context, " null awemeId", Toast.LENGTH_LONG).show();
            return;
        }

        ApiCaller.with(context).asTikTokApi().asComment().url(shorts.getAwemeId()).callback(response -> {
            if (response.isSuccessfully()) {
                CommentDetails details = response.getModel();
                if (details != null) {
                    if (details.getTotal() == 0) {
                        showEmptyState(binding.contentComment);
                        binding.contentComment.bottomSheetCommentDownloadBtn.setVisibility(View.GONE);
                        binding.contentComment.bottomSheetCommentIsNoneTv.setText(context.getString(R.string.comments_is_none));
                    } else {
                        List<Comment> commentList = details.getComments();
                        for (Comment comment : commentList) {
                            comment.setMediaVideoId(shorts.getAwemeId());
                            TiktokDataSourceBuilder builder = new TiktokDataSourceBuilder(context)
                                    .doOnSuccess(() -> {
                                        commentDao2.insert(comment);
                                        shortsUserDao.insert(comment.getShortsUser());
                                    });
                            builder.createAuthorAvatar(comment.getShortsUser().getAvatarUrl(), comment.getUid(), 30, false);
                        }
                        binding.contentComment.bottomSheetLoadingComment.setVisibility(View.GONE);
                        adapter.setCommentList(commentList);
                        adapter.setHasMore(details.isHasMore());
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    showEmptyState(binding.contentComment);
                    binding.contentComment.bottomSheetCommentDownloadBtn.setVisibility(View.GONE);
                    binding.contentComment.bottomSheetCommentIsNoneTv.setText(context.getString(R.string.cannot_get_comments));
                }
            }
            else {
                showErrorState(binding.contentComment);
                binding.contentComment.bottomSheetCommentDownloadBtn.setText(context.getString(R.string.try_again));
                binding.contentComment.bottomSheetCommentIsNoneTv.setText(context.getString(R.string.server_errol));
            }
        }).get();
    }


    public static void downloadReplies(Context context, Comment shorts, ContentReplyLayoutBinding binding, CommentAdapter adapter) {
        FxRoomDB newDb = FxRoomDB.get(context);
        ShortsUserDao shortsUserDao = newDb.shortsUserDao();
        CommentDao commentDao2 = newDb.commentDao();

        ApiCaller.with(context).asTikTokApi().asComment().asReply().url(shorts.getId())
                .offset(Math.min(49, shorts.getReplyCount()))
                .callback(response -> {
                    if (response.isSuccessfully()) {
                        CommentDetails details = response.getModel();
                        if (details != null) {
                            if(details.getTotal() == 0){
                                showEmptyState(binding);
                                binding.bottomSheetCommentDownloadBtn.setVisibility(View.GONE);
                                binding.bottomSheetCommentIsNoneTv.setText(context.getString(R.string.comments_is_none));
                            } else {
                                List<Comment> commentList = details.getComments();
                                for (Comment comment : commentList) {
                                    comment.setReplyId(shorts.getId());
                                    TiktokDataSourceBuilder builder = new TiktokDataSourceBuilder(context)
                                            .doOnSuccess(() -> {
                                                commentDao2.insert(comment);
                                                shortsUserDao.insert(comment.getShortsUser());
                                            });
                                    builder.createAuthorAvatar(comment.getShortsUser().getAvatarUrl(), comment.getUid(), 30, false);
                                }
                                binding.bottomSheetLoadingComment.setVisibility(View.GONE);
                                adapter.setCommentList(commentList);
                                adapter.setHasMore(details.isHasMore());
                                adapter.notifyDataSetChanged();
                            }
                        } else {
                            showEmptyState(binding);
                            binding.bottomSheetCommentDownloadBtn.setVisibility(View.GONE);
                            binding.bottomSheetCommentIsNoneTv.setText(context.getString(R.string.cannot_get_comments));
                        }
                    } else {
                        showErrorState(binding);
                        binding.bottomSheetCommentDownloadBtn.setText(context.getString(R.string.try_again));
                        binding.bottomSheetCommentIsNoneTv.setText(context.getString(R.string.server_errol));
                    }
                }).get();
    }
}