package com.amibar.boggle.views;

import static com.amibar.boggle.engine.BoggleGame.WordCheckResult.VALID;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amibar.boggle.R;
import com.amibar.boggle.data.GameMode;
import com.amibar.boggle.databinding.ViewBoggleBinding;
import com.amibar.boggle.engine.BoggleGame;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Locale;

/**
 * A custom view representing the Boggle game board and its associated UI elements.
 * This class handles user interactions with the game grid, updates the UI based on game state,
 * and manages the game timer.
 */
public class BoggleView extends LinearLayout {
    ViewBoggleBinding binding;


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
    private final GameMode gameMode;


    public BoggleView(@NonNull Context context) {
        this(context, null);
    }

    public BoggleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BoggleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @SuppressWarnings("unused")
    public BoggleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        // TODO : custom attribute for mode (singleplayer or multiplayer)
        //  and whether or not the create a new BoggleGame or wait for one from the bd
        @SuppressLint("Recycle") TypedArray array = context.obtainStyledAttributes(
                attrs,
                R.styleable.BoggleView,
                defStyleAttr, defStyleRes);
        try (array) {
            gameMode = GameMode.values()[array.getInt(R.styleable.BoggleView_gameMode, 0)];
        }
        initView();
    }

    /**
     * Initializes the view by inflating the layout, setting up the game engine,
     * binding UI components, and starting the game timer.
     */
    private void initView(){
        binding = ViewBoggleBinding.inflate(LayoutInflater.from(getContext()), this, true);
        if (gameMode == GameMode.singleplayer)
            game = new BoggleGame();

        // Set up the 4x4 grid of dice cells
        cells = new TextView[16];

        // Set up the submit button
        binding.bSubmit.setOnClickListener(this::onClickSubmit);

        // Bind score and current word displays
        score = binding.tvScore;

        word = binding.tvWord;

        msg = binding.tvErrors;

        if (gameMode == GameMode.singleplayer) {
            setupUI();
            game.startTimer();
        }

    }
    public BoggleGame newGame(){
        game = new BoggleGame();
        setupUI();
        return game;
    }

    public BoggleGame setGame(char[] board){
        game = new BoggleGame(board);
        setupUI();
        return game;
    }

    public void startGame(){
        game.startTimer();
    }


    private void setupUI() {
        GridLayout gl = binding.glGameLayout;
        for (int i = 0; i < gl.getChildCount(); i++) {
            cells[i] = (TextView) gl.getChildAt(i);
            cells[i].setOnClickListener(cellOnClickListener(i));
            char letter = game.getDie(i);
            // Handle special 'Qu' case for Boggle
            cells[i].setText(letter == 'Q' ? "Qu" : String.valueOf(letter));
        }

        updateScore();
        updateWord();

        // Initialize and start the game timer
        TextView timerText = binding.tvTime;
        LinearProgressIndicator timerIndicator = binding.progressBar;

        game.addOnTickListener(elapsedTime -> {
            // Update indicator
            float progress = (float) elapsedTime / BoggleGame.GAME_TIME_MILLIS;
            timerIndicator.setProgress((int) (progress * timerIndicator.getMax()));

            // Update text
            timerText.setText(formatTime(elapsedTime));
        });

        game.addOnGameEndListener(() -> timerText.setText("00:00"));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (game != null && gameMode == GameMode.singleplayer) {
            game.stopTimer();
        }
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

        clearSolution();

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
            if (game.selectDie(cellId)) {
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

    public void showSolution(String path) {
        clearSolution();
        char[] indices = path.toCharArray();
        int selectedColor = resolveAttribute(R.attr.colorSelected);
        for (int i = 0; i < indices.length; i++) {
            int index = Character.getNumericValue(indices[i]);
            if (i == indices.length - 1) {
                lastSelected = cells[index];
                cells[index].setBackgroundColor(resolveAttribute(R.attr.colorLastSelected));
            } else {
                cells[index].setBackgroundColor(selectedColor);
            }
        }
    }

    public void clearSolution() {
        int unselectedColor = resolveAttribute(R.attr.colorUnselected);
        for (TextView cell : cells) {
            cell.setBackgroundColor(unselectedColor);
        }
        lastSelected = null;
    }
}
