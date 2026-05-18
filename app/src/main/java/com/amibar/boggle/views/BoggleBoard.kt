package com.amibar.boggle.views

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amibar.boggle.R
import com.amibar.boggle.engine.BoggleGame
import com.amibar.boggle.ui.shared.SampleData
import com.amibar.boggle.ui.theme.BoggleTheme

@Composable
fun BoggleBoard(
    modifier: Modifier = Modifier,
    viewModel: BoggleViewModel = BoggleViewModel(),
    onWordSubmitted: (String) -> Unit = {}
) {
    val state = viewModel.uiState.collectAsState().value
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Row (
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = BoggleViewModel.formatTime(state.remainingTimeMillis)
            )
            LinearProgressIndicator(
                progress = { state.remainingTimeMillis / BoggleGame.GAME_TIME_MILLIS.toFloat() },
                modifier = Modifier.padding( end = 8.dp )
            )
        }
        Text(
            text = stringResource(R.string.boggle),
            fontSize = 50.sp
        )
        Text(
            text = state.currentWord.uppercase(),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.height(48.dp)
        )
        Text(
            text = "Score: ${state.score}",
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.tertiary
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .padding( bottom =16.dp)
                .aspectRatio(1f)
        ) {
            items(state.board.size) { index ->
                BoggleCell(
                    char = state.board[index],
                    isSelected = state.selectedIndices.contains(index),
                    isLastSelected = state.selectedIndices.lastOrNull() == index,
                    onClick = { viewModel.onDieSelected(index) }
                )
            }
        }
        Text(
            text = if (state.feedbackMessageResId != null)
                stringResource(state.feedbackMessageResId, state.currentWord)
            else "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(0.75f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Spacer to balance the icon on the right for button centering
            Box(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val word = state.currentWord
                    viewModel.submitWord()
                    onWordSubmitted(word)
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(stringResource(R.string.submit))
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                BadgedBox(
                    badge = {
                        if (state.hintsAvailable > 1)
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Text("${state.hintsAvailable}")
                            }
                    }
                ){
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Hints",
                        modifier = Modifier
                            .size(40.dp)
                            .clickable(
                                enabled = state.hintsAvailable > 0,
                                onClick = viewModel::showHint
                            ),
                        tint = if (state.hintsAvailable > 0) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            }
        }
    }
}

@Composable
fun BoggleCell(
    char: Char,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    isLastSelected: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
            modifier = modifier
                .aspectRatio(1f),
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = if (isSelected) 8.dp else 4.dp,
        color = when {
            isLastSelected -> MaterialTheme.colorScheme.primary
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        }
    ){
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = char.toString().uppercase().let { if (it == "Q") "Qu" else it },
                color = when {
                    isLastSelected -> MaterialTheme.colorScheme.onPrimary
                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onBackground
                },
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable(
                    interactionSource = null,
                    indication = null,
                    onClick = onClick
                ),
                autoSize = TextAutoSize.StepBased()
            )
        }
    }
}


@Preview(name = "BoggleBoardNight", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "BoggleBoardLight", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun BoggleBoardPreview() {
    BoggleTheme {
        Surface(
            color = MaterialTheme.colorScheme.surface
        ) {
            BoggleBoard(
                viewModel = BoggleViewModel(
                    BoggleGame(
                        SampleData.board
                    )
                )
            )
        }
    }
}