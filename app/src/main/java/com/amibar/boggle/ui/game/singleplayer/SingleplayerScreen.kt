package com.amibar.boggle.ui.game.singleplayer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.amibar.boggle.ui.shared.BoggleBoard
import com.amibar.boggle.ui.shared.BoggleUiState

@Composable
fun SingleplayerContent(
    viewModel: SingleplayerViewModel,
    showingDialogState: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    onExit: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    SingleplayerContent(
        modifier = modifier,
        state = state,
        solutions = viewModel.solutions,
        showingDialogState = showingDialogState,
        onWordSelected = { _, path ->
            viewModel.game.selectPath(path)
            viewModel.syncState()
        },
        onExit = onExit,
        onResume = viewModel::resumeGameManual,
        boardContent = {
            BoggleBoard(
                viewModel = viewModel,
                onPauseClick = viewModel::pauseGame
            )
        }
    )
}

@Composable
internal fun SingleplayerContent(
    state: BoggleUiState,
    solutions: Map<String, String>,
    showingDialogState: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    onWordSelected: (String, String) -> Unit,
    onExit: () -> Unit,
    onResume: () -> Unit,
    boardContent: @Composable () -> Unit
) {
    Surface(
        modifier
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

    if (state.isPaused) {
        SingleplayerPauseDialog(
            onResume = onResume,
            onExit = onExit
        )
    }
}

@Composable
fun SingleplayerPauseDialog(
    onResume: () -> Unit,
    onExit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onResume,
        title = { Text("Game Paused") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Do you want to continue or exit?")
            }
        },
        confirmButton = {
            Button(onClick = onResume) {
                Text("Resume")
            }
        },
        dismissButton = {
            TextButton(onClick = onExit) {
                Text("Exit")
            }
        }
    )
}
