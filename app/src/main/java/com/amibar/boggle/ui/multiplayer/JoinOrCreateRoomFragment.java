package com.amibar.boggle.ui.multiplayer;

import static com.amibar.boggle.ui.multiplayer.MultiplayerActivity.ARG_PLAYER_ROLE;
import static com.amibar.boggle.ui.multiplayer.MultiplayerActivity.ARG_ROOM_CODE;

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

public class JoinOrCreateRoomFragment extends DialogFragment {
    public static final String TAG = "JoinOrCreateRoomFragment";

    private FragmentJoinOrCreateRoomBinding binding;

    public JoinOrCreateRoomFragment() {
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
        Intent intent = makeIntent(PlayerRole.GUEST);
        if (intent != null) {
            startActivity(intent);
        }
        dismiss();
    }

    private Intent makeIntent(PlayerRole role) {
        Editable roomCodeTVText = binding.roomCodeTV.getText();
        if (roomCodeTVText != null && !roomCodeTVText.toString().isEmpty()) {
            Intent intent = new Intent(requireContext(), MultiplayerActivity.class);
            intent.putExtra(ARG_ROOM_CODE, roomCodeTVText.toString());
            intent.putExtra(ARG_PLAYER_ROLE, role);
            // We removed the User object (ARG_PLAYER) from the Intent to avoid DeadObjectException.
            // The User class contains a Base64 encoded profile image which can exceed the 1MB 
            // Binder transaction limit. MultiplayerActivity now retrieves the User data 
            // directly from the FirebaseHandler singleton.
            return intent;
        }
        return null;
    }
}
