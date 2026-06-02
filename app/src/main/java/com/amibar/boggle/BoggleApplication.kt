package com.amibar.boggle

import android.app.Application
import com.amibar.boggle.data.Dictionary
import com.amibar.boggle.data.FirebaseHandler
import java.util.concurrent.Executors


/**
 * Custom Application class for the Boggle app.
 * Used for global initialization tasks that should run once per application lifecycle.
 */
class BoggleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Global Initialization
        // Load the dictionary from raw resources into memory (Trie structure)
        // This ensures the word list is ready before any game activities are launched.
        Executors.newSingleThreadExecutor().execute {
            Dictionary.ROOT.init(resources.openRawResource(R.raw.boggle_dictionary))
        }
        FirebaseHandler.updateUserData()
    }
}
