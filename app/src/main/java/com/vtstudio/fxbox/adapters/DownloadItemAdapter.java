package com.vtstudio.fxbox.adapters;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.databinding.DownloadItemLayoutBinding;
import com.vtstudio.fxbox.downloader.FXDownloader;
import com.vtstudio.fxbox.downloader.ModelDownload;
import com.vtstudio.fxbox.downloader.OnDownloadListener;
import com.vtstudio.fxbox.downloader.RequestInfo;
import com.vtstudio.fxbox.models.TiktokPackage;
import com.vtstudio.fxbox.ui.MyRequestOptions;

import java.util.List;
import java.util.Map;


public class DownloadItemAdapter extends RecyclerView.Adapter<DownloadItemAdapter.DownloadItemHolder> implements OnDownloadListener {

    private List<RequestInfo> requestInfoList;
    private Map<Integer, ModelDownload> saveModelMap;
    private RecyclerView parent;
    private FXDownloader manager;
    private int indexToRemove = -1;

    public void setManager(FXDownloader manager) {
        this.manager = manager;
    }

    public void setRequestInfoList(List<RequestInfo> requestInfoList) {
        this.requestInfoList = requestInfoList;
    }

    public void setSaveModelMap(Map<Integer, ModelDownload> tiktokMap) {
        this.saveModelMap = tiktokMap;
    }

    public DownloadItemAdapter(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull RecyclerView parent, @NonNull FXDownloader manager) {
        this.parent = parent;
    }

