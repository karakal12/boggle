package com.amibar.boggle.ui.shared;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amibar.boggle.databinding.ItemWordBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A shared adapter for displaying a list of words, typically used in game summary screens.
 * It highlights words found by the player and optionally words found by multiple players (common words).
 * Supports clicking on a word to trigger a callback (e.g., to show the word's path on the board).
 */
public class WordsAdapter extends RecyclerView.Adapter<WordsAdapter.ViewHolder> {

    /**
     * Interface definition for a callback to be invoked when a word is clicked.
     */
    public interface OnWordClickListener {
        /**
         * Called when a word is clicked.
         * @param word The clicked word.
         * @param path The hex-encoded path of the word on the board.
         */
        void onWordClick(String word, String path);
    }

    /** Map of all valid words on the board to their hex-encoded paths. */
    private final Map<String, String> solutions;
    /** List of words found by the current player. */
    private final List<String> playerWords;
    /** Set of words found by more than one player (for multiplayer). */
    private final Set<String> commonWords;
    /** Callback listener for word click events. */
    private final OnWordClickListener listener;
    /** Sorted list of words for efficient access. */
    private final List<String> sortedWords;

    /**
     * Full constructor for WordsAdapter.
     * @param solutions    Map of all solutions and their paths.
     * @param playerWords  Words found by the player.
     * @param commonWords  Words found by multiple players.
     * @param listener     Click listener.
     */
    public WordsAdapter(Map<String, String> solutions, List<String> playerWords, Set<String> commonWords, OnWordClickListener listener) {
        this.solutions = solutions;
        this.playerWords = playerWords;
        this.commonWords = commonWords;
        this.listener = listener;
        // Sort solutions alphabetically once in the constructor for performance
        this.sortedWords = new ArrayList<>(solutions.keySet());
        Collections.sort(this.sortedWords);
    }

    /**
     * Simplified constructor for single-player results.
     * @param solutions    Map of all solutions and their paths.
     * @param playerWords  Words found by the player.
     * @param listener     Click listener.
     */
    public WordsAdapter(Map<String, String> solutions, List<String> playerWords, OnWordClickListener listener) {
        this(solutions, playerWords, null, listener);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWordBinding binding = ItemWordBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String word = sortedWords.get(position);
        
        holder.binding.setWord(word);
        
        // Visual feedback based on word status:
        // RED: Common word (found by others)
        // GREEN: Found by this player (and not common)
        // BLACK: Missed word (not found by this player)
        if (commonWords != null && commonWords.contains(word)) {
            holder.binding.wordText.setTextColor(Color.RED);
        } else if (playerWords != null && playerWords.contains(word)){
            holder.binding.wordText.setTextColor(Color.GREEN);
        } else {
            holder.binding.wordText.setTextColor(Color.BLACK);
        }

        // Setup click listener to notify the parent component
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWordClick(word, solutions.get(word));
            }
        });

        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return sortedWords.size();
    }

    /**
     * ViewHolder for individual word items.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /** View binding for the word item. */
        final ItemWordBinding binding;
        ViewHolder(ItemWordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
