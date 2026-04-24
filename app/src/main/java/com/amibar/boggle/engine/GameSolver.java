package com.amibar.boggle.engine;

import static java.util.concurrent.ForkJoinTask.invokeAll;

import com.amibar.boggle.data.Dictionary;
import com.amibar.boggle.data.PathTrie;
import com.amibar.boggle.data.Trie;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveAction;

/**
 * GameSolver provides the core logic for solving a Boggle board.
 * It identifies all valid words from a dictionary that can be formed on a given 4x4 board
 * by connecting adjacent dice (horizontally, vertically, or diagonally) without reusing any die.
 *
 * <p>The solver utilizes a parallelized approach using the Java ForkJoin framework (RecursiveAction)
 * to perform a depth-first search (DFS) from each starting position on the board.</p>
 */
public class GameSolver {
    /**
     * A Trie to store all unique words found on the board.
     * The value associated with each terminal node is the hex-encoded path representing the word's discovery.
     */
    private PathTrie solutions;

    /**
     * A thread-safe queue used to collect every valid path discovered during the search.
     * Since multiple paths can form the same word, this stores all of them for visualization or scoring purposes.
     */
    private ConcurrentLinkedQueue<String> allPaths;

    /**
     * Container for the results of a solve operation.
     *
     * @param solutions A PathTrie containing all unique words found and their primary paths.
     * @param allPaths  A list of all valid word paths found (including duplicates for the same word).
     */
    public record SolverResult(PathTrie solutions, List<String> allPaths) {
    }

    /**
     * A recursive task that explores the board from a specific cell to find valid words.
     * Inherits from {@link RecursiveAction} to enable parallel execution via a ForkJoinPool.
     */
    class GameSolverTask extends RecursiveAction {

        /** The current node in the dictionary Trie corresponding to the prefix formed so far. */
        private final Dictionary root;

        /** The 4x4 Boggle board. */
        private final char[][] board;

        /** Current row index on the board (0-3). */
        private final int i;

        /** Current column index on the board (0-3). */
        private final int j;

        /**
         * A 16-bit bitmap tracking visited dice in the current path.
         * Bit 'k' is set if the die at index k (row k/4, col k%4) has been visited.
         * This efficiently prevents reusing the same die in a single word path.
         */
        private short visited;

        /** The sequence of board indices (as hex digits) representing the current path. */
        private final String path;

        /** The actual string formed by the current path. */
        private final String string;

        /**
         * Initializes a new solver task for a specific board position.
         *
         * @param root    The current node in the dictionary Trie.
         * @param board   The 4x4 character board.
         * @param i       Current row index.
         * @param j       Current column index.
         * @param visited Bitmap of visited cells in the current recursion branch.
         * @param path    The path of indices followed so far.
         * @param string  The string formed so far in this path.
         */
        public GameSolverTask(Dictionary root, char[][] board, int i, int j, short visited, String path, String string) {
            this.root = root;
            this.board = board;
            // The solver is currently optimized for a standard 4x4 Boggle board.
            assert (board.length == board[0].length && board.length == 4) : "Board must be 4x4";
            this.i = i;
            this.j = j;
            this.visited = visited;
            this.path = path;
            this.string = string;
        }

