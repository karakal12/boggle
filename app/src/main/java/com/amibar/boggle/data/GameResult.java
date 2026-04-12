package com.amibar.boggle.data;

import java.util.List;

public class GameResult {
    private int score;
    private int wordsFound;
    private int possibleWords;
    private int maxScore;
    private List<String> foundWords;

    public GameResult() {
        // Default constructor required for calls to DataSnapshot.getValue(GameResult.class)
    }

    public GameResult(int score, int wordsFound, int possibleWords, int maxScore, List<String> foundWords) {
        this.score = score;
        this.wordsFound = wordsFound;
        this.possibleWords = possibleWords;
        this.maxScore = maxScore;
        this.foundWords = foundWords;
    }

    public List<String> getFoundWords() {
        return foundWords;
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
