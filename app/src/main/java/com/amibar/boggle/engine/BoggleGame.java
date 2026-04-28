package com.amibar.boggle.engine;

import static com.amibar.boggle.engine.BoggleGame.WordCheckResult.ALREADY_FOUND;
import static com.amibar.boggle.engine.BoggleGame.WordCheckResult.INVALID;
import static com.amibar.boggle.engine.BoggleGame.WordCheckResult.NULL_WORD;
import static com.amibar.boggle.engine.BoggleGame.WordCheckResult.TOO_SHORT;
import static com.amibar.boggle.engine.BoggleGame.WordCheckResult.VALID;

import android.util.Log;

import com.amibar.boggle.R;
import com.amibar.boggle.data.Dictionary;
import com.amibar.boggle.data.PathTrie;
import com.amibar.boggle.data.Trie;
import com.amibar.boggle.utils.Timer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a Boggle game instance, managing the game state, dice, scoring, and word validation.
 * It handles board generation, user word selection, scoring logic, and pre-calculates solutions.
 */
public class BoggleGame {
    /**
     * The total duration of a game in milliseconds.
     */
    public static final long GAME_TIME_MILLIS = 180000; // 180000 millis = 3 minutes

    /** The 1D array representation of the 4x4 board letters. */
    private final char[] board;
    /** Tracks the indices of dice currently selected by the player to form a word. */
    private final ArrayDeque<Integer> selectedIndices;
    /** Stores words successfully found and submitted by the player. */
    private final ArrayList<String> foundWords;
    /** The player's current cumulative score. */
    private int score = 0;
    /** Number of hints available to the player. */
    private int hints = 999;
    /** Flag indicating if the game has concluded. */
    private boolean gameEnded;

    // Listeners for game events, using CopyOnWriteArrayList for thread safety during iteration
    private final List<OnGameEndListener> onGameEndListeners = new CopyOnWriteArrayList<>();
    private final List<OnWordFoundListener> onWordFoundListeners = new CopyOnWriteArrayList<>();
    private final List<OnTickListener> onTickListeners = new CopyOnWriteArrayList<>();

    /** Trie containing all valid words that can be formed on the current board. */
    private final PathTrie solutions;
    /** List of all possible valid paths on the board. */
    private final List<String> allPaths;
    /** Timer managing the game countdown. */
    private final Timer gameTimer;

    /**
     * Listener interface for game end events.
     */
    public interface OnGameEndListener {
        /**
         * Called when the game timer expires or the game is manually ended.
         */
        void onGameEnd();
    }

    /**
     * Listener interface for word found events.
     */
    public interface OnWordFoundListener {
        /**
         * Called when a valid word is found.
         * @param word The word that was found.
         */
        void onWordFound(String word);
    }

    /**
     * Listener interface for timer tick events.
     */
    public interface OnTickListener {
        /**
         * Called on every timer tick.
         * @param elapsedTime Time elapsed since start in ms.
         */
        void onTick(long elapsedTime);
    }


    /**
     * Initializes a new Boggle game with a randomly generated board.
     * Generates the dice, shuffles their positions, and rolls each one to determine the face.
     * Also calculates all possible solutions for the generated board.
     */
    public BoggleGame() {
        this(generateBoard());
    }


    /**
     * Initializes a new Boggle game with a specific board configuration.
     *
     * @param board A char array of size 16 representing the 4x4 grid.
     */
    public BoggleGame(char[] board) {
        this.board = board;

        this.foundWords = new ArrayList<>();
        this.selectedIndices = new ArrayDeque<>();
        this.gameEnded = false;

        // Solve the board using the GameSolver and the dictionary root.
        // This is done upfront to provide immediate feedback on word validity during the game.
        GameSolver.SolverResult result = new GameSolver().solve(getDice(), Dictionary.ROOT);
        solutions = result.solutions();
        allPaths = result.allPaths();
        Log.d("BoggleGame", "Found " + solutions.size() + " solutions and " + allPaths.size() + " total paths");
        Log.v("BoggleGame", "Solutions: " + solutions);

        // Initialize the game timer with total duration and callbacks for ticks and completion.
        gameTimer = new Timer(GAME_TIME_MILLIS,
                (elapsedTime) -> {
                    for (OnTickListener listener : onTickListeners) {
                        listener.onTick(elapsedTime);
                    }
                },
                this::endGame);
    }

