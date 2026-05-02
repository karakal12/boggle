package com.amibar.boggle.data;


import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Trie (Prefix Tree) implementation representing the game dictionary.
 * Inherits from {@link Trie} to store words efficiently and allow for fast lookups.
 */
public final class Dictionary extends Trie<Dictionary>{
    private static final String TAG = "Dictionary";

    /**
     * Static root instance of the dictionary.
     */
    public static final Dictionary ROOT = new Dictionary();

    /** Flag indicating if the dictionary has been loaded with words. */
    private boolean isInitialized = false;


    /**
     * Private constructor for Dictionary.
     */
    private Dictionary() {
        super();
    }


    /**
     * Checks if a word exists in the dictionary.
     *
     * @param word The word to search for.
     * @return True if the word is present and valid, false otherwise.
     */
    public static boolean contains(@NonNull String word) {
        Trie<?> node = ROOT.get(word);
        return node != null && node.isEndOfWord();
    }

    /**
     * Initializes the dictionary by reading words from an InputStream (typically a text file).
     * This operation is synchronized to prevent concurrent initializations.
     * @param file The input stream containing the word list.
     */
    public synchronized void init(InputStream file) {
        if (isInitialized) return;
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim().toLowerCase();
                if (!word.isEmpty()) {
                    put(word);
                }
            }
            isInitialized = true;
        } catch (IOException e) {
            Log.e(TAG, "Error reading dictionary file", e);
        }
    }
}
