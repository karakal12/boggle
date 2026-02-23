package com.amibar.boggle.ui.multiplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.amibar.boggle.data.User;
import com.amibar.boggle.databinding.FragmentMultiplayerOnGameEndBinding;

import java.util.ArrayList;
import java.util.HashMap;

public class MultiplayerOnGameEndFragment extends DialogFragment {

    public static final String TAG = "MultiplayerOnGameEndFragment";

    private FragmentMultiplayerOnGameEndBinding binding;
    private PlayersWordsAdapter playersWordsAdapter;

    public static MultiplayerOnGameEndFragment newInstance(HashMap<User, ArrayList<String>> playersWords) {
        MultiplayerOnGameEndFragment fragment = new MultiplayerOnGameEndFragment();
        Bundle args = new Bundle();
        args.putSerializable("playersWords", playersWords);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMultiplayerOnGameEndBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.playersWordsList.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        if (getArguments() != null) {
            HashMap<User, ArrayList<String>> playersWords = (HashMap<User, ArrayList<String>>) getArguments().getSerializable("playersWords");
            playersWordsAdapter = new PlayersWordsAdapter(requireContext(), playersWords);
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
}
