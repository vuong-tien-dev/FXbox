package com.vtstudio.fxbox.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.databinding.LibItemLayBinding;
import com.vtstudio.fxbox.listeners.OnItemListClickListener;

public class SpecialITemAdapter extends RecyclerView.Adapter<SpecialITemAdapter.SpecialItemHolder>{
    private OnItemListClickListener onItemClickListener;
    @NonNull
    @Override
    public SpecialITemAdapter.SpecialItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LibItemLayBinding binding = LibItemLayBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SpecialITemAdapter.SpecialItemHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SpecialITemAdapter.SpecialItemHolder holder, int position) {
        if(position == 0) {
            Glide.with(holder.binding.imageItem).load(R.drawable.favorite_32_dp)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).into(holder.binding.imageItem);
            holder.binding.textItem.setText(R.string.favorite_list);
        } else {
            Glide.with(holder.binding.imageItem).load(R.drawable.limit_32_dp)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).into(holder.binding.imageItem);
            holder.binding.textItem.setText(R.string.limit_list);
        }

        holder.itemView.setOnClickListener(v -> {
            if(onItemClickListener != null) onItemClickListener.onClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public void setOnItemClickListener(OnItemListClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public static class SpecialItemHolder extends RecyclerView.ViewHolder {
        private final LibItemLayBinding binding;
        public SpecialItemHolder(@NonNull LibItemLayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }
}
