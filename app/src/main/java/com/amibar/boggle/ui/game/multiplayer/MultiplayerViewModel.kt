package com.amibar.boggle.ui.game.multiplayer

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.amibar.boggle.data.FirebaseHandler
import com.amibar.boggle.data.PlayerRole
import com.amibar.boggle.data.User
import com.amibar.boggle.engine.BoggleGame
import com.amibar.boggle.views.BoggleViewModel
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

sealed class MultiplayerEvent {
    data class ResultsReady(val solutions: Map<String, String>, val playersWords: Map<User, List<String>>) : MultiplayerEvent()
    object GameDestroyed : MultiplayerEvent()
    data class ShowToast(val message: String) : MultiplayerEvent()
}

class MultiplayerViewModel : BoggleViewModel() {

    private val _events = Channel<MultiplayerEvent>()
    val events = _events.receiveAsFlow()

    private var roomCode: String? = null
    private var playerRole: PlayerRole? = null
    private var roomRef: DatabaseReference? = null

    private var boardListener: ValueEventListener? = null
    private var gameEndListener: ValueEventListener? = null
    private var gameDestroyedListener: ChildEventListener? = null

    fun initRoom(code: String, role: PlayerRole) {
        if (this.roomCode != null) return // Already initialized

        this.roomCode = code
        this.playerRole = role
        this.roomRef = FirebaseHandler.database.getReference("rooms").child(code)

        listenForGameEnd()
        listenForGameDestroyed()

        if (role == PlayerRole.Host) {
            val boardStr = String(game.board)
            roomRef?.child("board")?.setValue(boardStr)?.addOnFailureListener { e ->
                Log.e("MultiplayerVM", "Failed to upload board", e)
            }
            setupMultiplayerListeners()
        } else {
            boardListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val boardStr = snapshot.getValue(String::class.java)
                    if (!boardStr.isNullOrEmpty()) {
                        setBoard(boardStr.toCharArray())
                        setupMultiplayerListeners()
                        roomRef?.child("board")?.removeEventListener(this)
                        boardListener = null
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("MultiplayerVM", "Failed to download board: " + error.message)
                }
            }
            roomRef?.child("board")?.addValueEventListener(boardListener!!)
        }
    }

    private fun setBoard(board: CharArray) {
        // We need a way to update the game in BoggleViewModel.
        // For now, let's assume we'll update BoggleViewModel to support this.
        // I will add a `updateGame` method to BoggleViewModel.
        updateGame(BoggleGame(board))
    }

    private fun setupMultiplayerListeners() {
        val userId = FirebaseHandler.currentUserId ?: return

        game.addOnWordFoundListener { word ->
            roomRef?.child("players")?.child(userId)?.child("words")?.child(word)?.setValue(true)
        }

        game.addOnGameEndListener {
            if (playerRole == PlayerRole.Host) {
                if (roomRef?.parent == null) return@addOnGameEndListener
                roomRef?.child("gameEnded")?.setValue(true)
            }
        }
        
        startGame()
    }

    private fun listenForGameEnd() {
        gameEndListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val gameEnded = snapshot.getValue(Boolean::class.java) == true
                if (gameEnded) {
                    collectResultsAndFinish()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        roomRef?.child("gameEnded")?.addValueEventListener(gameEndListener!!)
    }

    private fun listenForGameDestroyed() {
        gameDestroyedListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {
                if (snapshot.key == roomCode) {
                    viewModelScope.launch {
                        _events.send(MultiplayerEvent.GameDestroyed)
                    }
                }
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        roomRef?.parent?.addChildEventListener(gameDestroyedListener!!)
    }

    private fun collectResultsAndFinish() {
        roomRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val playersWordsMap = HashMap<User, ArrayList<String>>()
                val playersSnapshot = snapshot.child("players")
                val playersCount = playersSnapshot.childrenCount
                if (playersCount == 0L) return

                val fetchedCount = AtomicInteger(0)

                for (playerSnap in playersSnapshot.children) {
                    val uid = playerSnap.key ?: continue
                    val words = ArrayList<String>()
                    for (wordSnap in playerSnap.child("words").children) {
                        words.add(wordSnap.key ?: continue)
                    }

                    FirebaseHandler.database.getReference("users").child(uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnap: DataSnapshot) {
                                val user = userSnap.getValue(User::class.java)
                                if (user != null) {
                                    playersWordsMap[user] = words
                                }
                                if (fetchedCount.incrementAndGet() == playersCount.toInt()) {
                                    finalizeResults(playersWordsMap)
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                if (fetchedCount.incrementAndGet() == playersCount.toInt()) {
                                    finalizeResults(playersWordsMap)
                                }
                            }
                        })
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("MultiplayerVM", "Failed to collect final results: " + error.message)
            }
        })
    }

    private fun finalizeResults(playersWordsMap: Map<User, ArrayList<String>>) {
        viewModelScope.launch {
            _events.send(MultiplayerEvent.ResultsReady(game.solutions.toMap(), playersWordsMap))
        }
    }

    override fun onCleared() {
        super.onCleared()
        boardListener?.let { roomRef?.child("board")?.removeEventListener(it) }
        gameEndListener?.let { roomRef?.child("gameEnded")?.removeEventListener(it) }
        gameDestroyedListener?.let { roomRef?.parent?.removeEventListener(it) }
    }
}
