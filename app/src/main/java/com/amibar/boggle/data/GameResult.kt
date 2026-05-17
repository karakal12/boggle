package com.amibar.boggle.data

/**
 * Data class representing the result of a single-player Boggle game.
 * Used for storing and retrieving game history from Firebase.
 *
 * Firebase Realtime Database requires a no-argument constructor and
 * property setters (or public/internal var properties).
 */
@Suppress("unused")
data class GameResult(
    /** The player's final score. */
    var score: Int = 0,
    /** The number of valid words found by the player. */
    var wordsFound: Int = 0,
    /** The total number of valid words possible on the board. */
    var possibleWords: Int = 0,
    /** The maximum possible score achievable on the board. */
    var maxScore: Int = 0,
    /** List of words found by the player. */
    var foundWords: List<String>? = null,
    /** String representation of the game board. */
    var board: String? = null
)
