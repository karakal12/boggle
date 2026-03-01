package com.amibar.boggle.data;

import android.util.ArraySet;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Represents a thread-safe Trie (Prefix Tree) data structure.
 * Each node in the Trie can also store a path, useful for Boggle word tracking.
 */
public class Trie {
    /** The number of letters in the English alphabet. */
    private static final int ALPHABET_SIZE = 26;

    /** Atomic array of pointers to child nodes, indexed by character ('a' to 'z'). */
    private final AtomicReferenceArray<Trie> children = new AtomicReferenceArray<>(ALPHABET_SIZE);

    /** Flag indicating if this node represents the end of a complete word. */
    private volatile boolean isEndOfWord;

    /** Flag indicating if this node has no children. */
    private volatile boolean isLeaf;

    /** Optional path string associated with this word (e.g., coordinates on a Boggle board). */
    private volatile String path;

    /** Atomic integer to track the number of words stored in the subtree rooted at this node. */
    private final AtomicInteger size = new AtomicInteger(0);

    /**
     * Initializes a new Trie node.
     */
    public Trie() {
        this.isEndOfWord = false;
        this.isLeaf = true;
    }

    /**
     * Checks if the node has a child corresponding to the given character.
     *
     * @param ch The character to check ('a' to 'z').
     * @return True if a child exists for the given character.
     */
    public boolean containsKey(char ch) {
        int index = ch - 'a';
        return index >= 0 && index < ALPHABET_SIZE && children.get(index) != null;
    }

    /**
     * Retrieves the child node for a given character.
     *
     * @param ch The character ('a' to 'z').
     * @return The child Trie node or null if it doesn't exist.
     */
    public Trie get(char ch) {
        int index = ch - 'a';
        if (index < 0 || index >= ALPHABET_SIZE) return null;
        return children.get(index);
    }

    /**
     * Traverses the trie to find the node corresponding to the given string.
     *
     * @param s The string to search for.
     * @return The Trie node representing the end of the string, or null if not found.
     */
    public Trie get(String s){
        Trie node = this;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (node.containsKey(ch)) {
                node = node.get(ch);
            } else {
                return null;
            }
        }
        return node;
    }

    /**
     * Adds or updates a child node for a given character.
     *
     * @param ch   The character ('a' to 'z').
     * @param node The node to associate with the character.
     */
    public void put(char ch, Trie node) {
        int index = ch - 'a';
        if (index >= 0 && index < ALPHABET_SIZE) {
            children.set(index, node);
            isLeaf = false;
        }
    }

    /**
     * Inserts a word and its associated path into the Trie.
     *
     * @param str  The word string to insert.
     * @param path The optional path string associated with the word.
     */
    public void put(String str, String path){
        if (get(str) != null) return;
        Trie node = this;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch < 'a' || ch > 'z') continue;
            if (node.get(ch) == null) {
                node.put(ch, new Trie());
            }
            node.size.addAndGet(1);
            node = node.get(ch);
        }
        if (path != null) {
            node.path = path;
        }
        node.setEndOfWord(true);
    }

    /**
     * Returns whether this node marks the end of a valid word.
     *
     * @return True if this node is the end of a word.
     */
    public boolean isEndOfWord() {
        return isEndOfWord;
    }

    /**
     * Sets whether this node represents the end of a word.
     *
     * @param endOfWord True if it's the end of a word.
     */
    public void setEndOfWord(boolean endOfWord) {
        isEndOfWord = endOfWord;
    }

    /**
     * Returns whether this node is a leaf (has no children).
     *
     * @return True if this node has no children.
     */
    public boolean isLeaf() {
        return isLeaf;
    }

    /**
     * Returns the path associated with this word node.
     *
     * @return The path string, or null if not set.
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path associated with this word node.
     * @param path The path string.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Returns the number of words in the Trie rooted at this node.
     *
     * @return Total word count.
     */
    public int size() {
        return size.get();
    }

    /**
     * Retrieves all words stored in the Trie.
     *
     * @return A set of all complete words.
     */
    public Set<String> getWords(){
        Set<String> words = new ArraySet<>(size());
        getWordsRec("", words);
        return words;
    }

    /**
     * Recursive helper to collect words from the trie structure.
     *
     * @param word The prefix string accumulated so far.
     * @param set  The set to add discovered words to.
     */
    private void getWordsRec(String word, Set<String> set) {
        if (isEndOfWord()) {
            set.add(word);
        }
        if (isLeaf) return;
        for (int i = 0; i < ALPHABET_SIZE; i++) {
            Trie child = children.get(i);
            if (child != null) {
                child.getWordsRec(word + (char)(i + 'a'), set);
            }
        }
    }


    public HashMap<String, String> toMap() {
        HashMap<String, String> map = new HashMap<>();
        for (String s : getWords()) {
            map.put(s, get(s).getPath());
        }
        return map;
    }

    @NonNull
    @Override
    public String toString() {
        return toMap().keySet().toString();
    }
}
