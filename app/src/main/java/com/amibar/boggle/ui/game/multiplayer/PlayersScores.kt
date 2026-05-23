package com.amibar.boggle.ui.game.multiplayer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amibar.boggle.data.User
import com.amibar.boggle.engine.BoggleGame
import com.amibar.boggle.ui.shared.WordsList
import com.amibar.boggle.ui.shared.visibleWords

/**
 * Identifies words found by at least two different players.
 */
private fun findCommonWords(playersWords: Map<User, List<String>>): Set<String> {
    return playersWords.values
        .flatten()
        .groupingBy { it }
        .eachCount()
        .filterValues { it > 1 }
        .keys
}

@Composable
fun PlayersScores(
    modifier: Modifier = Modifier,
    solutions: Map<String, String>,
    playersWords: Map<User, List<String>>,
    onWordClick: (String, String) -> Unit = { _, _ -> }
) {
    val commonWords = remember(playersWords) { findCommonWords(playersWords) }

    val players = remember(playersWords) { playersWords.keys.toList().sortedBy { it.uid } }

    val scores = remember(playersWords) {
        playersWords.mapValues { words ->
            words.value.sumOf { BoggleGame.wordScore(it) }
        }
    }

    val states = players.associate { it.uid to rememberLazyListState() }

    states.forEach { (uid, state) ->
        LaunchedEffect(state) {
            snapshotFlow { state.firstVisibleItemIndex to state.firstVisibleItemScrollOffset }
                .collect { (index, offset) ->
                    if (state.isScrollInProgress) {
                        states.forEach { (otherUid, otherState) ->
                            if (otherUid != uid) {
                                otherState.scrollToItem(index, offset)
                            }
                        }
                    }
                }
        }
    }

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(3),
    ) {
        items(playersWords.keys.toList(), key = { it.uid }) { player ->
            val words = playersWords[player] ?: emptyList()
            Card(
                modifier = Modifier.padding(8.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = player.displayName,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "score: ${scores[player]}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray
                        )
                    )
                    WordsList(
                        modifier = Modifier.visibleWords(5),
                        solutions = solutions,
                        playerWords = words,
                        commonWords = commonWords,
                        onWordClick = onWordClick,
                        listState = states[player.uid]!!
                    )
                }
            }
        }
    }
}
