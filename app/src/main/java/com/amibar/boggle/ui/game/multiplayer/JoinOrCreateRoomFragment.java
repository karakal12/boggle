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

/**
 * A DialogFragment that allows users to either create a new multiplayer room or join an existing one.
 * It handles input validation and checks for the existence of a room code in Firebase before joining.
 */
public class JoinOrCreateRoomFragment extends DialogFragment {
    /** Tag used for identifying this fragment in the FragmentManager. */
    public static final String TAG = "JoinOrCreateRoomFragment";
    /** Key for the initial room code passed in arguments. */
    private static final String ARG_INITIAL_ROOM_CODE = "initial_room_code";
    /** Key for the initial player role passed in arguments. */
    private static final String ARG_INITIAL_PLAYER_ROLE = "initial_player_role";

    /** View binding for fragment layout. */
    private FragmentJoinOrCreateRoomBinding binding;

    /**
     * Required empty public constructor.
     */
    public JoinOrCreateRoomFragment() {
        // Required empty public constructor
    }

    /**
     * Creates a new instance of this fragment with optional initial room code and role.
     * @param roomCode The initial room code to display.
     * @param playerRole The player's role (HOST or GUEST).
     * @return A configured fragment instance.
     */
    public static JoinOrCreateRoomFragment newInstance(String roomCode, PlayerRole playerRole) {
        JoinOrCreateRoomFragment fragment = new JoinOrCreateRoomFragment();
        Bundle args = new Bundle();
        args.putString(ARG_INITIAL_ROOM_CODE, roomCode);
        args.putString(ARG_INITIAL_PLAYER_ROLE, playerRole.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Set dialog width to match parent for better usability
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
        
        // Handle arguments if they exist (e.g. when opening from a notification)
        if (getArguments() == null) return;
        if (!getArguments().containsKey(ARG_INITIAL_ROOM_CODE)) return;
        
        PlayerRole role = PlayerRole.valueOf(getArguments().getString(ARG_INITIAL_PLAYER_ROLE, PlayerRole.host.toString()));
        String initialRoomCode = getArguments().getString(ARG_INITIAL_ROOM_CODE);
        
        binding.roomCodeTV.setText(initialRoomCode);
        binding.roomCodeTIL.setVisibility(View.VISIBLE);
        
        // Toggle visibility based on the intended role
        binding.createRoom.setVisibility(role == PlayerRole.host ? View.VISIBLE: View.GONE);
        binding.createRoom.setOnClickListener(this::createRoom);
        binding.joinRoom.setVisibility(role == PlayerRole.guest ? View.VISIBLE: View.GONE);
        binding.joinRoom.setOnClickListener(this::joinRoom);
    }

    /**
     * Initializes UI state and basic button listeners.
     */
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

    /**
     * Logic for creating a room. Transitions to MultiplayerActivity as HOST.
     * @param view The clicked view.
     */
    private void createRoom(View view) {
        Intent intent = makeIntent(PlayerRole.host);
        if (intent != null) {
            startActivity(intent);
        }
        dismiss();
    }

    /**
     * Logic for joining a room. Validates room code exists in Firebase before transitioning.
     * @param view The clicked view.
     */
    private void joinRoom(View view) {
        Editable roomCodeTVText = binding.roomCodeTV.getText();
        if (roomCodeTVText == null || roomCodeTVText.toString().isEmpty()) return;

        String roomCode = roomCodeTVText.toString();
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomCode);

        // Verify if room exists in Realtime Database
        roomRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // Room exists, proceed to join as GUEST
                Intent intent = makeIntent(PlayerRole.guest);
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


    /**
     * Helper to create an Intent for MultiplayerActivity.
     * @param role The role to pass to the activity.
     * @return The configured Intent or null if room code is missing.
     */
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
