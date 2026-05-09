package com.amibar.boggle.views;

import static com.amibar.boggle.engine.BoggleGame.WordCheckResult.VALID;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;

import com.amibar.boggle.R;
import com.amibar.boggle.data.GameMode;
import com.amibar.boggle.databinding.ViewBoggleBinding;
import com.amibar.boggle.engine.BoggleGame;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * A custom view representing the Boggle game board and its associated UI elements.
 * This class handles user interactions with the game grid, updates the UI based on game state,
 * manages word selection, and handles the game timer/progress display.
 */
public class BoggleView extends LinearLayout {
    /** View binding for the boggle layout. */
    private ViewBoggleBinding binding;

    /** The underlying Boggle game engine that manages logic and state. */
    private BoggleGame game;
    /** Array of TextViews representing the 16 dice in the 4x4 grid. */
    private TextView[] cells;
    /** Reference to the last selected cell to manage visual feedback. */
    private TextView lastSelected;
    /** Badge drawable for the hint button showing number of available hints. */
    private BadgeDrawable hintBadge;

    /** The mode of the game, determined by XML attributes. */
    private final GameMode gameMode;

    /**
     * Basic constructor for programmatic instantiation.
     * @param context The Context the view is running in.
     */
    public BoggleView(@NonNull Context context) {
        this(context, null);
    }

    /**
     * Constructor used when inflating from XML.
     * @param context The Context the view is running in.
     * @param attrs The attributes of the XML tag that is inflating the view.
     */
    public BoggleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructor that allows specifying a default style.
     * @param context The Context the view is running in.
     * @param attrs The attributes of the XML tag.
     * @param defStyleAttr An attribute in the current theme that contains a reference to a style resource.
     */
    public BoggleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    /**
     * Full constructor for BoggleView.
     * Resolves custom XML attributes such as 'gameMode'.
     * @param context The Context.
     * @param attrs The AttributeSet.
     * @param defStyleAttr Default style attribute.
     * @param defStyleRes Default style resource.
     */
    @SuppressWarnings("unused")
    public BoggleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        // Resolve custom attributes from XML
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
     * Initializes the view by inflating the layout, initializing the game engine (if in singleplayer),
     * and setting up UI component bindings.
     */
    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    private void initView(){
        binding = ViewBoggleBinding.inflate(LayoutInflater.from(getContext()), this, true);
        
        // Auto-initialize game if in singleplayer mode
        if (gameMode == GameMode.singleplayer)
            game = new BoggleGame();

        // Initialize the array for the 4x4 grid cells and attach click listeners
        cells = new TextView[16];
        GridLayout gl = binding.glGameLayout;
        for (int i = 0; i < cells.length; i++) {
            cells[i] = (TextView) gl.getChildAt(i);
            cells[i].setOnClickListener(cellOnClickListener(i));
        }

        // Attach click listener to the submit button
        binding.bSubmit.setOnClickListener(this::onClickSubmit);

        // Attach click listener to the hint button
        binding.ivHint.setOnClickListener(v -> showHint());

        // Initialize the badge for hints
        hintBadge = BadgeDrawable.create(getContext());
        if (game != null) {
            hintBadge.setNumber(game.getHints());
            hintBadge.setVisible(game.getHints() > 0);
        } else {
            hintBadge.setVisible(false);
        }

        // Attach it to the icon (must be done after view is laid out)
        binding.ivHint.post(() -> BadgeUtils.attachBadgeDrawable(hintBadge, binding.ivHint, null));

        // For singleplayer, immediately setup UI and start the countdown
        if (gameMode == GameMode.singleplayer) {
            setupUI();
            game.startTimer();
        }
    }

    /**
     * Starts a fresh game instance and refreshes the UI.
     * @return The newly created {@link BoggleGame} instance.
     */
    public BoggleGame newGame(){
        game = new BoggleGame();
        setupUI();
        return game;
    }

