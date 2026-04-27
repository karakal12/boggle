package com.amibar.boggle.ui.game.multiplayer;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amibar.boggle.data.FirebaseHandler;
import com.amibar.boggle.data.PlayerRole;
import com.amibar.boggle.data.User;
import com.amibar.boggle.databinding.ActivityMultiplayerBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Activity that hosts the multiplayer game experience.
 * It manages the transition between the lobby and the active game, handles window insets,
 * and ensures proper cleanup of the room in Firebase when the activity is destroyed.
 */
public class MultiplayerActivity extends AppCompatActivity {
    /** Tag used for logging. */
    public static final String TAG = "MultiplayerActivity";
    /** Intent extra key for the room code. */
    public static final String ARG_ROOM_CODE = "room_code";
    /** Intent extra key for the player's role (HOST or GUEST). */
    public static final String ARG_PLAYER_ROLE = "player_role";
    
    /** View binding for the activity layout. */
    private ActivityMultiplayerBinding binding;
    
    /** The code of the current multiplayer room. */
    private String roomCode;
    /** The role of the local player in this session. */
    private PlayerRole playerRole;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge display
        EdgeToEdge.enable(this);
        
        binding = ActivityMultiplayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Adjust layout for system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Retrieve room details from the starting Intent
        if (getIntent() != null) {
            playerRole = getIntent().getSerializableExtra(ARG_PLAYER_ROLE, PlayerRole.class);
            roomCode = getIntent().getStringExtra(ARG_ROOM_CODE);
        }

        // Initialize by showing the LobbyFragment
        if (savedInstanceState == null && roomCode != null && playerRole != null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(binding.main.getId(), LobbyFragment.newInstance(roomCode, playerRole), LobbyFragment.TAG)
                    .commit();
        }
    }
    
    /**
     * Replaces the current fragment with MultiplayerGameFragment to start the active game.
     */
    public void startGame(){
        if (roomCode != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(binding.main.getId(), MultiplayerGameFragment.newInstance(playerRole, roomCode), MultiplayerGameFragment.TAG)
                    .commit();
        }
    }

    /**
     * Displays a dialog showing the final words and scores of all players.
     * @param solutions Map of all possible words and their paths.
     * @param playersWords Map of each user to the list of words they found.
     */
    public void showGameResults(HashMap<String, String> solutions, HashMap<User, ArrayList<String>> playersWords) {
        MultiplayerOnGameEndFragment fragment = MultiplayerOnGameEndFragment.newInstance(solutions, playersWords);
        fragment.show(getSupportFragmentManager(), MultiplayerOnGameEndFragment.TAG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup: remove the player from the room or delete the room if empty
        if (roomCode != null) {
            String userId = FirebaseHandler.getInstance().getCurrentUserId();
            if (userId != null) {
                DatabaseReference roomRef = FirebaseHandler.getDatabase().getReference("rooms").child(roomCode);
                // Remove local player from the Firebase list
                roomRef.child("players").child(userId).removeValue().addOnCompleteListener(task -> {
                    // Check if any players remain; if not, remove the entire room node
                    roomRef.child("players").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                                // Housekeeping: remove empty room node
                                roomRef.removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                });
            }
        }
    }
}
