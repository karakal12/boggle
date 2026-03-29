package com.amibar.boggle.ui.game.multiplayer;

import static com.amibar.boggle.ui.game.multiplayer.MultiplayerActivity.ARG_PLAYER_ROLE;
import static com.amibar.boggle.ui.game.multiplayer.MultiplayerActivity.ARG_ROOM_CODE;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.amibar.boggle.data.PlayerRole;
import com.amibar.boggle.databinding.FragmentJoinOrCreateRoomBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class JoinOrCreateRoomFragment extends DialogFragment {
    public static final String TAG = "JoinOrCreateRoomFragment";
    private static final String ARG_INITIAL_ROOM_CODE = "initial_room_code";

    private FragmentJoinOrCreateRoomBinding binding;

    public JoinOrCreateRoomFragment() {
        // Required empty public constructor
    }

    public static JoinOrCreateRoomFragment newInstance(String roomCode) {
        JoinOrCreateRoomFragment fragment = new JoinOrCreateRoomFragment();
        Bundle args = new Bundle();
        args.putString(ARG_INITIAL_ROOM_CODE, roomCode);
        fragment.setArguments(args);
        return fragment;
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

        if (getArguments() != null && getArguments().containsKey(ARG_INITIAL_ROOM_CODE)) {
            String initialRoomCode = getArguments().getString(ARG_INITIAL_ROOM_CODE);
            binding.roomCodeTV.setText(initialRoomCode);
            binding.roomCodeTIL.setVisibility(View.VISIBLE);
            binding.createRoom.setVisibility(View.GONE);
            binding.joinRoom.setOnClickListener(this::joinRoom);
        }
    }

    private void init() {
        binding.createRoom.setOnClickListener(view -> {
            binding.roomCodeTIL.setVisibility(View.VISIBLE);
            binding.joinRoom.setVisibility(View.GONE);
            view.setOnClickListener(this::createRoom);
        });

        binding.joinRoom.setOnClickListener(view -> {
            binding.roomCodeTIL.setVisibility(View.VISIBLE);
            binding.createRoom.setVisibility(View.GONE);
            view.setOnClickListener(this::joinRoom);
        });
    }

    private void createRoom(View view) {
        Intent intent = makeIntent(PlayerRole.HOST);
        if (intent != null) {
            startActivity(intent);
        }
        dismiss();
    }

    private void joinRoom(View view) {
        Editable roomCodeTVText = binding.roomCodeTV.getText();
        if (roomCodeTVText == null || roomCodeTVText.toString().isEmpty()) return;

        String roomCode = roomCodeTVText.toString();
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomCode);

        roomRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // Room exists, proceed to join
                Intent intent = makeIntent(PlayerRole.GUEST);
                if (intent != null) {
                    startActivity(intent);
                    dismiss();
                }
            } else {
                // Room doesn't exist, show error to user
                binding.roomCodeTIL.setError("Room not found");
            }
        });
    }



    private Intent makeIntent(PlayerRole role) {
        Editable roomCodeTVText = binding.roomCodeTV.getText();
        if (roomCodeTVText != null && !roomCodeTVText.toString().isEmpty()) {
            Intent intent = new Intent(requireContext(), MultiplayerActivity.class);
            intent.putExtra(ARG_ROOM_CODE, roomCodeTVText.toString());
            intent.putExtra(ARG_PLAYER_ROLE, role);
            return intent;
        }
        return null;
    }
}