    @NonNull
    @Override
    public DownloadItemAdapter.DownloadItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DownloadItemLayoutBinding binding = DownloadItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DownloadItemHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadItemAdapter.DownloadItemHolder holder, int position) {
        if(requestInfoList == null || requestInfoList.isEmpty() || position >= requestInfoList.size()) return;
        RequestInfo info = requestInfoList.get(position);
        if (info == null) return;

        ModelDownload model = saveModelMap.get(info.getId());

        if(model == null) return;

        Glide.with(holder.itemView)
                .load(info.getThumbnailPath())
                .apply(MyRequestOptions.getOptions())
                .placeholder(R.drawable.default_avatar_user)
                .override(holder.binding.downloadItemThumbnail.getWidth(), holder.binding.downloadItemThumbnail.getHeight())
                .into(holder.binding.downloadItemThumbnail);

        holder.binding.downloadItemAuthor.setText(model.onGetAuthor());
        holder.binding.downloadItemName.setText(model.onGetTitle());

        holder.binding.downloadCancel.setOnClickListener(v -> {
            if(manager != null) {
                manager.cancel(info.getId());
                indexToRemove = holder.getBindingAdapterPosition();
            }
        });

        if (info.isStarted() && !holder.isStarted) {
            prepareViews(holder);
            startViews(holder, info);
            resumeViews(holder, info);
        } else {
            return;
        }

        if (info.isWaitingConnect() && !holder.isWaitingConnect) {
            stateNetworkErrolViews(holder, info);
        }

        if (info.isPause() && !holder.isPause) {
            pauseViews(holder, info);
        }

        if (info.isFailed() && !holder.isFailed) {
            interruptionViews(holder, info);
        }

        if (info.isCompleted() && !holder.isCompleted) {
            successViews(holder, info);
        }

    }

    @Override
    public void onViewRecycled(@NonNull DownloadItemHolder holder) {
        holder.binding.downloadStatusTv.setVisibility(View.VISIBLE);
        holder.binding.downloadProgressBar.setVisibility(View.INVISIBLE);
        holder.binding.downloadProgressBar.setIndeterminate(true);
        holder.binding.downloadResumeOrPause.setOnClickListener(null);
        holder.binding.downloadResumeOrPause.setVisibility(View.INVISIBLE);
        holder.binding.downloadStatusTv.setText(R.string.download_is_waiting);
        holder.isCompleted = false;
        holder.isWaitingConnect = false;
        holder.isStarted = false;
        holder.isPause = false;
    }

    @Override
    public void onPrepare(RequestInfo requestInfo) {
        int holderIndex = getHolderIndex(requestInfo.getId());
        if (holderIndex == -1) return;

        RecyclerView.ViewHolder holder = parent.findViewHolderForLayoutPosition(holderIndex);
        if (holder instanceof DownloadItemHolder) {
            prepareViews((DownloadItemHolder) holder);
        }
    }

    @Override
    public void onStart(RequestInfo requestInfo) {
        int holderIndex = getHolderIndex(requestInfo.getId());
        if (holderIndex == -1) return;

        RecyclerView.ViewHolder holder = parent.findViewHolderForLayoutPosition(holderIndex);
        if (holder instanceof DownloadItemHolder) {
            startViews((DownloadItemHolder) holder, requestInfo);
        }
    }

    @Override
    public void onResume(@NonNull RequestInfo requestInfo) {
        int holderIndex = getHolderIndex(requestInfo.getId());
        if (holderIndex == -1) return;

        RecyclerView.ViewHolder holder = parent.findViewHolderForLayoutPosition(holderIndex);
        if (holder instanceof DownloadItemHolder) {
            resumeViews((DownloadItemHolder) holder, requestInfo);
        }
    }

    @Override
    public void onNetworkErrol(@NonNull RequestInfo requestInfo) {
        int holderIndex = getHolderIndex(requestInfo.getId());
        if (holderIndex == -1) return;

        RecyclerView.ViewHolder holder = parent.findViewHolderForLayoutPosition(holderIndex);
        if (holder instanceof DownloadItemHolder) {
            DownloadItemHolder itemHolder = (DownloadItemHolder) holder;
            stateNetworkErrolViews(itemHolder, requestInfo);
        }
    }


    @Override
    public void onPause(@NonNull RequestInfo requestInfo) {
        int holderIndex = getHolderIndex(requestInfo.getId());
        if (holderIndex == -1) return;

        RecyclerView.ViewHolder holder = parent.findViewHolderForLayoutPosition(holderIndex);
        if (holder instanceof DownloadItemHolder) {
            pauseViews((DownloadItemHolder) holder, requestInfo);
        }

        // Log thông báo debug
        Log.d("DownloadManager", "fxOnPause - requestInfo: " + requestInfo);
    }

    @Override
    public void onProgress(@NonNull RequestInfo requestInfo) {
        int holderIndex = getHolderIndex(requestInfo.getId());
        if (holderIndex == -1) return;

        RecyclerView.ViewHolder holder = parent.findViewHolderForLayoutPosition(holderIndex);
        if (holder instanceof DownloadItemHolder) {
            progressViews((DownloadItemHolder) holder, requestInfo);
        }
    }

    @Override
    public void onInterruption(@NonNull RequestInfo requestInfo) {
        int holderIndex = getHolderIndex(requestInfo.getId());
        if (holderIndex == -1) return;

        RecyclerView.ViewHolder holder = parent.findViewHolderForLayoutPosition(holderIndex);
        if (holder instanceof DownloadItemHolder) {
            interruptionViews((DownloadItemHolder) holder, requestInfo);
        }

        // Log thông báo debug
        Log.d("DownloadManager", "onInterruption - requestInfo: " + requestInfo);
    }

    @Override
    public void onSuccess(@NonNull RequestInfo requestInfo) {
        int holderIndex = getHolderIndex(requestInfo.getId());
        if (holderIndex == -1) return;

        RecyclerView.ViewHolder holder = parent.findViewHolderForLayoutPosition(holderIndex);
        if (holder instanceof DownloadItemHolder) {
            successViews((DownloadItemHolder) holder, requestInfo);
        }

        // Log thông báo debug
        Log.d("DownloadManager", "onSuccess - requestInfo: " + requestInfo);
    }

    @Override
    public void onFailed(RequestInfo requestInfo) {
        int holderIndex = getHolderIndex(requestInfo.getId());
        if (holderIndex == -1) return;

        RecyclerView.ViewHolder holder = parent.findViewHolderForLayoutPosition(holderIndex);
        if (holder instanceof DownloadItemHolder) {
            interruptionViews((DownloadItemHolder) holder, requestInfo);
        }

        // Log thông báo debug
        Log.d("DownloadManager", "onFailed - requestInfo: ");
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onCancel(@NonNull RequestInfo requestInfo) {
        if(indexToRemove != -1) {
            notifyDataSetChanged();
            indexToRemove = -1;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onDataChanged() {
        notifyDataSetChanged();
    }

    private void prepareViews(DownloadItemHolder holder) {
        holder.binding.downloadResumeOrPause.setVisibility(View.INVISIBLE);
        holder.binding.downloadResumeOrPause.setOnClickListener(null);
        holder.binding.downloadStatusTv.setVisibility(View.GONE);
        holder.binding.downloadProgressBar.setVisibility(View.VISIBLE);
        holder.binding.downloadProgressBar.setIndeterminate(true);
        Log.d("DownloadManager", "prepareViews - requestInfo: ");
    }

    private void startViews(DownloadItemHolder holder, RequestInfo info) {
        holder.binding.downloadResumeOrPause.setVisibility(View.VISIBLE);
        holder.binding.downloadResumeOrPause.setAlpha(0.6f);
        holder.binding.downloadStatusTv.setVisibility(View.GONE);
        holder.binding.downloadProgressBar.setVisibility(View.VISIBLE);
        holder.binding.downloadProgressBar.setIndeterminate(false);
        holder.binding.downloadProgressBar.setMax((int) info.getContentLength());
        holder.isStarted = true;
        // Log thông báo debug
        Log.d("DownloadManager", "fxOnStart - requestInfo: ");
    }

    private void resumeViews(DownloadItemHolder holder, RequestInfo info) {
        holder.binding.downloadResumeOrPause.setImageResource(R.drawable.pause);
        holder.binding.downloadResumeOrPause.setAlpha(1.f);
        holder.binding.downloadResumeOrPause.setOnClickListener(v -> {
            manager.pause();
        });
        holder.binding.downloadProgressBar.setProgress((int) info.getCurrentBytes());
        // Log thông báo debug
        Log.d("DownloadManager", "fxOnResume - requestInfo: ");
    }

    private void pauseViews(DownloadItemHolder holder, RequestInfo info) {
        holder.binding.downloadResumeOrPause.setImageResource(R.drawable.play);
        holder.binding.downloadResumeOrPause.setOnClickListener(v -> {
            manager.resume();
        });
        holder.isPause = true;
    }

    private void progressViews(DownloadItemHolder holder, RequestInfo info) {
        // Log thông báo debug
        Log.d("DownloadManager", "onProgress - requestInfo: ");
        holder.binding.downloadProgressBar.setProgress((int) info.getCurrentBytes());
    }

    private void successViews(DownloadItemHolder holder, RequestInfo info) {
        holder.binding.downloadResumeOrPause.setImageResource(R.drawable.success_icon);
        holder.binding.downloadProgressBar.setVisibility(View.GONE);
        holder.binding.downloadStatusTv.setVisibility(View.VISIBLE);
        holder.binding.downloadStatusTv.setText(R.string.download_is_success);
    }

    private void interruptionViews(DownloadItemHolder holder, RequestInfo info) {
        holder.binding.downloadResumeOrPause.setVisibility(View.VISIBLE);
        holder.binding.downloadResumeOrPause.setImageResource(R.drawable.recall_download);
        holder.binding.downloadResumeOrPause.setOnClickListener((v) -> {
            onPrepare(info);
            v.setVisibility(View.INVISIBLE);
            manager.reCall();
        });
        holder.binding.downloadProgressBar.setVisibility(View.GONE);
        holder.binding.downloadStatusTv.setVisibility(View.VISIBLE);
        holder.binding.downloadStatusTv.setText(R.string.download_is_failed);
    }

    private void stateNetworkErrolViews(DownloadItemHolder holder, RequestInfo info) {
        holder.binding.downloadResumeOrPause.setImageResource(R.drawable.pause);
        holder.binding.downloadResumeOrPause.setVisibility(View.INVISIBLE);
        holder.binding.downloadResumeOrPause.setOnClickListener(null);
        holder.binding.downloadProgressBar.setVisibility(View.GONE);
        holder.binding.downloadStatusTv.setVisibility(View.VISIBLE);
        holder.binding.downloadStatusTv.setText(R.string.download_is_waiting_network);
    }

    private int getHolderIndex(int infoId) {
        for (int i = 0; i < requestInfoList.size(); ++i) {
            if (requestInfoList.get(i).getId() == infoId) return i;
        }
        return -1;
    }

    public void release() {
        parent = null;
        manager = null;
    }

    @Override
    public int getItemCount() {
        return requestInfoList.size();
    }

    public static class DownloadItemHolder extends RecyclerView.ViewHolder {
        private final DownloadItemLayoutBinding binding;
        private boolean isStarted;
        private boolean isPause;
        private boolean isCompleted;
        private boolean isFailed;
        private boolean isWaitingConnect;

        public DownloadItemHolder(@NonNull DownloadItemLayoutBinding binding) {
            super(binding.getRoot());
            this.setIsRecyclable(false);
            this.binding = binding;
        }
    }
}
