package com.vtstudio.fxbox.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.databinding.MediaSegmentItemBinding;
import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.models.MediaSegment;
import com.vtstudio.fxbox.utils.TimeUtils;

import java.util.List;

public class MediaSegmentCheckboxAdapter extends RecyclerView.Adapter<MediaSegmentCheckboxAdapter.MediaSegmentHolder> {
    private final List<MediaSegment> mediaSegmentList;
    private final FxMediaVideo media;

    public MediaSegmentCheckboxAdapter(@NonNull FxMediaVideo media, @NonNull List<MediaSegment> mediaSegmentList) {
        this.mediaSegmentList = mediaSegmentList;
        this.media = media;
    }

    public long getCurrentSegmentId() {
        return media.getMediaSegmentId();
    }

    private void changeCurrentSegment(MediaSegment segment) {
        media.setMediaSegmentId(segment == null ? -1 : segment.getId());
        media.setCurrentSegment(segment);
    }

    @NonNull
    @Override
    public MediaSegmentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MediaSegmentHolder(MediaSegmentItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MediaSegmentHolder holder, int position) {
        MediaSegment segment = mediaSegmentList.get(position);
        holder.binding.checkbox.setOnCheckedChangeListener(null);
        holder.binding.checkbox.setChecked(getCurrentSegmentId() == segment.getId());
        holder.binding.segmentTitle.setText(segment.getTitle());
        holder.binding.segmentRange.setText(holder.itemView.getContext().getString(
                com.vtstudio.fxbox.R.string.segment_range,
                TimeUtils.formatDuration(segment.getStartTime()),
                TimeUtils.formatDuration(segment.getEndTime())));

        holder.binding.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int changedPosition = holder.getBindingAdapterPosition();
            if (changedPosition == RecyclerView.NO_POSITION) return;

            if (isChecked) {
                long previousSegmentId = getCurrentSegmentId();
                changeCurrentSegment(mediaSegmentList.get(changedPosition));
                int previousPosition = getPositionById(previousSegmentId);
                if (previousPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(previousPosition);
                }
            } else if (segment.getId() == getCurrentSegmentId()) {
                changeCurrentSegment(null);
            }
            notifyItemChanged(changedPosition);
        });

        holder.itemView.setOnClickListener(v -> holder.binding.checkbox.toggle());
        holder.binding.deleteSegment.setOnClickListener(v -> deleteSegment(holder));
    }

    private void deleteSegment(@NonNull MediaSegmentHolder holder) {
        int position = holder.getBindingAdapterPosition();
        if (position == RecyclerView.NO_POSITION) return;

        MediaSegment segment = mediaSegmentList.get(position);
        if (segment.getId() == getCurrentSegmentId()) {
            changeCurrentSegment(null);
        }
        FxRoomDB.get(holder.itemView.getContext()).mediaSegmentDao().delete(segment);
        mediaSegmentList.remove(position);
        notifyItemRemoved(position);
    }

    private int getPositionById(long id) {
        for (int i = 0; i < mediaSegmentList.size(); i++) {
            if (mediaSegmentList.get(i).getId() == id) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    @Override
    public int getItemCount() {
        return mediaSegmentList.size();
    }

    static class MediaSegmentHolder extends RecyclerView.ViewHolder {
        final MediaSegmentItemBinding binding;

        MediaSegmentHolder(@NonNull MediaSegmentItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
