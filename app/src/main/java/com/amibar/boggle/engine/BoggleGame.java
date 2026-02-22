package com.amibar.boggle.engine;

import static com.amibar.boggle.engine.BoggleGame.WordCheckResult.ALREADY_FOUND;
import static com.amibar.boggle.engine.BoggleGame.WordCheckResult.INVALID;
import static com.amibar.boggle.engine.BoggleGame.WordCheckResult.NULL_WORD;
import static com.amibar.boggle.engine.BoggleGame.WordCheckResult.TOO_SHORT;
import static com.amibar.boggle.engine.BoggleGame.WordCheckResult.VALID;

import android.util.Log;

import androidx.annotation.NonNull;

import com.amibar.boggle.R;
import com.amibar.boggle.data.Dictionary;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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

    private final char[] board;
    private final ArrayDeque<Integer> selectedIndices;
    private final ArrayList<String> foundWords;
    private int score;
    private boolean gameEnded;
    private final List<OnGameEndListener> onGameEndListeners = new CopyOnWriteArrayList<>();
    private final List<OnWordFoundListener> onWordFoundListeners = new CopyOnWriteArrayList<>();

    private final Set<String> solutions;

    /**
     * Listener interface for game end events.
     */
    public interface OnGameEndListener{
        /**
         * Called when the game timer expires or the game is manually ended.
         */
        void onGameEnd();
    }

    /**
     * Listener interface for word found events.
     */
    public interface OnWordFoundListener{
        /**
         * Called when a valid word is found.
         * @param word The word that was found.
         */
        void onWordFound(String word);
    }


    /**
     * Initializes a new Boggle game.
     * Generates the dice, shuffles their positions, and rolls each one to determine the face.
     * Also calculates all possible solutions for the generated board.
     */
    public BoggleGame(){
        this(generateBoard());
    }


    public BoggleGame(char[] board){
        this.board = board;

        foundWords = new ArrayList<>();
        selectedIndices = new ArrayDeque<>();
        gameEnded = false;

        // Solve the board using the GameSolver and the dictionary singleton
        solutions = new GameSolver().solve(getDice(), Dictionary.getInstance());
        Log.d("BoggleGame", "Found " + solutions.size() + " solutions");
        Log.d("BoggleGame", "Solution: " + solutions);
    }
    private static char[] generateBoard() {
        ArrayList<Die> diceList = Die.generateDice();
        Collections.shuffle(diceList);
        char[] board = new char[16];
        for (int i = 0; i < 16; i++){
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
     * Gets the current player score.
     * @return The current score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Returns the list of words correctly found by the player.
     * @return A list of found words.
     */
    @SuppressWarnings("unused")
    public ArrayList<String> getFoundWords() {
        return foundWords;
    }

    /**
     * Converts the internal list of dice into a 4x4 character array.
     * Letters are converted to lowercase.
     * @return A 2D char array representing the board.
     */
    public char[][] getDice(){
        char[][] diceGrid = new char[4][4];
        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 4; j++){
                // Convert index to grid coordinates and get lowercase letter
                diceGrid[i][j] = Character.toLowerCase(board[i * 4 + j]);
            }
        }
        return diceGrid;
    }

    /**
     * Returns the string representation of the currently selected letters.
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
     * Returns all possible valid words that can be found on this board.
     * @return A set of solution words.
     */
    public Set<String> getSolutions() {
        return solutions;
    }

    /**
     * Checks if the game has ended.
     * @return True if ended, false otherwise.
     */
    public boolean isEnded() {
        return gameEnded;
    }

    /**
     * Signals the end of the game and notifies all registered listeners.
     */
    public void endGame(){
        if (gameEnded) return;
        gameEnded = true;
        Log.d("BoggleGame", "Game ended. Final score: " + score + ", Words found: " + foundWords);
        for (OnGameEndListener listener : onGameEndListeners) {
            listener.onGameEnd();
        }
    }

    /**
     * Submits the current word selection for scoring.
     * Checks for validity, minimum length, and whether it was already found.
     * If valid, updates score and notifies listeners.
     * @return The result of the word check.
     */
    public WordCheckResult submitWord(){
        String formedWord = formWord();
        if (formedWord.isBlank()){
            return NULL_WORD;
        }
        // Boggle words must be at least 3 letters long
        if (formedWord.length() < 3){
            return TOO_SHORT;
        }
        // Cannot submit the same word twice
        if (foundWords.contains(formedWord)) {
            return ALREADY_FOUND;
        }
        // Check if word exists in the dictionary
        if (Dictionary.getInstance().contains(formedWord)){
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
        for (String s : solutions) {
            maxScore += wordScore(s);
        }
        return maxScore;
    }

    /**
     * Extracts the word from the current selection queue and clears the selection.
     * Handles 'Q' -> 'QU' conversion.
     * @return The lowercase string representation of the selected dice.
     */
    public String formWord(){
        StringBuilder sb = new StringBuilder();
        while (!selectedIndices.isEmpty()){
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
    public boolean selectDie(int index){
        // First letter in a word
        if (selectedIndices.isEmpty()){
            selectedIndices.add(index);
            return true;
        }
        // Subsequent letters must be adjacent and not reused
        int lastIndex = selectedIndices.getLast();
        if (isAdjacent(lastIndex, index) && !selectedIndices.contains(index)){
            selectedIndices.add(index);
            return true;
        }
        return false;
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
    public char getDie(int index){
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
                {'A','A','E','E','G','N'},
                {'E','L','R','T','T','Y'},
                {'A','O','O','T','T','W'},
                {'A','B','B','J','O','O'},
                {'E','H','R','T','V','W'},
                {'C','I','M','O','T','U'},
                {'D','I','S','T','T','Y'},
                {'E','I','O','S','S','T'},
                {'D','E','L','R','V','Y'},
                {'A','C','H','O','P','S'},
                {'H','I','M','N','Q','U'},
                {'E','E','I','N','S','U'},
                {'E','E','G','H','N','W'},
                {'A','F','F','K','P','S'},
                {'H','L','N','N','R','Z'},
                {'D','E','I','L','R','X'}
        };

        private final char[] letters;
        private int selectedLetter;

        @SuppressWarnings("unused")
        private Die(){
            throw new UnsupportedOperationException("Use Die.generateDice()");
        }

        private Die(char[] letters){
            this.letters = letters;
        }

        /**
         * Randomly selects one of the 6 letters on the die.
         */
        public void roll(){
            selectedLetter = (int)(Math.random() * 6);
        }

        /**
         * Gets the letter currently showing on the top face.
         * @return The character.
         */
        public char getLetter(){
            return letters[selectedLetter];
        }

        /**
         * Factory method to create the set of 16 dice based on standard configurations.
         * @return A list of 16 Die objects.
         */
        static ArrayList<Die> generateDice(){
            ArrayList<Die> dice = new ArrayList<>(16);
            for (char[] config : DICE_CONFIGS){
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

        private final int messageId;

        /**
         * @return The resource ID of the message to display to the user.
         */
        public int getMessageId() {
            return messageId;
        }

        WordCheckResult(int messageId) {
            this.messageId = messageId;
        }
    }
}
