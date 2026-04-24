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

public class MultiplayerActivity extends AppCompatActivity {
    public static final String TAG = "MultiplayerActivity";
    public static final String ARG_ROOM_CODE = "room_code";
    public static final String ARG_PLAYER_ROLE = "player_role";
    
    private ActivityMultiplayerBinding binding;
    
    private String roomCode;
    private PlayerRole playerRole;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityMultiplayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Retrieve data from the Intent
        if (getIntent() != null) {
            playerRole = getIntent().getSerializableExtra(ARG_PLAYER_ROLE, PlayerRole.class);
            roomCode = getIntent().getStringExtra(ARG_ROOM_CODE);
        }

        // Load the LobbyFragment with arguments if this is the first time the activity is created
        if (savedInstanceState == null && roomCode != null && playerRole != null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(binding.main.getId(), LobbyFragment.newInstance(roomCode, playerRole), LobbyFragment.TAG)
                    .commit();
        }
    }
    
    public void startGame(){
        if (roomCode != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(binding.main.getId(), MultiplayerGameFragment.newInstance(playerRole, roomCode), MultiplayerGameFragment.TAG)
                    .commit();
        }
    }

    public void showGameResults(HashMap<String, String> solutions, HashMap<User, ArrayList<String>> playersWords) {
        MultiplayerOnGameEndFragment fragment = MultiplayerOnGameEndFragment.newInstance(solutions, playersWords);
        fragment.show(getSupportFragmentManager(), MultiplayerOnGameEndFragment.TAG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (roomCode != null) {
            String userId = FirebaseHandler.getInstance().getCurrentUserId();
            if (userId != null) {
                DatabaseReference roomRef = FirebaseHandler.getDatabase().getReference("rooms").child(roomCode);
                // Remove only this player
                roomRef.child("players").child(userId).removeValue().addOnCompleteListener(task -> {
                    // Check if there are any players left in the room
                    roomRef.child("players").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                                // Last player left, delete the entire room
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
