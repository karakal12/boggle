package com.amibar.boggle.ui.game.multiplayer

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.amibar.boggle.data.PlayerRole
import com.amibar.boggle.data.User
import com.amibar.boggle.ui.theme.BoggleTheme


/**
 * Composable that displays the lobby UI, including the room code, the list of joined players,
 * and a start button for the host.
 *
 * @param viewModel The [MultiplayerViewModel] providing the state and actions for the lobby.
 * @param modifier The [Modifier] to be applied to the layout.
 */
@Composable
fun LobbyScreen(viewModel: MultiplayerViewModel, modifier: Modifier = Modifier) {
    val players by viewModel.players.collectAsState()
    LobbyScreen(
        roomCode = viewModel.roomCode,
        playerList = players,
        playerRole = viewModel.playerRole,
        modifier = modifier,
        onStart = viewModel::hostStartGame
    )
}

/**
 * Composable that renders the lobby UI using explicit data parameters.
 *
 * This version of the LobbyScreen is stateless and pure, making it suitable for
 * previews and decoupling the UI from the [MultiplayerViewModel].
 *
 * @param roomCode The unique identifier for the game room to be displayed.
 * @param playerList The list of [User] objects currently present in the lobby.
 * @param playerRole The [PlayerRole] of the local user, determining if administrative
 * controls (like the start button) are visible.
 * @param modifier The [Modifier] to be applied to the layout.
 * @param onStart A callback invoked when the host clicks the start game button.
 */
@Composable
fun LobbyScreen(
    roomCode: String,
    playerList: List<User>,
    playerRole: PlayerRole,
    modifier: Modifier = Modifier,
    onStart: () -> Unit = {}
) {
    Column(
        modifier = modifier.safeDrawingPadding().fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (playerRole == PlayerRole.Host) {
            Button(
                onClick = onStart,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("start")
            }
        }
        Text(
            text = "Room Code: $roomCode",
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 64.sp
        )
        LazyColumn {
            items(playerList) { player ->
                Player(player = player)
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LobbyScreenPreview() {
    BoggleTheme {
        Surface (
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            LobbyScreen(
                roomCode = "******",
                playerList = List(10) { i ->
                    User(displayName = "John Doe $i")
                },
                playerRole = PlayerRole.Host
            )
        }
    }
}
