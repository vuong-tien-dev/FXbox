package com.vtstudio.fxbox.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.api.CommentDetails;
import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.database.dao.CommentDao;
import com.vtstudio.fxbox.database.dao.RawResponseDao;
import com.vtstudio.fxbox.database.dao.ShortsUserDao;
import com.vtstudio.fxbox.databinding.CommentItemLayoutBinding;
import com.vtstudio.fxbox.databinding.CommentItemLoadMoreLayoutBinding;
import com.vtstudio.fxbox.helpers.BottomSheetDialogHelper;
import com.vtstudio.fxbox.listeners.OnItemClickListener;
import com.vtstudio.fxbox.api.TiktokDataSourceBuilder;
import com.vtstudio.fxbox.media.models.tiktok.Comment;
import com.vtstudio.fxbox.media.models.RawResponse;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.server.ApiCaller;
import com.vtstudio.fxbox.utils.TimeUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_COMMENT = 1;
    private static final int VIEW_TYPE_LOAD_MORE = 2;
    public static final int MODE_COMMENT = 1;
    public static final int MODE_REPLY = 2;
    private List<Comment> commentList;
    private final String ownerId;
    private String commentId;
    private String authorId;
    private OnItemClickListener itemClickListener;
    private boolean isLoading = false;
    private int viewMode = 1;
    private boolean isViewComment = true;
    private boolean hasMore = true;
    private boolean hasMoreInDatabase = true;

    public boolean isHasMoreInDatabase() {
        return hasMoreInDatabase;
    }

    public void setHasMoreInDatabase(boolean hasMoreInDatabase) {
        this.hasMoreInDatabase = hasMoreInDatabase;
    }

    public List<Comment> getCommentList() {
        return commentList;
    }

    public void setViewMode(int viewMode) {
        this.viewMode = viewMode;
        isViewComment = viewMode == MODE_COMMENT;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < commentList.size()) {
            return VIEW_TYPE_COMMENT;
        } else {
            return VIEW_TYPE_LOAD_MORE;
        }
    }

    public CommentAdapter(@NonNull List<Comment> commentList, String ownerId) {
        this.commentList = commentList;
        this.ownerId = ownerId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_COMMENT) {
            CommentItemLayoutBinding binding = CommentItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new CommentHolder(binding);
        } else {
            CommentItemLoadMoreLayoutBinding binding = CommentItemLoadMoreLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new LoadMoreHolder(binding);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        if (holder.getItemViewType() == VIEW_TYPE_COMMENT && holder instanceof CommentHolder) {
            Comment comment = commentList.get(position);
            ShortsUser user = comment.getShortsUser();
            Context context = holder.itemView.getContext();

            //Log.d("Comments", comment.getRawResponse());
            CommentItemLayoutBinding binding = ((CommentHolder) holder).binding;
            ImageView userAvatar = binding.commentUserAvatar;
//
            binding.commentContent.setText(comment.getContent().trim());
            binding.commentCreateTime.setText(TimeUtils.getTimeAgo(comment.getCreateTime() * 1000L, context));
            binding.commentDiggCount.setText(String.valueOf(comment.getDiggCount()));

            if (itemClickListener != null && comment.getReplyCount() > 0) {
                binding.commentViewReplies.setVisibility(View.VISIBLE);
                String viewReplies = String.format(binding.commentViewReplies.getContext().getString(R.string.view_more_replies), comment.getReplyCount());
                binding.commentViewReplies.setText(viewReplies);
                binding.commentViewReplies.setOnClickListener(v -> {
                    if (itemClickListener != null)
                        itemClickListener.onItemClick(CommentAdapter.this, binding.getRoot(), holder.getBindingAdapterPosition(), holder.getItemId());
                });
            } else {
                binding.commentViewReplies.setVisibility(View.GONE);
            }
//
            if (comment.getShortsUser() == null) return;

            Glide.with(context).load(user.getAvatarPath()).placeholder(R.drawable.default_avatar_user)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .override(userAvatar.getWidth(), userAvatar.getHeight())
                    .into(userAvatar);

            if (Objects.equals(user.getUid(), authorId)) {
                String originalText = user.getNickName().trim();
                String appendedText = context.getString(R.string.author);

                SpannableString expandedUserName = new SpannableString(originalText + " " + appendedText);
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(context.getColor(R.color.fx_label_color));
                Typeface typeface = context.getResources().getFont(R.font.tiktok_text_medium);
                TypefaceSpan typefaceSpan = new TypefaceSpan(typeface);

                int startIndex = originalText.length() + 1; // +1 để bỏ qua khoảng trắng
                int endIndex = expandedUserName.length();

                expandedUserName.setSpan(colorSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                expandedUserName.setSpan(typefaceSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                Log.d("Comment", expandedUserName.toString());
                binding.commentUserName.setText(expandedUserName);
            } else {
                binding.commentUserName.setText(user.getNickName().trim());
            }

        } else if (holder.getItemViewType() == VIEW_TYPE_LOAD_MORE && holder instanceof LoadMoreHolder) {
            CommentItemLoadMoreLayoutBinding binding = ((LoadMoreHolder) holder).binding;
            binding.commentLoadMoreButton.setOnClickListener(v -> {
                Executors.newSingleThreadExecutor().execute(() -> {
                    loadCommentsFromDatabase(binding, new Runnable() {
                        @Override
                        public void run() {
                            v.post(() -> downloadComments(v.getContext(), binding));
                        }
                    });
                });
            });
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        if (hasMoreInDatabase && holder instanceof LoadMoreHolder) {
            loadCommentsFromDatabase(((LoadMoreHolder) holder).binding, null);
        }
    }

    public void loadCommentsFromDatabase(CommentItemLoadMoreLayoutBinding binding, @Nullable Runnable onEmpty) {

        if (commentList == null) {
            showErrorState(binding);
            return;
        }

        View v = binding.getRoot();

        FxRoomDB database2 = FxRoomDB.get(v.getContext());
        List<Comment> comments;
        ShortsUserDao userDao = database2.shortsUserDao();
        v.post(() -> showLoadingState(binding));

        if (isViewComment) {
            comments = database2.commentDao().getCommentsByVideoId(ownerId, BottomSheetDialogHelper.COMMENT_LIMIT, commentList.size());
        } else {
            comments = database2.commentDao().getReplyById(commentId, BottomSheetDialogHelper.COMMENT_LIMIT, commentList.size());
        }

        if (!comments.isEmpty()) {
            for (Comment comment : comments) {
                ShortsUser user = userDao.getUserById(comment.getUid());
                comment.setShortsUser(user);
            }
        } else if (onEmpty != null) {
            onEmpty.run();
            return;
        }

        v.postDelayed(() -> {
            if (!comments.isEmpty()) {
                commentList.addAll(comments);
                showSuccessState(binding);
            } else {
                hasMoreInDatabase = false;
                showDownloadMoreState(binding);
            }
        }, 200);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void downloadComments(Context context, CommentItemLoadMoreLayoutBinding binding) {
        FxRoomDB newDb = FxRoomDB.get(context);
        ShortsUserDao shortsUserDao = newDb.shortsUserDao();
        CommentDao commentDao2 = newDb.commentDao();

        Log.d("Comment", "Downloading comments for " + commentId + " ... " + ownerId);

        showLoadingState(binding);

        ApiCaller.with(context).asTikTokApi().asComment().isReply(!isViewComment).url(ownerId, commentId).cursor(commentList.size() + 1).callback(response -> {
            if (response.isSuccessfully()) {
                CommentDetails details = response.getModel();
                if (details != null && !details.getComments().isEmpty()) {
                    List<Comment> commentList = details.getComments();
                    RawResponseDao rawResponseDao = FxRoomDB.get(context).rawResponseDao();
                    for (Comment comment : commentList) {
                        if (isViewComment) {
                            comment.setMediaVideoId(ownerId);
                        } else {
                            comment.setReplyId(ownerId);
                        }
                        TiktokDataSourceBuilder builder = new TiktokDataSourceBuilder(context)
                                .doOnSuccess(() -> {
                                    commentDao2.insert(comment);
                                    shortsUserDao.insert(comment.getShortsUser());
                                    rawResponseDao.insert(RawResponse.fromShortsComment(comment));
                                    rawResponseDao.insert(RawResponse.fromShortsUser(comment.getShortsUser()));
                                });
                        builder.createAuthorAvatar(comment.getShortsUser().getAvatarUrl(), comment.getUid(), 30, false);
                    }
                    binding.commentLoadMoreBar.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (CommentAdapter.this.commentList != null) {
                                CommentAdapter.this.commentList.addAll(commentList);
                                CommentAdapter.this.notifyDataSetChanged();
                            }
                            showSuccessState(binding);
                        }
                    }, 1000);

                } else {
                    hasMore = details != null && !details.getComments().isEmpty();
                    showErrorState(binding);
                }
            } else {
                showErrorState(binding);
            }
        }).get();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        try {
            if (holder instanceof CommentHolder) {
                CommentItemLayoutBinding binding = ((CommentHolder) holder).binding;
                binding.commentViewReplies.setOnClickListener(null);
                Glide.with(holder.itemView).clear(binding.commentUserAvatar);
            }
        } catch (Exception ignored) {
        }
    }

    private void showSuccessState(CommentItemLoadMoreLayoutBinding binding) {
        hideLoadBarAndShowLoadButton(binding);
        binding.commentLoadMoreButton.setText(binding.getRoot().getContext().getString(R.string.view_more));
        binding.commentLoadMoreBar.clearAnimation();
    }

    private void showDownloadMoreState(CommentItemLoadMoreLayoutBinding binding) {
        hideLoadBarAndShowLoadButton(binding);
        binding.commentLoadMoreButton.setText(binding.getRoot().getContext().getString(R.string.download_more));
        binding.commentLoadMoreBar.clearAnimation();
    }

    private void hideLoadBarAndShowLoadButton(CommentItemLoadMoreLayoutBinding binding) {
        binding.commentLoadMoreBar.setVisibility(View.GONE);
        binding.commentLoadMoreButton.setVisibility(View.VISIBLE);
    }

    private void showLoadingState(CommentItemLoadMoreLayoutBinding binding) {
        binding.commentLoadMoreBar.setVisibility(View.VISIBLE);
        binding.commentLoadMoreButton.setVisibility(View.GONE);
        Drawable drawable = ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.comment_load_more_icon);
        if (drawable != null) {
            drawable.setTint(Color.WHITE);
        }
        binding.commentLoadMoreButton.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null);
    }

    private void showErrorState(CommentItemLoadMoreLayoutBinding binding) {
        hideLoadBarAndShowLoadButton(binding);
        binding.commentLoadMoreButton.setText(binding.getRoot().getContext().getString(R.string.try_again));
        Drawable drawable = ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.recall_download);
        if (drawable != null) {
            drawable.setTint(Color.WHITE);
        }
        binding.commentLoadMoreButton.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null);
    }

    @Override
    public int getItemCount() {
        if (commentList.isEmpty() || !hasMore) return commentList.size();
        else return commentList.size() + 1;
    }

    public void setCommentList(List<Comment> commentList) {
        this.commentList = commentList;
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void release() {
        itemClickListener = null;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public static class CommentHolder extends RecyclerView.ViewHolder {
        final CommentItemLayoutBinding binding;

        public CommentHolder(@NonNull CommentItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static class LoadMoreHolder extends RecyclerView.ViewHolder {
        final CommentItemLoadMoreLayoutBinding binding;

        public LoadMoreHolder(@NonNull CommentItemLoadMoreLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
