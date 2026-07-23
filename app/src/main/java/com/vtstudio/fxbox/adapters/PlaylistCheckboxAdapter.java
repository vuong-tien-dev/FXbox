package com.vtstudio.fxbox.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vtstudio.fxbox.databinding.CheckboxItemBinding;
import com.vtstudio.fxbox.media.models.tiktok.Playlist;

import java.util.ArrayList;
import java.util.List;

public class PlaylistCheckboxAdapter extends RecyclerView.Adapter<PlaylistCheckboxAdapter.PlaylistCheckboxHolder> {
    private List<Playlist> playlistList;
    private List<String> checkedIdList;
    private List<String> notRecheckedIdList;
    public PlaylistCheckboxAdapter (@NonNull List<Playlist> playlistList) {
        this.playlistList = playlistList;
        notRecheckedIdList = new ArrayList<>();
        checkedIdList = new ArrayList<> ();
    }

    public List<Playlist> getPlaylistList() {
        return playlistList;
    }

    public void setPlaylistList(List<Playlist> playlistList) {
        this.playlistList = playlistList;
    }

    public List<String> getCheckedIdList() {
        return checkedIdList;
    }

    public void setCheckedIdList(List<String> checkedIdList) {
        this.checkedIdList = checkedIdList;
        this.notRecheckedIdList = new ArrayList<>(checkedIdList);
    }

    public List<String> getNotRecheckedIdList () {
        return notRecheckedIdList;
    }

    @NonNull
    @Override
    public PlaylistCheckboxAdapter.PlaylistCheckboxHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CheckboxItemBinding binding  = CheckboxItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PlaylistCheckboxHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistCheckboxAdapter.PlaylistCheckboxHolder holder, int position) {

        Playlist playlist = playlistList.get(position);
        holder.playListId = playlist.getId();

        holder.binding.checkbox.setOnCheckedChangeListener(null);
        holder.binding.checkbox.setChecked(checkedIdList != null && checkedIdList.contains(playlist.getId()));

        holder.binding.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (checkedIdList == null || playlistList == null) return;

            final int position1 = holder.getBindingAdapterPosition();
            if( position1 >= 0 && position1 < playlistList.size() ) {
                Playlist playlist1 = playlistList.get(position1);
                String id = playlist1.getId();
                if (isChecked) {
                    checkedIdList.add(id);
                } else {
                    checkedIdList.remove(id);
                    notRecheckedIdList.remove(id);
                }
            }
        });

        holder.binding.name.setText(playlist.getName());
    }

    @Override
    public void onViewRecycled(@NonNull PlaylistCheckboxHolder holder) {
        holder.playListId = "";
        holder.binding.checkbox.setOnCheckedChangeListener(null);
    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }

    public static class PlaylistCheckboxHolder extends RecyclerView.ViewHolder {
        final CheckboxItemBinding binding;
        private String playListId;
        public PlaylistCheckboxHolder(@NonNull CheckboxItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
