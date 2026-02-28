package com.amibar.boggle.data;


import androidx.annotation.NonNull;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Singleton class representing the game dictionary.
 * Uses a Trie (Prefix Tree) data structure to store words efficiently and allow for fast lookups.
 */
public final class Dictionary {
    /**
     * Singleton instance.
     */
    private static final Dictionary instance = new Dictionary();
    private boolean isInitialized = false;

    /**
     * The root node of the Trie.
     */
    private final Trie root = new Trie();

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
     * Returns the root node of the word Trie.
     * @return The root Trie node.
     */
    public Trie getRoot() {
        return root;
    }

    /**
     * Checks if a word exists in the dictionary.
     * @param word The word to search for.
     * @return True if the word is present and valid, false otherwise.
     */
    public boolean contains(@NonNull String word) {
        return root.get(word) != null;
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
        root.put(word, null);
    }
}
