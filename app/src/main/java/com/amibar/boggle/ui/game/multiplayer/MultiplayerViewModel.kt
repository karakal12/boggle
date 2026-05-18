package com.amibar.boggle.ui.game.multiplayer

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.amibar.boggle.data.FirebaseHandler
import com.amibar.boggle.data.PlayerRole
import com.amibar.boggle.data.User
import com.amibar.boggle.engine.BoggleGame
import com.amibar.boggle.utils.awaitValue
import com.amibar.boggle.utils.childRemovedFlow
import com.amibar.boggle.utils.valueFlow
import com.amibar.boggle.views.BoggleViewModel
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Represents the various one-time events or navigation triggers that can occur
 * during a multiplayer game session.
 */
sealed class MultiplayerEvent {
    /**
     * Event triggered when the multiplayer game ends and all player results have been collected.
     *
     * @property solutions A map of all possible valid words on the board and their definitions/metadata.
     * @property playersWords A map associating each [User] with the list of words they successfully found during the game.
     */
    data class ResultsReady(val solutions: Map<String, String>, val playersWords: Map<User, List<String>>) : MultiplayerEvent()
    /**
     * Event triggered when the multiplayer game room is deleted or disbanded from the database.
     */
    object GameDestroyed : MultiplayerEvent()
    /**
     * Event triggered to request the UI to display a temporary toast message.
     *
     * @property message The text content to be displayed in the toast.
     */
    data class ShowToast(val message: String) : MultiplayerEvent()
}

/**
 * ViewModel responsible for managing the state and logic of a multiplayer Boggle session.
 *
 * It handles real-time synchronization with Firebase, including board distribution,
 * tracking words found by players, and coordinating the end-of-game result collection.
 *
 * Activities or Fragments observing this ViewModel should listen to the [events] flow
 * to handle navigation to results, game termination, or UI notifications.
 */
class MultiplayerViewModel : BoggleViewModel() {

    private val _events = Channel<MultiplayerEvent>()
    val events = _events.receiveAsFlow()

    private var roomCode: String? = null
    private var playerRole: PlayerRole? = null
    private var roomRef: DatabaseReference? = null

    fun initRoom(code: String, role: PlayerRole) {
        if (this.roomCode != null) return // Already initialized

        this.roomCode = code
        this.playerRole = role
        val ref = FirebaseHandler.database.getReference("rooms").child(code)
        this.roomRef = ref

        listenForGameEnd()
        listenForGameDestroyed()

        if (role == PlayerRole.Host) {
            val boardStr = String(game.board)
            ref.child("board").setValue(boardStr).addOnFailureListener { e ->
                Log.e("MultiplayerVM", "Failed to upload board", e)
            }
            setupMultiplayerListeners()
        } else {
            viewModelScope.launch {
                try {
                    val snapshot = ref.child("board").valueFlow()
                        .filter { it.getValue(String::class.java)?.isNotEmpty() == true }
                        .first()
                    
                    val boardStr = snapshot.getValue(String::class.java)!!
                    setBoard(boardStr.toCharArray())
                    setupMultiplayerListeners()
                } catch (e: Exception) {
                    Log.e("MultiplayerVM", "Failed to download board", e)
                }
            }
        }
    }

    private fun setBoard(board: CharArray) {
        updateGame(BoggleGame(board))
    }

    private fun setupMultiplayerListeners() {
        val userId = FirebaseHandler.currentUserId ?: return
        val ref = roomRef ?: return

        game.addOnWordFoundListener { word ->
            ref.child("players").child(userId).child("words").child(word).setValue(true)
        }

        game.addOnGameEndListener {
            if (playerRole == PlayerRole.Host) {
                ref.child("gameEnded").setValue(true)
            }
        }
        
        startGame()
    }

    private fun listenForGameEnd() {
        viewModelScope.launch {
            roomRef?.child("gameEnded")?.valueFlow()?.collect { snapshot ->
                if (snapshot.getValue(Boolean::class.java) == true) {
                    collectResultsAndFinish()
                }
            }
        }
    }

    private fun listenForGameDestroyed() {
        val code = roomCode ?: return
        viewModelScope.launch {
            roomRef?.parent?.childRemovedFlow()?.collect { snapshot ->
                if (snapshot.key == code) {
                    _events.send(MultiplayerEvent.GameDestroyed)
                }
            }
        }
    }

    private fun collectResultsAndFinish() {
        viewModelScope.launch {
            try {
                val snapshot = roomRef?.awaitValue() ?: return@launch
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
}
