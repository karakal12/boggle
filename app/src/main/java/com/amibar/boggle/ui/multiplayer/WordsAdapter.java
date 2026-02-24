package com.amibar.boggle.ui.multiplayer;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amibar.boggle.databinding.ItemWordBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WordsAdapter extends RecyclerView.Adapter<WordsAdapter.ViewHolder> {

    private final Map<String, String> solutions;
    private final List<String> playerWords;
    private final Set<String> commonWords;

    public WordsAdapter(HashMap<String, String> solutions, List<String> playerWords, Set<String> commonWords) {
        this.solutions = solutions;
        this.playerWords = playerWords;
        this.commonWords = commonWords;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWordBinding binding = ItemWordBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        List<String> words = new ArrayList<>(solutions.keySet());
        Collections.sort(words);
        String word = words.get(position);
        holder.binding.setWord(word);
        if (commonWords.contains(word)) {
            holder.binding.wordText.setTextColor(Color.RED);
        } else if (playerWords.contains(word)){
            holder.binding.wordText.setTextColor(Color.GREEN);
        } else {
            holder.binding.wordText.setTextColor(Color.BLACK);
        }
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return solutions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemWordBinding binding;
        ViewHolder(ItemWordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
