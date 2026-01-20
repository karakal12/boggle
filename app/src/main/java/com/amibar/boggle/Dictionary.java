package com.amibar.boggle;


import android.content.res.Resources;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public final class Dictionary {
    private Dictionary() {throw new UnsupportedOperationException("Dictionary is a singleton!");}

    static List<String> words;

    static boolean contains(String word){
        return Collections.binarySearch(words, word) >= 0;
    }

    static void init(InputStream file){
        words = createDict(file);
    }

    private static ArrayList<String> createDict(InputStream file) {
        ArrayList<String> list = new ArrayList<>();
        Scanner reader = new Scanner(file);
        while (reader.hasNext()){
            list.add(reader.next());
        }
        return list;
    }
}
