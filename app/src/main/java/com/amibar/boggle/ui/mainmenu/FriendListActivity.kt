package com.amibar.boggle.ui.mainmenu

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.amibar.boggle.R
import com.amibar.boggle.data.FirebaseHandler
import com.amibar.boggle.data.User
import com.amibar.boggle.databinding.ActivityFriendlistBinding
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import java.util.Locale

/**
 * Activity for managing and viewing a user's friend list.
 * Allows users to search for others by email, add friends, and invite them to game rooms.
 * Uses Firebase Realtime Database for all persistence.
 */
class FriendListActivity : AppCompatActivity() {
    /** View binding for the activity.  */
    private var binding: ActivityFriendlistBinding? = null

    /** Adapter for the friends list RecyclerView.  */
    private var adapter: FriendAdapter? = null

    /** Local list of friend objects fetched from the database.  */
    private val friendsList: MutableList<User> = ArrayList()

    /** Cached snapshot of all users for searching purposes.  */
    private var usersSnapshot: DataSnapshot? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_friendlist
        )

        setupRecyclerView()
        setupClickListeners()
        setupSearchInput()

        loadUsers()
    }

    /**
     * Loads the global user list (for searching) and the current user's friends list.
     */
    private fun loadUsers() {
        FirebaseHandler.rootRef.child("users").get()
            .addOnSuccessListener(OnSuccessListener { snapshot: DataSnapshot? ->
                usersSnapshot = snapshot
            }).addOnFailureListener { e: Exception? ->
                Log.e(
                    TAG,
                    "Failed to load users",
                    e
                )
            }
        loadFriends()
    }

    /**
     * Initializes the RecyclerView for displaying friends and its adapter.
     */
    private fun setupRecyclerView() {
        adapter = FriendAdapter { friend -> friend?.let { showInviteDialog(it) } }
        binding!!.friendsRecyclerView.adapter = adapter
    }

    /**
     * Displays a dialog to invite a friend to a specific game room by entering a code.
     * @param friend The user object to invite.
     */
    private fun showInviteDialog(friend: User) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Invite " + friend.displayName)
        builder.setMessage("Enter room code to invite them to play:")

        val input = EditText(this)
        input.setHint("Room Code")
        builder.setView(input)

        builder.setPositiveButton(
            "Send"
        ) { dialog: DialogInterface?, which: Int ->
            val roomCode = input.getText().toString().trim { it <= ' ' }
            if (!roomCode.isEmpty()) {
                sendInvitation(friend, roomCode)
                // After sending, transition the host to the MainActivity which will open the room
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("roomCode", roomCode)
                intent.putExtra("action", "host")
                startActivity(intent)
            } else {
                Toast.makeText(this, "Room code cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog: DialogInterface?, which: Int -> dialog!!.cancel() }

        builder.show()
    }

    /**
     * Sends a game invitation record to the recipient's invitations node in Firebase.
     * This will trigger an FCM notification via the InvitationService.
     * @param friend   The recipient of the invitation.
     * @param roomCode The room code the recipient should join.
     */
    private fun sendInvitation(friend: User, roomCode: String?) {
        val currentUserId = FirebaseHandler.currentUserId
        val currentUser = FirebaseHandler.userData

        if (currentUserId == null || currentUser == null) {
            Toast.makeText(this, "Error: You must be logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val invitationsRef = FirebaseHandler.rootRef
            .child("invitations")
            .child(friend.uid)
            .push()

        val invitation: MutableMap<String?, Any?> = HashMap()
        invitation["senderId"] = currentUserId
        invitation["message"] = "Join my Boggle game!"
        invitation["roomCode"] = roomCode
        invitation["timestamp"] = ServerValue.TIMESTAMP

        invitationsRef.setValue(invitation)
            .addOnSuccessListener(OnSuccessListener { aVoid: Void? ->
                Toast.makeText(
                    this,
                    "Invitation sent to " + friend.displayName,
                    Toast.LENGTH_SHORT
                ).show()
            })
            .addOnFailureListener { _: Exception? ->
                Toast.makeText(
                    this,
                    "Failed to send invitation",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    /**
     * Sets up click listeners for the refresh and add friend UI elements.
     */
    private fun setupClickListeners() {
        binding!!.refreshButton.setOnClickListener {
            loadUsers()
            loadFriends()
        }
        binding!!.addFriendButton.setOnClickListener { _ ->
            this.addFriend()
        }
    }

    /**
     * Adds the currently searched user as a friend in the database.
     */
    private fun addFriend() {
        val friendId = binding!!.getSearchedUser()?.uid
        if (friendId.isNullOrEmpty()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return
        }
        FirebaseHandler.addFriend(friendId)
        binding!!.friendEmailInput.setText("")
        Toast.makeText(this, "Friend added!", Toast.LENGTH_SHORT).show()


        // Refresh local friend list
        loadFriends()
    }

    /**
     * Configures the search input field with a TextWatcher for live user filtering.
     */
    private fun setupSearchInput() {
        binding!!.friendEmailInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (usersSnapshot == null) return

                val query = s.toString().lowercase(Locale.getDefault())
                val filteredList: MutableList<User?> = ArrayList()
                for (userSnapshot in usersSnapshot!!.getChildren()) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null && user.email.lowercase(Locale.getDefault())
                            .contains(query)
                    ) {
                        // Don't show current user in search results
                        if (user.uid != FirebaseHandler.currentUserId) {
                            filteredList.add(user)
                        }
                    }
                }
                filteredList.sortWith { u1: User, u2: User ->
                    u1.displayName.compareTo(u2.displayName, ignoreCase = true)
                }
                // Update data binding for the searched user UI
                binding!!.setSearchedUser(if (filteredList.isEmpty()) null else filteredList[0])
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    /**
     * Loads the current user's friend list from Firebase.
     */
    private fun loadFriends() {
        val userRef = FirebaseHandler.userRef ?: return

        val friendsRef = userRef.child("friends")
        friendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                friendsList.clear()
                if (!snapshot.exists()) {
                    adapter!!.submitList(ArrayList<User?>(friendsList))
                    return
                }
                for (friendSnapshot in snapshot.getChildren()) {
                    val friendId = friendSnapshot.key
                    if (friendId != null) {
                        fetchFriendData(friendId)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load friends", error.toException())
            }
        })
    }

    /**
     * Fetches details for a specific friend ID and updates the list.
     * @param friendId The UID of the friend to fetch.
     */
    private fun fetchFriendData(friendId: String) {
        FirebaseHandler.rootRef.child("users").child(friendId).get()
            .addOnSuccessListener(OnSuccessListener { dataSnapshot: DataSnapshot? ->
                val friend = dataSnapshot!!.getValue(User::class.java)
                if (friend != null) {
                    // Avoid duplicates in the local list
                    var exists = false
                    for (u in friendsList) {
                        if (u.uid == friend.uid) {
                            exists = true
                            break
                        }
                    }
                    if (!exists) {
                        friendsList.add(friend)
                        adapter!!.submitList(ArrayList<User?>(friendsList))
                    }
                }
            })
    }

    companion object {
        /** Tag used for logging.  */
        private const val TAG = "FriendListActivity"
    }
}
