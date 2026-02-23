package com.amibar.boggle.ui.multiplayer;

import static com.amibar.boggle.ui.multiplayer.MultiplayerActivity.ARG_PLAYER;
import static com.amibar.boggle.ui.multiplayer.MultiplayerActivity.ARG_PLAYER_ROLE;
import static com.amibar.boggle.ui.multiplayer.MultiplayerActivity.ARG_ROOM_CODE;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.amibar.boggle.data.FirebaseHandler;
import com.amibar.boggle.data.PlayerRole;
import com.amibar.boggle.data.User;
import com.amibar.boggle.databinding.FragmentMultiplayerGameBinding;
import com.amibar.boggle.engine.BoggleGame;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MultiplayerGameFragment extends Fragment {
    public static final String TAG = "MultiplayerGameFragment";
    private FragmentMultiplayerGameBinding binding;
    private String roomCode;
    private PlayerRole playerRole;
    private User player;

    private DatabaseReference roomRef;
    private ValueEventListener boardListener;
    private ValueEventListener gameEndListener;

    public static MultiplayerGameFragment newInstance(PlayerRole playerRole, String roomCode, User player) {
        MultiplayerGameFragment fragment = new MultiplayerGameFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOM_CODE, roomCode);
        args.putString(ARG_PLAYER_ROLE, playerRole.name());
        args.putSerializable(ARG_PLAYER, player);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            roomCode = getArguments().getString(ARG_ROOM_CODE);
            playerRole = PlayerRole.valueOf(getArguments().getString(ARG_PLAYER_ROLE));
            player = getArguments().getSerializable(ARG_PLAYER, User.class);
        }

        if (roomCode != null){
            roomRef = FirebaseHandler.getDatabase().getReference("rooms").child(roomCode);
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

        listenForGameEnd();

        if (playerRole == PlayerRole.HOST) {
            // Host creates the game and uploads the board
            BoggleGame game = binding.boggleView.newGame();
            roomRef.child("board").setValue(new String(game.getBoard()))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to upload board", e));
            setupGame(game);
        } else {
            // Guest downloads the board and creates the local game
            boardListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String boardStr = snapshot.getValue(String.class);
                    if (boardStr != null && !boardStr.isEmpty()) {
                        BoggleGame game = binding.boggleView.setGame(boardStr.toCharArray());
                        setupGame(game);
                        // Once the board is received, we can stop listening
                        roomRef.child("board").removeEventListener(this);
                        boardListener = null;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to download board: " + error.getMessage());
                }
            };
            roomRef.child("board").addValueEventListener(boardListener);
        }
    }

    private void listenForGameEnd() {
        gameEndListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean gameEnded = snapshot.getValue(Boolean.class);
                if (Boolean.TRUE.equals(gameEnded)) {
                    collectResultsAndFinish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        roomRef.child("gameEnded").addValueEventListener(gameEndListener);
    }

    private void collectResultsAndFinish() {
        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<User, ArrayList<String>> playersWordsMap = new HashMap<>();
                
                DataSnapshot playersSnapshot = snapshot.child("players");
                DataSnapshot wordsSnapshot = snapshot.child("playerWords");
                
                Map<String, User> userIdToUser = new HashMap<>();
                for (DataSnapshot playerSnap : playersSnapshot.getChildren()) {
                    User u = playerSnap.getValue(User.class);
                    if (u != null) {
                        userIdToUser.put(playerSnap.getKey(), u);
                    }
                }
                
                for (DataSnapshot userWordsSnap : wordsSnapshot.getChildren()) {
                    String userId = userWordsSnap.getKey();
                    User u = userIdToUser.get(userId);
                    if (u != null) {
                        ArrayList<String> words = new ArrayList<>();
                        for (DataSnapshot wordSnap : userWordsSnap.getChildren()) {
                            words.add(wordSnap.getKey());
                        }
                        playersWordsMap.put(u, words);
                    }
                }
                
                if (isAdded()) {
                    ((MultiplayerActivity) requireActivity()).showGameResults(playersWordsMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupGame(BoggleGame game) {
        String userId = FirebaseHandler.getInstance().getCurrentUserId();
        game.addOnWordFoundListener(word ->
            roomRef.child("playerWords").child(userId).child(word).setValue(true)
        );
        
        game.addOnGameEndListener(() -> {
            if (playerRole == PlayerRole.HOST) {
                roomRef.child("gameEnded").setValue(true);
            }
        });

        binding.boggleView.startGame();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (boardListener != null && roomRef != null) {
            roomRef.child("board").removeEventListener(boardListener);
        }
        if (gameEndListener != null && roomRef != null) {
            roomRef.child("gameEnded").removeEventListener(gameEndListener);
        }
        binding = null;
    }
}