    /**
     * Generates a randomized 16-character board based on standard Boggle dice.
     *
     * @return A char array representing the board.
     */
    private static char[] generateBoard() {
        ArrayList<Die> diceList = Die.generateDice();
        Collections.shuffle(diceList);
        char[] board = new char[16];
        for (int i = 0; i < 16; i++) {
            Die die = diceList.get(i);
            die.roll();
            board[i] = die.getLetter();
        }
        return board;
    }

    /**
     * Adds a listener to be notified when the game ends.
     * @param listener The listener to add.
     */
    public void addOnGameEndListener(OnGameEndListener listener) {
        this.onGameEndListeners.add(listener);
    }

    /**
     * Adds a listener to be notified when a valid word is found.
     * @param listener The listener to add.
     */
    public void addOnWordFoundListener(OnWordFoundListener listener) {
        this.onWordFoundListeners.add(listener);
    }

    /**
     * Adds a listener to be notified on every timer tick.
     * @param listener The listener to add.
     */
    public void addOnTickListener(OnTickListener listener) {
        this.onTickListeners.add(listener);
    }


    /**
     * Gets the current player score.
     * @return The current score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Gets the number of hints available to the player.
     * @return The number of hints.
     */
    public int getHints() {
        return hints;
    }
    /**
     * Sets the number of hints available to the player.
     * @param hints The number of hints.
     */
    public void setHints(int hints) {
        this.hints = hints;
    }
    /**
     * Subtracts 1 from the number of hints available to the player.
     */
    public void subHint(){
        hints--;
    }

    /**
     * Returns the list of words correctly found by the player.
     * @return A list of found words.
     */
    @SuppressWarnings("unused")
    public ArrayList<String> getFoundWords() {
        return foundWords;
    }

    public int[] getSelectedIndices() {
        return selectedIndices.stream().mapToInt(i -> i).toArray();
    }


