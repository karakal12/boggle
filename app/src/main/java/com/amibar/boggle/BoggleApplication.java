package com.amibar.boggle;

import android.app.Application;
import com.amibar.boggle.data.Dictionary;
import com.amibar.boggle.data.FirebaseHandler;

/**
 * Custom Application class for the Boggle app.
 * Used for global initialization tasks that should run exactly once per application lifecycle,
 * before any activities or services are created.
 */
public class BoggleApplication extends Application {
    
    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Global Initialization
        
        // Load the dictionary from raw resources into memory (Trie structure).
        // This is a heavy operation done upfront to ensure word lookups are near-instant during gameplay.
        Dictionary.getInstance().init(getResources().openRawResource(R.raw.word_list));
        
        // Initialize Firebase user data and synchronization.
        // This ensures the local User cache is populated if the user is already authenticated.
        FirebaseHandler.getInstance().updateUserData();
    }
}
