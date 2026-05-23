package com.amibar.boggle.ui.shared

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showBackground = true)
@Composable
fun WordsListPreview() {
    val solutions = SampleData.solutions
    val playerWords = SampleData.player1Words
    val commonWords = setOf("hit", "hire", "met")

    MaterialTheme {
        WordsList(
            solutions = solutions,
            playerWords = playerWords,
            commonWords = commonWords,
            modifier = Modifier
                .padding(16.dp)
                .visibleWords(10)
        )
    }
}
