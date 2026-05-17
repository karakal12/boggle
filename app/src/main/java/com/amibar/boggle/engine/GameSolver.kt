package com.amibar.boggle.engine

import android.util.Log
import com.amibar.boggle.data.Dictionary
import com.amibar.boggle.data.PathTrie
import com.amibar.boggle.data.Trie
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ForkJoinTask
import java.util.concurrent.RecursiveAction

/**
 * GameSolver provides the core logic for solving a Boggle board.
 * It identifies all valid words from a dictionary that can be formed on a given 4x4 board
 * by connecting adjacent dice (horizontally, vertically, or diagonally) without reusing any die.
 * 
 * 
 * The solver utilizes a parallelized approach using the Java ForkJoin framework (RecursiveAction)
 * to perform a depth-first search (DFS) from each starting position on the board.
 */
class GameSolver {
    /**
     * A Trie to store all unique words found on the board.
     * The value associated with each terminal node is the hex-encoded path representing the word's discovery.
     */
    private lateinit var solutions: PathTrie

    /**
     * A thread-safe queue used to collect every valid path discovered during the search.
     * Since multiple paths can form the same word, this stores all of them for visualization or scoring purposes.
     */
    private lateinit var allPaths: ConcurrentLinkedQueue<String>

    /**
     * Container for the results of a solve operation.
     * 
     * @param solutions A PathTrie containing all unique words found and their primary paths.
     * @param allPaths  A list of all valid word paths found (including duplicates for the same word).
     */
    @JvmRecord
    data class SolverResult(val solutions: PathTrie, val allPaths: MutableList<String>)

