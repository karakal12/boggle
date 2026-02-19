package com.amibar.boggle.data;


import androidx.annotation.NonNull;

import java.io.InputStream;
import java.util.Arrays;
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
    private final DictNode root = new DictNode();

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
     * @return The root DictNode.
     */
    public DictNode getRoot() {
        return root;
    }

    /**
     * Checks if a word exists in the dictionary.
     * @param word The word to search for.
     * @return True if the word is present and valid, false otherwise.
     */
    public boolean contains(@NonNull String word) {
        DictNode node = root;
        // Traverse the Trie according to the characters in the word
        for (char ch : word.toLowerCase().toCharArray()) {
            if (ch < 'a' || ch > 'z') return false; // Non-alphabetic characters are invalid
            if (!node.containsKey(ch)) {
                return false; // Path doesn't exist in the Trie
            }
            node = node.get(ch);
        }
        // Check if the traversal ended at a valid word terminator
        return node.isEndOfWord;
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
        DictNode node = root;
        for (char ch : word.toCharArray()) {
            if (ch < 'a' || ch > 'z') continue;
            if (!node.containsKey(ch)) {
                node.put(ch, new DictNode());
            }
            node = node.get(ch);
        }
        node.isEndOfWord = true;
    }

    /**
     * Represents a single node in the dictionary Trie.
     */
    public static class DictNode {
        static final int ALPHABET_SIZE = 26;
        /** Array of pointers to child nodes, indexed by character ('a' to 'z'). */
        private final DictNode[] children = new DictNode[ALPHABET_SIZE];
        /** Flag indicating if this node represents the end of a complete word. */
        private boolean isEndOfWord;
        /** Flag indicating if this node has no children. */
        private boolean isLeaf;

        /**
         * Initializes a new Trie node.
         */
        public DictNode() {
            this.isEndOfWord = false;
            this.isLeaf = true;
            Arrays.fill(children, null);
        }

        /**
         * Checks if the node has a child corresponding to the given character.
         * @param ch The character to check.
         * @return True if a child exists.
         */
        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public boolean containsKey(char ch) {
            int index = ch - 'a';
            return index >= 0 && index < ALPHABET_SIZE && children[index] != null;
        }

        /**
         * Retrieves the child node for a given character.
         * @param ch The character.
         * @return The child DictNode or null if it doesn't exist.
         */
        public DictNode get(char ch) {
            int index = ch - 'a';
            if (index < 0 || index >= ALPHABET_SIZE) return null;
            return children[index];
        }

        /**
         * Adds or updates a child node for a given character.
         * @param ch The character.
         * @param node The node to associate with the character.
         */
        public void put(char ch, DictNode node) {
            int index = ch - 'a';
            if (index >= 0 && index < ALPHABET_SIZE) {
                children[index] = node;
                isLeaf = false;
            }
        }

        /**
         * @return True if this node completes a valid word.
         */
        public boolean isEndOfWord() {
            return isEndOfWord;
        }

        /**
         * @return True if this node has no children.
         */
        public boolean isLeaf() {
            return isLeaf;
        }
    }
}
