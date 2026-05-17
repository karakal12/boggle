package com.amibar.boggle.ui.mainmenu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amibar.boggle.data.User
import com.amibar.boggle.databinding.ItemFriendBinding
import com.amibar.boggle.ui.mainmenu.FriendAdapter.FriendViewHolder

/**
 * Adapter for displaying a list of friends in the FriendListActivity.
 * Extends ListAdapter to provide efficient list updates using DiffUtil.
 */
class FriendAdapter(
    /** Callback for when the invite button is clicked for a specific friend. */
    private val onInviteClick: (User?) -> Unit
) : ListAdapter<User?, FriendViewHolder?>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        // Inflate the item layout using View Binding
        val binding = ItemFriendBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = getItem(position)
        // Bind the friend data to the layout
        holder.binding.setFriend(friend)


        // Handle invite button clicks
        holder.binding.inviteButton.setOnClickListener {
            onInviteClick(friend)
        }


        // Immediate binding execution to prevent layout flickering
        holder.binding.executePendingBindings()
    }

    /**
     * ViewHolder for individual friend items in the list.
     */
    class FriendViewHolder
    /**
     * @param binding The binding object for the item.
     */(
        /** View binding for the friend item layout.  */
        val binding: ItemFriendBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    )

    /**
     * DiffUtil callback for comparing User objects to optimize list updates.
     */
    private class UserDiffCallback : DiffUtil.ItemCallback<User?>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            // Check identity based on email (assuming emails are unique)
            return oldItem.email == newItem.email
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            // Check if displayed details have changed
            return oldItem.displayName == newItem.displayName &&
                    oldItem.profileImageBase64 == newItem.profileImageBase64
        }
    }
}
