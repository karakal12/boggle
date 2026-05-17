package com.amibar.boggle.ui.game.multiplayer;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amibar.boggle.data.User;
import com.amibar.boggle.databinding.ItemPlayerBinding;

import java.util.List;

/**
 * Adapter for displaying a list of players in the multiplayer lobby.
 * Binds User data to a layout showing their name and profile image.
 */
public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder> {

    /** List of users currently in the lobby. */
    private final List<User> playerList;

    /**
     * Constructs a new PlayerAdapter.
     * @param playerList The list of players to display.
     */
    public PlayerAdapter(List<User> playerList) {
        this.playerList = playerList;
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPlayerBinding binding = ItemPlayerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PlayerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        User player = playerList.get(position);
        // Bind the player object to the layout using Data Binding
        holder.binding.setPlayer(player);
    }

    @Override
    public int getItemCount() {
        return playerList != null ? playerList.size() : 0;
    }

    /**
     * ViewHolder class for individual player items in the RecyclerView.
     */
    public static class PlayerViewHolder extends RecyclerView.ViewHolder {
        /** View binding for the player item layout. */
        final ItemPlayerBinding binding;

        /**
         * @param binding The binding object for the item layout.
         */
        public PlayerViewHolder(@NonNull ItemPlayerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
