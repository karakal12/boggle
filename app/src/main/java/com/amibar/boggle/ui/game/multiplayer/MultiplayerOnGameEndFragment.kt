package com.amibar.boggle.ui.game.multiplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.amibar.boggle.data.User;
import com.amibar.boggle.databinding.FragmentMultiplayerOnGameEndBinding;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A DialogFragment displayed at the end of a multiplayer game.
 * It shows a comprehensive list of words found by each player and indicates which words were common.
 */
public class MultiplayerOnGameEndFragment extends DialogFragment {

    /** Tag for identifying the fragment. */
    public static final String TAG = "MultiplayerOnGameEndFragment";
    /** Argument key for the map of players to their found words. */
    public static final String ARG_PLAYERS_WORDS = "playersWords";
    /** Argument key for the map of solution words to their paths. */
    public static final String ARG_SOLUTIONS = "solutions";

    /** View binding for the fragment layout. */
    private FragmentMultiplayerOnGameEndBinding binding;
    /** Adapter for displaying players' words in a list. */
    private PlayersWordsAdapter playersWordsAdapter;

    /**
     * Creates a new instance of MultiplayerOnGameEndFragment.
     * @param solutions Map of all valid words on the board and their paths.
     * @param playersWords Map of users and the words they found during the game.
     * @return A new fragment instance.
     */
    public static MultiplayerOnGameEndFragment newInstance(HashMap<String, String> solutions, HashMap<User, ArrayList<String>> playersWords) {
        MultiplayerOnGameEndFragment fragment = new MultiplayerOnGameEndFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PLAYERS_WORDS, playersWords);
        args.putSerializable(ARG_SOLUTIONS, solutions);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMultiplayerOnGameEndBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Populate the adapter with data from arguments
        if (getArguments() != null) {
            HashMap<User, ArrayList<String>> playersWords = getArguments().getSerializable(ARG_PLAYERS_WORDS, HashMap.class);
            HashMap<String, String> solutions = getArguments().getSerializable(ARG_SOLUTIONS, HashMap.class);
            
            if (playersWords != null) {
                playersWordsAdapter = new PlayersWordsAdapter(requireContext(), playersWords, solutions);
                binding.playersWordsList.setAdapter(playersWordsAdapter);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Set dialog width to match parent
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Ensure dialog layout remains consistent on resume
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}
