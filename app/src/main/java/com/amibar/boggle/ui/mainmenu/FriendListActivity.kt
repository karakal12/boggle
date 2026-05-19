package com.amibar.boggle.ui.mainmenu

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.amibar.boggle.R
import com.amibar.boggle.data.User
import com.amibar.boggle.databinding.ActivityFriendlistBinding
import com.amibar.boggle.ui.game.multiplayer.Player
import com.amibar.boggle.ui.shared.SampleData
import com.amibar.boggle.ui.theme.BoggleTheme
import kotlinx.coroutines.launch

/**
 * Activity for managing and viewing a user's friend list.
 * Allows users to search for others by email, add friends, and invite them to game rooms.
 * Uses Firebase Realtime Database for all persistence.
 */
class FriendListActivity : AppCompatActivity() {
    private val viewModel: FriendListViewModel by viewModels()

    /** View binding for the activity.  */
    private var binding: ActivityFriendlistBinding? = null

    /** Adapter for the friends list RecyclerView.  */
    private var adapter: FriendAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_friendlist
        )
        binding?.lifecycleOwner = this

        setupRecyclerView()
        setupClickListeners()
        setupSearchInput()
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.friends.collect { friends ->
                        adapter?.submitList(friends)
                    }
                }
                launch {
                    viewModel.searchedUser.collect { user ->
                        binding?.searchedUser = user
                    }
                }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is FriendListEvent.ShowToast -> Toast.makeText(
                                this@FriendListActivity,
                                event.message,
                                Toast.LENGTH_SHORT
                            ).show()

                            is FriendListEvent.NavigateToHostGame -> {
                                val intent = Intent(this@FriendListActivity, MainActivity::class.java)
                                intent.putExtra("roomCode", event.roomCode)
                                intent.putExtra("action", "host")
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
        }
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
            val roomCode = input.text.toString().trim()
            if (roomCode.isNotEmpty()) {
                viewModel.sendInvitation(friend, roomCode)
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
     * Sets up click listeners for the refresh and add friend UI elements.
     */
    private fun setupClickListeners() {
        binding!!.refreshButton.setOnClickListener {
            viewModel.loadData()
        }
        binding!!.addFriendButton.setOnClickListener {
            viewModel.addFriend()
            binding!!.friendEmailInput.setText("")
        }
    }

    /**
     * Configures the search input field with a TextWatcher for live user filtering.
     */
    private fun setupSearchInput() {
        binding!!.friendEmailInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.onSearchQueryChanged(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    companion object {
        /** Tag used for logging.  */
        private const val TAG = "FriendListActivity"
    }
}

@Composable
fun FriendListScreen(
    modifier: Modifier = Modifier,
    viewModel: FriendListViewModel,
    onInviteFriend: (User) -> Unit = {}
) {
    val searchedUser by viewModel.searchedUser.collectAsState()
    val friends by viewModel.friends.collectAsState()

    FriendListScreenContent(
        searchedUser = searchedUser,
        friends = friends,
        searchQueryState = viewModel.searchQueryState,
        onAddFriend = viewModel::addFriend,
        onRefresh = viewModel::loadData,
        onInviteFriend = onInviteFriend,
        modifier = modifier
    )
}

@Composable
fun FriendListScreenContent(
    searchedUser: User?,
    friends: List<User>,
    searchQueryState: TextFieldState,
    onAddFriend: () -> Unit,
    onRefresh: () -> Unit,
    onInviteFriend: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row {
            TextField(
                state = searchQueryState,
                label = { Text("Email") }
            )
            Button(
                onClick = onAddFriend
            ) {
                Text("Add")
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            searchedUser?.let { user ->
                Player(player = user)
            }
            Button(
                onClick = onRefresh
            ) {
                Text("Refresh")
            }
        }

        LazyColumn {
            items(friends) { friend ->
                Friend(friend = friend, onClickInvite = { onInviteFriend(friend) })
            }
        }
    }
}

// TODO: fix
@Composable
fun Friend(friend: User, modifier: Modifier = Modifier, onClickInvite: () -> Unit = {}) {
    Row(
        modifier = modifier
    ) {
        Player(player = friend)
        Button(
            onClick = onClickInvite
        ) {
            Text("invite")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FriendListScreenPreview() {
    BoggleTheme {
        FriendListScreenContent(
            searchedUser = SampleData.player2,
            friends = listOf(SampleData.player1, SampleData.player2),
            searchQueryState = remember { TextFieldState("test@example.com") },
            onAddFriend = {},
            onRefresh = {},
            onInviteFriend = {}
        )
    }
}