    /**
     * A recursive task that explores the board from a specific cell to find valid words.
     * Inherits from [RecursiveAction] to enable parallel execution via a ForkJoinPool.
     */
    internal inner class GameSolverTask(
        /** The current cursor in the dictionary Trie corresponding to the prefix formed so far.  */
        private val cursor: Trie.Cursor,
        /** The 4x4 Boggle board.  */
        private val board: Array<CharArray?>,
        i: Int,
        j: Int,
        visited: Short,
        path: String?,
        string: String
    ) : RecursiveAction() {

        /** Current row index on the board (0-3).  */
        private val i: Int

        /** Current column index on the board (0-3).  */
        private val j: Int

        /**
         * A 16-bit bitmap tracking visited dice in the current path.
         * Bit 'k' is set if the die at index k (row k/4, col k%4) has been visited.
         * This efficiently prevents reusing the same die in a single word path.
         */
        private var visited: Short

        /** The sequence of board indices (as hex digits) representing the current path.  */
        private val path: String?

        /** The actual string formed by the current path.  */
        private val string: String

        /**
         * Initializes a new solver task for a specific board position.
         * 
         * @param cursor  The current cursor in the dictionary Trie.
         * @param board   The 4x4 character board.
         * @param i       Current row index.
         * @param j       Current column index.
         * @param visited Bitmap of visited cells in the current recursion branch.
         * @param path    The path of indices followed so far.
         * @param string  The string formed so far in this path.
         */
        init {
            // The solver is currently optimized for a standard 4x4 Boggle board.
            assert(board.size == board[0]!!.size && board.size == 4) { "Board must be 4x4" }
            this.i = i
            this.j = j
            this.visited = visited
            this.path = path
            this.string = string
        }

        /**
         * Executes the search logic for this task.
         * Checks if the current prefix is a word and spawns sub-tasks for all valid adjacent dice.
         */
        override fun compute() {
            // Check if we've reached a leaf in the dictionary (no further characters possible).
            if (cursor.isLeaf) {
                // Standard Boggle rules require words to be at least 3 letters long.
                if (string.length > 2) {
                    allPaths.add(path)
                    // Add to unique solutions if this word hasn't been discovered yet.
                    if (solutions.getPath(string) == null) {
                        solutions.put(string, path)
                    }
                }
                // Terminate recursion branch as no further extensions are possible from this Trie node.
                return
            }

            // If the current node marks a valid word in the dictionary (it may also be a prefix for longer words).
            if (cursor.isEndOfWord) {
                if (string.length > 2) {
                    allPaths!!.add(path)
                    // Ensure the word is added to the unique solutions set if not already present.
                    if (solutions!!.getPath(string) == null) {
                        Log.v("GameSolver", "Adding $string to solutions")
                        solutions!!.put(string, path)
                    }
                }
            }

            // Mark the current cell as visited in the bitmap for child branches to prevent reuse.
            val currentVisited = (visited.toInt() or (1 shl (i * board!!.size + j))).toShort()

            val tasks: MutableList<GameSolverTask> = ArrayList()

            // Iterate through possible next characters in the Trie to filter neighbor exploration.
            for (chCode in 'a'.code..'z'.code) {
                val ch = chCode.toChar()
                // Only proceed if the character exists as a child of the current Trie node.
                val childCursor = cursor.getChildCursor(ch) ?: continue

                // Explore all 8 adjacent neighbors (horizontal, vertical, and diagonal).
                for (x in -1..1) {
                    for (y in -1..1) {
                        if (x == 0 && y == 0) continue  // Skip the current cell.


                        val nextI = i + x
                        val nextJ = j + y

                        // Check if neighbor is within bounds, not visited in this path, and matches character 'ch'.
                        if (isSafe(nextI, nextJ, currentVisited) && board[nextI]!![nextJ] == ch) {
                            var nextCursor: Trie.Cursor? = childCursor
                            // Append the neighbor's index (as a hex digit) to the path tracking.
                            val nextPath = path + Integer.toHexString(nextI * board.size + nextJ)
                            var nextString = string + ch

                            // Boggle special case: The 'Qu' die.
                            // In many Boggle versions, 'Q' is treated as 'Qu' on a single die.
                            if (ch == 'q') {
                                nextCursor = nextCursor?.getChildCursor('u')
                                // If 'qu' is not a valid prefix in the dictionary, skip this path.
                                if (nextCursor == null) continue
                                nextString += "u"
                            }

                            // Create a new task to continue searching from this neighbor.
                            tasks.add(
                                GameSolverTask(
                                    nextCursor!!,
                                    board,
                                    nextI,
                                    nextJ,
                                    currentVisited,
                                    nextPath,
                                    nextString
                                )
                            )
                        }
                    }
                }
            }

            // Parallelize the search by invoking all sub-tasks in the ForkJoinPool.
            if (tasks.isNotEmpty()) {
                invokeAll(tasks)
            }
        }

        /**
         * Validates if a cell is within board boundaries and hasn't been visited in the current path.
         * 
         * @param i       Row index to check.
         * @param j       Column index to check.
         * @param visited The current visited bitmap for the path.
         * @return `true` if the cell can be safely visited; `false` otherwise.
         */
        private fun isSafe(i: Int, j: Int, visited: Short): Boolean {
            return i >= 0 && i < board!!.size && j >= 0 && j < board.size && (visited.toInt() and (1 shl (i * board.size + j))) == 0
        }
    }

    /**
     * Solves the given Boggle board by finding all possible words from the dictionary.
     * This method initiates a parallel search starting from every cell on the board.
     * 
     * @param board      A 4x4 character array representing the board.
     * @param dictionary The dictionary to use for word validation.
     * @return A [SolverResult] containing the unique words found and all valid paths.
     */
    fun solve(board: Array<CharArray?>, dictionary: Dictionary): SolverResult {
        solutions = PathTrie()
        allPaths = ConcurrentLinkedQueue<String>()
        val tasks: MutableList<GameSolverTask> = ArrayList()

        // Start a search task for every cell on the board as a potential word beginning.
        for (i in board.indices) {
            for (j in board.indices) {
                val c = board[i]!![j]
                var cursor = dictionary.rootCursor().getChildCursor(c)

                if (cursor != null) {
                    var s = c.toString()
                    val path = Integer.toHexString(i * board.size + j)

                    // Handle special 'Qu' case if the word starts with 'Q'.
                    if (c == 'q') {
                        cursor = cursor.getChildCursor('u')
                        if (cursor == null) continue
                        s = "qu"
                    }
                    tasks.add(GameSolverTask(cursor, board, i, j, 0.toShort(), path, s))
                }
            }
        }

        // Execute all starting tasks in parallel using the ForkJoin framework.
        if (tasks.isNotEmpty()) {
            ForkJoinTask.invokeAll(tasks)
        }

        return SolverResult(solutions, ArrayList(allPaths))
    }
}
