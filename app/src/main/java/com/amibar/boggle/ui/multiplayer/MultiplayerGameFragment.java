package com.amibar.boggle.ui.multiplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.amibar.boggle.databinding.FragmentMultiplayerGameBinding;

public class MultiplayerGameFragment extends Fragment {
    public static final String TAG = "MultiplayerGameFragment";
    private static final String ARG_ROOM_CODE = "room_code";

    private FragmentMultiplayerGameBinding binding;
    private String roomCode;

    public static MultiplayerGameFragment newInstance(String roomCode) {
        MultiplayerGameFragment fragment = new MultiplayerGameFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOM_CODE, roomCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            roomCode = getArguments().getString(ARG_ROOM_CODE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMultiplayerGameBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TODO: Initialize game with roomCode and handle multiplayer logic
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
