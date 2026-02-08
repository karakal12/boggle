package com.amibar.boggle.data;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHandler {
    private static FirebaseHandler instance;
    private final FirebaseAuth mAuth;
    private final FirebaseDatabase mDatabase;

    private FirebaseHandler() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
    }

    public static synchronized FirebaseHandler getInstance() {
        if (instance == null) {
            instance = new FirebaseHandler();
        }
        return instance;
    }

    public static synchronized FirebaseAuth getAuth() {
        return getInstance().mAuth;
    }

    public static synchronized FirebaseDatabase getDatabase() {
        return getInstance().mDatabase;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }

    public DatabaseReference getRootRef() {
        return mDatabase.getReference();
    }

    public DatabaseReference getUserRef() {
        String userId = getCurrentUserId();
        if (userId != null) {
            return mDatabase.getReference("users").child(userId);
        }
        return null;
    }

    public void signOut() {
        mAuth.signOut();
    }
}
