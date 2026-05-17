package com.amibar.boggle.ui.mainmenu;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.amibar.boggle.data.User;
import com.amibar.boggle.databinding.ItemFriendBinding;

/**
 * Adapter for displaying a list of friends in the FriendListActivity.
 * Extends ListAdapter to provide efficient list updates using DiffUtil.
 */
public class FriendAdapter extends ListAdapter<User, FriendAdapter.FriendViewHolder> {

    /** Callback for when the invite button is clicked for a specific friend. */
    private final OnInviteClickListener inviteClickListener;

    /**
     * Interface definition for a callback to be invoked when an invite button is clicked.
     */
    public interface OnInviteClickListener {
        /**
         * Called when the invite button for a friend is clicked.
         * @param friend The user to invite.
         */
        void onInviteClick(User friend);
    }

    /**
     * Constructs a new FriendAdapter.
     * @param inviteClickListener The listener for invite button clicks.
     */
    public FriendAdapter(OnInviteClickListener inviteClickListener) {
        super(new UserDiffCallback());
        this.inviteClickListener = inviteClickListener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout using View Binding
        ItemFriendBinding binding = ItemFriendBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FriendViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User friend = getItem(position);
        // Bind the friend data to the layout
        holder.binding.setFriend(friend);
        
        // Handle invite button clicks
        holder.binding.inviteButton.setOnClickListener(v -> {
            if (inviteClickListener != null) {
                inviteClickListener.onInviteClick(friend);
            }
        });
        
        // Immediate binding execution to prevent layout flickering
        holder.binding.executePendingBindings();
    }

    /**
     * ViewHolder for individual friend items in the list.
     */
    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        /** View binding for the friend item layout. */
        final ItemFriendBinding binding;

        /**
         * @param binding The binding object for the item.
         */
        public FriendViewHolder(@NonNull ItemFriendBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    /**
     * DiffUtil callback for comparing User objects to optimize list updates.
     */
    private static class UserDiffCallback extends DiffUtil.ItemCallback<User> {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            // Check identity based on email (assuming emails are unique)
            return oldItem.getEmail().equals(newItem.getEmail());
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            // Check if displayed details have changed
            return oldItem.getDisplayName().equals(newItem.getDisplayName()) &&
                   oldItem.getProfileImageBase64().equals(newItem.getProfileImageBase64());
        }
    }
}
