package com.amibar.boggle.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.amibar.boggle.R
import com.amibar.boggle.data.GameMode
import com.amibar.boggle.databinding.ViewBoggleBinding
import com.amibar.boggle.engine.BoggleGame
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.core.content.withStyledAttributes

/**
 * A custom view representing the Boggle game board and its associated UI elements.
 * Refactored to use MVVM pattern.
 */
class BoggleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: ViewBoggleBinding = ViewBoggleBinding.inflate(LayoutInflater.from(context), this, true)
    private lateinit var cells: Array<TextView>
    private var hintBadge: BadgeDrawable? = null
    private var observeJob: Job? = null

    var viewModel: BoggleViewModel = BoggleViewModel()
        set(value) {
            field = value
            binding.viewModel = value
            observeViewModel()
        }

    init {
        context.withStyledAttributes(attrs, R.styleable.BoggleView, defStyleAttr, defStyleRes) {
            // gameMode attribute is currently unused in the refactored logic but could be used for mode-specific VM initialization
        }

        initView()
    }

    @OptIn(markerClass = [ExperimentalBadgeUtils::class])
    private fun initView() {
        val gl = binding.glGameLayout
        cells = Array(16) { i ->
            (gl.getChildAt(i) as TextView).apply {
                setOnClickListener {
                    viewModel.onDieSelected(i)
                }
            }
        }

        hintBadge = BadgeDrawable.create(context)
        binding.ivHint.post {
            BadgeUtils.attachBadgeDrawable(hintBadge!!, binding.ivHint, null)
        }

        binding.viewModel = viewModel
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        observeViewModel()
    }

    private fun observeViewModel() {
        val lifecycleOwner = findViewTreeLifecycleOwner() ?: return
        observeJob?.cancel()
        observeJob = lifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.state = state
                updateBoardUI(state)
                updateSelectionUI(state)
                updateBadgeUI(state)
            }
        }
    }

    private fun updateBoardUI(state: BoggleUiState) {
        for (i in cells.indices) {
            val letter = state.board[i]
            cells[i].text = if (letter == 'Q') "Qu" else letter.toString()
        }
    }

    private fun updateSelectionUI(state: BoggleUiState) {
        val selectedColor = resolveAttribute(R.attr.colorSelected)
        val lastSelectedColor = resolveAttribute(R.attr.colorLastSelected)
        val unselectedColor = resolveAttribute(R.attr.colorUnselected)

        for (i in cells.indices) {
            cells[i].setBackgroundColor(
                when {
                    state.selectedIndices.isNotEmpty() && i == state.selectedIndices.last() -> lastSelectedColor
                    state.selectedIndices.contains(i) -> selectedColor
                    else -> unselectedColor
                }
            )
        }
    }

    private fun updateBadgeUI(state: BoggleUiState) {
        hintBadge?.apply {
            number = state.hintsAvailable
            isVisible = state.hintsAvailable > 0
        }
    }

    private fun resolveAttribute(attrRes: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue.data
    }

    // --- Backward compatibility and Helper methods ---

    val game: BoggleGame get() = viewModel.game

    fun newGame(): BoggleGame {
        viewModel = BoggleViewModel()
        return viewModel.game
    }

    fun setGame(board: CharArray): BoggleGame {
        viewModel = BoggleViewModel(BoggleGame(board))
        return viewModel.game
    }

    fun startGame() = viewModel.startGame()

    fun showSolution(path: String) {
        viewModel.game.selectPath(path)
        viewModel.syncState()
    }

    fun clearSolution() {
        viewModel.game.deselectPath()
        viewModel.syncState()
    }

    fun showHint() = viewModel.showHint()

    // These setters are now mostly redundant if using the ViewModel directly, 
    // but kept for compatibility with existing code that pushes state manually.
    
    fun setScore(score: Int) {
        binding.tvScore.text = context.getString(R.string.score, score)
    }

    fun setBoard(board: CharArray) {
        for (i in cells.indices) {
            val letter = board[i]
            cells[i].text = if (letter == 'Q') "Qu" else letter.toString()
        }
    }

    fun setSelectedIndices(indices: List<Int>) {
        val selectedColor = resolveAttribute(R.attr.colorSelected)
        val lastSelectedColor = resolveAttribute(R.attr.colorLastSelected)
        val unselectedColor = resolveAttribute(R.attr.colorUnselected)

        for (i in cells.indices) {
            cells[i].setBackgroundColor(
                when {
                    indices.isNotEmpty() && i == indices.last() -> lastSelectedColor
                    indices.contains(i) -> selectedColor
                    else -> unselectedColor
                }
            )
        }
    }

    fun setWord(word: String) {
        binding.tvWord.text = context.getString(R.string.word, word)
    }

    fun setTime(millis: Long) {
        binding.tvTime.text = formatTime(millis)
        binding.progressBar.progress = (millis / 1000).toInt()
    }

    fun setTime(time: String) {
        binding.tvTime.text = time
    }

    fun setHints(count: Int) {
        hintBadge?.number = count
        hintBadge?.isVisible = count > 0
    }

    fun setSubmit(function: () -> Unit) {
        binding.bSubmit.setOnClickListener { function() }
    }

    fun setOnDieSelectedListener(listener: (Int) -> Unit) {
        for (i in cells.indices) {
            cells[i].setOnClickListener { listener(i) }
        }
    }

    fun setOnHintRequestedListener(listener: () -> Unit) {
        binding.ivHint.setOnClickListener { listener() }
    }
}
