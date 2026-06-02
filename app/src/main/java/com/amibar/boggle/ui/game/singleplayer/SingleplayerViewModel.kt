package com.amibar.boggle.ui.game.singleplayer

import androidx.lifecycle.viewModelScope
import com.amibar.boggle.data.FirebaseHandler
import com.amibar.boggle.data.GameResult
import com.amibar.boggle.engine.BoggleGame
import com.amibar.boggle.ui.shared.BoggleViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class SingleplayerEvent {
    data class GameEnded(val score: Int) : SingleplayerEvent()
    object ActivateDonutSecret : SingleplayerEvent()
    data class ShowToast(val message: String) : SingleplayerEvent()
}

class SingleplayerViewModel(initialGame: BoggleGame = BoggleGame()) : BoggleViewModel(initialGame) {
    private val _events = Channel<SingleplayerEvent>()
    val events = _events.receiveAsFlow()

    val solutions: Map<String, String> get() = game.solutions.toMap()

    init {
        setupGameListener()
    }

    private fun setupGameListener() {
        game.addOnGameEndListener {
            _uiState.update { it.copy(isGameEnded = true) }
            uploadGameResults()
            viewModelScope.launch {
                _events.send(SingleplayerEvent.GameEnded(game.score))
            }
        }
    }

    override fun submitWord() {
        super.submitWord()

        if (_uiState.value.lastSubmittedWord.equals("donut", ignoreCase = true)) {
            viewModelScope.launch {
                _events.send(SingleplayerEvent.ActivateDonutSecret)
            }
        }

        _uiState.update {
            it.copy(
                score = game.score,
                currentWord = "",
                selectedIndices = emptyList(),
                foundWords = game.foundWords.toList()
            )
        }
    }

    override fun syncState() {
        _uiState.update {
            it.copy(
                board = game.board,
                selectedIndices = game.getSelectedIndices().toList(),
                currentWord = game.word,
                score = game.score,
                foundWords = game.foundWords.toList(),
                hintsAvailable = game.hints
            )
        }
    }

    fun uploadGameResults() {
        if (FirebaseHandler.userRef != null) {
            val result = GameResult(
                game.score,
                game.foundWords.size,
                game.solutions.size,
                game.maxScore,
                game.foundWords,
                String(game.board)
            )
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            FirebaseHandler.userRef!!.child("games").child(timestamp).setValue(result)
        }
    }

    fun resumeGame() {
        if (!uiState.value.isGameEnded && !uiState.value.isPaused) {
            game.startTimer()
        }
    }

    fun pauseGame() {
        game.stopTimer()
        _uiState.update { it.copy(isPaused = true) }
    }

    fun resumeGameManual() {
        _uiState.update { it.copy(isPaused = false) }
        game.startTimer()
    }
}
