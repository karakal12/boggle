package com.amibar.boggle.ui.game.multiplayer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amibar.boggle.data.PlayerRole
import com.google.firebase.database.FirebaseDatabase

@Composable
fun JoinOrCreateRoomDialog(
    onDismissRequest: () -> Unit,
    onJoinRoom: (PlayerRole, String) -> Unit,
    initialRoomCode: String? = null,
    initialPlayerRole: PlayerRole = PlayerRole.Guest,
    modifier: Modifier = Modifier
) {
    val roomCodeState = rememberTextFieldState(initialRoomCode ?: "")
    var errorText by remember { mutableStateOf<String?>(null) }
    var isRoomCodeVisible by remember { mutableStateOf(!initialRoomCode.isNullOrEmpty()) }

    fun handleJoin() {
        val roomCode = roomCodeState.text.toString()
        if (roomCode.isBlank()) return

        val roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomCode)
        roomRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result?.exists() == true) {
                onJoinRoom(PlayerRole.Guest, roomCode)
                onDismissRequest()
            } else {
                errorText = "Room not found"
            }
        }
    }

    fun handleCreate() {
        val roomCode = roomCodeState.text.toString()
        if (roomCode.isBlank()) return
        onJoinRoom(PlayerRole.Host, roomCode)
        onDismissRequest()
    }

    LaunchedEffect(initialRoomCode, initialPlayerRole) {
        if (!initialRoomCode.isNullOrEmpty()) {
            if (initialPlayerRole == PlayerRole.Host) {
                handleCreate()
            } else {
                handleJoin()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = { Text("Multiplayer") },
        text = {
            Column {
                if (!isRoomCodeVisible) {
                    Button(
                        onClick = { isRoomCodeVisible = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Join Room")
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { isRoomCodeVisible = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Create Room")
                    }
                } else {
                    TextField(
                        state = roomCodeState,
                        label = { Text("Enter room code") },
                        isError = errorText != null,
                        supportingText = errorText?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (isRoomCodeVisible) {
                Column {
                    Button(
                        onClick = { handleJoin() },
                        enabled = roomCodeState.text.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Join")
                    }
                    Button(
                        onClick = { handleCreate() },
                        enabled = roomCodeState.text.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Create")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
