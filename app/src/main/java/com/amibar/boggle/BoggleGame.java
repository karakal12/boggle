package com.amibar.boggle;

import android.util.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BoggleGame {
    private final ArrayList<Die> dice;
    private final ArrayDeque<Die> word;
    private final ArrayList<String> foundWords;
    private int score;

    public BoggleGame(){
        dice = Die.generateDice();
        Collections.shuffle(dice);
        for (Die die : dice){
            die.roll();
        }
        foundWords = new ArrayList<>();
        word = new ArrayDeque<>();
    }

    public int getScore() {
        return score;
    }

    public ArrayList<String> getWords() {
        return foundWords;
    }

    public String getWord() {
        StringBuilder sb = new StringBuilder();
        word.clone().forEach((it) -> sb.append(it.getLetter()));
        return sb.toString();
    }

    public void endGame(){
        System.out.println(foundWords);
    }

    public boolean submitWord(){
        String formedWord = checkWord();
        if (!formedWord.isEmpty() && !foundWords.contains(formedWord)){
            foundWords.add(formedWord);
            score += wordScore(formedWord);
            return true;
        }
        return false;
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

    public String checkWord(){
        StringBuilder sb = new StringBuilder();
        while (!word.isEmpty()){
            sb.append(word.removeFirst().getLetter());
        }
        String formedWord = sb.toString().toLowerCase();
        return Dictionary.contains(formedWord) ? formedWord : "";
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
}
