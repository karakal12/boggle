package com.amibar.boggle.ui.game.multiplayer

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.amibar.boggle.data.FirebaseHandler
import com.amibar.boggle.data.PlayerRole
import com.amibar.boggle.data.User
import com.amibar.boggle.engine.BoggleGame
import com.amibar.boggle.ui.shared.BoggleViewModel
import com.amibar.boggle.utils.awaitValue
import com.amibar.boggle.utils.childRemovedFlow
import com.amibar.boggle.utils.valueFlow
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Represents the various one-time events or navigation triggers that can occur
 * during a multiplayer game session.
 */
sealed interface MultiplayerEvent {
    /**
     * Event triggered when the multiplayer game ends and all player results have been collected.
     *
     * @property solutions A map of all possible valid words on the board and their definitions/metadata.
     * @property playersWords A map associating each [User] with the list of words they successfully found during the game.
     */
    data class ResultsReady(val solutions: Map<String, String>, val playersWords: Map<User, List<String>>) : MultiplayerEvent
    /**
     * Event triggered when the multiplayer game room is deleted or disbanded from the database.
     */
    object GameDestroyed : MultiplayerEvent
    /**
     * Event triggered when the game is started by the host.
     */
    object GameStarted : MultiplayerEvent
    /**
     * Event triggered to request the UI to display a temporary toast message.
     *
     * @property message The text content to be displayed in the toast.
     */
    data class ShowToast(val message: String) : MultiplayerEvent
}

 /**
 * ViewModel responsible for managing the state and logic of a multiplayer Boggle session.
 *
 * It handles real-time synchronization with Firebase, including lobby management,
 * board distribution, tracking words found by players, and coordinating the end-of-game result collection.
 *
 * Composables observing this ViewModel should listen to the [events] flow
 * to handle navigation to results, game termination, or UI notifications.
 */
class MultiplayerViewModel(playerRole: PlayerRole, roomCode: String) : BoggleViewModel() {
    private val _events = Channel<MultiplayerEvent>()
    val events = _events.receiveAsFlow()

    private lateinit var _roomCode: String
    private lateinit var _playerRole: PlayerRole
    private lateinit var _roomRef: DatabaseReference

    val roomCode: String get() = _roomCode
    val playerRole: PlayerRole get() = _playerRole

    private val _players = MutableStateFlow<List<User>>(emptyList())
    val players: StateFlow<List<User>> = _players.asStateFlow()

    init {
        initRoom(roomCode, playerRole)
    }

    fun initRoom(code: String, role: PlayerRole) {
        if (::_roomCode.isInitialized) return // Already initialized

        this._roomCode = code
        this._playerRole = role
        this._roomRef = FirebaseHandler.database.getReference("rooms").child(code)

        joinRoom()
        listenForPlayers()
        listenForGameStarted()
        listenForGameEnd()
        listenForGameDestroyed()
    }

    private fun joinRoom() {
        val userId = FirebaseHandler.currentUserId ?: return
        val myPlayerRef = _roomRef.child("players").child(userId)
        // Instead of putting all player data, only put a joined flag
        myPlayerRef.child("joined").setValue(true)
        // Ensure the player is removed from the room list if they disconnect or close the app
        myPlayerRef.onDisconnect().removeValue()
    }

    private fun listenForPlayers() {
        viewModelScope.launch {
            _roomRef.child("players").valueFlow().collect { snapshot ->
                val userList = mutableListOf<User>()
                coroutineScope {
                    snapshot.children.map { playerSnap ->
                        async {
                            val uid = playerSnap.key ?: return@async null
                            val userSnap = FirebaseHandler.database.getReference("users").child(uid).awaitValue()
                            userSnap.getValue(User::class.java)
                        }
                    }.awaitAll().filterNotNullTo(userList)
                }
                _players.value = userList
            }
        }
    }

    private fun listenForGameStarted() {
        viewModelScope.launch {
            _roomRef.child("gameStarted").valueFlow().collect { snapshot ->
                if (snapshot.getValue(Boolean::class.java) == true) {
                    onGameStarted()
                }
            }
        }
    }

