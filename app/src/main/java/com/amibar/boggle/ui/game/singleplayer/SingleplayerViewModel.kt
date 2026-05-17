package com.amibar.boggle.ui.game.singleplayer

import androidx.lifecycle.viewModelScope
import com.amibar.boggle.data.FirebaseHandler
import com.amibar.boggle.data.GameResult
import com.amibar.boggle.engine.BoggleGame
import com.amibar.boggle.views.BoggleViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class SinglePlayerEvent {
    data class GameEnded(val Score: Int) : SinglePlayerEvent()
    object NavigateToDonutSecret : SinglePlayerEvent()
    data class ShowToast(val message: String) : SinglePlayerEvent()
}

class SingleplayerViewModel : BoggleViewModel() {
    private val _events = Channel<SinglePlayerEvent>()
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
                _events.send(SinglePlayerEvent.GameEnded(game.score))
            }
        }
    }

    override fun submitWord() {
        if (game.isEnded) return

        val word = game.word
        val result = game.submitWord()

        if (word.equals("donut", ignoreCase = true)) {
            viewModelScope.launch {
                _events.send(SinglePlayerEvent.NavigateToDonutSecret)
            }
        }

        _uiState.update {
            it.copy(
                feedbackMessage = "$word is ${result.name}",
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
        if (!uiState.value.isGameEnded) {
            game.startTimer()
        }
    }

    fun pauseGame() {
        game.stopTimer()
    }
}
