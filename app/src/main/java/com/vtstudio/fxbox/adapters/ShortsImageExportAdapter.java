package com.vtstudio.fxbox.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vtstudio.fxbox.databinding.ShortsImageExportItemBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ShortsImageExportAdapter extends RecyclerView.Adapter<ShortsImageExportAdapter.ImageExportHolder> {
    private final List<String> imagePaths;
    private final Set<String> selectedPaths = new LinkedHashSet<>();

    public ShortsImageExportAdapter(@NonNull List<String> imagePaths) {
        this.imagePaths = imagePaths;
        selectedPaths.addAll(imagePaths);
    }

    @NonNull
    @Override
    public ImageExportHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImageExportHolder(ShortsImageExportItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ImageExportHolder holder, int position) {
        String imagePath = imagePaths.get(position);
        holder.binding.checkbox.setOnCheckedChangeListener(null);
        holder.binding.checkbox.setChecked(selectedPaths.contains(imagePath));
        holder.binding.imageName.setText(new File(imagePath).getName());
        Glide.with(holder.itemView).load(imagePath).into(holder.binding.imagePreview);

        View.OnClickListener toggleSelection = view -> holder.binding.checkbox.toggle();
        holder.itemView.setOnClickListener(toggleSelection);
        holder.binding.imagePreview.setOnClickListener(toggleSelection);
        holder.binding.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedPaths.add(imagePath);
            } else {
                selectedPaths.remove(imagePath);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    @NonNull
    public List<File> getSelectedFiles() {
        List<File> files = new ArrayList<>();
        for (String path : selectedPaths) {
            File file = new File(path);
            if (file.isFile()) {
                files.add(file);
            }
        }
        return files;
    }

    static class ImageExportHolder extends RecyclerView.ViewHolder {
        final ShortsImageExportItemBinding binding;

        ImageExportHolder(@NonNull ShortsImageExportItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
