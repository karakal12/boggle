package com.amibar.boggle.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Singleton object that centralizes Firebase Authentication and Realtime Database logic.
 * This simplifies Firebase access across various fragments and activities in the app.
 */
object FirebaseHandler {
    private const val TAG = "FirebaseHandler"

    /** Instance of Firebase Authentication. */
    val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    /** Instance of Firebase Realtime Database. */
    val database: FirebaseDatabase
        get() = FirebaseDatabase.getInstance()

    /** Instance of Firebase Messaging. */
    val messaging: FirebaseMessaging
        get() = FirebaseMessaging.getInstance()

    /** Cached local user data. */
    var userData: User? = null
        private set

    /**
     * Returns the currently authenticated FirebaseUser.
     * @return The currently authenticated FirebaseUser, or null if no user is signed in.
     */
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Returns the Unique ID (UID) of the current user.
     * @return The Unique ID (UID) of the current user, or null if not signed in.
     */
    val currentUserId: String?
        get() = currentUser?.uid

    /**
     * Returns a DatabaseReference pointing to the root of the Realtime Database.
     * @return A DatabaseReference pointing to the root of the Realtime Database.
     */
    val rootRef: DatabaseReference
        get() = database.reference

    /**
     * Returns a DatabaseReference pointing to the current user's entry in the "users" node.
     * @return A DatabaseReference pointing to the current user's entry in the "users" node,
     * or null if no user is signed in.
     */
    val userRef: DatabaseReference?
        get() = currentUserId?.let { database.getReference("users").child(it) }

    /**
     * Checks if the user is still valid in Firebase Auth and updates local user data from the database.
     */
    fun updateUserData() {
        val user = currentUser
        if (user != null) {
            // First, reload the user to check if they are still valid in Firebase Auth
            user.reload().addOnCompleteListener { reloadTask ->
                if (reloadTask.isSuccessful) {
                    // User is still valid in Auth, now check the database
                    userRef?.get()?.addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful && dbTask.result?.exists() == true) {
                            userData = dbTask.result?.getValue(User::class.java)
                        }
                    }
                } else {
                    Log.e(TAG, "User reload failed", reloadTask.exception)
                }
            }
        } else {
            userData = null
        }
    }

    /**
     * Signs out the current user from Firebase and clears local user data.
     */
    fun signOut() {
        auth.signOut()
        userData = null
    }

    /**
     * Adds a user as a friend in the database. Bidirectional link is created.
     * @param id The UID of the friend to add.
     */
    fun addFriend(id: String) {
        val usersRef = database.getReference("users")
        userRef?.let { myRef ->
            myRef.child("friends").child(id).setValue(true)
            currentUserId?.let { myId ->
                usersRef.child(id).child("friends").child(myId).setValue(true)
            }
            Log.d(TAG, "Friend added: $id")
        }
    }

}
