package com.amibar.boggle.data;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Singleton class that centralizes Firebase Authentication and Realtime Database logic.
 * This simplifies Firebase access across various fragments and activities in the app.
 */
public class FirebaseHandler {
    /** Tag used for logging. */
    private static final String TAG = "FirebaseHandler";
    /** Singleton instance. */
    private static FirebaseHandler instance;
    /** Instance of Firebase Authentication. */
    private final FirebaseAuth mAuth;
    /** Instance of Firebase Realtime Database. */
    private final FirebaseDatabase mDatabase;
    /** Instance of Firebase Messaging. */
    private final FirebaseMessaging mMessaging;

    /** Cached local user data. */
    private User user;

    /**
     * Initializes the Firebase instances.
     */
    private FirebaseHandler() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mMessaging = FirebaseMessaging.getInstance();
    }

    /**
     * Returns the singleton instance of FirebaseHandler.
     * @return The FirebaseHandler instance.
     */
    public static synchronized FirebaseHandler getInstance() {
        if (instance == null) {
            instance = new FirebaseHandler();
        }
        return instance;
    }

    /**
     * Returns the Firebase Authentication instance.
     * @return The FirebaseAuth instance.
     */
    public static synchronized FirebaseAuth getAuth() {
        return getInstance().mAuth;
    }

    /**
     * Returns the Firebase Realtime Database instance.
     * @return The FirebaseDatabase instance.
     */
    public static synchronized FirebaseDatabase getDatabase() {
        return getInstance().mDatabase;
    }

    /**
     * Returns the Firebase Messaging instance.
     * @return The FirebaseMessaging instance.
     */
    public static synchronized FirebaseMessaging getMessaging() {
        return getInstance().mMessaging;
    }


    /**
     * Returns the currently authenticated FirebaseUser.
     * @return The currently authenticated FirebaseUser, or null if no user is signed in.
     */
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    /**
     * Returns the local cached User data.
     * @return The User object containing profile details.
     */
    public User getUserData() {
        return user;
    }

    /**
     * Checks if the user is still valid in Firebase Auth and updates local user data from the database.
     */
    public void updateUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // First, reload the user to check if they are still valid in Firebase Auth
            currentUser.reload().addOnCompleteListener(reloadTask -> {
                if (reloadTask.isSuccessful()) {
                    // User is still valid in Auth, now check the database
                    DatabaseReference userRef = getUserRef();
                    if (userRef != null) {
                        userRef.get().addOnCompleteListener(dbTask -> {
                            if (dbTask.isSuccessful() && dbTask.getResult().exists()) {
                                user = dbTask.getResult().getValue(User.class);
                            }
                        });
                    }
                } else {
                    Log.e(TAG, "User reload failed", reloadTask.getException());
                }
            });
        } else {
            user = null;
        }
    }

    /**
     * Returns the Unique ID (UID) of the current user.
     * @return The Unique ID (UID) of the current user, or null if not signed in.
     */
    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }

    /**
     * Returns a DatabaseReference pointing to the root of the Realtime Database.
     * @return A DatabaseReference pointing to the root of the Realtime Database.
     */
    public DatabaseReference getRootRef() {
        return mDatabase.getReference();
    }

    /**
     * Returns a DatabaseReference pointing to the current user's entry in the "users" node.
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
     * Signs out the current user from Firebase and clears local user data.
     */
    public void signOut() {
        mAuth.signOut();
        user = null;
    }

    /**
     * Adds a user as a friend in the database. Bi-directional link is created.
     * @param id The UID of the friend to add.
     */
    public void addFriend(String id) {
        DatabaseReference usersRef = mDatabase.getReference("users");
        DatabaseReference myFriendRef = getUserRef();
        if (myFriendRef != null) {
            myFriendRef.child("friends").child(id).setValue(true);
            DatabaseReference friendFriendsRef = usersRef.child(id).child("friends").child(getCurrentUserId());
            friendFriendsRef.setValue(true);
            Log.d(TAG, "Friend added: " + id);
        }
    }
}
