package com.amibar.boggle.ui.multiplayer;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amibar.boggle.R;
import com.amibar.boggle.databinding.ItemWordBinding;
import java.util.List;
import java.util.Set;

public class WordsAdapter extends RecyclerView.Adapter<WordsAdapter.ViewHolder> {

    private final List<String> words;
    private final Set<String> commonWords;

    public WordsAdapter(List<String> words, Set<String> commonWords) {
        this.words = words;
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
        String word = words.get(position);
        holder.binding.setWord(word);
        if (commonWords.contains(word)) {
            holder.binding.wordText.setTextColor(Color.RED);
        } else {
            holder.binding.wordText.setTextColor(Color.BLACK);
        }
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemWordBinding binding;
        ViewHolder(ItemWordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
