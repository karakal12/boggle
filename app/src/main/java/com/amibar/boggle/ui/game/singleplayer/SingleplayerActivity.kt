package com.amibar.boggle.ui.game.singleplayer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.amibar.boggle.databinding.ActivitySingleplayerBinding
import com.amibar.boggle.ui.DonutActivity
import kotlinx.coroutines.launch

/**
 * Activity that hosts the single-player Boggle game session.
 * It manages the game lifecycle, UI layout adjustments for edge-to-edge display,
 * and handles the end-of-game result reporting and summary display.
 */
class SingleplayerActivity : AppCompatActivity() {
    /** View binding instance for accessing layout components.  */
    lateinit var binding: ActivitySingleplayerBinding

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

        // Initialize view binding
        binding = ActivitySingleplayerBinding.inflate(layoutInflater)

        setupUI()

        observeViewModel()
    }

    private fun observeViewModel() {
        binding.boggleView.viewModel = viewModel

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is SinglePlayerEvent.GameEnded -> handleGameEnd(event.Score)
                        is SinglePlayerEvent.NavigateToDonutSecret -> startActivity(
                            Intent(this@SingleplayerActivity, DonutActivity::class.java)
                        )
                        is SinglePlayerEvent.ShowToast -> Toast.makeText(
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
        showGameEndDialog()
    }

    private fun setupUI() {
        // Enable Edge-to-Edge display support for modern Android navigation
        this.enableEdgeToEdge()
        setContentView(binding.getRoot())

        // Adjust padding to account for system bars (status bar, navigation bar) to prevent UI overlap
        ViewCompat.setOnApplyWindowInsetsListener(
            binding.main
        ) { v: View?, insets: WindowInsetsCompat? ->
            val systemBars = insets!!.getInsets(WindowInsetsCompat.Type.systemBars())
            v!!.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Handle back press: if game ended, show results; otherwise, allow default behavior
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.uiState.value.isGameEnded) {
                    showingDialogState.value = !showingDialogState.value
                    showGameEndDialog()
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

    /**
     * Builds and displays a dialog summary showing all possible solutions.
     * Iterates through all possible words on the board and highlights words
     * successfully found by the player in green.
     */
    private fun showGameEndDialog() {
        val state = viewModel.uiState.value

        binding.composeView.setContent {
            if (showingDialogState.value) {
                SingleplayerGameEndDialog(
                    solutions = viewModel.solutions,
                    foundWords = state.foundWords,
                    score = state.score,
                    listener = { _, path ->
                        binding.boggleView.showSolution(path)
                    },
                    showingDialogState = showingDialogState
                )
            }
        }
    }

    companion object {
        /** Tag used for logging and debugging purposes.  */
        private const val TAG = "SingleplayerActivity"


        /** Key for passing the final score in an Intent result.  */
        const val EXTRA_SCORE: String = "extra_score"
    }
}
