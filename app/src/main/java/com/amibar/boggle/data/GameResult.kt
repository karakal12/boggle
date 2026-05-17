package com.amibar.boggle.data;

import java.util.List;

/**
 * Data class representing the result of a single-player Boggle game.
 * Used for storing and retrieving game history from Firebase.
 */
public class GameResult {
    /** The player's final score. */
    private int score;
    /** The number of valid words found by the player. */
    private int wordsFound;
    /** The total number of valid words possible on the board. */
    private int possibleWords;
    /** The maximum possible score achievable on the board. */
    private int maxScore;
    /** List of words found by the player. */
    private List<String> foundWords;
    /** String representation of the game board. */
    private String board;

    /**
     * Default constructor required for Firebase Realtime Database deserialization.
     */
    public GameResult() {
        // Default constructor required for calls to DataSnapshot.getValue(GameResult.class)
    }

    /**
     * Constructs a new GameResult.
     * @param score Final score.
     * @param wordsFound Number of words found.
     * @param possibleWords Total possible words.
     * @param maxScore Maximum possible score.
     * @param foundWords List of found words.
     * @param board The board layout.
     */
    public GameResult(int score, int wordsFound, int possibleWords, int maxScore, List<String> foundWords, String board) {
        this.score = score;
        this.wordsFound = wordsFound;
        this.possibleWords = possibleWords;
        this.maxScore = maxScore;
        this.foundWords = foundWords;
        this.board = board;
    }

    /** @return The list of words found by the player. */
    public List<String> getFoundWords() {
        return foundWords;
    }

    /** @return The player's final score. */
    public int getScore() {
        return score;
    }

    /** @return The number of words found. */
    public int getWordsFound() {
        return wordsFound;
    }

    /** @return The total number of possible words on the board. */
    public int getPossibleWords() {
        return possibleWords;
    }

    /** @return The maximum possible score for the board. */
    public int getMaxScore() {
        return maxScore;
    }

    /** @return The string representation of the board. */
    public String getBoard() {
        return board;
    }
}