    /**
     * Sets the game board with a specific configuration and refreshes the UI.
     * Useful for multiplayer or loading saved states.
     * @param board A 16-character array representing the dice letters.
     * @return The updated {@link BoggleGame} instance.
     */
    public BoggleGame setGame(char[] board){
        game = new BoggleGame(board);
        setupUI();
        return game;
    }

    /**
     * Manually triggers the game timer to start.
     */
    public void startGame(){
        if (game != null) {
            game.startTimer();
        }
    }

    /**
     * Configures the grid UI, attaches listeners to cells, and sets up
     * the game-state observation (timer and scoring).
     */
    private void setupUI() {
        for (int i = 0; i < cells.length; i++) {
            char letter = game.getDie(i);
            // Handle special 'Qu' case for Boggle
            cells[i].setText(letter == 'Q' ? "Qu" : String.valueOf(letter));
        }

        // Initial UI state sync
        updateScore();
        updateWord();

        // Initialize and start the game timer
        LinearProgressIndicator timerIndicator = binding.progressBar;

        game.addOnTickListener(elapsedTime -> {
            // Update indicator
            float progress = (float) elapsedTime / BoggleGame.GAME_TIME_MILLIS;
            timerIndicator.setProgress((int) (progress * timerIndicator.getMax()));

            // Update text
            binding.setTime(formatTime(elapsedTime));
        });

        // Ensure timer displays zero exactly when game ends
        game.addOnGameEndListener(() -> binding.setTime("00:00"));

        if (hintBadge != null) {
            hintBadge.setNumber(game.getHints());
            hintBadge.setVisible(game.getHints() > 0);
        }
    }

