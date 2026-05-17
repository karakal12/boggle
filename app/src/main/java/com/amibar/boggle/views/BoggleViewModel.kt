package com.amibar.boggle.views

import androidx.lifecycle.ViewModel
import com.amibar.boggle.engine.BoggleGame
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

/**
 * ViewModel for the BoggleView, managing the game state and logic.
 */
fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / 1000) / 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

open class BoggleViewModel(initialGame: BoggleGame = BoggleGame()) : ViewModel() {

    private var _game: BoggleGame = initialGame
    val game: BoggleGame get() = _game

    protected val _uiState = MutableStateFlow(BoggleUiState())
    val uiState: StateFlow<BoggleUiState> = _uiState.asStateFlow()

    init {
        setupGameListeners(_game)
        syncState()
    }

    private fun setupGameListeners(game: BoggleGame) {
        game.addOnTickListener { elapsedTime ->
            val remaining = BoggleGame.GAME_TIME_MILLIS - elapsedTime
            _uiState.update { it.copy(remainingTimeMillis = remaining) }
        }
        game.addOnGameEndListener {
            _uiState.update { it.copy(isGameEnded = true) }
        }
    }

    fun updateGame(newGame: BoggleGame) {
        _game.stopTimer()
        _game = newGame
        setupGameListeners(_game)
        syncState()
    }

    open fun onDieSelected(index: Int) {
        if (_game.isEnded) return
        if (_game.selectDie(index)) {
            syncState()
        }
    }

    open fun submitWord() {
        if (_game.isEnded) return
        val word = _game.word
        val result = _game.submitWord()
        _uiState.update { it.copy(
            feedbackMessage = if (result.messageId != 0) {
                // We can't easily access context here without a dependency. 
                // But we can let the Activity handle it or provide a way.
                // For now, I'll just store the result and let the UI handle it.
                null 
            } else null,
            feedbackMessageResId = result.messageId
        ) }
        syncState()
    }

    open fun showHint() {
        if (_game.isEnded || _game.hints <= 0) return

        val currentPathSB = StringBuilder()
        for (index in _game.getSelectedIndices()) {
            currentPathSB.append(Integer.toHexString(index))
        }
        val currentPath = currentPathSB.toString()

        val candidatePaths = _game.allPaths.filter {
            it.startsWith(currentPath) && it.length > currentPath.length && !_game.foundWords.contains(_game.getWordFromPath(it))
        }.ifEmpty {
            _game.allPaths.filter { !_game.foundWords.contains(_game.getWordFromPath(it)) }
        }

        if (candidatePaths.isEmpty()) return

        val fullPath = candidatePaths.shuffled().first()
        val currentPathLength = currentPath.length
        val remainingLength = fullPath.length - currentPathLength
        val revealCount = currentPathLength + kotlin.math.ceil(remainingLength / 2.0).toInt()

        if (revealCount >= fullPath.length) return

        _game.subHint()
        _game.selectPath(fullPath.substring(0, revealCount))
        syncState()
    }

    open fun syncState() {
        _uiState.update {
            it.copy(
                board = _game.board,
                selectedIndices = _game.getSelectedIndices(),
                currentWord = _game.word,
                score = _game.score,
                hintsAvailable = _game.hints,
                foundWords = _game.foundWords
            )
        }
    }

    fun startGame() {
        _game.startTimer()
    }

    fun stopTimer() {
        _game.stopTimer()
    }
}

data class BoggleUiState(
    val board: CharArray = CharArray(16),
    val selectedIndices: List<Int> = emptyList(),
    val currentWord: String = "",
    val score: Int = 0,
    val remainingTimeMillis: Long = BoggleGame.GAME_TIME_MILLIS,
    val isGameEnded: Boolean = false,
    val hintsAvailable: Int = 0,
    val feedbackMessage: String? = null,
    val feedbackMessageResId: Int? = null,
    val foundWords: List<String> = emptyList()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BoggleUiState
        if (!board.contentEquals(other.board)) return false
        if (selectedIndices != other.selectedIndices) return false
        if (currentWord != other.currentWord) return false
        if (score != other.score) return false
        if (remainingTimeMillis != other.remainingTimeMillis) return false
        if (isGameEnded != other.isGameEnded) return false
        if (hintsAvailable != other.hintsAvailable) return false
        if (feedbackMessage != other.feedbackMessage) return false
        if (feedbackMessageResId != other.feedbackMessageResId) return false
        if (foundWords != other.foundWords) return false
        return true
    }

    override fun hashCode(): Int {
        var result = board.contentHashCode()
        result = 31 * result + selectedIndices.hashCode()
        result = 31 * result + currentWord.hashCode()
        result = 31 * result + score
        result = 31 * result + remainingTimeMillis.hashCode()
        result = 31 * result + isGameEnded.hashCode()
        result = 31 * result + hintsAvailable
        result = 31 * result + (feedbackMessage?.hashCode() ?: 0)
        result = 31 * result + (feedbackMessageResId ?: 0)
        result = 31 * result + foundWords.hashCode()
        return result
    }
}
