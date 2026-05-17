package com.amibar.boggle.ui.game.multiplayer;

import static com.amibar.boggle.ui.game.multiplayer.MultiplayerActivity.ARG_PLAYER_ROLE;
import static com.amibar.boggle.ui.game.multiplayer.MultiplayerActivity.ARG_ROOM_CODE;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.amibar.boggle.data.FirebaseHandler;
import com.amibar.boggle.data.PlayerRole;
import com.amibar.boggle.data.User;
import com.amibar.boggle.databinding.FragmentMultiplayerGameBinding;
import com.amibar.boggle.engine.BoggleGame;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fragment responsible for the multiplayer game logic.
 * This fragment manages the Boggle game state, synchronizes the game board via Firebase,
 * listens for game completion, and handles real-time updates for found words.
 */
public class MultiplayerGameFragment extends Fragment {
    /** Tag used for logging and fragment identification. */
    public static final String TAG = "MultiplayerGameFragment";
    
    /** View binding for the fragment layout. */
    private FragmentMultiplayerGameBinding binding;
    /** The code of the current multiplayer room. */
    private String roomCode;
    /** The role of the local player (HOST or GUEST). */
    private PlayerRole playerRole;

    /** Reference to the room node in Firebase Realtime Database. */
    private DatabaseReference roomRef;
    /** Listener for the game board string in Firebase. */
    private ValueEventListener boardListener;
    /** Listener for the game end flag in Firebase. */
    private ValueEventListener gameEndListener;
    /** Listener to detect if the room is deleted from outside. */
    private ChildEventListener gameDestroyedListener;

    /**
     * Creates a new instance of MultiplayerGameFragment with the specified role and room code.
     *
     * @param playerRole The role of the player (HOST or GUEST).
     * @param roomCode   The unique code for the multiplayer room.
     * @return A new instance of MultiplayerGameFragment.
     */
    public static MultiplayerGameFragment newInstance(PlayerRole playerRole, String roomCode) {
        MultiplayerGameFragment fragment = new MultiplayerGameFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOM_CODE, roomCode);
        args.putString(ARG_PLAYER_ROLE, playerRole.name());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve arguments passed via newInstance
        if (getArguments() != null) {
            roomCode = getArguments().getString(ARG_ROOM_CODE);
            playerRole = PlayerRole.valueOf(getArguments().getString(ARG_PLAYER_ROLE));
        }

        // Initialize the Firebase reference for the specific room
        if (roomCode != null){
            roomRef = FirebaseHandler.getDatabase().getReference("rooms").child(roomCode);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout using View Binding
        binding = FragmentMultiplayerGameBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Start listening for game-wide events
        listenForGameEnd();
        listenForGameDestroyed();

        if (playerRole == PlayerRole.host) {
            // The Host is responsible for generating the game board and sharing it
            BoggleGame game = binding.boggleView.newGame();
            roomRef.child("board").setValue(new String(game.getBoard()))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to upload board", e));
            setupGame(game);
        } else {
            // Guests wait for the Host to upload the board before starting
            boardListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String boardStr = snapshot.getValue(String.class);
                    if (boardStr != null && !boardStr.isEmpty()) {
                        // Once the board is available, initialize the local game with it
                        BoggleGame game = binding.boggleView.setGame(boardStr.toCharArray());
                        setupGame(game);
                        
                        // Stop listening for board changes once it's successfully received
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

    /**
     * Sets up a listener for the 'gameEnded' flag in Firebase.
     * When the flag is set to true, it triggers the results collection process.
     */
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

    /**
     * Listens for the deletion of the room in Firebase.
     * If the room node is removed (e.g., host cancels), the game session is terminated.
     */
    private void listenForGameDestroyed() {
        gameDestroyedListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String deletedNodeKey = snapshot.getKey();
                // Check if the removed node is the current room
                if (deletedNodeKey != null && deletedNodeKey.equals(roomCode)) {
                    if (isAdded()) {
                        requireActivity().finish();
                        Toast.makeText(requireContext(), "Game was destroyed by HOST", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };

        // Listen for changes in the parent 'rooms' node
        if (roomRef.getParent() != null) {
            roomRef.getParent().addChildEventListener(gameDestroyedListener);
        }
    }

    /**
     * Collects all players' found words and the game results from Firebase.
     * After data collection, it notifies the activity to show the final results screen.
     */
    private void collectResultsAndFinish() {
        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<User, ArrayList<String>> playersWordsMap = new HashMap<>();
                DataSnapshot playersSnapshot = snapshot.child("players");
                
                long playersCount = playersSnapshot.getChildrenCount();
                if (playersCount == 0) return;

                AtomicInteger fetchedCount = new AtomicInteger(0);

                for (DataSnapshot playerSnap : playersSnapshot.getChildren()) {
                    String uid = playerSnap.getKey();
                    if (uid == null) continue;

                    // Aggregate words found by this player from the 'words' child
                    ArrayList<String> words = new ArrayList<>();
                    DataSnapshot wordsSnap = playerSnap.child("words");
                    for (DataSnapshot wordSnap : wordsSnap.getChildren()) {
                        words.add(wordSnap.getKey());
                    }

                    // Fetch the full User object from the central 'users' node
                    FirebaseHandler.getDatabase().getReference("users").child(uid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnap) {
                                    User user = userSnap.getValue(User.class);
                                    if (user != null) {
                                        playersWordsMap.put(user, words);
                                    }
                                    
                                    if (fetchedCount.incrementAndGet() == (int) playersCount) {
                                        finalizeResults(playersWordsMap);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    if (fetchedCount.incrementAndGet() == (int) playersCount) {
                                        finalizeResults(playersWordsMap);
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to collect final results: " + error.getMessage());
            }
        });
    }

    private void finalizeResults(HashMap<User, ArrayList<String>> playersWordsMap) {
        if (isAdded()) {
            BoggleGame game = binding.boggleView.getGame();
            // Transition to the results view in the parent activity
            ((MultiplayerActivity) requireActivity()).showGameResults(game.getSolutions().toMap(), playersWordsMap);
        }
    }

    /**
     * Initializes the Boggle game logic and attaches listeners for local game events.
     *
     * @param game The BoggleGame instance to configure.
     */
    private void setupGame(BoggleGame game) {
        String userId = FirebaseHandler.getInstance().getCurrentUserId();
        
        // Listen for words found locally and sync them to Firebase under the player's node
        game.addOnWordFoundListener(word ->
            roomRef.child("players").child(userId).child("words").child(word).setValue(true)
        );
        
        // Listen for game end (timer expire)
        game.addOnGameEndListener(() -> {
            if (playerRole == PlayerRole.host) {
                // if room already deleted
                if (roomRef.getParent() == null) return;

                // Host marks the game as ended globally in Firebase
                roomRef.child("gameEnded").setValue(true);
            }
        });

        // Start the visual countdown/timer
        binding.boggleView.startGame();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up Firebase listeners to prevent memory leaks and unexpected behavior
        if (boardListener != null && roomRef != null) {
            roomRef.child("board").removeEventListener(boardListener);
        }
        if (gameEndListener != null && roomRef != null) {
            roomRef.child("gameEnded").removeEventListener(gameEndListener);
        }
        if (gameDestroyedListener != null && roomRef != null && roomRef.getParent() != null) {
            roomRef.getParent().removeEventListener(gameDestroyedListener);
        }
        binding = null;
    }
}
