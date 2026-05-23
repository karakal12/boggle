package com.amibar.boggle.ui.mainmenu

import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amibar.boggle.data.FirebaseHandler
import com.amibar.boggle.data.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Events that can be triggered from the FriendListViewModel.
 */
sealed interface FriendListEvent {
    data class ShowToast(val message: String) : FriendListEvent
    data class NavigateToHostGame(val roomCode: String) : FriendListEvent
}

/**
 * ViewModel for the FriendListActivity.
 * Handles user search, friend list loading, and sending game invitations.
 */
class FriendListViewModel : ViewModel() {
    val searchQueryState = TextFieldState()

    private val _friends = MutableStateFlow<List<User>>(emptyList())
    val friends: StateFlow<List<User>> = _friends.asStateFlow()

    private val _searchedUser = MutableStateFlow<User?>(null)
    val searchedUser: StateFlow<User?> = _searchedUser.asStateFlow()

    private val _events = MutableSharedFlow<FriendListEvent>()
    val events: SharedFlow<FriendListEvent> = _events.asSharedFlow()

    private var usersSnapshot: DataSnapshot? = null

    init {
        loadData()
        observeSearchQuery()
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            snapshotFlow { searchQueryState.text }
                .collect { query ->
                    onSearchQueryChanged(query.toString())
                }
        }
    }

    /**
     * Loads both the global users (for search) and the user's friend list.
     */
    fun loadData() {
        loadUsers()
        loadFriends()
    }

    private fun loadUsers() {
        FirebaseHandler.rootRef.child("users").get()
            .addOnSuccessListener { snapshot ->
                usersSnapshot = snapshot
            }.addOnFailureListener { e ->
                Log.e(TAG, "Failed to load users", e)
            }
    }

    private fun loadFriends() {
        val userRef = FirebaseHandler.userRef ?: return

        val friendsRef = userRef.child("friends")
        friendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    _friends.value = emptyList()
                    return
                }
                
                val currentFriends = mutableListOf<User>()
                val children = snapshot.children.toList()
                var pendingCount = children.size
                
                if (pendingCount == 0) {
                    _friends.value = emptyList()
                    return
                }

                for (friendSnapshot in children) {
                    val friendId = friendSnapshot.key
                    if (friendId != null) {
                        FirebaseHandler.rootRef.child("users").child(friendId).get()
                            .addOnSuccessListener { dataSnapshot ->
                                val friend = dataSnapshot.getValue(User::class.java)
                                if (friend != null) {
                                    currentFriends.add(friend)
                                }
                                pendingCount--
                                if (pendingCount == 0) {
                                    _friends.value = currentFriends.sortedBy { it.displayName.lowercase() }
                                }
                            }
                            .addOnFailureListener {
                                pendingCount--
                                if (pendingCount == 0) {
                                    _friends.value = currentFriends.sortedBy { it.displayName.lowercase() }
                                }
                            }
                    } else {
                        pendingCount--
                        if (pendingCount == 0) {
                            _friends.value = currentFriends.sortedBy { it.displayName.lowercase() }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load friends", error.toException())
            }
        })
    }

    /**
     * Filters the global user list based on the search query.
     */
    fun onSearchQueryChanged(query: String) {
        if (searchQueryState.text.toString() != query) {
            searchQueryState.edit {
                replace(0, length, query)
            }
        }

        val snapshot = usersSnapshot ?: return

        val lowercaseQuery = query.lowercase(Locale.getDefault())
        if (lowercaseQuery.isEmpty()) {
            _searchedUser.value = null
            return
        }

        val filteredList = mutableListOf<User>()
        for (userSnapshot in snapshot.children) {
            val user = userSnapshot.getValue(User::class.java)
            if (user != null && user.email.lowercase(Locale.getDefault()).contains(lowercaseQuery)) {
                // Don't show current user in search results
                if (user.uid != FirebaseHandler.currentUserId) {
                    filteredList.add(user)
                }
            }
        }
        filteredList.sortWith { u1, u2 ->
            u1.displayName.compareTo(u2.displayName, ignoreCase = true)
        }
        _searchedUser.value = filteredList.firstOrNull()
    }

    /**
     * Adds the currently searched user as a friend.
     */
    fun addFriend() {
        val friendId = _searchedUser.value?.uid
        if (friendId.isNullOrEmpty()) {
            emitEvent(FriendListEvent.ShowToast("Please enter a valid email"))
            return
        }
        FirebaseHandler.addFriend(friendId)
        searchQueryState.clearText()
        _searchedUser.value = null
        emitEvent(FriendListEvent.ShowToast("Friend added!"))
        loadFriends()
    }

    /**
     * Sends a game invitation to a friend.
     */
    fun sendInvitation(friend: User, roomCode: String) {
        val currentUserId = FirebaseHandler.currentUserId
        val currentUser = FirebaseHandler.userData

        if (currentUserId == null || currentUser == null) {
            emitEvent(FriendListEvent.ShowToast("Error: You must be logged in"))
            return
        }

        val invitationsRef = FirebaseHandler.rootRef
            .child("invitations")
            .child(friend.uid)
            .push()

        val invitation: MutableMap<String, Any?> = HashMap()
        invitation["senderId"] = currentUserId
        invitation["message"] = "Join my Boggle game!"
        invitation["roomCode"] = roomCode
        invitation["timestamp"] = ServerValue.TIMESTAMP

        invitationsRef.setValue(invitation)
            .addOnSuccessListener {
                emitEvent(FriendListEvent.ShowToast("Invitation sent to ${friend.displayName}"))
                emitEvent(FriendListEvent.NavigateToHostGame(roomCode))
            }
            .addOnFailureListener {
                emitEvent(FriendListEvent.ShowToast("Failed to send invitation"))
            }
    }

    private fun emitEvent(event: FriendListEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    companion object {
        private const val TAG = "FriendListViewModel"
    }
}
