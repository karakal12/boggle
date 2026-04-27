package com.amibar.boggle.ui.game.multiplayer;

import static com.amibar.boggle.ui.game.multiplayer.MultiplayerActivity.ARG_PLAYER_ROLE;
import static com.amibar.boggle.ui.game.multiplayer.MultiplayerActivity.ARG_ROOM_CODE;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.amibar.boggle.data.FirebaseHandler;
import com.amibar.boggle.data.PlayerRole;
import com.amibar.boggle.data.User;
import com.amibar.boggle.databinding.FragmentLobbyBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays the multiplayer lobby.
 * It shows the list of players currently in the room and allows the host to start the game.
 * It listens for changes in the Firebase room data to update the player list and detect game start.
 */
public class LobbyFragment extends Fragment {
    /** Tag used for identifying this fragment. */
    public static final String TAG = "LobbyFragment";

    /** View binding for fragment layout. */
    private FragmentLobbyBinding binding;
    /** Adapter for the player list RecyclerView. */
    private PlayerAdapter playerAdapter;
    /** Local list of users currently in the lobby. */
    private final List<User> playerList = new ArrayList<>();

    /** The unique code for the current game room. */
    private String roomCode;
    /** The role of the local player (HOST or GUEST). */
    private PlayerRole playerRole;
    /** The local player's user data. */
    private User player;
    /** Reference to the room's node in Firebase Realtime Database. */
    private DatabaseReference roomRef;
    /** Listener for player list and game start updates in Firebase. */
    private ValueEventListener playerListener;

    /**
     * Creates a new instance of LobbyFragment.
     * @param roomCode The code of the room to join.
     * @param playerRole The role of the player.
     * @return A new instance.
     */
    public static LobbyFragment newInstance(String roomCode, PlayerRole playerRole) {
        LobbyFragment fragment = new LobbyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOM_CODE, roomCode);
        args.putString(ARG_PLAYER_ROLE, playerRole.name());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLobbyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up the RecyclerView and adapter for displaying players
        binding.playerList.setLayoutManager(new LinearLayoutManager(requireContext()));
        playerAdapter = new PlayerAdapter(playerList);
        binding.playerList.setAdapter(playerAdapter);

        // Extract room code and player role from arguments
        Bundle args = getArguments();
        if (args != null) {
            roomCode = args.getString(ARG_ROOM_CODE);
            binding.setRoomCode(roomCode);
            String roleStr = args.getString(ARG_PLAYER_ROLE);
            if (roleStr != null) {
                playerRole = PlayerRole.valueOf(roleStr);
            }
        }
        
        // Retrieve local player data from FirebaseHandler
        player = FirebaseHandler.getInstance().getUserData();

        // Only the host can see and click the "Start Game" button
        if (playerRole == PlayerRole.HOST) {
            binding.startButton.setVisibility(View.VISIBLE);
            binding.startButton.setOnClickListener(this::startGame);
        } else {
            binding.startButton.setVisibility(View.GONE);
        }

        // Connect to Firebase and register as a player in this room
        if (roomCode != null) {
            roomRef = FirebaseHandler.getInstance().getRootRef().child("rooms").child(roomCode);
            listenForPlayers();
            
            if (player != null) {
                DatabaseReference myPlayerRef = roomRef.child("players").child(FirebaseHandler.getInstance().getCurrentUserId());
                myPlayerRef.setValue(player);
                // Ensure the player is removed from the room list if they disconnect or close the app
                myPlayerRef.onDisconnect().removeValue();
            }
        }
    }

    /**
     * Attaches a listener to the Firebase room reference.
     * Updates the player list when players join/leave and navigates to the game when started.
     */
    private void listenForPlayers() {
        playerListener = new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                playerList.clear();
                DataSnapshot playersSnapshot = snapshot.child("players");
                for (DataSnapshot playerSnapshot : playersSnapshot.getChildren()) {
                    User player = playerSnapshot.getValue(User.class);
                    if (player != null) {
                        playerList.add(player);
                    }
                }
                playerAdapter.notifyDataSetChanged();
                
                // If the host has marked the game as started, transition to the game fragment
                Boolean gameStarted = snapshot.child("gameStarted").getValue(Boolean.class);
                if (Boolean.TRUE.equals(gameStarted)) {
                    ((MultiplayerActivity) requireActivity()).startGame();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Potential error handling
            }
        };
        roomRef.addValueEventListener(playerListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove the Firebase listener when the view is destroyed to avoid memory leaks
        if (roomRef != null && playerListener != null) {
            roomRef.removeEventListener(playerListener);
        }
        binding = null;
    }

    /**
     * Sets the 'gameStarted' flag to true in Firebase.
     * This is only callable by the host.
     * @param view The clicked view.
     */
    private void startGame(View view) {
        if (playerRole == PlayerRole.HOST && roomRef != null) {
            roomRef.child("gameStarted").setValue(true);
        }
    }
}
