package com.amibar.boggle.data;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Singleton class that centralizes Firebase Authentication and Realtime Database logic.
 * This simplifies Firebase access across various fragments and activities in the app.
 */
public class FirebaseHandler {
    /** Singleton instance. */
    private static FirebaseHandler instance;
    /** Instance of Firebase Authentication. */
    private final FirebaseAuth mAuth;
    /** Instance of Firebase Realtime Database. */
    private final FirebaseDatabase mDatabase;

    /**
     * Initializes the Firebase instances.
     */
    private FirebaseHandler() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
    }

    /**
     * @return The singleton instance of FirebaseHandler.
     */
    public static synchronized FirebaseHandler getInstance() {
        if (instance == null) {
            instance = new FirebaseHandler();
        }
        return instance;
    }

    /**
     * @return The Firebase Authentication instance.
     */
    public static synchronized FirebaseAuth getAuth() {
        return getInstance().mAuth;
    }

    /**
     * @return The Firebase Realtime Database instance.
     */
    public static synchronized FirebaseDatabase getDatabase() {
        return getInstance().mDatabase;
    }

    /**
     * @return The currently authenticated FirebaseUser, or null if no user is signed in.
     */
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    /**
     * @return The Unique ID (UID) of the current user, or null if not signed in.
     */
    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }

    /**
     * @return A DatabaseReference pointing to the root of the Realtime Database.
     */
    public DatabaseReference getRootRef() {
        return mDatabase.getReference();
    }

    /**
     * @return A DatabaseReference pointing to the current user's entry in the "users" node,
     *         or null if no user is signed in.
     */
    public DatabaseReference getUserRef() {
        String userId = getCurrentUserId();
        if (userId != null) {
            return mDatabase.getReference("users").child(userId);
        }
        return null;
    }

    /**
     * Signs out the current user from Firebase.
     */
    public void signOut() {
        mAuth.signOut();
    }
}
