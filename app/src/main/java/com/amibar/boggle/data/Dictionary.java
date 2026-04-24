package com.amibar.boggle.data;


import androidx.annotation.NonNull;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Singleton Trie (Prefix Tree) implementation representing the game dictionary.
 * Inherits from {@link Trie} to store words efficiently and allow for fast lookups.
 */
public final class Dictionary extends Trie<Dictionary>{
    /**
     * Singleton instance.
     */
    private static final Dictionary instance = new Dictionary();
    private boolean isInitialized = false;


    /**
     * Private constructor for singleton pattern.
     */
    private Dictionary() {}

    /**
     * Returns the singleton instance of the Dictionary.
     * @return The Dictionary instance.
     */
    public static Dictionary getInstance() {
        return instance;
    }


    /**
     * Checks if a word exists in the dictionary.
     *
     * @param word The word to search for.
     * @return True if the word is present and valid, false otherwise.
     */
    public boolean contains(@NonNull String word) {
        Trie<?> node = get(word);
        return node != null && node.isEndOfWord();
    }

    /**
     * Initializes the dictionary by reading words from an InputStream (typically a text file).
     * This operation is synchronized to prevent concurrent initializations.
     * @param file The input stream containing the word list.
     */
    public synchronized void init(InputStream file) {
        if (isInitialized) return;
        
        Scanner sc = new Scanner(file);
        while (sc.hasNextLine()) {
            String word = sc.nextLine().trim().toLowerCase();
            if (!word.isEmpty()) {
                insert(word);
            }
        }
        sc.close();
        isInitialized = true;
    }

    /**
     * Inserts a word into the Trie structure.
     * @param word The word to insert.
     */
    private void insert(@NonNull String word) {
        put(word);
    }
}
