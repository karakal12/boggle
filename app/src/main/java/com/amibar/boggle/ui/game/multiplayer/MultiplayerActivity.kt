package com.amibar.boggle.ui.game.multiplayer

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amibar.boggle.data.FirebaseHandler
import com.amibar.boggle.data.PlayerRole
import com.amibar.boggle.data.User
import com.amibar.boggle.databinding.ActivityMultiplayerBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

/**
 * Activity that hosts the multiplayer game experience.
 * It manages the transition between the lobby and the active game, handles window insets,
 * and ensures proper cleanup of the room in Firebase when the activity is destroyed.
 */
class MultiplayerActivity : AppCompatActivity() {
    /** View binding for the activity layout.  */
    private lateinit var binding: ActivityMultiplayerBinding

    /** The code of the current multiplayer room.  */
    private lateinit var roomCode: String

    /** The role of the local player in this session.  */
    private lateinit var playerRole: PlayerRole

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge display
        this.enableEdgeToEdge()

        binding = ActivityMultiplayerBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())

        // Adjust layout for system bars
        ViewCompat.setOnApplyWindowInsetsListener(
            binding.main
        ) { v: View?, insets: WindowInsetsCompat? ->
            val systemBars = insets!!.getInsets(WindowInsetsCompat.Type.systemBars())
            v!!.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Retrieve room details from the starting Intent
        if (intent != null) {
            playerRole = intent.getSerializableExtra(
                ARG_PLAYER_ROLE,
                PlayerRole::class.java
            ) ?: PlayerRole.Guest
            roomCode = intent.getStringExtra(ARG_ROOM_CODE) ?: return
        }

        // Initialize by showing the LobbyFragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(
                    binding!!.main.id,
                    LobbyFragment.newInstance(roomCode, playerRole),
                    LobbyFragment.TAG
                )
                .commit()
        }
    }

    /**
     * Replaces the current fragment with MultiplayerGameFragment to start the active game.
     */
    fun startGame() {
        supportFragmentManager.beginTransaction()
            .replace(
                binding!!.main.id,
                MultiplayerGameFragment.newInstance(playerRole, roomCode),
                MultiplayerGameFragment.TAG
            )
            .commit()
    }

    /**
     * Displays a dialog showing the final words and scores of all players.
     * @param solutions Map of all possible words and their paths.
     * @param playersWords Map of each user to the list of words they found.
     */
    fun showGameResults(
        solutions: Map<String, String>,
        playersWords: Map<User, List<String>>
    ) {
        val fragment: MultiplayerOnGameEndFragment =
            MultiplayerOnGameEndFragment.newInstance(solutions, playersWords)
        fragment.show(supportFragmentManager, MultiplayerOnGameEndFragment.TAG)
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