    /**
     * Ensures the game timer is stopped when the view is removed from the window
     * to prevent leaks.
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (game != null && gameMode == GameMode.singleplayer) {
            game.stopTimer();
        }
    }

    /**
     * Formats the remaining time into an MM:SS string.
     * @param elapsedTime Time elapsed since start in milliseconds.
     * @return Formatted time string (e.g., "01:30").
     */
    private String formatTime(long elapsedTime) {
        long remainingTime = Math.max(0, BoggleGame.GAME_TIME_MILLIS - elapsedTime);
        long minutes = remainingTime / 60000;
        long seconds = (remainingTime % 60000) / 1000;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    /**
     * @return The current {@link BoggleGame} logic instance.
     */
    public BoggleGame getGame() {
        return game;
    }

    /**
     * Logic for the word submission button.
     * Evaluates the current word, updates score/feedback, and resets board highlights.
     * 
     * @param v The button view.
     */
    private void onClickSubmit(View v) {
        if (game == null || game.isEnded()) return;

        // Capture the word and result BEFORE clearing the selection/UI
        String lastWord = game.getWord();
        BoggleGame.WordCheckResult result = game.submitWord();

        // Now safe to clear visual highlights and selection state
        clearSolution();

        // Update score display if the word was valid
        if (result == VALID)
            updateScore();

        // Display feedback message based on the result of the word submission
        binding.setError(getContext().getString(result.getMessageId(), lastWord));
        lastSelected = null;
    }

    /**
     * Synchronizes the UI score display with the current game engine score.
     */
    private void updateScore() {
        binding.setScore(game.getScore());
    }

    /**
     * Factory for cell click listeners. Handles the selection logic and visual feedback.
     * 
     * @param cellId The index of the die in the 0-15 grid.
     * @return An OnClickListener for the die cell.
     */
    private OnClickListener cellOnClickListener(int cellId) {
        return (view) -> {
            if (game == null || game.isEnded())
                return;
            
            // Attempt to select the die. This validates adjacency and re-selection rules.
            if (game.selectDie(cellId)) {
                // If there was a previous selection, change its color to the generic 'selected' state
                if (lastSelected != null) {
                    lastSelected.setBackgroundColor(resolveAttribute(R.attr.colorSelected));
                }
                
                // Update the current word display
                updateWord();
                
                // Highlight the most recently selected cell with a distinct color
                view.setBackgroundColor(resolveAttribute(R.attr.colorLastSelected));
                lastSelected = (TextView) view;
            }
        };
    }

    /**
     * Utility to resolve theme-dependent attributes (like colors) at runtime.
     * @param attrRes The theme attribute resource ID.
     * @return The resolved integer value (usually a color).
     */
    private int resolveAttribute(int attrRes){
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(attrRes, typedValue, true);
        return typedValue.data;
    }

    /**
     * Synchronizes the UI word preview with the word currently being built in the game engine.
     */
    private void updateWord(){
        binding.setWord(game.getWord());
    }

    /**
     * Highlights a specific word path on the board.
     * This is used for showing hints or historical word paths.
     * @param path A string of digits (0-9, a-f) representing cell indices.
     */
    public void showSolution(String path) {
        clearSolution();
        game.selectPath(path);

        char[] indices = path.toCharArray();
        int selectedColor = resolveAttribute(R.attr.colorSelected);

        for (int i = 0; i < indices.length; i++) {
            int index = Character.getNumericValue(indices[i]);

            // Highlight cells along the path, with special color for the last one
            if (i == indices.length - 1) {
                lastSelected = cells[index];
                cells[index].setBackgroundColor(resolveAttribute(R.attr.colorLastSelected));
            } else {
                cells[index].setBackgroundColor(selectedColor);
            }
        }
        updateWord();
    }

    /**
     * Resets all cell backgrounds to the default state and clears the engine's current path.
     */
    public void clearSolution() {
        game.deselectPath();
        for (TextView cell : cells) {
            cell.setBackgroundColor(resolveAttribute(R.attr.colorUnselected));
        }
        updateWord();
    }

    /**
     * Picks a random valid word that hasn't been found yet and highlights its path.
     * Subtracts a hint from the player's total.
     */
    public void showHint(){
        if (game == null || game.isEnded()) return;
        if (game.getHints() <= 0) return;

        // Get current path as hex string
        StringBuilder currentPathSB = new StringBuilder();
        for (int index : game.getSelectedIndices()) {
            currentPathSB.append(Integer.toHexString(index));
        }
        String currentPath = currentPathSB.toString();

        List<String> candidatePaths = new ArrayList<>();
        // Search for completions of the current path that form words not yet found
        for (String path : game.getAllPaths()) {
            if (path.startsWith(currentPath) && path.length() > currentPath.length()) {
                String word = game.getWordFromPath(path);
                if (!game.getFoundWords().contains(word)) {
                    candidatePaths.add(path);
                }
            }
        }

        // If no completions for current path, try to find ANY word not yet found
        if (candidatePaths.isEmpty()) {
            Log.d("BoggleView", "No completions for current path, trying any word");
            for (String path : game.getAllPaths()) {
                String word = game.getWordFromPath(path);
                if (!game.getFoundWords().contains(word)) {
                    candidatePaths.add(path);
                }
            }
        }

        if (candidatePaths.isEmpty()) return;

        Log.d("BoggleView", "Found " + candidatePaths.size() + " candidate paths");
        Log.v("BoggleView", "Candidate paths: " + candidatePaths);

        // Shuffle so hints are random among valid completions
        Collections.shuffle(candidatePaths);
        String fullPath = candidatePaths.get(0);

        int currentPathLength = currentPath.length();
        int remainingLength = fullPath.length() - currentPathLength;
        int revealCount = currentPathLength + (int) Math.ceil(remainingLength / 2.0);


        if (revealCount >= fullPath.length())
            return;

        Log.d("BoggleView", "found solution " + game.getWordFromPath(fullPath) + " with path " + fullPath + " revealing " + revealCount + " characters");
        showSolution(fullPath.substring(0, revealCount));

        game.subHint();
        hintBadge.setNumber(game.getHints());
        if (game.getHints() == 0) hintBadge.setVisible(false);
    }
}
