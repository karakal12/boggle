package com.amibar.boggle.ui.game.multiplayer

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amibar.boggle.ui.shared.SampleData

@Preview(showBackground = true)
@Composable
fun PlayersScoresPreview() {
    MaterialTheme {
        PlayersScores(
            modifier = Modifier.padding(16.dp),
            solutions = SampleData.solutions,
            playersWords = SampleData.playersWordsMap
        )
    }
}
