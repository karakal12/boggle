package com.amibar.boggle.ui.mainmenu

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
import com.amibar.boggle.data.User
import com.amibar.boggle.ui.game.multiplayer.Player

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
