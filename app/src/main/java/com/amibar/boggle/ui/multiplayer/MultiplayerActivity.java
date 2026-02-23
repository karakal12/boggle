package com.amibar.boggle.ui.multiplayer;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amibar.boggle.data.PlayerRole;
import com.amibar.boggle.data.User;
import com.amibar.boggle.databinding.ActivityMultiplayerBinding;

import java.util.ArrayList;
import java.util.HashMap;

public class MultiplayerActivity extends AppCompatActivity {
    public static final String TAG = "MultiplayerActivity";
    public static final String ARG_ROOM_CODE = "room_code";
    public static final String ARG_PLAYER_ROLE = "player_role";
    public static final String ARG_PLAYER = "player";
    
    private ActivityMultiplayerBinding binding;
    
    private String roomCode;
    private PlayerRole playerRole;
    private User player;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        // Correct ViewBinding initialization
        binding = ActivityMultiplayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Retrieve data from the Intent
        if (getIntent() != null) {
            String roleStr = getIntent().getStringExtra(ARG_PLAYER_ROLE);
            if (roleStr != null) {
                playerRole = PlayerRole.valueOf(roleStr);
            }
            roomCode = getIntent().getStringExtra(ARG_ROOM_CODE);
            player = getIntent().getSerializableExtra(ARG_PLAYER, User.class);
        }

        // Load the LobbyFragment with arguments if this is the first time the activity is created
        if (savedInstanceState == null && roomCode != null && playerRole != null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(binding.main.getId(), LobbyFragment.newInstance(roomCode, playerRole, player), LobbyFragment.TAG)
                    .commit();
        }
    }
    
    public void startGame(){
        if (roomCode != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(binding.main.getId(), MultiplayerGameFragment.newInstance(playerRole, roomCode, player), MultiplayerGameFragment.TAG)
                    .commit();
        }
    }

    public void showGameResults(HashMap<User, ArrayList<String>> playersWords) {
        MultiplayerOnGameEndFragment fragment = MultiplayerOnGameEndFragment.newInstance(playersWords);
        fragment.show(getSupportFragmentManager(), MultiplayerOnGameEndFragment.TAG);
    }
}
