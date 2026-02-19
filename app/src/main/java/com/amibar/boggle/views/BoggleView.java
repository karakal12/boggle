package com.amibar.boggle.views;

import static com.amibar.boggle.engine.BoggleGame.WordCheckResult.VALID;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amibar.boggle.R;
import com.amibar.boggle.engine.BoggleGame;
import com.amibar.boggle.utils.Timer;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Locale;

/**
 * A custom view representing the Boggle game board and its associated UI elements.
 * This class handles user interactions with the game grid, updates the UI based on game state,
 * and manages the game timer.
 */
public class BoggleView extends LinearLayout {

    /** The underlying Boggle game engine. */
    private BoggleGame game;
    /** Array of TextViews representing the 16 dice in the 4x4 grid. */
    private TextView[] cells;
    /** Reference to the last selected cell to manage visual feedback. */
    private TextView lastSelected;

    /** TextView displaying the word currently being formed. */
    private TextView word;
    /** TextView displaying feedback messages (e.g., word validity). */
    private TextView msg;
    /** TextView displaying the current score. */
    private TextView score;


    public BoggleView(@NonNull Context context) {
        super(context);
        initView();
    }

    public BoggleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public BoggleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @SuppressWarnings("unused")
    public BoggleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    /**
     * Initializes the view by inflating the layout, setting up the game engine,
     * binding UI components, and starting the game timer.
     */
    @SuppressLint("SetTextI18n")
    private void initView(){
        inflate(getContext(), R.layout.view_boggle, this);

        game = new BoggleGame();

        // Set up the 4x4 grid of dice cells
        cells = new TextView[16];
        GridLayout gl = findViewById(R.id.glGameLayout);
        for (int i = 0; i < gl.getChildCount(); i++) {
            cells[i] = (TextView) gl.getChildAt(i);
            cells[i].setOnClickListener(cellOnClickListener(i));
            char letter = game.getDie(i);
            // Handle special 'Qu' case for Boggle
            cells[i].setText(letter == 'Q' ? "Qu" : String.valueOf(letter));
        }

        // Set up the submit button
        Button submit = findViewById(R.id.bSubmit);
        submit.setOnClickListener(this::onClickSubmit);

        // Bind score and current word displays
        score = findViewById(R.id.tvScore);
        updateScore();

        word = findViewById(R.id.tvWord);
        updateWord();

        msg = findViewById(R.id.tvErrors);

        // Initialize and start the game timer
        TextView timerText = findViewById(R.id.tvTime);
        LinearProgressIndicator timerIndicator = findViewById(R.id.progressBar);

        new Timer(BoggleGame.GAME_TIME_MILLIS,
                (elapsedTime) -> {
                    // Update indicator
                    float progress = (float) elapsedTime / BoggleGame.GAME_TIME_MILLIS;
                    timerIndicator.setProgress((int) (progress * timerIndicator.getMax()));
                    
                    // Update text
                    timerText.setText(formatTime(elapsedTime));
                },
                () -> {
                    timerText.setText("00:00");
                    game.endGame();
                }).start();
    }

    /**
     * Formats the remaining time into a MM:SS string.
     * @param elapsedTime Time elapsed since start in ms.
     * @return Formatted string.
     */
    private String formatTime(long elapsedTime) {
        long remainingTime = Math.max(0, BoggleGame.GAME_TIME_MILLIS - elapsedTime);
        long minutes = remainingTime / 60000;
        long seconds = (remainingTime % 60000) / 1000;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    /**
     * @return The BoggleGame instance associated with this view.
     */
    public BoggleGame getGame() {
        return game;
    }

    /**
     * Handles the click event for the submit button.
     * Checks the validity of the current word, updates score/messages, and resets the board state.
     * 
     * @param v The clicked view.
     */
    private void onClickSubmit(View v) {
        if (game.isEnded()) return;

        // Reset visual state of all cells
        int unselectedColor = resolveAttribute(R.attr.colorUnselected);
        for (TextView cell : cells) {
            cell.setBackgroundColor(unselectedColor);
        }

        String lastWord = game.getWord();
        BoggleGame.WordCheckResult result = game.submitWord();

        if (result == VALID)
            updateScore();

        // Display feedback message based on the result of the word submission
        msg.setText(getContext().getString(result.getMessageId(), lastWord));
        updateWord();
        lastSelected = null;
    }

    /**
     * Updates the score display with the current score from the game engine.
     */
    private void updateScore() {
        this.score.setText(getContext().getString(R.string.score, game.getScore()));
    }

    /**
     * Creates an OnClickListener for a specific cell in the grid.
     * 
     * @param cellId The index of the cell in the dice array.
     * @return An OnClickListener that handles cell selection.
     */
    private OnClickListener cellOnClickListener(int cellId) {
        return (view) -> {
            if (game.isEnded())
                return;
            
            // Attempt to select the die in the game logic
            if (game.selectDie(cellId)){
                // Provide visual feedback for selection sequence
                if (lastSelected != null) {
                    lastSelected.setBackgroundColor(resolveAttribute(R.attr.colorSelected));
                }
                updateWord();
                view.setBackgroundColor(resolveAttribute(R.attr.colorLastSelected));
                lastSelected = (TextView) view;
            }
        };
    }

    /**
     * Resolves a theme attribute (like a color) to its actual value.
     * 
     * @param attrRes The attribute resource ID to resolve.
     * @return The resolved data value (e.g., color integer).
     */
    private int resolveAttribute(int attrRes){
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(attrRes, typedValue, true);
        return typedValue.data;
    }

    /**
     * Updates the text display of the word currently being formed.
     */
    private void updateWord(){
        this.word.setText(getContext().getString(R.string.word, game.getWord()));
    }
}
