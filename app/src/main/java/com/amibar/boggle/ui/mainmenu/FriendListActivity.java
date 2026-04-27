package com.amibar.boggle.ui.mainmenu;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.amibar.boggle.R;
import com.amibar.boggle.data.FirebaseHandler;
import com.amibar.boggle.data.User;
import com.amibar.boggle.databinding.ActivityFriendlistBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for managing and viewing a user's friend list.
 * Allows users to search for others by email, add friends, and invite them to game rooms.
 * Uses Firebase Realtime Database for all persistence.
 */
public class FriendListActivity extends AppCompatActivity {

    /** Tag used for logging. */
    private static final String TAG = "FriendListActivity";
    /** View binding for the activity. */
    private ActivityFriendlistBinding binding;
    /** Adapter for the friends list RecyclerView. */
    private FriendAdapter adapter;
    /** Local list of friend objects fetched from the database. */
    private final List<User> friendsList = new ArrayList<>();
    /** Singleton instance of the FirebaseHandler. */
    private FirebaseHandler firebaseHandler;

    /** Cached snapshot of all users for searching purposes. */
    private DataSnapshot usersSnapshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_friendlist);
        firebaseHandler = FirebaseHandler.getInstance();

        setupRecyclerView();
        setupClickListeners();
        setupSearchInput();

        loadUsers();
    }

    /**
     * Loads the global user list (for searching) and the current user's friends list.
     */
    private void loadUsers() {
        firebaseHandler.getRootRef().child("users").get().addOnSuccessListener(snapshot -> {
            usersSnapshot = snapshot;
        }).addOnFailureListener(e -> Log.e(TAG, "Failed to load users", e));
        loadFriends();
    }

    /**
     * Initializes the RecyclerView for displaying friends and its adapter.
     */
    private void setupRecyclerView() {
        adapter = new FriendAdapter(this::showInviteDialog);
        binding.friendsRecyclerView.setAdapter(adapter);
    }

    /**
     * Displays a dialog to invite a friend to a specific game room by entering a code.
     * @param friend The user object to invite.
     */
    private void showInviteDialog(User friend) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Invite " + friend.getDisplayName());
        builder.setMessage("Enter room code to invite them to play:");

        final EditText input = new EditText(this);
        input.setHint("Room Code");
        builder.setView(input);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String roomCode = input.getText().toString().trim();
            if (!roomCode.isEmpty()) {
                sendInvitation(friend, roomCode);
                // After sending, transition the host to the MainActivity which will open the room
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("roomCode", roomCode);
                intent.putExtra("action", "host");
                startActivity(intent);
            } else {
                Toast.makeText(this, "Room code cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Sends a game invitation record to the recipient's invitations node in Firebase.
     * This will trigger an FCM notification via the InvitationService.
     * @param friend   The recipient of the invitation.
     * @param roomCode The room code the recipient should join.
     */
    private void sendInvitation(User friend, String roomCode) {
        String currentUserId = firebaseHandler.getCurrentUserId();
        User currentUser = firebaseHandler.getUserData();
        
        if (currentUserId == null || currentUser == null) {
            Toast.makeText(this, "Error: You must be logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference invitationsRef = firebaseHandler.getRootRef()
                .child("invitations")
                .child(friend.getUid())
                .push();

        Map<String, Object> invitation = new HashMap<>();
        invitation.put("senderId", currentUserId);
        invitation.put("senderName", currentUser.getDisplayName());
        invitation.put("message", "Join my Boggle game!");
        invitation.put("roomCode", roomCode);
        invitation.put("timestamp", ServerValue.TIMESTAMP);

        invitationsRef.setValue(invitation)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Invitation sent to " + friend.getDisplayName(), Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to send invitation", Toast.LENGTH_SHORT).show());
    }

    /**
     * Sets up click listeners for the refresh and add friend UI elements.
     */
    private void setupClickListeners() {
        binding.refreshButton.setOnClickListener(v -> {
            loadUsers();
            loadFriends();
        });
        binding.addFriendButton.setOnClickListener(this::addFriend);
    }

    /**
     * Adds the currently searched user as a friend in the database.
     * @param view The clicked view.
     */
    private void addFriend(View view) {
        String friendId = binding.getSearchedUser().getUid();
        if (friendId.isEmpty()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }
        firebaseHandler.addFriend(friendId);
        binding.friendEmailInput.setText("");
        Toast.makeText(this, "Friend added!", Toast.LENGTH_SHORT).show();
        
        // Refresh local friend list
        loadFriends();
    }

    /**
     * Configures the search input field with a TextWatcher for live user filtering.
     */
    private void setupSearchInput() {
        binding.friendEmailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (usersSnapshot == null) return;
                
                String query = s.toString().toLowerCase();
                List<User> filteredList = new ArrayList<>();
                for (DataSnapshot userSnapshot : usersSnapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null && user.getEmail().toLowerCase().contains(query)) {
                        // Don't show current user in search results
                        if (!user.getUid().equals(firebaseHandler.getCurrentUserId())) {
                            filteredList.add(user);
                        }
                    }
                }
                filteredList.sort((u1, u2) -> u1.getDisplayName().compareToIgnoreCase(u2.getDisplayName()));
                // Update data binding for the searched user UI
                binding.setSearchedUser(filteredList.isEmpty() ? null : filteredList.get(0));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Loads the current user's friend list from Firebase.
     */
    private void loadFriends() {
        DatabaseReference userRef = firebaseHandler.getUserRef();
        if (userRef == null) return;

        DatabaseReference friendsRef = userRef.child("friends");
        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friendsList.clear();
                if (!snapshot.exists()) {
                    adapter.submitList(new ArrayList<>(friendsList));
                    return;
                }
                for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                    String friendId = friendSnapshot.getKey();
                    if (friendId != null) {
                        fetchFriendData(friendId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load friends", error.toException());
            }
        });
    }

    /**
     * Fetches details for a specific friend ID and updates the list.
     * @param friendId The UID of the friend to fetch.
     */
    private void fetchFriendData(String friendId) {
        firebaseHandler.getRootRef().child("users").child(friendId).get()
                .addOnSuccessListener(dataSnapshot -> {
                    User friend = dataSnapshot.getValue(User.class);
                    if (friend != null) {
                        // Avoid duplicates in the local list
                        boolean exists = false;
                        for (User u : friendsList) {
                            if (u.getUid().equals(friend.getUid())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            friendsList.add(friend);
                            adapter.submitList(new ArrayList<>(friendsList));
                        }
                    }
                });
    }
}
