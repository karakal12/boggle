package com.amibar.boggle.ui.mainmenu;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.amibar.boggle.data.User;
import com.amibar.boggle.databinding.ItemFriendBinding;

public class FriendAdapter extends ListAdapter<User, FriendAdapter.FriendViewHolder> {

    private final OnInviteClickListener inviteClickListener;

    public interface OnInviteClickListener {
        void onInviteClick(User friend);
    }

    public FriendAdapter(OnInviteClickListener inviteClickListener) {
        super(new UserDiffCallback());
        this.inviteClickListener = inviteClickListener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFriendBinding binding = ItemFriendBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FriendViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User friend = getItem(position);
        holder.binding.setFriend(friend);
        holder.binding.inviteButton.setOnClickListener(v -> {
            if (inviteClickListener != null) {
                inviteClickListener.onInviteClick(friend);
            }
        });
        holder.binding.executePendingBindings();
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        final ItemFriendBinding binding;

        public FriendViewHolder(@NonNull ItemFriendBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static class UserDiffCallback extends DiffUtil.ItemCallback<User> {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getEmail().equals(newItem.getEmail());
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getDisplayName().equals(newItem.getDisplayName()) &&
                   oldItem.getProfileImageBase64().equals(newItem.getProfileImageBase64());
        }
    }
}