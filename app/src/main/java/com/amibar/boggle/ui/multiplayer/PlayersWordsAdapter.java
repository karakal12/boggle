package com.amibar.boggle.ui.multiplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.amibar.boggle.data.User;
import com.amibar.boggle.databinding.ItemPlayerScoreBinding;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlayersWordsAdapter extends RecyclerView.Adapter<PlayersWordsAdapter.ViewHolder> {

    private final List<User> players;
    private final HashMap<User, ArrayList<String>> playersWords;
    private final Set<String> commonWords;
    private final LayoutInflater inflater;

    public PlayersWordsAdapter(Context context, HashMap<User, ArrayList<String>> playersWords) {
        this.playersWords = playersWords;
        this.players = new ArrayList<>(playersWords.keySet());
        this.inflater = LayoutInflater.from(context);
        this.commonWords = findCommonWords(playersWords);
    }

    private Set<String> findCommonWords(HashMap<User, ArrayList<String>> playersWords) {
        Set<String> allWords = new HashSet<>();
        Set<String> common = new HashSet<>();
        for (ArrayList<String> words : playersWords.values()) {
            for (String word : words) {
                if (!allWords.add(word)) {
                    common.add(word);
                }
            }
        }
        return common;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPlayerScoreBinding binding = ItemPlayerScoreBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User player = players.get(position);
        holder.binding.setPlayerName(player.getDisplayName());
        
        ArrayList<String> words = playersWords.get(player);
        holder.binding.wordsList.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.binding.wordsList.setAdapter(new WordsAdapter(words, commonWords));
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemPlayerScoreBinding binding;
        ViewHolder(ItemPlayerScoreBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
