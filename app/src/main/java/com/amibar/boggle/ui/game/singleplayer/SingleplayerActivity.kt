package com.amibar.boggle.ui.game.singleplayer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.amibar.boggle.ui.DonutActivity
import com.amibar.boggle.ui.theme.BoggleTheme
import com.amibar.boggle.views.BoggleBoard
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.getValue
import com.amibar.boggle.ui.shared.SampleData
import com.amibar.boggle.views.BoggleUiState
import com.amibar.boggle.views.BoggleViewModel

/**
 * Activity that hosts the single-player Boggle game session.
 * It manages the game lifecycle, UI layout adjustments for edge-to-edge display,
 * and handles the end-of-game result reporting and summary display.
 */
class SingleplayerActivity : AppCompatActivity() {

    private val viewModel: SingleplayerViewModel by viewModels()

    private var showingDialogState = mutableStateOf(false)

    /**
     * Called when the activity is first created.
     * Sets up the UI, handles window insets for edge-to-edge display,
     * and initializes the game end logic.
     * 
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in [.onSaveInstanceState].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is SingleplayerEvent.GameEnded -> handleGameEnd(event.score)
                        is SingleplayerEvent.NavigateToDonutSecret -> startActivity(
                            Intent(this@SingleplayerActivity, DonutActivity::class.java)
                        )
                        is SingleplayerEvent.ShowToast -> Toast.makeText(
                            this@SingleplayerActivity, event.message, Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    fun handleGameEnd(score: Int) {
        val resultIntent = Intent().apply {
            putExtra(EXTRA_SCORE, score)
        }

        setResult(RESULT_OK, resultIntent)

        showingDialogState.value = true
    }

    private fun setupUI() {
        // Enable Edge-to-Edge display support for modern Android navigation
        this.enableEdgeToEdge()
        setContent {
            BoggleTheme {
                SingleplayerContent(viewModel, showingDialogState)
            }
        }


        // Handle back press: if game ended, show results; otherwise, allow default behavior
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.uiState.value.isGameEnded) {
                    viewModel.game.deselectPath()
                    viewModel.syncState()
                    showingDialogState.value = true
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // Resume game timer
        viewModel.resumeGame()
    }

    override fun onPause() {
        super.onPause()
        viewModel.pauseGame()
    }


    companion object {
        /** Key for passing the final score in an Intent result.  */
        const val EXTRA_SCORE: String = "extra_score"
    }
}

/**
 * Composables that defines the UI for the SingleplayerActivity.
 */
@Composable
fun SingleplayerContent(
    viewModel: SingleplayerViewModel,
    showingDialogState: MutableState<Boolean>
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
        boardContent = {
            BoggleBoard(
                viewModel = viewModel
            )
        }
    )
}

@Composable
private fun SingleplayerContent(
    state: BoggleUiState,
    solutions: Map<String, String>,
    showingDialogState: MutableState<Boolean>,
    onWordSelected: (String, String) -> Unit,
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
            showingDialogState = showingDialogState
        )
    }
}

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
