package com.amibar.boggle.data;

public class GameResult {
    private int score;
    private int wordsFound;
    private int possibleWords;
    private int maxScore;

    public GameResult() {
        // Default constructor required for calls to DataSnapshot.getValue(GameResult.class)
    }

    public GameResult(int score, int wordsFound, int possibleWords, int maxScore) {
        this.score = score;
        this.wordsFound = wordsFound;
        this.possibleWords = possibleWords;
        this.maxScore = maxScore;
    }

    public int getScore() {
        return score;
    }

    public int getWordsFound() {
        return wordsFound;
    }

    public int getPossibleWords() {
        return possibleWords;
    }

    public int getMaxScore() {
        return maxScore;
    }
}
