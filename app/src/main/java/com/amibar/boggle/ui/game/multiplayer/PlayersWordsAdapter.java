package com.amibar.boggle.ui.game.multiplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amibar.boggle.data.User;
import com.amibar.boggle.databinding.ItemPlayerScoreBinding;
import com.amibar.boggle.engine.BoggleGame;
import com.amibar.boggle.ui.shared.WordsAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Adapter for displaying the list of words found by each player in a multiplayer game.
 * It contains nested RecyclerViews (one per player) and synchronizes their horizontal scrolling.
 */
public class PlayersWordsAdapter extends RecyclerView.Adapter<PlayersWordsAdapter.ViewHolder> {

    /** List of players whose words are being displayed. */
    private final List<User> players;
    /** Map of each user to their list of found words. */
    private final HashMap<User, ArrayList<String>> playersWords;
    /** Map of all valid words on the board to their paths. */
    private final HashMap<String, String> solutions;
    /** Set of words that were found by more than one player. */
    private final Set<String> commonWords;
    /** Inflater for creating item views. */
    private final LayoutInflater inflater;
    /** Listener for word click events. */
    private final WordsAdapter.OnWordClickListener onWordClickListener;

    /** Set of child RecyclerViews to synchronize scrolling across. */
    private final Set<RecyclerView> childRecyclerViews = Collections.newSetFromMap(new WeakHashMap<>());
    /** Current horizontal scroll position to maintain consistency. */
    private int currentScrollX = 0;
    
    /**
     * Scroll listener attached to child RecyclerViews to synchronize their horizontal movement.
     */
    private final RecyclerView.OnScrollListener syncScrollHandler = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            currentScrollX += dx;

            // Only propagate scroll if the user is actively dragging this specific view
            if (recyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE){
                for (RecyclerView rv : childRecyclerViews) {
                    if (rv != recyclerView) {
                        rv.scrollBy(dx, 0);
                    }
                }
            }
        }
    };


    /**
     * Constructs a PlayersWordsAdapter.
     * @param context Current context.
     * @param playersWords Mapping of players to their words.
     * @param solutions Map of all solutions.
     */
    public PlayersWordsAdapter(Context context, HashMap<User, ArrayList<String>> playersWords, HashMap<String, String> solutions) {
        this(context, playersWords, solutions, null);
    }

    /**
     * Constructs a PlayersWordsAdapter with a click listener.
     * @param context Current context.
     * @param playersWords Mapping of players to their words.
     * @param solutions Map of all solutions.
     * @param onWordClickListener Callback for when a word is clicked.
     */
    public PlayersWordsAdapter(Context context, HashMap<User, ArrayList<String>> playersWords, HashMap<String, String> solutions, WordsAdapter.OnWordClickListener onWordClickListener) {
        this.playersWords = playersWords;
        this.solutions = solutions;
        this.players = new ArrayList<>(playersWords.keySet());
        this.inflater = LayoutInflater.from(context);
        this.commonWords = findCommonWords(playersWords);
        this.onWordClickListener = onWordClickListener;
    }

    /**
     * Identifies words found by at least two different players.
     * @param playersWords Mapping of users to their word lists.
     * @return A set of common words.
     */
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
        
        ArrayList<String> playerWords = playersWords.get(player);
        assert playerWords != null;
        holder.binding.wordsList.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        // Setup inner adapter for this player's words
        holder.binding.wordsList.setAdapter(new WordsAdapter(solutions, playerWords, commonWords, onWordClickListener));
        holder.binding.executePendingBindings();

        List<String> uniqueWords = new ArrayList<>(playerWords);
        uniqueWords.removeAll(commonWords);
        int score = 0;
        for (String word : uniqueWords) {
            score += BoggleGame.wordScore(word);
        }
        holder.binding.setScore(score);

        // Manage synchronized scrolling for the horizontal list
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

    /**
     * ViewHolder for individual player score items.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /** Binding for the player score item. */
        final ItemPlayerScoreBinding binding;
        ViewHolder(ItemPlayerScoreBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
