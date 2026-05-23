package com.amibar.boggle.ui.game.singleplayer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.amibar.boggle.ui.shared.SampleData

@Preview(showBackground = true)
@Composable
fun SingleplayerGameEndDialogPreview() {
    val solutions = SampleData.solutions
    val foundWords = SampleData.player1Words
    val score = 42
    val showingDialogState = remember { mutableStateOf(true) }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            SingleplayerGameEndDialog(
                solutions = solutions,
                foundWords = foundWords,
                score = score,
                listener = { _, _ -> },
                showingDialogState = showingDialogState
            )
        }
    }
}
