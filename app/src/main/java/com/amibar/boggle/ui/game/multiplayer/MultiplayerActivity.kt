package com.amibar.boggle.ui.game.multiplayer

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.amibar.boggle.data.FirebaseHandler
import com.amibar.boggle.data.PlayerRole
import com.amibar.boggle.ui.theme.BoggleTheme
import com.amibar.boggle.views.BoggleBoard
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

/**
 * Activity that hosts the multiplayer game experience.
 * It manages the transition between the lobby and the active game, handles window insets,
 * and ensures proper cleanup of the room in Firebase when the activity is destroyed.
 */
class MultiplayerActivity : AppCompatActivity() {

    /** The code of the current multiplayer room.  */
    private lateinit var roomCode: String

    /** The role of the local player in this session.  */
    private lateinit var playerRole: PlayerRole

    private val viewModel: MultiplayerViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MultiplayerViewModel(playerRole, roomCode) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge display
        this.enableEdgeToEdge()

        // Retrieve room details from the starting Intent
        if (intent != null) {
            playerRole = intent.getSerializableExtra(
                ARG_PLAYER_ROLE,
                PlayerRole::class.java
            ) ?: PlayerRole.Guest
            roomCode = intent.getStringExtra(ARG_ROOM_CODE) ?: return
        }
        
        setContent { 
            BoggleTheme {
                Surface(
                    color = MaterialTheme.colorScheme.surface
                ) { 
                    LobbyScreen(viewModel = viewModel)
                }
            }
        }

        // Observe events from the ViewModel
        lifecycleScope.launch {
            viewModel.events.collect { event ->
                when (event) {
                    is MultiplayerEvent.GameStarted -> startGame()
                    is MultiplayerEvent.GameDestroyed -> finish()
                    else -> {} // Handle other events if necessary
                }
            }
        }
    }

    /**
     * Replaces the current screen with BoggleBoard to start the active game.
     */
    fun startGame() {
        setContent {
            BoggleTheme {
                Surface (
                    color = MaterialTheme.colorScheme.surface
                ) {
                    BoggleBoard(viewModel = viewModel)
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Cleanup: remove the player from the room or delete the room if empty
        // Only perform cleanup if the activity is actually finishing (not just rotating)
        if (isFinishing) {
            val userId: String? = FirebaseHandler.currentUserId
            if (userId != null) {
                val roomRef: DatabaseReference =
                    FirebaseHandler.database.getReference("rooms").child(roomCode)
                // Remove local player from the Firebase list
                roomRef.child("players").child(userId).removeValue()
                    .addOnCompleteListener { task: Task<Void?>? ->
                        // Check if any players remain; if not, remove the entire room node
                        roomRef.child("players")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (!snapshot.exists() || snapshot.getChildrenCount() == 0L) {
                                        // Housekeeping: remove empty room node
                                        roomRef.removeValue()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                    }
            }
        }
    }

    companion object {
        /** Tag used for logging.  */
        const val TAG: String = "MultiplayerActivity"

        /** Intent extra key for the room code.  */
        const val ARG_ROOM_CODE: String = "room_code"

        /** Intent extra key for the player's role (HOST or GUEST).  */
        const val ARG_PLAYER_ROLE: String = "player_role"
    }
}
