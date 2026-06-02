package com.amibar.boggle.engine

import android.util.Log
import com.amibar.boggle.R
import com.amibar.boggle.data.Dictionary
import com.amibar.boggle.data.PathTrie
import com.amibar.boggle.utils.Timer
import java.util.ArrayDeque
import java.util.Collections
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs

/**
 * Represents a Boggle game instance, managing the game state, dice, scoring, and word validation.
 * It handles board generation, user word selection, scoring logic, and pre-calculates solutions.
 */
class BoggleGame @JvmOverloads constructor(
    /** The 1D array representation of the 4x4 board letters.  */
    val board: CharArray = generateBoard()
) {
    /**
     * Gets the raw 1D character array representing the board.
     * @return The board array.
     */

    /** Tracks the indices of dice currently selected by the player to form a word.  */
    private val selectedIndices: ArrayDeque<Int> = ArrayDeque()
    /**
     * Returns the list of words correctly found by the player.
     * @return A list of found words.
     */
    /** Stores words successfully found and submitted by the player.  */
    private val _foundWords: ArrayList<String> = ArrayList()
    val foundWords: List<String> get() = _foundWords.toList()

    /**
     * Gets the current player score.
     * @return The current score.
     */
    /** The player's current cumulative score.  */
    var score: Int = 0
        private set
    /**
     * Gets the number of hints available to the player.
     * @return The number of hints.
     */
    /**
     * Sets the number of hints available to the player.
     * @param hints The number of hints.
     */
    /** Number of hints available to the player.  */
    var hints: Int = 999
    /**
     * Checks if the game has ended.
     * @return True if ended, false otherwise.
     */
    /** Flag indicating if the game has concluded.  */
    var isEnded: Boolean = false
        private set

    // Listeners for game events, using CopyOnWriteArrayList for thread safety during iteration
    private val onGameEndListeners: MutableList<()-> Unit> =
        CopyOnWriteArrayList()
    private val onWordFoundListeners: MutableList<(word: String)-> Unit> =
        CopyOnWriteArrayList()
    private val onTickListeners: MutableList<(elapsedTime: Long)-> Unit> =
        CopyOnWriteArrayList()

    /**
     * Returns all possible valid words that can be found on this board as a Trie.
     * @return A Trie of solution words.
     */
    /** Trie containing all valid words that can be formed on the current board.  */
    val solutions: PathTrie
    /**
     * Returns a list of all possible word paths on the board.
     * @return A list of path strings.
     */
    /** List of all possible valid paths on the board.  */
    val allPaths: MutableList<String>

    /** Timer managing the game countdown.  */
    private val gameTimer: Timer



    /**
     * Initializes a new Boggle game with a specific board configuration.
     * 
     * @param board A char array of size 16 representing the 4x4 grid.
     */
    /**
     * Initializes a new Boggle game with a randomly generated board.
     * Generates the dice, shuffles their positions, and rolls each one to determine the face.
     * Also calculates all possible solutions for the generated board.
     */
    init {

        // Solve the board using the GameSolver and the dictionary root.
        // This is done upfront to provide immediate feedback on word validity during the game.
        Dictionary.ROOT.waitUntilInitialized()

        val result = GameSolver().solve(this.dice, Dictionary.ROOT)
        solutions = result.solutions
        allPaths = result.allPaths
        Log.d(
            "BoggleGame",
            "Found " + solutions.size + " solutions and " + allPaths.size + " total paths"
        )
        Log.v("BoggleGame", "Solutions: $solutions")

        // Initialize the game timer with total duration and callbacks for ticks and completion.
        gameTimer = Timer(
            millisTime = GAME_TIME_MILLIS,
            onTick = { elapsedTime: Long ->
                for (listener in onTickListeners) {
                    listener(elapsedTime)
                }
            },
            onTimerEnd = { this.endGame() })
    }


    /**
     * Adds a listener to be notified when the game ends.
     * @param listener The listener to add.
     */
    fun addOnGameEndListener(listener: () -> Unit ) {
        this.onGameEndListeners.add(listener)
    }

    /**
     * Adds a listener to be notified when a valid word is found.
     * @param listener The listener to add.
     */
    fun addOnWordFoundListener(listener: (String) -> Unit) {
        this.onWordFoundListeners.add(listener)
    }

    /**
     * Adds a listener to be notified on every timer tick.
     * @param listener The listener to add.
     */
    fun addOnTickListener(listener: (elapsedTime: Long) -> Unit) {
        this.onTickListeners.add(listener)
    }


    /**
     * Subtracts 1 from the number of hints available to the player.
     */
    fun subHint() {
        hints--
    }

    fun getSelectedIndices(): List<Int> {
        return selectedIndices.toList()
    }


    val dice: Array<CharArray?>
        /**
         * Converts the internal 1D board into a 4x4 character array for solvers or UI.
         * Letters are converted to lowercase.
         * @return A 2D char array representing the board.
         */
        get() {
            val diceGrid = Array<CharArray?>(4) { CharArray(4) }
            for (i in 0..3) {
                for (j in 0..3) {
                    // Convert index to grid coordinates and get lowercase letter
                    diceGrid[i]!![j] = board[i * 4 + j].lowercaseChar()
                }
            }
            return diceGrid
        }

    val word: String
        /**
         * Returns the string representation of the currently selected letters without clearing them.
         * Note: Special handling for 'Q' which is treated as 'QU' in Boggle.
         * @return The current word selection.
         */
        get() {
            val sb = StringBuilder()
            for (index in selectedIndices) {
                val c = board[index!!]
                sb.append(c)
                if (c == 'Q') sb.append('U')
            }
            return sb.toString()
        }

    /**
     * Reconstructs the word formed by a given path of indices.
     * @param path A string of hexadecimal digits representing board indices.
     * @return The word string.
     */
    fun getWordFromPath(path: String): String {
        val sb = StringBuilder()
        for (i in 0..<path.length) {
            val index = Character.getNumericValue(path[i])
            val c = board[index]
            sb.append(c)
            if (c == 'Q') sb.append('U')
        }
        return sb.toString().lowercase(Locale.getDefault())
    }

    /**
     * Starts the game timer.
     */
    fun startTimer() {
        gameTimer.start()
    }

    /**
     * Stops the game timer.
     */
    fun stopTimer() {
        gameTimer.stop()
    }

    /**
     * Signals the end of the game and notifies all registered listeners.
     */
    fun endGame() {
        if (this.isEnded) return
        this.isEnded = true
        gameTimer.stop()
        Log.d("BoggleGame", "Game ended. Final score: $score, Words found: $_foundWords")
        for (listener in onGameEndListeners) {
            listener()
        }
    }

    /**
     * Submits the current word selection for scoring.
     * Checks for validity, minimum length, and whether it was already found.
     * If valid, updates score and notifies listeners.
     * 
     * @return The result of the word check (VALID, INVALID, TOO_SHORT, etc.).
     */
    fun submitWord(): WordCheckResult {
        val formedWord = formWord() // Note: this clears the selection indices
        if (formedWord.isBlank()) {
            return WordCheckResult.NULL_WORD
        }
        // Boggle words must be at least 3 letters long
        if (formedWord.length < 3) {
            return WordCheckResult.TOO_SHORT
        }
        // Cannot submit the same word twice
        if (_foundWords.contains(formedWord)) {
            return WordCheckResult.ALREADY_FOUND
        }
        // Check if word exists in the dictionary. Using solutions trie would also work and be faster.
        if (Dictionary.contains(formedWord)) {
            score += wordScore(formedWord)
            _foundWords.add(formedWord)
            for (listener in onWordFoundListeners) {
                listener(formedWord)
            }
            Log.d("BoggleGame", "Found word: $formedWord")
            return WordCheckResult.VALID
        }
        return WordCheckResult.INVALID
    }

    val maxScore: Int
        /**
         * Calculates the maximum possible score for this board.
         * @return The sum of scores for all possible solutions.
         */
        get() {
            var maxScore = 0
            for (s in solutions.words) {
                maxScore += wordScore(s)
            }
            return maxScore
        }

    /**
     * Extracts the word from the current selection queue and clears the selection.
     * Handles 'Q' -> 'QU' conversion and converts the result to lowercase.
     * @return The lowercase string representation of the selected dice.
     */
    private fun formWord(): String {
        val sb = StringBuilder()
        while (!selectedIndices.isEmpty()) {
            val index: Int = selectedIndices.removeFirst()!!
            val c = board[index]
            sb.append(c)
            if (c == 'Q') sb.append('U')
        }
        return sb.toString().lowercase(Locale.getDefault())
    }

    /**
     * Attempts to select a die at a specific index to be part of the current word.
     * Selection is valid if it's the first die or adjacent to the last selected die,
     * and hasn't been used yet in the current word.
     * @param index The index of the die in the 1D list (0-15).
     * @return True if the die was successfully added to the selection.
     */
    fun selectDie(index: Int): Boolean {
        // First letter in a word
        if (selectedIndices.isEmpty()) {
            selectedIndices.add(index)
            return true
        }
        // Subsequent letters must be adjacent and not reused
        val lastIndex: Int = selectedIndices.getLast()!!
        if (isAdjacent(lastIndex, index) && !selectedIndices.contains(index)) {
            selectedIndices.add(index)
            return true
        }
        return false
    }

    /**
     * Selects a sequence of dice indices as the current word selection.
     * @param path A string where each character is a hexadecimal digit (0-f) representing a die index.
     */
    fun selectPath(path: String) {
        selectedIndices.clear()
        for (c in path.toCharArray()) {
            selectedIndices.add(Character.getNumericValue(c))
        }
    }

    /**
     * Clears the current word selection.
     */
    fun deselectPath() {
        selectedIndices.clear()
    }

    /**
     * Checks if two dice are adjacent on the 4x4 grid.
     * Adjacency includes horizontal, vertical, and diagonal neighbors.
     * @param lastIndex The index of the previous die.
     * @param index The index of the current die.
     * @return True if the dice are neighbors.
     */
    private fun isAdjacent(lastIndex: Int, index: Int): Boolean {
        val lastRow = lastIndex / 4
        val lastCol = lastIndex % 4
        val row = index / 4
        val col = index % 4
        // Check if both row and column differences are <= 1
        return abs(lastRow - row) <= 1 && abs(lastCol - col) <= 1
    }

    /**
     * Internal class representing a Boggle die with 6 sides.
     */
    private class Die
        /**
         * Creates a die with the specified faces.
         * @param letters Array of 6 characters.
         */
        private constructor(
            /** The six letters on this specific die.  */
            private val letters: CharArray
        ) {


        /** The index of the letter currently facing up.  */
        private var selectedLetter = 0

        /**
         * Randomly selects one of the 6 letters on the die.
         */
        fun roll() {
            selectedLetter = (Math.random() * 6).toInt()
        }

        val letter: Char
            /**
             * Gets the letter currently showing on the top face.
             * @return The character.
             */
            get() = letters[selectedLetter]

        companion object {
            /**
             * The letter configurations for the 16 standard Boggle dice.
             */
            val DICE_CONFIGS: Array<CharArray> = arrayOf(
                charArrayOf('A', 'A', 'E', 'E', 'G', 'N'),
                charArrayOf('E', 'L', 'R', 'T', 'T', 'Y'),
                charArrayOf('A', 'O', 'O', 'T', 'T', 'W'),
                charArrayOf('A', 'B', 'B', 'J', 'O', 'O'),
                charArrayOf('E', 'H', 'R', 'T', 'V', 'W'),
                charArrayOf('C', 'I', 'M', 'O', 'T', 'U'),
                charArrayOf('D', 'I', 'S', 'T', 'T', 'Y'),
                charArrayOf('E', 'I', 'O', 'S', 'S', 'T'),
                charArrayOf('D', 'E', 'L', 'R', 'V', 'Y'),
                charArrayOf('A', 'C', 'H', 'O', 'P', 'S'),
                charArrayOf('H', 'I', 'M', 'N', 'Q', 'U'),
                charArrayOf('E', 'E', 'I', 'N', 'S', 'U'),
                charArrayOf('E', 'E', 'G', 'H', 'N', 'W'),
                charArrayOf('A', 'F', 'F', 'K', 'P', 'S'),
                charArrayOf('H', 'L', 'N', 'N', 'R', 'Z'),
                charArrayOf('D', 'E', 'I', 'L', 'R', 'X'),
            )

            /**
             * Factory method to create the set of 16 dice based on standard configurations.
             * @return A list of 16 Die objects.
             */
            fun generateDice(): ArrayList<Die> {
                val dice: ArrayList<Die> = ArrayList(16)
                for (config in DICE_CONFIGS) {
                    dice.add(Die(config))
                }
                return dice
            }
        }
    }

    /**
     * Represents the possible outcomes when a player submits a word.
     */
    enum class WordCheckResult
    /**
     * Constructor for WordCheckResult.
     * @param messageId The R.string ID.
     */(
        /** The string resource ID for the message to be displayed for this result.  */
        val messageId: Int
    ) {
        /** The word is valid and found for the first time.  */
        VALID(R.string.word_valid),

        /** The word is not in the dictionary.  */
        INVALID(R.string.word_invalid),

        /** The word was already found by the player in this game.  */
        ALREADY_FOUND(R.string.word_already_found),

        /** The word is too short (less than 3 letters).  */
        TOO_SHORT(R.string.word_too_short),

        /** The word is empty or null.  */
        NULL_WORD(R.string.word_null)

        /**
         * @return The resource ID of the message to display to the user.
         */
    }

    companion object {
        /**
         * The total duration of a game in milliseconds.
         */
        const val GAME_TIME_MILLIS: Long = 180000 // 180000 millis = 3 minutes

        /**
         * Generates a randomized 16-character board based on standard Boggle dice.
         * 
         * @return A char array representing the board.
         */
        private fun generateBoard(): CharArray {
            val diceList = Die.generateDice()
            Collections.shuffle(diceList)
            val board = CharArray(16)
            for (i in 0..15) {
                val die = diceList[i]
                die.roll()
                board[i] = die.letter
            }
            return board
        }

        /**
         * Calculates the score of a word based on standard Boggle scoring rules.
         * 3-4 letters: 1 pt, 5 letters: 2 pts, 6 letters: 3 pts, 7 letters: 5 pts, 8+ letters: 11 pts.
         * 
         * @param word The word to score.
         * @return The points awarded for the word.
         */
        fun wordScore(word: String): Int {
            return when (val wordLength = word.length) {
                3, 4 -> 1
                5 -> 2
                6 -> 3
                7 -> 5
                else -> if (wordLength >= 8) 11 else 0
            }
        }
    }
}
