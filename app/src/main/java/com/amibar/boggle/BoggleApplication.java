package com.amibar.boggle;


import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import android.app.Application;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.amibar.boggle.data.Dictionary;
import com.amibar.boggle.data.FirebaseHandler;


/**
 * Custom Application class for the Boggle app.
 * Used for global initialization tasks that should run once per application lifecycle.
 */
public class BoggleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Global Initialization
        // Load the dictionary from raw resources into memory (Trie structure)
        // This ensures the word list is ready before any game activities are launched.
        Dictionary.ROOT.init(getResources().openRawResource(R.raw.word_list));
        FirebaseHandler.getInstance().updateUserData();

    }
}
