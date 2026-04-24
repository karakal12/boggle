package com.amibar.boggle.data;

import androidx.annotation.NonNull;
import java.util.HashMap;

/**
 * A specialized Trie implementation that stores a string path for each word.
 * This is used to store discovered words and the sequence of board coordinates that form them.
 */
public class PathTrie extends Trie<PathTrie>{
    private String path;

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
     * Inserts a word and its associated path into the Trie.
     *
     * @param str  The word string to insert.
     * @param path The optional path string associated with the word.
     *
     * @return the Trie node representing the end of the string
     */
    public PathTrie put(String str, String path){
        PathTrie node = super.put(str);
        if (path != null) {
            node.path = path;
        }
        return node;
    }

    /**
     * Converts the Trie into a Map where keys are words and values are their associated paths.
     *
     * @return A HashMap containing all words and their paths.
     */
    public HashMap<String, String> toMap() {
        HashMap<String, String> map = new HashMap<>();
        for (String s : getWords()) {
            map.put(s, get(s).getPath());
        }
        return map;
    }

    /**
     * Returns a string representation of the words in the Trie.
     *
     * @return A string containing all stored words.
     */
    @NonNull
    @Override
    public String toString() {
        return toMap().keySet().toString();
    }
}
