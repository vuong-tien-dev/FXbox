package com.vtstudio.fxbox.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vtstudio.fxbox.databinding.CheckboxItemBinding;
import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.models.MediaSegment;
import com.vtstudio.fxbox.media.models.tiktok.Playlist;

import java.util.ArrayList;
import java.util.List;

public class MediaSegmentCheckboxAdapter extends RecyclerView.Adapter<MediaSegmentCheckboxAdapter.MediaSegmentCheckboxHolder> {
    private List<MediaSegment> mediaSegmentList;
    private final FxMediaVideo media;

    public MediaSegmentCheckboxAdapter(@NonNull FxMediaVideo media, @NonNull List<MediaSegment> mediaSegmentList) {
        this.mediaSegmentList = mediaSegmentList;
        this.media = media;
    }

    public List<MediaSegment> getMediaSegmentList() {
        return mediaSegmentList;
    }

    public void setMediaSegmentList(List<MediaSegment> mediaSegmentList) {
        this.mediaSegmentList = mediaSegmentList;
    }

    public long getCurrentSegmentId() {
        return media.getMediaSegmentId();
    }

    public void setCurrentSegmentId(long currentSegmentId) {
        this.media.setMediaSegmentId(currentSegmentId);
    }

    private void changeCurrentSegment(MediaSegment segment) {
        media.setMediaSegmentId(segment == null? 0 : segment.getId());
        media.setCurrentSegment(segment);
    }

    @NonNull
    @Override
    public MediaSegmentCheckboxAdapter.MediaSegmentCheckboxHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CheckboxItemBinding binding = CheckboxItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MediaSegmentCheckboxHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaSegmentCheckboxAdapter.MediaSegmentCheckboxHolder holder, int position) {
        MediaSegment segment = mediaSegmentList.get(position);
        holder.mediaSegmentId = segment.getId();
        holder.binding.checkbox.setOnCheckedChangeListener(null);
        holder.binding.checkbox.setChecked(getCurrentSegmentId() == segment.getId());

        holder.binding.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            final int checkedChangePosition = holder.getBindingAdapterPosition();
            if (checkedChangePosition != RecyclerView.NO_POSITION &&
                    checkedChangePosition < mediaSegmentList.size()) {
                MediaSegment mediaSegment = mediaSegmentList.get(checkedChangePosition);
                long id = mediaSegment.getId();

                if (isChecked) {
                    long previousSegmentId = getCurrentSegmentId();
                    changeCurrentSegment(mediaSegment);
                    if (previousSegmentId != id) {
                        notifyItemChanged(getPositionById(previousSegmentId));
                    }
                    notifyItemChanged(checkedChangePosition);
                } else {
                    if (id == getCurrentSegmentId()) {
                        changeCurrentSegment(null);
                    } else {
                        notifyItemChanged(checkedChangePosition);
                    }
                }
            }
        });

        holder.binding.name.setText(segment.getTitle());
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
    public void onViewRecycled(@NonNull MediaSegmentCheckboxHolder holder) {
        holder.mediaSegmentId = -1;
    }

    @Override
    public int getItemCount() {
        return mediaSegmentList.size();
    }


    public static class MediaSegmentCheckboxHolder extends RecyclerView.ViewHolder {
        final CheckboxItemBinding binding;
        private long mediaSegmentId;

        public MediaSegmentCheckboxHolder(@NonNull CheckboxItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
