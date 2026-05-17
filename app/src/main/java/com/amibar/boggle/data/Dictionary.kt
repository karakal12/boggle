package com.amibar.boggle.data

import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Locale


/**
 * Trie (Prefix Tree) implementation representing the game dictionary.
 * Inherits from [Trie] to store words efficiently and allow for fast lookups.
 */
class Dictionary
/**
 * Private constructor for Dictionary.
 */
private constructor() : Trie() {
    /** Flag indicating if the dictionary has been loaded with words.  */
    var isInitialized: Boolean = false
        private set

    /**
     * Ensures the dictionary is fully loaded. If init() is still running
     * in another thread, this method will block until it completes.
     */
    @Synchronized
    fun waitUntilInitialized() {
        if (!isInitialized) {
            Log.d(TAG, "Waiting for dictionary initialization...")
        }
    }

    /**
     * Initializes the dictionary by reading words from an InputStream (typically a text file).
     * This operation is synchronized to prevent concurrent initializations.
     * @param file The input stream containing the word list.
     */
    @Synchronized
    fun init(file: InputStream?) {
        if (isInitialized) return

        try {
            BufferedReader(InputStreamReader(file)).use { reader ->
                var line: String?
                while ((reader.readLine().also { line = it }) != null) {
                    val word = line!!.trim { it <= ' ' }.lowercase(Locale.getDefault())
                    if (!word.isEmpty()) {
                        put(word)
                    }
                }
                isInitialized = true
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading dictionary file", e)
        }
    }

    companion object {
        private const val TAG = "Dictionary"

        /**
         * Static root instance of the dictionary.
         */
        val ROOT: Dictionary = Dictionary()

        /**
         * Checks if a word exists in the dictionary.
         * 
         * @param word The word to search for.
         * @return True if the word is present and valid, false otherwise.
         */
        fun contains(word: String): Boolean {
            return ROOT.contains(word)
        }
    }
}
