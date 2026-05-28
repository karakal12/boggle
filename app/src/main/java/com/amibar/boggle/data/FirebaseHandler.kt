package com.amibar.boggle.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton object that centralizes Firebase Authentication and Realtime Database logic.
 * This simplifies Firebase access across various fragments and activities in the app.
 */
object FirebaseHandler {
    private const val TAG = "FirebaseHandler"

    /** Instance of Firebase Authentication. */
    val auth by lazy { FirebaseAuth.getInstance() }

    /** Instance of Firebase Realtime Database. */
    val database by lazy { FirebaseDatabase.getInstance() }

    /** Instance of Firebase Messaging. */
    val messaging by lazy { FirebaseMessaging.getInstance() }

    private val _userData = MutableStateFlow<User?>(null)

    private var userListener: ValueEventListener? = null

    /** Cached local user data as an observable flow. */
    val userDataFlow = _userData.asStateFlow()

    /** Cached local user data. */
    val userData: User?
        get() = _userData.value

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
    val rootRef by lazy { database.reference }

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
            // Initially populate with what we have from FirebaseUser if we don't have data yet
            if (_userData.value == null || _userData.value?.uid != user.uid) {
                _userData.value = User(
                    uid = user.uid,
                    displayName = user.displayName ?: "User",
                    email = user.email ?: ""
                )
            }

            // Remove previous listener if any
            userListener?.let { userRef?.removeEventListener(it) }

            // Set up a real-time listener for the user data
            userListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        _userData.value = snapshot.getValue(User::class.java)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Database listener cancelled", error.toException())
                }
            }
            userRef?.addValueEventListener(userListener!!)

            // Also reload the user to check if they are still valid in Firebase Auth
            user.reload().addOnFailureListener { e ->
                Log.e(TAG, "User reload failed", e)
                signOut()
            }
        } else {
            userListener?.let { userRef?.removeEventListener(it) }
            userListener = null
            _userData.value = null
        }
    }

    /**
     * Signs out the current user from Firebase and clears local user data.
     */
    fun signOut() {
        auth.signOut()
        _userData.value = null
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
