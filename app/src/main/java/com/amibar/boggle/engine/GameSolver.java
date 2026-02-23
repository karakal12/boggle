package com.amibar.boggle.engine;


import static java.util.concurrent.ForkJoinTask.invokeAll;

import com.amibar.boggle.data.Dictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RecursiveAction;

/**
 * Provides functionality to solve a Boggle board by finding all possible valid words.
 * It uses a parallelized approach with the ForkJoin framework (RecursiveAction) to search the board efficiently.
 */
public class GameSolver {
    /** A synchronized set to store all unique words found on the board. */
    private Map<String, String> solutions;

    /**
     * Represents a recursive task for searching words starting from a specific die on the board.
     */
    class GameSolverTask extends RecursiveAction {

        private final Dictionary.DictNode root;
        private final char[][] board;
        private final int i, j;
        /** A 16-bit bitmap tracking visited dice in the current path. */
        private short visited; // bitmap
        private String path;
        private final String string;

        /**
         * Initializes a new solver task.
         * @param root The current node in the dictionary Trie.
         * @param board The 4x4 character board.
         * @param i Current row index.
         * @param j Current column index.
         * @param visited Bitmap of visited cells.
         * @param string The string formed so far in this path.
         */
        public GameSolverTask(Dictionary.DictNode root, char[][] board, int i, int j, short visited, String path, String string) {
            this.root = root;
            this.board = board;
            assert (board.length == board[0].length && board.length == 4) : "board must be 4x4";
            this.i = i;
            this.j = j;
            this.visited = visited;
            this.path = path;
            this.string = string;
        }

        @Override
        protected void compute() {
            // If the Trie node is a leaf, we've reached the end of a potential word path
            if (root.isLeaf()) {
                if (string.length() > 2 &&
                    solutions.get(string) == null) solutions.put(string, path);
                return;
            }
            // If the current node marks the end of a word in the dictionary, add it to solutions
            if (root.isEndOfWord()) {
                if (string.length() > 2 &&
                    solutions.get(string) == null) solutions.put(string, path);
            }

            // Mark the current cell as visited in the bitmap
            visited = (short) (visited | (1 << (i * board.length + j)));

            List<GameSolverTask> tasks = new ArrayList<>();

            // Optimization: Iterate through possible next characters from the Trie
            for (char ch = 'a'; ch <= 'z'; ch++) {
                if (root.get(ch) != null) {
                    // Search all 8 neighbors
                    for (int x = -1; x <= 1; x++) {
                        for (int y = -1; y <= 1; y++) {
                            if (x == 0 && y == 0) {
                                continue;
                            }
                            int nextI = i + x;
                            int nextJ = j + y;
                            // Check if neighbor is within bounds, not visited, and matches the dictionary path
                            if (isSafe(nextI, nextJ, visited) && board[nextI][nextJ] == ch) {
                                char c = board[nextI][nextJ];
                                Dictionary.DictNode nextNode = root.get(c);
                                if (nextNode != null) {
                                    String nextPath = path + (char) (i * board.length + j);
                                    String nextString = string + c;
                                    // Boggle special case: 'q' is always followed by 'u'
                                    if (c == 'q') {
                                        nextNode = nextNode.get('u');
                                        if (nextNode == null) continue;
                                        nextString = string + "qu";
                                    }
                                    // Spawn a new task to continue searching from this neighbor
                                    tasks.add(new GameSolverTask(nextNode, board, nextI, nextJ, visited, nextPath, nextString));
                                }
                            }
                        }
                    }
                }
                // Parallelize the search by invoking all sub-tasks
                invokeAll(tasks);
            }
        }

        /**
         * Validates if a cell is within board boundaries and has not been visited yet.
         * @param i Row index.
         * @param j Column index.
         * @param visited The current visited bitmap.
         * @return True if safe to visit.
         */
        private boolean isSafe(int i, int j, short visited) {
            return i >= 0 && i < board.length && j >= 0 && j < board.length && (visited & (1 << (i * board.length + j))) == 0;
        }
    }

    /**
     * Solves the given Boggle board using the provided dictionary.
     * @param board A 4x4 char array representing the board.
     * @param dictionary The dictionary to use for word validation.
     * @return A set of all unique valid words found on the board.
     */
    public Map<String, String> solve(char[][] board, Dictionary dictionary) {
        solutions = Collections.synchronizedMap(new HashMap<>());
        List<GameSolverTask> tasks = new ArrayList<>();
        // Start a search from every cell on the board
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                char c = board[i][j];
                Dictionary.DictNode node = dictionary.getRoot().get(c);
                if (node != null) {
                    String s = String.valueOf(c);
                    String path = String.valueOf(i * board.length + j);
                    // Handle special 'q' -> 'qu' case
                    if (c == 'q') {
                        node = node.get('u');
                        if (node == null) continue;
                        s = "qu";
                    }
                    tasks.add(new GameSolverTask(node, board, i, j, (short) 0, path, s));
                }
            }
        }
        // Execute all starting tasks in parallel
        invokeAll(tasks);
        return solutions;
    }
}
