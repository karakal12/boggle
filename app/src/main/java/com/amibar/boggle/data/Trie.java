package com.amibar.boggle.data;

import android.util.ArraySet;

import androidx.annotation.NonNull;

import java.lang.reflect.ParameterizedType;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Represents a thread-safe Trie (Prefix Tree) data structure.
 * This is a generic base class that can be extended to store additional metadata per node.
 * It uses atomic components to support concurrent read and write operations safely.
 *
 * @param <T> The concrete type of the Trie node, allowing for extension.
 */
public abstract class Trie<T extends Trie<T>> {
    /** The number of letters in the English alphabet ('a' through 'z'). */
    protected static final int ALPHABET_SIZE = 26;

    /** Atomic array of pointers to child nodes, indexed by character ('a' to 'z'). */
    protected final AtomicReferenceArray<T> children = new AtomicReferenceArray<>(ALPHABET_SIZE);

    /** Flag indicating if this node represents the end of a complete word. */
    protected volatile boolean isEndOfWord;

    /** Flag indicating if this node has no children. */
    protected volatile boolean isLeaf;

    /**
     * Initializes a new Trie node as a leaf and not an end-of-word.
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
    public T get(char ch) {
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
    @SuppressWarnings("unchecked")
    public T get(String s){
        T node = (T) this;
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
     * Adds a child node for a given character by instantiating the specialized type via reflection.
     * This allows the generic Trie to create nodes of the correct subclass (e.g., PathTrie).
     *
     * @param ch The character ('a' to 'z').
     * @return the node that was added or null if the character is out of range.
     */
    @SuppressWarnings("unchecked")
    public T putIfAbsent(char ch){
        int index = ch - 'a';
        if (index < 0 || index >= ALPHABET_SIZE) return null;
        if (containsKey(ch)) return get(ch);

        T newNode;
        try {
            ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
            assert genericSuperclass != null : "Failed to instantiate Trie node";
            Class<T> type = (Class<T>) genericSuperclass.getActualTypeArguments()[0];
            newNode = type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate Trie node", e);
        }

        if (children.compareAndSet(index, null, newNode)) {
            isLeaf = false;
            return newNode;
        } else {
            return children.get(index);
        }
    }

    /**
     * Inserts a word into the Trie, creating nodes as necessary.
     * Increments the size of each node along the path if a new word is being added.
     *
     * @param str The word string to insert.
     * @return The Trie node representing the end of the inserted word.
     */
    @SuppressWarnings("unchecked")
    public T put(String str){
        T node = (T) this;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch < 'a' || ch > 'z') continue;
            node = node.putIfAbsent(ch);
        }
        node.isEndOfWord = true;
        return node;
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
     * Returns the number of words stored in the Trie rooted at this node.
     *
     * @return Total word count.
     */
    public int size() {
        int toAdd = isEndOfWord ? 1: 0;
        for (int i = 0; i < ALPHABET_SIZE; i++) {
            T child = children.get(i);
            if (child != null) {
                toAdd += child.size();
            }
        }
        return toAdd;
    }

    /**
     * Retrieves all words stored in the Trie.
     *
     * @return A set containing all complete words.
     */
    public Set<String> getWords(){
        Set<String> words = new ArraySet<>(size());
        _getWords("", words);
        return words;
    }

    /**
     * Recursive helper to collect words from the trie structure via Depth-First Search.
     *
     * @param word The prefix string accumulated so far.
     * @param set  The set to add discovered words to.
     */
    protected void _getWords(String word, Set<String> set) {
        if (isEndOfWord()) {
            set.add(word);
        }
        if (isLeaf) return;
        for (int i = 0; i < ALPHABET_SIZE; i++) {
            T child = children.get(i);
            if (child != null) {
                child._getWords(word + (char)(i + 'a'), set);
            }
        }
    }


    /**
     * Returns a string representation of all words in the Trie.
     * @return A string containing the words set.
     */
    @NonNull
    @Override
    public String toString() {
        return getWords().toString();
    }
}
