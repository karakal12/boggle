package com.amibar.boggle.ui.game.singleplayer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.amibar.boggle.ui.shared.SampleData
import com.amibar.boggle.ui.theme.BoggleTheme
import com.amibar.boggle.views.BoggleBoard
import com.amibar.boggle.views.BoggleUiState

@Preview(showBackground = true)
@Composable
fun SingleplayerActivityPreview() {
    BoggleTheme {
        SingleplayerContent(
            state = BoggleUiState(board = SampleData.board),
            solutions = emptyMap(),
            showingDialogState = remember { mutableStateOf(false) },
            onWordSelected = { _, _ -> },
            boardContent = {
                BoggleBoard(
                    viewModel = SampleData.boggleViewModel
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SingleplayerActivityGameEndPreview() {
    BoggleTheme {
        SingleplayerContent(
            state = BoggleUiState(
                board = SampleData.board,
                foundWords = SampleData.player1Words,
                score = 42,
                isGameEnded = true
            ),
            solutions = SampleData.solutions,
            showingDialogState = remember { mutableStateOf(true) },
            onWordSelected = { _, _ -> },
            boardContent = {
                BoggleBoard(
                    viewModel = SampleData.boggleViewModel
                )
            }
        )
    }
}
