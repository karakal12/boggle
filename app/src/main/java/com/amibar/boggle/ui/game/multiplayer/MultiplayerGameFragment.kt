package com.amibar.boggle.ui.game.multiplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.amibar.boggle.R
import com.amibar.boggle.data.PlayerRole
import com.amibar.boggle.databinding.FragmentMultiplayerGameBinding
import com.amibar.boggle.views.BoggleBoard
import kotlinx.coroutines.launch

/**
 * Fragment responsible for the multiplayer game logic.
 * Refactored to use MVVM pattern.
 */
class MultiplayerGameFragment : Fragment() {
    private lateinit var binding: FragmentMultiplayerGameBinding
    private val viewModel: MultiplayerViewModel by viewModels()

    private lateinit var roomCode: String
    private lateinit var playerRole: PlayerRole

    private var showingEndDialog by mutableStateOf(false)
    private var gameResult: MultiplayerEvent.ResultsReady? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            try {
                roomCode = requireArguments().getString(MultiplayerActivity.ARG_ROOM_CODE)!!
                playerRole = PlayerRole.valueOf(requireArguments().getString(MultiplayerActivity.ARG_PLAYER_ROLE)!!)
            } catch (_: NullPointerException) {
//                parentFragmentManager.beginTransaction().replace(R.id.main, LobbyFragment()).commit()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMultiplayerGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.boggleView.setContent {
            BoggleBoard(
                viewModel = viewModel
            )

            if (showingEndDialog) {
                AlertDialog(
                    text = {
                        PlayersScores(
                            solutions = gameResult!!.solutions,
                            playersWords = gameResult!!.playersWords
                        )
                    },
                    onDismissRequest = {  },
                    confirmButton = {
                        Button(
                            onClick = { activity?.finish() }
                        ) {
                            Text("EXIT")
                        }
                    }
                )
            }
        }

        observeViewModel()

        viewModel.initRoom(roomCode, playerRole)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is MultiplayerEvent.ResultsReady -> {
                            gameResult = event
                        }
                        is MultiplayerEvent.GameDestroyed -> {
                            if (isAdded) {
                                requireActivity().finish()
                                Toast.makeText(
                                    requireContext(),
                                    "Game was destroyed by HOST",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        is MultiplayerEvent.ShowToast -> {
                            Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                        }
                        is MultiplayerEvent.GameStarted -> viewModel::startGame
                    }
                }
            }
        }
    }

    companion object {
        const val TAG: String = "MultiplayerGameFragment"

        fun newInstance(playerRole: PlayerRole, roomCode: String?): MultiplayerGameFragment {
            val fragment = MultiplayerGameFragment()
            val args = Bundle()
            args.putString(MultiplayerActivity.ARG_ROOM_CODE, roomCode)
            args.putString(MultiplayerActivity.ARG_PLAYER_ROLE, playerRole.name)
            fragment.arguments = args
            return fragment
        }
    }
}
