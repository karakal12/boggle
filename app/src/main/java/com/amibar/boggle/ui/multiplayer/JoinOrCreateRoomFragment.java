package com.amibar.boggle.ui.multiplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.amibar.boggle.databinding.FragmentJoinOrCreateRoomBinding;

public class JoinOrCreateRoomFragment extends DialogFragment {
    public static final String TAG = "JoinOrCreateRoomFragment";

    private FragmentJoinOrCreateRoomBinding binding;


    public JoinOrCreateRoomFragment(){
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentJoinOrCreateRoomBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void init(){
        binding.createRoom.setOnClickListener(this::createRoom);
        binding.joinRoom.setOnClickListener(this::joinRoom);
    }

    private void createRoom(View view){
        // TODO: Create Room
    }

    private void joinRoom(View view){
        // TODO: Join Room
    }

}