    /**
     * Converts the internal 1D board into a 4x4 character array for solvers or UI.
     * Letters are converted to lowercase.
     * @return A 2D char array representing the board.
     */
    public char[][] getDice() {
        char[][] diceGrid = new char[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                // Convert index to grid coordinates and get lowercase letter
                diceGrid[i][j] = Character.toLowerCase(board[i * 4 + j]);
            }
        }
        return diceGrid;
    }

    /**
     * Returns the string representation of the currently selected letters without clearing them.
     * Note: Special handling for 'Q' which is treated as 'QU' in Boggle.
     * @return The current word selection.
     */
    public String getWord() {
        StringBuilder sb = new StringBuilder();
        for (int index : selectedIndices) {
            char c = board[index];
            sb.append(c);
            if (c == 'Q') sb.append('U');
        }
        return sb.toString();
    }

    /**
     * Gets the raw 1D character array representing the board.
     * @return The board array.
     */
    public char[] getBoard() {
        return board;
    }

    /**
     * Returns all possible valid words that can be found on this board as a Trie.
     * @return A Trie of solution words.
     */
    public PathTrie getSolutions() {
        return solutions;
    }

    /**
     * Returns a list of all possible word paths on the board.
     * @return A list of path strings.
     */
    public List<String> getAllPaths() {
        return allPaths;
    }

    /**
     * Reconstructs the word formed by a given path of indices.
     * @param path A string of hexadecimal digits representing board indices.
     * @return The word string.
     */
    public String getWordFromPath(String path) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.length(); i++) {
            int index = Character.getNumericValue(path.charAt(i));
            char c = board[index];
            sb.append(c);
            if (c == 'Q') sb.append('U');
        }
        return sb.toString().toLowerCase();
    }

    /**
     * Checks if the game has ended.
     * @return True if ended, false otherwise.
     */
    public boolean isEnded() {
        return gameEnded;
    }

    /**
     * Starts the game timer.
     */
    public void startTimer() {
        gameTimer.start();
    }

    /**
     * Stops the game timer.
     */
    public void stopTimer() {
        gameTimer.stop();
    }

    /**
     * Signals the end of the game and notifies all registered listeners.
     */
    public void endGame() {
        if (gameEnded) return;
        gameEnded = true;
        gameTimer.stop();
        Log.d("BoggleGame", "Game ended. Final score: " + score + ", Words found: " + foundWords);
        for (OnGameEndListener listener : onGameEndListeners) {
            listener.onGameEnd();
        }
    }

    /**
     * Submits the current word selection for scoring.
     * Checks for validity, minimum length, and whether it was already found.
     * If valid, updates score and notifies listeners.
     *
     * @return The result of the word check (VALID, INVALID, TOO_SHORT, etc.).
     */
    public WordCheckResult submitWord() {
        String formedWord = formWord(); // Note: this clears the selection indices
        if (formedWord.isBlank()) {
            return NULL_WORD;
        }
        // Boggle words must be at least 3 letters long
        if (formedWord.length() < 3) {
            return TOO_SHORT;
        }
        // Cannot submit the same word twice
        if (foundWords.contains(formedWord)) {
            return ALREADY_FOUND;
        }
        // Check if word exists in the dictionary. Using solutions trie would also work and be faster.
        if (Dictionary.ROOT.contains(formedWord)) {
            score += wordScore(formedWord);
            foundWords.add(formedWord);
            for (OnWordFoundListener listener : onWordFoundListeners) {
                listener.onWordFound(formedWord);
            }
            Log.d("BoggleGame", "Found word: " + formedWord);
            return VALID;
        }
        return INVALID;
    }

    /**
     * Calculates the score of a word based on standard Boggle scoring rules.
     * 3-4 letters: 1 pt, 5 letters: 2 pts, 6 letters: 3 pts, 7 letters: 5 pts, 8+ letters: 11 pts.
     *
     * @param word The word to score.
     * @return The points awarded for the word.
     */
    public int wordScore(String word) {
        int wordLength = word.length();
        return switch (wordLength) {
            case 3, 4 -> 1;
            case 5 -> 2;
            case 6 -> 3;
            case 7 -> 5;
            default -> wordLength >= 8 ? 11 : 0;
        };
    }

    /**
     * Calculates the maximum possible score for this board.
     * @return The sum of scores for all possible solutions.
     */
    public int getMaxScore() {
        int maxScore = 0;
        for (String s : solutions.getWords()) {
            maxScore += wordScore(s);
        }
        return maxScore;
    }

    /**
     * Extracts the word from the current selection queue and clears the selection.
     * Handles 'Q' -> 'QU' conversion and converts the result to lowercase.
     * @return The lowercase string representation of the selected dice.
     */
    private String formWord() {
        StringBuilder sb = new StringBuilder();
        while (!selectedIndices.isEmpty()) {
            int index = selectedIndices.removeFirst();
            char c = board[index];
            sb.append(c);
            if (c == 'Q') sb.append('U');
        }
        return sb.toString().toLowerCase();
    }

    /**
     * Attempts to select a die at a specific index to be part of the current word.
     * Selection is valid if it's the first die or adjacent to the last selected die,
     * and hasn't been used yet in the current word.
     * @param index The index of the die in the 1D list (0-15).
     * @return True if the die was successfully added to the selection.
     */
    public boolean selectDie(int index) {
        // First letter in a word
        if (selectedIndices.isEmpty()) {
            selectedIndices.add(index);
            return true;
        }
        // Subsequent letters must be adjacent and not reused
        int lastIndex = selectedIndices.getLast();
        if (isAdjacent(lastIndex, index) && !selectedIndices.contains(index)) {
            selectedIndices.add(index);
            return true;
        }
        return false;
    }

    /**
     * Selects a sequence of dice indices as the current word selection.
     * @param path A string where each character is a hexadecimal digit (0-f) representing a die index.
     */
    public void selectPath(String path) {
        selectedIndices.clear();
        for (char c : path.toCharArray()) {
            selectedIndices.add(Character.getNumericValue(c));
        }
    }

    /**
     * Clears the current word selection.
     */
    public void deselectPath() {
        selectedIndices.clear();
    }

    /**
     * Checks if two dice are adjacent on the 4x4 grid.
     * Adjacency includes horizontal, vertical, and diagonal neighbors.
     * @param lastIndex The index of the previous die.
     * @param index The index of the current die.
     * @return True if the dice are neighbors.
     */
    private boolean isAdjacent(int lastIndex, int index) {
        int lastRow = lastIndex / 4;
        int lastCol = lastIndex % 4;
        int row = index / 4;
        int col = index % 4;
        // Check if both row and column differences are <= 1
        return Math.abs(lastRow - row) <= 1 && Math.abs(lastCol - col) <= 1;
    }

    /**
     * Returns the uppercase letter currently showing on the die at the given index.
     * @param index The index of the die.
     * @return The character on the die.
     */
    public char getDie(int index) {
        return board[index];
    }

    /**
     * Internal class representing a Boggle die with 6 sides.
     */
    private static class Die {
        /**
         * The letter configurations for the 16 standard Boggle dice.
         */
        static final char[][] DICE_CONFIGS = new char[][]{
                {'A', 'A', 'E', 'E', 'G', 'N'},
                {'E', 'L', 'R', 'T', 'T', 'Y'},
                {'A', 'O', 'O', 'T', 'T', 'W'},
                {'A', 'B', 'B', 'J', 'O', 'O'},
                {'E', 'H', 'R', 'T', 'V', 'W'},
                {'C', 'I', 'M', 'O', 'T', 'U'},
                {'D', 'I', 'S', 'T', 'T', 'Y'},
                {'E', 'I', 'O', 'S', 'S', 'T'},
                {'D', 'E', 'L', 'R', 'V', 'Y'},
                {'A', 'C', 'H', 'O', 'P', 'S'},
                {'H', 'I', 'M', 'N', 'Q', 'U'},
                {'E', 'E', 'I', 'N', 'S', 'U'},
                {'E', 'E', 'G', 'H', 'N', 'W'},
                {'A', 'F', 'F', 'K', 'P', 'S'},
                {'H', 'L', 'N', 'N', 'R', 'Z'},
                {'D', 'E', 'I', 'L', 'R', 'X'}
        };

        /** The six letters on this specific die. */
        private final char[] letters;
        /** The index of the letter currently facing up. */
        private int selectedLetter;

        @SuppressWarnings("unused")
        private Die() {
            throw new UnsupportedOperationException("Use Die.generateDice()");
        }

        /**
         * Creates a die with the specified faces.
         * @param letters Array of 6 characters.
         */
        private Die(char[] letters) {
            this.letters = letters;
        }

        /**
         * Randomly selects one of the 6 letters on the die.
         */
        public void roll() {
            selectedLetter = (int) (Math.random() * 6);
        }

        /**
         * Gets the letter currently showing on the top face.
         * @return The character.
         */
        public char getLetter() {
            return letters[selectedLetter];
        }

        /**
         * Factory method to create the set of 16 dice based on standard configurations.
         * @return A list of 16 Die objects.
         */
        static ArrayList<Die> generateDice() {
            ArrayList<Die> dice = new ArrayList<>(16);
            for (char[] config : DICE_CONFIGS) {
                dice.add(new Die(config));
            }
            return dice;
        }
    }

    /**
     * Represents the possible outcomes when a player submits a word.
     */
    public enum WordCheckResult {
        /** The word is valid and found for the first time. */
        VALID(R.string.word_valid),
        /** The word is not in the dictionary. */
        INVALID(R.string.word_invalid),
        /** The word was already found by the player in this game. */
        ALREADY_FOUND(R.string.word_already_found),
        /** The word is too short (less than 3 letters). */
        TOO_SHORT(R.string.word_too_short),
        /** The word is empty or null. */
        NULL_WORD(R.string.word_null);

        /** The string resource ID for the message to be displayed for this result. */
        private final int messageId;

        /**
         * @return The resource ID of the message to display to the user.
         */
        public int getMessageId() {
            return messageId;
        }

        /**
         * Constructor for WordCheckResult.
         * @param messageId The R.string ID.
         */
        WordCheckResult(int messageId) {
            this.messageId = messageId;
        }
    }
}
