package com.amibar.boggle.ui.mainmenu

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.amibar.boggle.R
import com.amibar.boggle.data.User
import com.amibar.boggle.ui.game.multiplayer.Player
import com.amibar.boggle.ui.theme.BoggleTheme
import kotlinx.coroutines.launch

/**
 * Activity for managing and viewing a user's friend list.
 * Allows users to search for others by email, add friends, and invite them to game rooms.
 * Uses Firebase Realtime Database for all persistence.
 */
class FriendListActivity : AppCompatActivity() {
    private val viewModel: FriendListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FriendListScreen(
                viewModel = viewModel,
                onInviteFriend = {

                }
            )
        }
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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

}

@Composable
fun FriendListScreen(
    modifier: Modifier = Modifier,
    viewModel: FriendListViewModel,
    onInviteFriend: (User) -> Unit = {}
) {
    val searchedUser by viewModel.searchedUser.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val friendToInvite = remember { mutableStateOf<User?>(null) }

    FriendListScreenContent(
        searchedUser = searchedUser,
        friends = friends,
        searchQueryState = viewModel.searchQueryState,
        onAddFriend = viewModel::addFriend,
        onRefresh = viewModel::loadData,
        onInviteFriend = {
            friendToInvite.value = it
            onInviteFriend(it)
        },
        modifier = modifier
    )

    friendToInvite.value?.let { friend ->
        InviteDialog(
            friend = friend,
            onDismissRequest = { friendToInvite.value = null },
            onInviteSent = { user, roomCode ->
                viewModel.sendInvitation(user, roomCode)
                friendToInvite.value = null
            }
        )
    }
}

@Composable
fun FriendListScreenContent(
    friends: List<User>,
    onAddFriend: () -> Unit,
    onRefresh: () -> Unit,
    onInviteFriend: (User) -> Unit,
    modifier: Modifier = Modifier,
    searchedUser: User? = null,
    searchQueryState: TextFieldState = rememberTextFieldState(),
) {
    Column(
        modifier = modifier
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                state = searchQueryState,
                label = { Text("Email") },
                modifier = Modifier.weight(1f)
            )
            Button(
                modifier = Modifier.padding( start = 8.dp ),
                onClick = onAddFriend
            ) {
                Text("Add")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth()
                .heightIn( max = 32.dp ),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            searchedUser?.let { user ->
                Player(
                    player = user,
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
            TextButton(
                onClick = onRefresh,
                modifier = Modifier.padding( start = 8.dp )
            ) {
                Text("Refresh")
            }
        }

        LazyColumn {
            items(friends) { friend ->
                Friend(
                    friend = friend,
                    onClickInvite = { onInviteFriend(friend) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun Friend(friend: User, modifier: Modifier = Modifier, onClickInvite: () -> Unit = {}) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Player(
            player = friend,
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = onClickInvite
        ) {
            Text("invite")
        }
    }
}

/**
 * Displays a composable dialog that allows the user to invite a friend to a game.
 * Includes a text field for entering a room code and actions to send the invitation or cancel.
 *
 * @param modifier The [Modifier] to be applied to the dialog.
 * @param friend The [User] being invited.
 * @param onDismissRequest Callback invoked when the user attempts to dismiss the dialog.
 * @param onInviteSent Callback invoked with the [User] object when the "Send" button is clicked.
 */
@Composable
private fun InviteDialog(
    modifier: Modifier = Modifier,
    friend: User,
    onDismissRequest: () -> Unit = {},
    onInviteSent: (User, String) -> Unit = { _, _ -> }
) {
    val roomCodeState = rememberTextFieldState()
    androidx.compose.material3.AlertDialog(
        modifier = modifier,
        title = {
            Text(text = "Invite ${friend.displayName}?")
        },
        text = {
            TextField(
                state = roomCodeState,
                lineLimits = TextFieldLineLimits.SingleLine,
                label = {
                    Text("Room Code")
                }
            )
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(
                onClick = { onInviteSent(friend, roomCodeState.text.toString()) }
            ) {
                Text("Send")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(
                    text = "Cancel"
                )
            }
        }
    )
}
