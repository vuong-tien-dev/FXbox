package com.vtstudio.fxbox.fxviews.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.adapters.CommentAdapter;
import com.vtstudio.fxbox.api.CommentDetails;
import com.vtstudio.fxbox.api.TiktokDataSourceBuilder;
import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.database.dao.CommentDao;
import com.vtstudio.fxbox.database.dao.RawResponseDao;
import com.vtstudio.fxbox.database.dao.ShortsUserDao;
import com.vtstudio.fxbox.databinding.BottomSheetCommentBinding;
import com.vtstudio.fxbox.databinding.CommentItemLayoutBinding;
import com.vtstudio.fxbox.databinding.ContentReplyLayoutBinding;
import com.vtstudio.fxbox.helpers.BottomSheetDialogHelper;
import com.vtstudio.fxbox.listeners.SimpleAnimationListener;
import com.vtstudio.fxbox.media.models.tiktok.Comment;
import com.vtstudio.fxbox.media.models.RawResponse;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.media.utils.ModelUtils;
import com.vtstudio.fxbox.server.ApiCaller;
import com.vtstudio.fxbox.utils.TimeUtils;
import com.vtstudio.fxbox.utils.ViewsUtils;

import java.util.ArrayList;
import java.util.List;

public class FxCommentDialog extends BottomSheetDialog {

    public static final int COMMENT_LIMIT = 10;
    private final BottomSheetCommentBinding mBinding;
    private FxRoomDB mDatabase;
    private ShortsUserDao mUserDao;
    private CommentDao mCommentDao;
    private CommentAdapter mAdapter;
    private ShortsVideo mShortsItem;
    private List<Comment> mCommentList;
    private int windowWidth;
    private int windowHeight;

