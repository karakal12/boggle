package com.amibar.boggle.ui.singleplayer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amibar.boggle.R;
import com.amibar.boggle.data.FirebaseHandler;
import com.amibar.boggle.data.GameResult;
import com.amibar.boggle.databinding.ActivitySingleplayerBinding;
import com.amibar.boggle.engine.BoggleGame;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity that hosts the single-player Boggle game session.
 * It manages the game lifecycle, UI layout adjustments for edge-to-edge display,
 * and handles the end-of-game result reporting and summary display.
 */
public class SingleplayerActivity extends AppCompatActivity {

    /** Tag used for logging and debugging purposes. */
    private static final String TAG = "SingleplayerActivity";


    /** View binding instance for accessing layout components. */
    ActivitySingleplayerBinding binding;

    /** Key for passing the final score in an Intent result. */
    public static final String EXTRA_SCORE = "extra_score";

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
        BoggleGame game = binding.boggleView.getGame();

        // Set up a listener for when the game timer runs out or the game ends
        game.addOnGameEndListener(() ->
                runOnUiThread(() -> {
                    // Ensure activity is still active before updating UI
                    if (isDestroyed()) {
                        return;
                    }

                    // Prepare result data to be returned to the calling activity (e.g., MainActivity)
                    Intent data = new Intent();
                    data.putExtra(EXTRA_SCORE, game.getScore());
                    setResult(RESULT_OK, data);

                    // Show the game summary dialog with found/missed words
                    showGameEndDialog(game);

                    // Synchronize the game results with the cloud database
                    uploadGameResults(game);
                }));
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
     *
     * @param game The finished {@link BoggleGame} instance.
     */
    private void showGameEndDialog(BoggleGame game) {
        if (isDestroyed()) {
            return;
        }

        // Prepare alphabetical list of all valid words that were hidden in the grid
        List<String> sortedSolutions = new ArrayList<>(game.getSolutions());
        Collections.sort(sortedSolutions);
        List<String> foundByPlayer = game.getFoundWords();

        // Use SpannableStringBuilder to apply rich text formatting (colors) to the list
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append("Possible words (").append(String.valueOf(sortedSolutions.size())).append("):\n\n");

        for (int i = 0; i < sortedSolutions.size(); i++) {
            String s = sortedSolutions.get(i);
            int start = ssb.length();
            ssb.append(s);

            // If the player successfully identified this word, highlight it in green
            if (foundByPlayer.contains(s)) {
                ssb.setSpan(new ForegroundColorSpan(Color.GREEN), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            // Add a newline between words for readability, except after the last word
            if (i < sortedSolutions.size() - 1) {
                ssb.append('\n');
            }
        }

        // Initialize and display the custom dialog fragment
        try {
            // Create fragment instance with the formatted word list and final score string
            SingleplayerGameEndDialogFragment fragment = SingleplayerGameEndDialogFragment.newInstance(
                    ssb,
                    getString(R.string.score, game.getScore())
            );

            // Use commitAllowingStateLoss to prevent crashes if the activity state was already saved
            getSupportFragmentManager().beginTransaction()
                    .add(fragment, SingleplayerGameEndDialogFragment.TAG)
                    .commitAllowingStateLoss();
        } catch (Exception e) {
            // Fallback to prevent app crash if fragment transaction fails
            Log.e(TAG, "Failed to show game end dialog", e);
        }
    }
}
