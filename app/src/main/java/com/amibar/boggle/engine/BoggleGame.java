package com.amibar.boggle.engine;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;

import static com.amibar.boggle.engine.BoggleGame.WordCheckResult.*;

import com.amibar.boggle.R;
import com.amibar.boggle.data.Dictionary;

public class BoggleGame {
    public static final long GAME_TIME_MILLIS = 120000; // 2 minutes
    private final ArrayList<Die> dice;
    private final ArrayDeque<Die> word;
    private final ArrayList<String> foundWords;
    private int score;
    private boolean gameEnded;

    public BoggleGame(){
        dice = Die.generateDice();
        Collections.shuffle(dice);
        for (Die die : dice){
            die.roll();
        }
        foundWords = new ArrayList<>();
        word = new ArrayDeque<>();
        gameEnded = false;
    }

    public int getScore() {
        return score;
    }

    @SuppressWarnings("unused")
    public ArrayList<String> getFoundWords() {
        return foundWords;
    }

    public char[][] getDice(){
        char[][] dice = new char[4][4];
        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 4; j++){
                dice[i][j] = (char) (this.dice.get(i * 4 + j).getLetter() - 'A' + 'a');
            }
        }
        return dice;
    }

    public String getWord() {
        StringBuilder sb = new StringBuilder();
        word.clone().forEach((it) -> sb.append(it.getLetter()));
        return sb.toString();
    }

    public boolean isEnded() {
        return gameEnded;
    }

    public void endGame(){
        gameEnded = true;
        Log.d("BoggleGame", "Game ended. Final score: " + score + ", Words found: " + foundWords);
    }

    public WordCheckResult submitWord(){
        String formedWord = formWord();
        if (formedWord.isBlank()){
            return NULL_WORD;
        }
        if (formedWord.length() < 3){
            return TOO_SHORT;
        }
        if (foundWords.contains(formedWord)) {
            return ALREADY_FOUND;
        }
        if (Dictionary.contains(formedWord)){
            score += wordScore(formedWord);
            foundWords.add(formedWord);
            return VALID;
        }
        return INVALID;
    }

    private int wordScore(String word) {
        int wordLength = word.length();
        switch (wordLength) {
            case 3:
            case 4: return 1;
            case 5: return 2;
            case 6: return 3;
            case 7: return 5;
            default: return wordLength >= 8 ? 11 : 0;
        }
    }

    public String formWord(){
        StringBuilder sb = new StringBuilder();
        while (!word.isEmpty()){
            sb.append(word.removeFirst().getLetter());
        }
        return sb.toString().toLowerCase();
    }

    public boolean selectDie(int index){
        Die die = dice.get(index);
        if (word.isEmpty()){
            word.add(die);
            return true;
        }
        Die lastDie = word.getLast();
        int lastIndex = dice.indexOf(lastDie);
        if (isAdjacent(lastIndex, index) && !word.contains(die)){
            word.add(die);
            return true;
        }
        return false;
    }

    private boolean isAdjacent(int lastIndex, int index) {
        int lastRow = lastIndex / 4;
        int lastCol = lastIndex % 4;
        int row = index / 4;
        int col = index % 4;
        return Math.abs(lastRow - row) <= 1 && Math.abs(lastCol - col) <= 1;
    }

    public char getDie(int index){
        return dice.get(index).getLetter();
    }

    private static class Die {
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
        public void roll(){
            selectedLetter = (int)(Math.random() * 6);
        }

        public char getLetter(){
            return letters[selectedLetter];
        }

        static ArrayList<Die> generateDice(){
            ArrayList<Die> dice = new ArrayList<>(16);
            for (char[] config : DICE_CONFIGS){
                dice.add(new Die(config));
            }
            return dice;
        }
    }

    public enum WordCheckResult {
        VALID(R.string.word_valid),
        INVALID(R.string.word_invalid),
        ALREADY_FOUND(R.string.word_already_found),
        TOO_SHORT(R.string.word_too_short),
        NULL_WORD(R.string.word_null);

        private final int messageId;

        public int getMessageId() {
            return messageId;
        }

        WordCheckResult(int messageId) {
            this.messageId = messageId;
        }
    }
}
