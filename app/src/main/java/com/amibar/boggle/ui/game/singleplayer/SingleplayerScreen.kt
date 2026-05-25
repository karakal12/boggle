package com.amibar.boggle.ui.game.singleplayer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.amibar.boggle.views.BoggleBoard
import com.amibar.boggle.views.BoggleUiState

@Composable
fun SingleplayerContent(
    viewModel: SingleplayerViewModel,
    showingDialogState: MutableState<Boolean>,
    onExit: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    SingleplayerContent(
        state = state,
        solutions = viewModel.solutions,
        showingDialogState = showingDialogState,
        onWordSelected = { _, path ->
            viewModel.game.selectPath(path)
            viewModel.syncState()
        },
        onExit = onExit,
        boardContent = {
            BoggleBoard(
                viewModel = viewModel
            )
        }
    )
}

@Composable
internal fun SingleplayerContent(
    state: BoggleUiState,
    solutions: Map<String, String>,
    showingDialogState: MutableState<Boolean>,
    onWordSelected: (String, String) -> Unit,
    onExit: () -> Unit,
    boardContent: @Composable () -> Unit
) {
    Surface(
        Modifier
            .safeDrawingPadding()
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            boardContent()
        }
    }
    if (showingDialogState.value) {
        SingleplayerGameEndDialog(
            solutions = solutions,
            foundWords = state.foundWords,
            score = state.score,
            listener = onWordSelected,
            onExit = onExit,
            showingDialogState = showingDialogState
        )
    }
}
