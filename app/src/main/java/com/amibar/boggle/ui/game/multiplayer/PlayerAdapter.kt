package com.amibar.boggle.ui.game.multiplayer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amibar.boggle.data.User
import com.amibar.boggle.databinding.ItemPlayerBinding
import com.amibar.boggle.ui.game.multiplayer.PlayerAdapter.PlayerViewHolder

/**
 * Adapter for displaying a list of players in the multiplayer lobby.
 * Binds User data to a layout showing their name and profile image.
 */
class PlayerAdapter
/**
 * Constructs a new PlayerAdapter.
 * @param playerList The list of players to display.
 */(
    /** List of users currently in the lobby.  */
    private val playerList: MutableList<User?>?
) : RecyclerView.Adapter<PlayerViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val binding = ItemPlayerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PlayerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = playerList!![position]
        // Bind the player object to the layout using Data Binding
        holder.binding.setPlayer(player)
    }

    override fun getItemCount(): Int {
        return playerList?.size ?: 0
    }

    /**
     * ViewHolder class for individual player items in the RecyclerView.
     */
    class PlayerViewHolder
    /**
     * @param binding The binding object for the item layout.
     */(
        /** View binding for the player item layout.  */
        val binding: ItemPlayerBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    )
}
