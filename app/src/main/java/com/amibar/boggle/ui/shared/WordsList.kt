package com.amibar.boggle.ui.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp

/**
 * Modifier that sets the height of a component to show a specific number of words
 * based on the current text style.
 */
@Composable
fun Modifier.visibleWords(count: Int): Modifier {
    val density = LocalDensity.current
    val textStyle = LocalTextStyle.current
    val lineHeight = if (textStyle.lineHeight.isUnspecified) {
        (textStyle.fontSize.value * 1.2).sp
    } else {
        textStyle.lineHeight
    }
    return this.height(with(density) { (lineHeight * count).toDp() })
}

@Composable
fun WordsList(
    /** Map of all valid words on the board to their paths.  */
    solutions: Map<String, String>,
    /** List of words found by the current player.  */
    playerWords: List<String>,
    modifier: Modifier = Modifier,
    /** Set of words found by more than one player (for multiplayer).  */
    commonWords: Set<String> = emptySet(),
    /** Callback listener for word click events.  */
    onWordClick: (word: String, path: String) -> Unit = { _, _ -> },
    listState: LazyListState = rememberLazyListState()
) {
    val words = remember(solutions) { solutions.keys.toList().sorted() }

    Box(modifier = Modifier
        .fillMaxHeight()
        .then(modifier)
        .drawWithContent {
            drawContent()
            val totalItemsCount = words.size
            val layoutInfo = listState.layoutInfo
            val visibleItemsCount = layoutInfo.visibleItemsInfo.size

            if (visibleItemsCount in 1..<totalItemsCount) {
                // Calculate total height of all items if they were all visible
                // Since items are similar (one line of text), we can estimate based on average visible item height
                val averageItemHeight = layoutInfo.visibleItemsInfo.sumOf { it.size }.toFloat() / visibleItemsCount
                val totalHeight = averageItemHeight * totalItemsCount
                
                // Calculate how much has been scrolled in pixels
                val scrolledPixels = (listState.firstVisibleItemIndex * averageItemHeight) + listState.firstVisibleItemScrollOffset
                
                // Scrollbar height is proportional to the visible area
                val viewportHeight = size.height
                val scrollbarHeight = (viewportHeight / totalHeight) * viewportHeight
                
                // Scrollbar offset is proportional to the scrolled amount
                val scrollbarOffsetY = (scrolledPixels / totalHeight) * viewportHeight

                // Background track
                drawRoundRect(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    topLeft = Offset(size.width - 4.dp.toPx(), 0f),
                    size = Size(4.dp.toPx(), size.height),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )

                // Thumb
                drawRoundRect(
                    color = Color.Gray,
                    topLeft = Offset(size.width - 4.dp.toPx(), scrollbarOffsetY),
                    size = Size(4.dp.toPx(), scrollbarHeight),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }
        },
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(words) { word ->
                WordItem(word, solutions[word] ?: "", playerWords, commonWords, onWordClick)
            }
        }
    }
}

@Composable
private fun WordItem(
    word: String,
    path: String,
    playerWords: List<String>,
    commonWords: Set<String>,
    onWordClick: (word: String, path: String) -> Unit
) {
    Text(
        text = word,
        color = when {
            commonWords.contains(word) -> Color.Red
            playerWords.contains(word) -> Color.Green
            else -> Color.Black
        },
        modifier = Modifier.clickable {
            onWordClick(word, path)
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun WordItemPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            WordItem(
                word = "NORMAL",
                path = "",
                playerWords = emptyList(),
                commonWords = emptySet(),
                onWordClick = { _, _ -> }
            )
            WordItem(
                word = "FOUND",
                path = "",
                playerWords = listOf("FOUND"),
                commonWords = emptySet(),
                onWordClick = { _, _ -> }
            )
            WordItem(
                word = "COMMON",
                path = "",
                playerWords = listOf("COMMON"),
                commonWords = setOf("COMMON"),
                onWordClick = { _, _ -> }
            )
        }
    }
}