        /**
         * Executes the search logic for this task.
         * Checks if the current prefix is a word and spawns sub-tasks for all valid adjacent dice.
         */
        @Override
        protected void compute() {
            // Check if we've reached a leaf in the dictionary (no further characters possible).
            if (root.isLeaf()) {
                // Standard Boggle rules require words to be at least 3 letters long.
                if (string.length() > 2) {
                    allPaths.add(path);
                    // Add to unique solutions if this word hasn't been discovered yet.
                    if (solutions.get(string) == null) {
                        solutions.put(string, path);
                    }
                }
                // Terminate recursion branch as no further extensions are possible from this Trie node.
                return;
            }

            // If the current node marks a valid word in the dictionary (it may also be a prefix for longer words).
            if (root.isEndOfWord()) {
                if (string.length() > 2) {
                    allPaths.add(path);
                    // Ensure the word is added to the unique solutions set if not already present.
                    if (solutions.get(string) == null) {
                        solutions.put(string, path);
                    }
                }
            }

            // Mark the current cell as visited in the bitmap for child branches to prevent reuse.
            visited = (short) (visited | (1 << (i * board.length + j)));

            List<GameSolverTask> tasks = new ArrayList<>();

            // Iterate through possible next characters in the Trie to filter neighbor exploration.
            for (char ch = 'a'; ch <= 'z'; ch++) {
                // Only proceed if the character exists as a child of the current Trie node.
                if (root.get(ch) == null) continue;

                // Explore all 8 adjacent neighbors (horizontal, vertical, and diagonal).
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        if (x == 0 && y == 0) continue; // Skip the current cell.

                        int nextI = i + x;
                        int nextJ = j + y;

                        // Check if neighbor is within bounds, not visited in this path, and matches character 'ch'.
                        if (isSafe(nextI, nextJ, visited) && board[nextI][nextJ] == ch) {
                            char c = board[nextI][nextJ];
                            Dictionary nextNode = root.get(c);

                            if (nextNode != null) {
                                // Append the neighbor's index (as a hex digit) to the path tracking.
                                String nextPath = path + Integer.toHexString(nextI * board.length + nextJ);
                                String nextString = string + c;

                                // Boggle special case: The 'Qu' die.
                                // In many Boggle versions, 'Q' is treated as 'Qu' on a single die.
                                if (c == 'q') {
                                    nextNode = nextNode.get('u');
                                    // If 'qu' is not a valid prefix in the dictionary, skip this path.
                                    if (nextNode == null) continue;
                                    nextString = string + "qu";
                                }

                                // Create a new task to continue searching from this neighbor.
                                tasks.add(new GameSolverTask(nextNode, board, nextI, nextJ, visited, nextPath, nextString));
                            }
                        }
                    }
                }
            }

            // Parallelize the search by invoking all sub-tasks in the ForkJoinPool.
            if (!tasks.isEmpty()) {
                invokeAll(tasks);
            }
        }

        /**
         * Validates if a cell is within board boundaries and hasn't been visited in the current path.
         *
         * @param i       Row index to check.
         * @param j       Column index to check.
         * @param visited The current visited bitmap for the path.
         * @return {@code true} if the cell can be safely visited; {@code false} otherwise.
         */
        private boolean isSafe(int i, int j, short visited) {
            return i >= 0 && i < board.length && j >= 0 && j < board.length && (visited & (1 << (i * board.length + j))) == 0;
        }
    }

    /**
     * Solves the given Boggle board by finding all possible words from the dictionary.
     * This method initiates a parallel search starting from every cell on the board.
     *
     * @param board      A 4x4 character array representing the board.
     * @param dictionary The dictionary to use for word validation.
     * @return A {@link SolverResult} containing the unique words found and all valid paths.
     */
    public SolverResult solve(char[][] board, Dictionary dictionary) {
        solutions = new PathTrie();
        allPaths = new ConcurrentLinkedQueue<>();
        List<GameSolverTask> tasks = new ArrayList<>();

        // Start a search task for every cell on the board as a potential word beginning.
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                char c = board[i][j];
                Dictionary node = dictionary.get(c);

                if (node != null) {
                    String s = String.valueOf(c);
                    String path = Integer.toHexString(i * board.length + j);

                    // Handle special 'Qu' case if the word starts with 'Q'.
                    if (c == 'q') {
                        node = node.get('u');
                        if (node == null) continue;
                        s = "qu";
                    }
                    tasks.add(new GameSolverTask(node, board, i, j, (short) 0, path, s));
                }
            }
        }

        // Execute all starting tasks in parallel using the ForkJoin framework.
        if (!tasks.isEmpty()) {
            invokeAll(tasks);
        }

        return new SolverResult(solutions, new ArrayList<>(allPaths));
    }
}