    fun hostStartGame() {
        if (_playerRole != PlayerRole.Host) return
        
        // Upload board
        val boardStr = String(game.board)
        _roomRef.child("board").setValue(boardStr).addOnFailureListener { e ->
            Log.e("MultiplayerVM", "Failed to upload board", e)
        }
        // Set gameStarted flag
        _roomRef.child("gameStarted").setValue(true)
    }

    private fun onGameStarted() {
        viewModelScope.launch {
            if (_playerRole == PlayerRole.Guest) {
                try {
                    val snapshot = _roomRef.child("board").valueFlow()
                        .filter { it.getValue(String::class.java)?.isNotEmpty() == true }
                        .first()
                    
                    val boardStr = snapshot.getValue(String::class.java)!!
                    setBoard(boardStr.toCharArray())
                } catch (e: Exception) {
                    Log.e("MultiplayerVM", "Failed to download board", e)
                    return@launch
                }
            }
            setupMultiplayerListeners()
            _events.send(MultiplayerEvent.GameStarted)
        }
    }

    private fun setBoard(board: CharArray) {
        updateGame(BoggleGame(board))
    }

    private fun setupMultiplayerListeners() {
        val userId = FirebaseHandler.currentUserId ?: return
        val ref = _roomRef

        game.addOnWordFoundListener { word ->
            ref.child("players").child(userId).child("words").child(word).setValue(true)
        }

        game.addOnGameEndListener {
            if (_playerRole == PlayerRole.Host) {
                ref.child("gameEnded").setValue(true)
            }
        }
        
        startGame()
    }

    private fun listenForGameEnd() {
        viewModelScope.launch {
            _roomRef.child("gameEnded").valueFlow().collect { snapshot ->
                if (snapshot.getValue(Boolean::class.java) == true) {
                    collectResultsAndFinish()
                }
            }
        }
    }

    private fun listenForGameDestroyed() {
        val code = _roomCode
        viewModelScope.launch {
            _roomRef.parent?.childRemovedFlow()?.collect { snapshot ->
                if (snapshot.key == code) {
                    _events.send(MultiplayerEvent.GameDestroyed)
                }
            }
        }
    }

    private fun collectResultsAndFinish() {
        viewModelScope.launch {
            try {
                val snapshot = _roomRef.awaitValue()
                val playersSnapshot = snapshot.child("players")
                if (playersSnapshot.childrenCount == 0L) return@launch

                val playersWordsMap = mutableMapOf<User, ArrayList<String>>()

                coroutineScope {
                    playersSnapshot.children.map { playerSnap ->
                        async {
                            val uid = playerSnap.key ?: return@async
                            val words = playerSnap.child("words").children.mapNotNullTo(ArrayList()) { it.key }

                            val userSnap = FirebaseHandler.database.getReference("users").child(uid).awaitValue()
                            userSnap.getValue(User::class.java)?.let { user ->
                                synchronized(playersWordsMap) {
                                    playersWordsMap[user] = words
                                }
                            }
                        }
                    }.awaitAll()
                }

                finalizeResults(playersWordsMap)
            } catch (e: Exception) {
                Log.e("MultiplayerVM", "Failed to collect final results", e)
            }
        }
    }

    private fun finalizeResults(playersWordsMap: Map<User, ArrayList<String>>) {
        viewModelScope.launch {
            _events.send(MultiplayerEvent.ResultsReady(game.solutions.toMap(), playersWordsMap))
        }
    }

    override fun onCleared() {
        super.onCleared()
        val userId = FirebaseHandler.currentUserId ?: return
        if (::_roomRef.isInitialized) {
            _roomRef.child("players").child(userId).removeValue().addOnCompleteListener {
                _roomRef.child("players").get().addOnSuccessListener { snapshot ->
                    if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                        _roomRef.removeValue()
                    }
                }
            }
        }
    }
}
