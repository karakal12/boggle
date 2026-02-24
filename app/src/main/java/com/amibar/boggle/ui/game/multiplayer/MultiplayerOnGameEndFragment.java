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

public class MultiplayerOnGameEndFragment extends DialogFragment {

    public static final String TAG = "MultiplayerOnGameEndFragment";
    public static final String ARG_PLAYERS_WORDS = "playersWords";
    public static final String ARG_SOLUTIONS = "solutions";

    private FragmentMultiplayerOnGameEndBinding binding;
    private PlayersWordsAdapter playersWordsAdapter;

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

        if (getArguments() != null) {
            HashMap<User, ArrayList<String>> playersWords = getArguments().getSerializable(ARG_PLAYERS_WORDS, HashMap.class);
            HashMap<String, String> solutions = getArguments().getSerializable(ARG_SOLUTIONS, HashMap.class);
            assert playersWords != null;
            playersWordsAdapter = new PlayersWordsAdapter(requireContext(), playersWords, solutions);
            binding.playersWordsList.setAdapter(playersWordsAdapter);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog() != null && getDialog().getWindow() != null) {{
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }}
    }
}
