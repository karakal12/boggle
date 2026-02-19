package com.amibar.boggle.ui.singleplayer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amibar.boggle.R;
import com.amibar.boggle.data.FirebaseHandler;
import com.amibar.boggle.data.GameResult;
import com.amibar.boggle.engine.BoggleGame;
import com.amibar.boggle.views.BoggleView;
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
public class SinglePlayerActivity extends AppCompatActivity {

    /** Key for passing the final score in an Intent result. */
    public static final String EXTRA_SCORE = "extra_score";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable Edge-to-Edge display support for modern Android navigation
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_single_player);
        
        // Adjust padding to account for system bars (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the BoggleView and set up a listener for when the game timer runs out
        BoggleView boggleView = findViewById(R.id.boggle_view);
        BoggleGame game = boggleView.getGame();
        game.addOnGameEndListener(() -> {
            // Prepare result data to be returned to the calling activity
            Intent data = new Intent();
            data.putExtra(EXTRA_SCORE, game.getScore());
            setResult(RESULT_OK, data);

            // Upload game results to Firebase
            uploadGameResults(game);

            // Show the game summary dialog
            showGameEndDialog(game);
        });
    }

    /**
     * Uploads the game results to Firebase Realtime Database.
     * Uses the current date and time as the node key.
     * @param game The finished BoggleGame instance.
     */
    private void uploadGameResults(BoggleGame game) {
        FirebaseHandler handler = FirebaseHandler.getInstance();
        DatabaseReference userRef = handler.getUserRef();
        if (userRef != null) {
            GameResult result = new GameResult(
                    game.getScore(),
                    game.getFoundWords().size(),
                    game.getSolutions().size(),
                    game.getMaxScore()
            );
            
            // Generate a timestamp for the node key
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String timestamp = sdf.format(new Date());
            
            userRef.child("games").child(timestamp).setValue(result);
        }
    }

    /**
     * Builds and displays a dialog summary showing all possible solutions.
     * Highlights words found by the player in green.
     * 
     * @param game The finished BoggleGame instance.
     */
    private void showGameEndDialog(BoggleGame game) {
        // Create an alphabetically sorted list of all valid words on the board
        List<String> sortedSolutions = new ArrayList<>(game.getSolutions());
        Collections.sort(sortedSolutions);
        List<String> foundByPlayer = game.getFoundWords();

        // Use SpannableStringBuilder to format the word list with colors
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append("Possible words (").append(String.valueOf(sortedSolutions.size())).append("):\n\n");

        for (int i = 0; i < sortedSolutions.size(); i++) {
            String s = sortedSolutions.get(i);
            int start = ssb.length();
            ssb.append(s);
            
            // If the player found this word, highlight it in green
            if (foundByPlayer.contains(s)) {
                ssb.setSpan(new ForegroundColorSpan(Color.GREEN), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            
            // Add a newline between words
            if (i < sortedSolutions.size() - 1) {
                ssb.append('\n');
            }
        }

        // Show the summary dialog fragment
        SinglePlayerGameEndDialogFragment.newInstance(ssb, getString(R.string.score, game.getScore())).show(
                getSupportFragmentManager(),
                SinglePlayerGameEndDialogFragment.TAG);
    }
}
