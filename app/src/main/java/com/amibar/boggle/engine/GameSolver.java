package com.amibar.boggle.engine;


import static java.util.concurrent.ForkJoinTask.invokeAll;

import com.amibar.boggle.data.Dictionary;

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
                            int nextI = i + x;
                            int nextJ = j + y;
                            if (isSafe(nextI, nextJ, visited) && board[nextI][nextJ] == ch) {
                                char c = board[nextI][nextJ];
                                Dictionary.DictNode nextNode = root.get(c);
                                if (nextNode != null) {
                                    String nextString = string + c;
                                    if (c == 'q') {
                                        nextNode = nextNode.get('u');
                                        if (nextNode == null) continue;
                                        nextString = string + "qu";
                                    }
                                    tasks.add(new GameSolverTask(nextNode, board, nextI, nextJ, visited, nextString));
                                }
                            }
                        }
                    }
                }
                invokeAll(tasks);
            }
        }

        private boolean isSafe(int i, int j, short visited) {
            return i >= 0 && i < board.length && j >= 0 && j < board.length && (visited & (1 << (i * board.length + j))) == 0;
        }
    }
    public Set<String> solve(char[][] board, Dictionary dictionary) {
        solutions = Collections.synchronizedSet(new HashSet<>());
        List<GameSolverTask> tasks = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                char c = board[i][j];
                Dictionary.DictNode node = dictionary.getRoot().get(c);
                if (node != null) {
                    String s = String.valueOf(c);
                    if (c == 'q') {
                        node = node.get('u');
                        if (node == null) continue;
                        s = "qu";
                    }
                    tasks.add(new GameSolverTask(node, board, i, j, (short) 0, s));
                }
            }
        }
        invokeAll(tasks);
        return solutions;
    }
}
