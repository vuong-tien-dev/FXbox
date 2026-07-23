package com.vtstudio.fxbox.adapters;

import static com.vtstudio.fxbox.activity.Search.CATEGORY_ALL;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.databinding.MediaSearchCategoryLayoutBinding;
import com.vtstudio.fxbox.listeners.OnCategoryItemClickListener;
import com.vtstudio.fxbox.media.models.Media;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MediaSearchCategoryAdapter extends RecyclerView.Adapter<MediaSearchCategoryAdapter.MediaSearchCategoryHolder> {

    private List<Map.Entry<Integer, Integer>> categories;

    public void setCategory(Map<Integer, Integer> category) {

        currentSelectedPosition = 0;
        oldSelectedPosition = 0;

        this.categories = new ArrayList<>(category.entrySet());

    }

    public MediaSearchCategoryAdapter() {
        categories = new ArrayList<>();
    }

    private int currentSelectedPosition = 0;
    private int oldSelectedPosition = 0;

    public void setOnItemClickListener(OnCategoryItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private OnCategoryItemClickListener onItemClickListener;

    @NonNull
    @Override
    public MediaSearchCategoryAdapter.MediaSearchCategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MediaSearchCategoryLayoutBinding binding = MediaSearchCategoryLayoutBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false);
        return new MediaSearchCategoryHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaSearchCategoryAdapter.MediaSearchCategoryHolder holder, @SuppressLint("RecyclerView") int position) {

        Map.Entry<Integer, Integer> map = categories.get(position);
        int key = map.getKey();

        String categoryName = holder.itemView.getContext().getString(R.string.category_storage);
        if (key == CATEGORY_ALL) {
            categoryName = holder.itemView.getContext().getString(R.string.category_all);
        } else if (key == Media.MediaType.TYPE_SHORTS_VIDEO) {
            categoryName = holder.itemView.getContext().getString(R.string.category_shorts);
        } else if (key == Media.MediaType.TYPE_EXTERNAL_STORAGE) {
            categoryName = holder.itemView.getContext().getString(R.string.category_storage);
        }

        if(categoryName != null){
            holder.binding.categoryName.setText(categoryName);
        }

        if(currentSelectedPosition == position)
        {
            holder.itemView.setSelected(true);
            oldSelectedPosition = position;
        } else {
            holder.itemView.setSelected(false);
        }

        holder.itemView.setOnClickListener(v -> {
            currentSelectedPosition = position;
            notifyItemChanged(currentSelectedPosition);
            notifyItemChanged(oldSelectedPosition);
            notifyOnItemClickListener(key, position);
        });

    }

    private void notifyOnItemClickListener(int key, int position)
    {
        if(onItemClickListener != null) onItemClickListener.onClick(key, position);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public int getCurrentSelectedPosition() {
        return currentSelectedPosition;
    }

    public class MediaSearchCategoryHolder extends RecyclerView.ViewHolder {
        private final MediaSearchCategoryLayoutBinding binding;

        public MediaSearchCategoryHolder(@NonNull MediaSearchCategoryLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
