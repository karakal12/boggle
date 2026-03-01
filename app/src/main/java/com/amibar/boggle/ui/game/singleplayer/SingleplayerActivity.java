package com.amibar.boggle.ui.game.singleplayer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amibar.boggle.data.FirebaseHandler;
import com.amibar.boggle.data.GameResult;
import com.amibar.boggle.databinding.ActivitySingleplayerBinding;
import com.amibar.boggle.engine.BoggleGame;
import com.amibar.boggle.ui.DonutActivity;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity that hosts the single-player Boggle game session.
 * It manages the game lifecycle, UI layout adjustments for edge-to-edge display,
 * and handles the end-of-game result reporting and summary display.
 */
public class SingleplayerActivity extends AppCompatActivity implements SingleplayerOnGameEndFragment.OnWordClickListener {

    /** Tag used for logging and debugging purposes. */
    private static final String TAG = "SingleplayerActivity";


    /** View binding instance for accessing layout components. */
    ActivitySingleplayerBinding binding;

    /** Key for passing the final score in an Intent result. */
    public static final String EXTRA_SCORE = "extra_score";

    private BoggleGame game;
    private boolean isGameEnded = false;

    /**
     * Called when the activity is first created.
     * Sets up the UI, handles window insets for edge-to-edge display,
     * and initializes the game end logic.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize view binding
        binding = ActivitySingleplayerBinding.inflate(getLayoutInflater());

        // Enable Edge-to-Edge display support for modern Android navigation
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        // Adjust padding to account for system bars (status bar, navigation bar) to prevent UI overlap
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Reference the underlying game engine from the custom BoggleView
        game = binding.boggleView.getGame();

        // Set up a listener for when the game timer runs out or the game ends
        game.addOnGameEndListener(() ->
                runOnUiThread(() -> {
                    // Ensure activity is still active before updating UI
                    if (isDestroyed()) {
                        return;
                    }
                    isGameEnded = true;

                    // Prepare result data to be returned to the calling activity (e.g., MainActivity)
                    Intent data = new Intent();
                    data.putExtra(EXTRA_SCORE, game.getScore());
                    setResult(RESULT_OK, data);

                    // Show the game summary dialog with found/missed words
                    showGameEndDialog();

                    // Synchronize the game results with the cloud database
                    uploadGameResults(game);
                }));

        game.addOnWordFoundListener(word -> {
            if (word.equalsIgnoreCase("donut")){
                game.stopTimer();
                Intent intent = new Intent(this, DonutActivity.class);
                startActivity(intent);
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isGameEnded) {
                    showGameEndDialog();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (game != null && !isGameEnded) {
            game.startTimer();
        }
    }

    /**
     * Uploads the game results to Firebase Realtime Database.
     * Data is organized under the user's reference in a "games" node,
     * using the current date and time as the unique key.
     *
     * @param game The finished {@link BoggleGame} instance containing final stats.
     */
    private void uploadGameResults(BoggleGame game) {
        FirebaseHandler handler = FirebaseHandler.getInstance();
        DatabaseReference userRef = handler.getUserRef();

        // Only attempt upload if the user is authenticated and reference is valid
        if (userRef != null) {
            // Map game engine data to a GameResult POJO
            GameResult result = new GameResult(
                    game.getScore(),
                    game.getFoundWords().size(),
                    game.getSolutions().size(),
                    game.getMaxScore()
            );

            // Generate a formatted timestamp to serve as the database key
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String timestamp = sdf.format(new Date());

            // Write the data to Firebase
            userRef.child("games").child(timestamp).setValue(result);
        }
    }

    /**
     * Builds and displays a dialog summary showing all possible solutions.
     * Iterates through all possible words on the board and highlights words
     * successfully found by the player in green.
     */
    private void showGameEndDialog() {
        if (isDestroyed()) {
            return;
        }

        // Initialize and display the custom dialog fragment
        try {
            // Create fragment instance with the formatted word list and final score string
            SingleplayerOnGameEndFragment fragment = SingleplayerOnGameEndFragment.newInstance(
                    game.getSolutions().toMap(),
                    game.getFoundWords(),
                    game.getScore()
            );
            fragment.setOnWordClickListener(this);

            // Use commitAllowingStateLoss to prevent crashes if the activity state was already saved
            getSupportFragmentManager().beginTransaction()
                    .add(fragment, SingleplayerOnGameEndFragment.TAG)
                    .commitAllowingStateLoss();
        } catch (Exception e) {
            // Fallback to prevent app crash if fragment transaction fails
            Log.e(TAG, "Failed to show game end dialog", e);
        }
    }

    @Override
    public void onWordClick(String word, String path) {
        binding.boggleView.showSolution(path);
    }
}