    public FxCommentDialog(@NonNull Context context, @NonNull ShortsVideo shortsItem) {
        super(context, R.style.TransparentBottomSheetDialog);

        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Point outSize = new Point();
        windowManager.getDefaultDisplay().getSize(outSize);
        windowWidth = outSize.x;
        windowHeight = outSize.y;

        mShortsItem = shortsItem;
        mBinding = BottomSheetCommentBinding.inflate(LayoutInflater.from(context));

//        mBinding.getRoot().getViewTreeObserver().addOnDrawListener(new ViewTreeObserver.OnDrawListener() {
//            @Override
//            public void onDraw() {
//                View root = mBinding.getRoot();
//                int[] size = new int[2];
//                root.getLocationInWindow(size);
//
//                Log.d("CommentDialog", "w: " + (windowWidth - size[0]) + ", h: " + (windowHeight - size[1]));
//            }
//        });

        ViewGroup.LayoutParams layoutParams = mBinding.commentViewsContainer.getLayoutParams();
        layoutParams.height = (int) (windowHeight*0.7f);
        mBinding.commentViewsContainer.setLayoutParams(layoutParams);

        initData(context);
        setContentView(mBinding.getRoot());
        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mAdapter.setItemClickListener(null);
                mBinding.contentComment.bottomSheetListComment.setAdapter(null);
                mAdapter.release();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        if (window != null) {

            View decorView = window.getDecorView();

            View view = decorView.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if(view != null) {
                view.setBackgroundColor(Color.TRANSPARENT);
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                WindowInsetsController controller = window.getInsetsController();
                if(controller != null) {
                    controller.hide(WindowInsets.Type.navigationBars());
                    controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            }
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @SuppressLint("SetTextI18n")
    private void initData(@NonNull Context context) {

        // ref mDatabase
        mDatabase = FxRoomDB.get(context);
        mUserDao = mDatabase.shortsUserDao();
        mCommentDao = mDatabase.commentDao();

        // create adapter
        mAdapter = new CommentAdapter(new ArrayList<>(), mShortsItem.getAwemeId());
        mAdapter.setAuthorId(mShortsItem.getAuthorId());

        // set comment count text
        String originalTitle = getContext().getString(R.string.comments);
        SpannableStringBuilder sb = new SpannableStringBuilder(originalTitle);
        int start = sb.length();
        sb.append("  ").append(String.valueOf(mShortsItem.getCommentCount()));
        int end = sb.length();
        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
        RelativeSizeSpan relSizeSpan = new RelativeSizeSpan(0.85f);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(getContext().getColor(R.color.rgb_210));
        sb.setSpan(styleSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(colorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(relSizeSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mBinding.contentComment.bottomSheetTitle.setText(new SpannableString(sb));

        // fetch comments data
        mCommentList = mCommentDao.getCommentsByVideoId(mShortsItem.getAwemeId(), COMMENT_LIMIT, 0);

        if (mCommentList == null || mCommentList.isEmpty()) {
            BottomSheetDialogHelper.showEmptyState(mBinding.contentComment);

            mBinding.contentComment.bottomSheetCommentDownloadBtn.setOnClickListener((v) -> {
                BottomSheetDialogHelper.showLoadingState(mBinding.contentComment);
                downloadComments();
            });
        } else {
            ModelUtils.fetchUserToComment(mCommentList, mUserDao);
            mAdapter.setCommentList(mCommentList);
        }

        // setup layout
        mBinding.contentComment.bottomSheetListComment.setAdapter(mAdapter);
        mBinding.contentComment.bottomSheetListComment.setLayoutManager(new LinearLayoutManager(getContext()));

        mAdapter.setItemClickListener((parent, view, position, id) -> {
            // inflate layout
            ContentReplyLayoutBinding replyBinding = ContentReplyLayoutBinding.inflate(LayoutInflater.from(view.getContext()));

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
            List<Comment> replyList = mCommentDao.getReplyById(comment.getId(), COMMENT_LIMIT, 0);

            replyBinding.bottomSheetListComment.setAdapter(replyAdapter);
            replyBinding.bottomSheetListComment.setLayoutManager(new LinearLayoutManager(replyBinding.getRoot().getContext()));

            // fetch data replies from database if is empty show empty state
            if (replyList != null && !replyList.isEmpty()) {
                for(Comment reply : replyList){
                    ShortsUser userReply = mUserDao.getUserById(reply.getUid());
                    reply.setShortsUser(userReply);
                }
                BottomSheetDialogHelper.requestLayout(replyBinding.bottomSheetListComment);
                replyAdapter.setCommentList(replyList);
            } else {
                BottomSheetDialogHelper.showEmptyState(replyBinding);
                replyBinding.bottomSheetCommentDownloadBtn.setOnClickListener(v -> {
                    BottomSheetDialogHelper.showLoadingState(replyBinding);
                    downloadReplies(replyBinding, comment, replyAdapter);
                });
            }

            Animation animOut = AnimationUtils.loadAnimation(context, R.anim.slide_out_left);
            animOut.setDuration(200);
            animOut.setAnimationListener(new SimpleAnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    mBinding.contentComment.getRoot().setVisibility(View.GONE);
                }
            });

            Animation animIn = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
            animIn.setDuration(200);
            mBinding.commentViewsContainer.addView(replyBinding.getRoot());
            mBinding.contentComment.getRoot().startAnimation(animOut);
            replyBinding.getRoot().startAnimation(animIn);

            // make touch effect on back button
            ViewsUtils.setTouchScaleEffect(replyBinding.bottomSheetBack, 0.95f);
            replyBinding.bottomSheetBack.setOnClickListener(v -> {
                Animation out = AnimationUtils.loadAnimation(v.getContext(), R.anim.fade_out);
                out.setDuration(200);
                out.setAnimationListener(new SimpleAnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mBinding.commentViewsContainer.removeView(replyBinding.getRoot());
                    }
                });

                replyBinding.getRoot().startAnimation(out);
                mBinding.contentComment.getRoot().setVisibility(View.VISIBLE);
            });
        });
    }


    @SuppressLint("NotifyDataSetChanged")
    private void downloadComments() {
        Context context = getContext();
        if (mShortsItem.getAwemeId() == null) {
            Toast.makeText(context, "Cannot download comment for this media", Toast.LENGTH_LONG).show();
            return;
        }

        // bắt đầu lệnh gọi lấy dữ liệu từ servers
        ApiCaller.with(getContext()).asTikTokApi().asComment().url(mShortsItem.getAwemeId()).callback(response -> {

            // xử lý khi thành công
            if (response.isSuccessfully()) {

                CommentDetails details = response.getModel(); // lấy dữ liệu bình luận
                if (details != null) {
                    // Nếu video tồn tại ở servers mà không có bình luận nào
                    if (details.getTotal() == 0) {
                        BottomSheetDialogHelper.showEmptyState(mBinding.contentComment);
                        mBinding.contentComment.bottomSheetCommentDownloadBtn.setVisibility(View.GONE);
                        mBinding.contentComment.bottomSheetCommentIsNoneTv.setText(context.getString(R.string.comments_is_none));
                    } else {
                        // Nếu có comment, xử lý tải hình ảnh và lưu vào cơ sở dữ liệu
                        List<Comment> commentList = details.getComments();
                        RawResponseDao rawResponseDao = FxRoomDB.get(context).rawResponseDao();

                        for (Comment comment : commentList) {
                            comment.setMediaVideoId(mShortsItem.getAwemeId());
                            TiktokDataSourceBuilder builder = new TiktokDataSourceBuilder(context)
                                    .doOnSuccess(() -> {
                                        mCommentDao.insert(comment);
                                        mUserDao.insert(comment.getShortsUser());
                                        rawResponseDao.insert(RawResponse.fromShortsComment(comment));
                                        rawResponseDao.insert(RawResponse.fromShortsUser(comment.getShortsUser()));
                                    });
                            builder.createAuthorAvatar(comment.getShortsUser().getAvatarUrl(), comment.getUid(), 20, false);
                        }

                        // gán dữ liệu và đóng loading
                        mBinding.contentComment.bottomSheetLoadingComment.setVisibility(View.GONE);
                        mAdapter.setCommentList(commentList);
                        mAdapter.setHasMore(details.isHasMore());
                        mAdapter.notifyDataSetChanged();
                    }
                } else {
                    // Nếu video không tồn tại trên servers
                    BottomSheetDialogHelper.showEmptyState(mBinding.contentComment);
                    mBinding.contentComment.bottomSheetCommentDownloadBtn.setVisibility(View.GONE);
                    mBinding.contentComment.bottomSheetCommentIsNoneTv.setText(context.getString(R.string.cannot_get_comments));
                }
            } else {
                // Nếu xảy ra sự cố đường truyền
                BottomSheetDialogHelper.showErrorState(mBinding.contentComment);
                mBinding.contentComment.bottomSheetCommentDownloadBtn.setText(context.getString(R.string.try_again));
                mBinding.contentComment.bottomSheetCommentIsNoneTv.setText(context.getString(R.string.server_errol));
            }
        }).get();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void downloadReplies(ContentReplyLayoutBinding binding, Comment parentComment, CommentAdapter replyAdapter) {
        Context context = getContext();

        // bắt đầu lệnh gọi lấy dữ liệu từ servers
        ApiCaller.with(context).asTikTokApi().asComment().asReply().url(parentComment.getMediaVideoId(), parentComment.getId())
                .offset(Math.min(49, parentComment.getReplyCount()))
                .callback(response -> {
                    if (response.isSuccessfully()) {

                        CommentDetails details = response.getModel();// lấy dữ liệu bình luận

                        if (details != null) {
                            // Nếu video tồn tại ở servers mà không có bình luận nào
                            if (details.getTotal() == 0) {
                                BottomSheetDialogHelper.showEmptyState(binding);
                                binding.bottomSheetCommentDownloadBtn.setVisibility(View.GONE);
                                binding.bottomSheetCommentIsNoneTv.setText(context.getString(R.string.comments_is_none));
                            } else {
                                // Nếu có comment, xử lý tải hình ảnh và lưu vào cơ sở dữ liệu
                                List<Comment> commentList = details.getComments();
                                RawResponseDao rawResponseDao = FxRoomDB.get(context).rawResponseDao();
                                for (Comment comment : commentList) {
                                    comment.setReplyId(parentComment.getId());
                                    comment.setMediaVideoId(parentComment.getMediaVideoId());
                                    TiktokDataSourceBuilder builder = new TiktokDataSourceBuilder(context)
                                            .doOnSuccess(() -> {
                                                mCommentDao.insert(comment);
                                                mUserDao.insert(comment.getShortsUser());
                                                rawResponseDao.insert(RawResponse.fromShortsComment(comment));
                                                rawResponseDao.insert(RawResponse.fromShortsUser(comment.getShortsUser()));
                                            });
                                    builder.createAuthorAvatar(comment.getShortsUser().getAvatarUrl(), comment.getUid(), 30, false);
                                }

                                // gán dữ liệu và đóng loading
                                binding.bottomSheetLoadingComment.setVisibility(View.GONE);
                                replyAdapter.setCommentList(commentList);
                                replyAdapter.setHasMore(details.isHasMore());
                                replyAdapter.notifyDataSetChanged();
                            }
                        } else {
                            // Nếu video không tồn tại trên servers
                            BottomSheetDialogHelper.showEmptyState(binding);
                            binding.bottomSheetCommentDownloadBtn.setVisibility(View.GONE);
                            binding.bottomSheetCommentIsNoneTv.setText(context.getString(R.string.cannot_get_comments));
                        }
                    } else {

                        // Nếu xảy ra sự cố đường truyền
                        BottomSheetDialogHelper.showErrorState(binding);
                        binding.bottomSheetCommentDownloadBtn.setText(context.getString(R.string.try_again));
                        binding.bottomSheetCommentIsNoneTv.setText(context.getString(R.string.server_errol));
                    }
                }).get();
    }
}
