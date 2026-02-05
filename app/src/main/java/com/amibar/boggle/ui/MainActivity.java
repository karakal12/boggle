package com.amibar.boggle.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amibar.boggle.R;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class MainActivity extends AppCompatActivity {
    private Button singlePlayerButton;
    private Button multiPlayerButton;
    private Button friendsButton;
    private Button leaderboardsButton;

    private final ActivityResultLauncher<Intent> singlePlayerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    int score = result.getData().getIntExtra(SinglePlayerActivity.EXTRA_SCORE, 0);
                    Toast.makeText(this, "Game finished! Your score: " + score, Toast.LENGTH_LONG).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
    }

    private void init(){
        singlePlayerButton = findViewById(R.id.singlePlayerButton);
        multiPlayerButton = findViewById(R.id.multiPlayerButton);
        friendsButton = findViewById(R.id.friendsListButton);
        leaderboardsButton = findViewById(R.id.leaderboardsButton);

        singlePlayerButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SinglePlayerActivity.class);
            singlePlayerLauncher.launch(intent);
        });
    }
}
