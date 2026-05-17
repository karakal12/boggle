package com.amibar.boggle.ui.game.multiplayer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.amibar.boggle.data.FirebaseHandler
import com.amibar.boggle.data.PlayerRole
import com.amibar.boggle.data.User
import com.amibar.boggle.databinding.FragmentLobbyBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.lang.Boolean
import kotlin.String

/**
 * Fragment that displays the multiplayer lobby.
 * It shows the list of players currently in the room and allows the host to start the game.
 * It listens for changes in the Firebase room data to update the player list and detect game start.
 */
class LobbyFragment : Fragment() {
    /** View binding for fragment layout.  */
    private var binding: FragmentLobbyBinding? = null

    /** Adapter for the player list RecyclerView.  */
    private var playerAdapter: PlayerAdapter? = null

    /** Local list of users currently in the lobby.  */
    private val playerList: MutableList<User?> = ArrayList()

    /** The unique code for the current game room.  */
    private var roomCode: String? = null

    /** The role of the local player (HOST or GUEST).  */
    private var playerRole: PlayerRole? = null

    /** The local player's user data.  */
    private var player: User? = null

    /** Reference to the room's node in Firebase Realtime Database.  */
    private var roomRef: DatabaseReference? = null

    /** Listener for player list and game start updates in Firebase.  */
    private var playerListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLobbyBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the RecyclerView and adapter for displaying players
        binding!!.playerList.setLayoutManager(LinearLayoutManager(requireContext()))
        playerAdapter = PlayerAdapter(playerList)
        binding!!.playerList.setAdapter(playerAdapter)

        // Extract room code and player role from arguments
        val args = arguments
        if (args != null) {
            roomCode = args.getString(MultiplayerActivity.ARG_ROOM_CODE)
            binding!!.setRoomCode(roomCode)
            val roleStr = args.getString(MultiplayerActivity.ARG_PLAYER_ROLE)
            if (roleStr != null) {
                playerRole = PlayerRole.valueOf(roleStr)
            }
        }


        // Retrieve local player data from FirebaseHandler
        player = FirebaseHandler.userData

        // Only the host can see and click the "Start Game" button
        if (playerRole == PlayerRole.Host) {
            binding!!.startButton.visibility = View.VISIBLE
            binding!!.startButton.setOnClickListener { view: View? ->
                this.startGame(
                    view
                )
            }
        } else {
            binding!!.startButton.visibility = View.GONE
        }

        // Connect to Firebase and register as a player in this room
        if (roomCode != null) {
            roomRef =
                FirebaseHandler.rootRef.child("rooms").child(roomCode!!)
            listenForPlayers()

            val userId: String? = FirebaseHandler.currentUserId
            if (userId != null) {
                val myPlayerRef = roomRef!!.child("players").child(userId)
                // Instead of putting all player data, only put a joined flag
                myPlayerRef.child("joined").setValue(true)
                // Ensure the player is removed from the room list if they disconnect or close the app
                myPlayerRef.onDisconnect().removeValue()
            }
        }
    }

    /**
     * Attaches a listener to the Firebase room reference.
     * Updates the player list when players join/leave and navigates to the game when started.
     */
    private fun listenForPlayers() {
        playerListener = object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return

                playerList.clear()
                val playersSnapshot = snapshot.child("players")
                for (playerSnapshot in playersSnapshot.getChildren()) {
                    val uid = playerSnapshot.key
                    if (uid != null) {
                        // Fetch the full User data from the central 'users' node
                        FirebaseHandler.database.getReference("users").child(uid)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnap: DataSnapshot) {
                                    val user = userSnap.getValue(User::class.java)
                                    if (user != null && isAdded) {
                                        if (!playerList.contains(user)) {
                                            playerList.add(user)
                                            playerAdapter!!.notifyDataSetChanged()
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                    }
                }


                // If the host has marked the game as started, transition to the game fragment
                val gameStarted =
                    snapshot.child("gameStarted").getValue<Boolean?>(Boolean::class.java)
                if (Boolean.TRUE == gameStarted) {
                    (requireActivity() as MultiplayerActivity).startGame()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Potential error handling
            }
        }
        roomRef!!.addValueEventListener(playerListener!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove the Firebase listener when the view is destroyed to avoid memory leaks
        if (roomRef != null && playerListener != null) {
            roomRef!!.removeEventListener(playerListener!!)
        }
        binding = null
    }

    /**
     * Sets the 'gameStarted' flag to true in Firebase.
     * This is only callable by the host.
     * @param view The clicked view.
     */
    private fun startGame(view: View?) {
        if (playerRole == PlayerRole.Host && roomRef != null) {
            roomRef!!.child("gameStarted").setValue(true)
        }
    }

    companion object {
        /** Tag used for identifying this fragment.  */
        const val TAG: String = "LobbyFragment"

        /**
         * Creates a new instance of LobbyFragment.
         * @param roomCode The code of the room to join.
         * @param playerRole The role of the player.
         * @return A new instance.
         */
        fun newInstance(roomCode: String?, playerRole: PlayerRole): LobbyFragment {
            val fragment = LobbyFragment()
            val args = Bundle()
            args.putString(MultiplayerActivity.ARG_ROOM_CODE, roomCode)
            args.putString(MultiplayerActivity.ARG_PLAYER_ROLE, playerRole.name)
            fragment.setArguments(args)
            return fragment
        }
    }
}
