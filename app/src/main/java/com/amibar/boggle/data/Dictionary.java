package com.amibar.boggle.data;


import androidx.annotation.NonNull;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;


public final class Dictionary{
    private Dictionary() {throw new UnsupportedOperationException("Dictionary is a singleton!");}

    static private final DictNode root = new DictNode();
    public static DictNode getRoot(){
        return root;
    }


    public static boolean contains(@NonNull String word){
        DictNode node = root;
        for(char ch : word.toCharArray()){
            if(!node.containsKey(ch)){
                return false;
            }
            node = node.get(ch);
        }
        return node.isEndOfWord;
    }

    public static void init(InputStream file){
        Scanner sc = new Scanner(file);
        while(sc.hasNextLine()){
            String word = sc.nextLine();
            insert(word);
        }
        sc.close();
    }

    private static void insert(@NonNull String word) {
        DictNode node = root;
        for(char ch : word.toCharArray()){
            if(!node.containsKey(ch)){
                node.put(ch, new DictNode());
            }
            node = node.get(ch);
        }
        node.isEndOfWord = true;
    }

    public static class DictNode{
        static final int ALPHABET_SIZE = 26;
        private final DictNode[] children = new DictNode[ALPHABET_SIZE];
        private boolean isEndOfWord, isLeaf;

        public DictNode() {
            this.isEndOfWord = false;
            this.isLeaf = true;
            Arrays.fill(children, null);
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public boolean containsKey(char ch) {
            return children[ch - 'a'] != null;
        }

        public DictNode get(char ch) {
            return children[ch - 'a'];
        }

        public void put(char ch, DictNode node) {
            children[ch - 'a'] = node;
            isLeaf = false;
        }

        public boolean isEndOfWord() {
            return isEndOfWord;
        }

        public boolean isLeaf() {
            return isLeaf;
        }
    }
}
