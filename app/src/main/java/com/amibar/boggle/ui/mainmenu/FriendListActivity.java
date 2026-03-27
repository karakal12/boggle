package com.amibar.boggle.ui.mainmenu;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.amibar.boggle.R;
import com.amibar.boggle.data.FirebaseHandler;
import com.amibar.boggle.data.User;
import com.amibar.boggle.databinding.ActivityFriendlistBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FriendListActivity extends AppCompatActivity {

    private static final String TAG = "FriendListActivity";
    private ActivityFriendlistBinding binding;
    private FriendAdapter adapter;
    private final List<User> friendsList = new ArrayList<>();
    private FirebaseHandler firebaseHandler;

    private DataSnapshot users;

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

    private void loadUsers() {
        firebaseHandler.getRootRef().child("users").get().addOnSuccessListener(dataSnapshot -> users = dataSnapshot);
        loadFriends();
    }

    private void setupRecyclerView() {
        adapter = new FriendAdapter(friend -> {
            Toast.makeText(this, "Inviting " + friend.getDisplayName(), Toast.LENGTH_SHORT).show();
            // TODO: Implement invitation logic
        });
        binding.friendsRecyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.refreshButton.setOnClickListener(v -> {
            loadFriends();
            loadUsers();
        });
    }

    private void setupSearchInput() {
        binding.friendEmailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
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

    private void fetchFriendData(String friendId) {
        firebaseHandler.getRootRef().child("users").child(friendId).get()
                .addOnSuccessListener(dataSnapshot -> {
                    User friend = dataSnapshot.getValue(User.class);
                    if (friend != null) {
                        boolean exists = false;
                        for (User u : friendsList) {
                            if (u.getEmail().equals(friend.getEmail())) {
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