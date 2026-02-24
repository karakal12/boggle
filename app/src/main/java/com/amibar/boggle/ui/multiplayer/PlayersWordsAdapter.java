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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

public class PlayersWordsAdapter extends RecyclerView.Adapter<PlayersWordsAdapter.ViewHolder> {

    private final List<User> players;
    private final HashMap<User, ArrayList<String>> playersWords;
    private final Set<String> commonWords;
    private final LayoutInflater inflater;

    private final Set<RecyclerView> childRecyclerViews = Collections.newSetFromMap(new WeakHashMap<>());
    private int currentScrollX = 0;
    private final RecyclerView.OnScrollListener syncScrollHandler = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            currentScrollX += dx;

            // only scroll if this view was scrolled by user and not another view
            if (recyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE){
                for (RecyclerView rv : childRecyclerViews) {
                    if (rv != recyclerView) {
                        rv.scrollBy(dx, 0);
                    }
                }
            }
        }
    };


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

        RecyclerView innerRv = holder.binding.wordsList;
        childRecyclerViews.add(innerRv);
        innerRv.clearOnScrollListeners();
        innerRv.addOnScrollListener(syncScrollHandler);
        innerRv.scrollTo(currentScrollX, 0);
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
