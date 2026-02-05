package com.amibar.boggle;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveAction;

public class GameSolver {
    private Set<String> solutions;
    class GameSolverTask extends RecursiveAction {

        private final Dictionary.DictNode root;
        private final char[][] board;
        private final int i, j;
        private short visited; // bitmap
        private final String string;

        public GameSolverTask(Dictionary.DictNode root, char[][] board, int i, int j, short visited, String string) {
            this.root = root;
            this.board = board;
            assert (board.length == board[0].length && board.length == 4) : "board must be 4x4";
            this.i = i;
            this.j = j;
            this.visited = visited;
            this.string = string;
        }

        @Override
        protected void compute() {
            if (root.isLeaf()) {
                if (string.length() > 2) solutions.add(string);
                return;
            }
            if (root.isEndOfWord()) {
                if (string.length() > 2) solutions.add(string);
            }

            visited = (short) (visited | (1 << (i * board.length + j)));

            List<GameSolverTask> tasks = new ArrayList<>();

            for (char ch = 'a'; ch <= 'z'; ch++) {
                if (root.get(ch) != null) {
                    for (int x = -1; x <= 1; x++) {
                        for (int y = -1; y <= 1; y++) {
                            if (x == 0 && y == 0) {
                                continue;
                            }
                            if (isSafe(i + x, j + y, visited) && board[i + x][j + y] == ch) {
                                GameSolverTask task = new GameSolverTask(root.get(ch),
                                        board, i + x, j + y, visited, string + ch);
                                tasks.add(task);
                            }
                        }
                    }
                }
            }
            invokeAll(tasks);
        }

        private boolean isSafe(int i, int j, short visited) {
            return i >= 0 && i < board.length && j >= 0 && j < board.length && (visited & (1 << (i * board.length + j))) == 0;
        }
    }
    public Set<String> solve(char[][] board) {
        solutions = Collections.synchronizedSet(new HashSet<>());
        new GameSolverTask(Dictionary.root, board, 0, 0, (short) 0, "").invoke();
        return solutions;
    }
}
