package com.amibar.boggle.ui.multiplayer;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amibar.boggle.data.User;
import com.amibar.boggle.databinding.ItemPlayerBinding;
import java.util.List;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder> {

    private List<User> playerList;

    public PlayerAdapter(List<User> playerList) {
        this.playerList = playerList;
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use View Binding to inflate the layout
        ItemPlayerBinding binding = ItemPlayerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PlayerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        User player = playerList.get(position);
        // Bind the player data to the view
        holder.binding.setPlayer(player);
    }

    @Override
    public int getItemCount() {
        return playerList != null ? playerList.size() : 0;
    }

    static class PlayerViewHolder extends RecyclerView.ViewHolder {
        // Store the binding instead of individual views
        final ItemPlayerBinding binding;

        public PlayerViewHolder(@NonNull ItemPlayerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}