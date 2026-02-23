package com.amibar.boggle.ui.multiplayer;

import static com.amibar.boggle.ui.multiplayer.MultiplayerActivity.*;

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

public class LobbyFragment extends Fragment {
    public static final String TAG = "LobbyFragment";

    private FragmentLobbyBinding binding;
    private PlayerAdapter playerAdapter;
    private final List<User> playerList = new ArrayList<>();

    private String roomCode;
    private PlayerRole playerRole;
    private User player;
    private DatabaseReference roomRef;
    private ValueEventListener playerListener;

    public static LobbyFragment newInstance(String roomCode, PlayerRole playerRole, User player) {
        LobbyFragment fragment = new LobbyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOM_CODE, roomCode);
        args.putString(ARG_PLAYER_ROLE, playerRole.name());
        args.putSerializable(ARG_PLAYER, player);
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

        // set up the RecyclerView and adapter
        binding.playerList.setLayoutManager(new LinearLayoutManager(requireContext()));
        playerAdapter = new PlayerAdapter(playerList);
        binding.playerList.setAdapter(playerAdapter);

        // get the room code from the arguments
        Bundle args = getArguments();
        if (args != null) {
            roomCode = args.getString(ARG_ROOM_CODE);
            binding.setRoomCode(roomCode);
            String roleStr = args.getString(ARG_PLAYER_ROLE);
            if (roleStr != null) {
                playerRole = PlayerRole.valueOf(roleStr);
            }
            player = args.getSerializable(ARG_PLAYER, User.class);
        }

        if (playerRole == PlayerRole.HOST) {
            binding.startButton.setVisibility(View.VISIBLE);
            binding.startButton.setOnClickListener(this::startGame);
        } else {
            binding.startButton.setVisibility(View.GONE);
        }

        // Initialize Firebase reference and start listening for players
        if (roomCode != null) {
            roomRef = FirebaseHandler.getDatabase().getReference("rooms").child(roomCode);
            listenForPlayers();
        }
        roomRef.child("players").child(FirebaseHandler.getInstance().getCurrentUserId());
    }

    private void listenForPlayers() {
        playerListener = new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                playerList.clear();
                DataSnapshot playersSnapshot = snapshot.child("players");
                for (DataSnapshot playerSnapshot : playersSnapshot.getChildren()) {
                    User player = playerSnapshot.getValue(User.class);
                    if (player != null) {
                        playerList.add(player);
                    }
                }
                playerAdapter.notifyDataSetChanged();
                
                // Check if the game has started
                Boolean gameStarted = snapshot.child("gameStarted").getValue(Boolean.class);
                if (Boolean.TRUE.equals(gameStarted)) {
                    ((MultiplayerActivity) requireContext()).startGame();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        };
        roomRef.addValueEventListener(playerListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (roomRef != null && playerListener != null) {
            roomRef.removeEventListener(playerListener);
        }
        binding = null;
    }

    private void startGame(View view) {
        if (playerRole == PlayerRole.HOST && roomRef != null) {
            roomRef.child("gameStarted").setValue(true);
        }
    }
}
