package com.amibar.boggle.ui.game.singleplayer

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amibar.boggle.ui.shared.SampleData
import com.amibar.boggle.ui.shared.WordsList

/**
 * An [AlertDialog] that appears when a single-player game ends.
 * It displays the final score and a list of all possible words that could have been found on the board.
 * Words found by the player are highlighted in green.
 * Clicking on a word triggers a callback to show its path on the board.
 */

@Composable
fun SingleplayerGameEndDialog(
    solutions: Map<String, String>,
    foundWords: List<String>,
    score: Int,
    modifier: Modifier = Modifier,
    listener: (String, String) -> Unit,
    showingDialogState: MutableState<Boolean> = mutableStateOf(false)
) {
    val activity = LocalActivity.current

    if (showingDialogState.value)
        AlertDialog(
            modifier = modifier,
            onDismissRequest = {},
            confirmButton = {
                TextButton(
                    onClick = {
                        activity?.finish()
                    }
                ) {
                    Text("EXIT")
                }
            },
            text = {
                WordsList(
                    solutions = solutions,
                    playerWords = foundWords,
                    onWordClick = { word, path ->
                        listener(word, path)
                        showingDialogState.value = false
                    }
                )
            },
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Game Over",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.padding(22.dp))
                    Text(
                        text = "score: $score",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.padding(8.dp))
                    Text(
                        text = activity?.resources?.getString(com.amibar.boggle.R.string.hint_click_on_the_words_for_solution) ?: "hint: click on the words for solution",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        )
}

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
