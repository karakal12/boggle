package com.amibar.boggle.ui.game.multiplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.amibar.boggle.data.User
import com.amibar.boggle.databinding.FragmentMultiplayerOnGameEndBinding

/**
 * A DialogFragment displayed at the end of a multiplayer game.
 * It shows a comprehensive list of words found by each player and indicates which words were common.
 */
class MultiplayerOnGameEndFragment : DialogFragment() {
    /** View binding for the fragment layout.  */
    private var binding: FragmentMultiplayerOnGameEndBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMultiplayerOnGameEndBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Populate the adapter with data from arguments
        if (arguments != null) {
            @Suppress("UNCHECKED_CAST") val playersWords: HashMap<User, ArrayList<String>>? =
                requireArguments().getSerializable(
                    ARG_PLAYERS_WORDS, HashMap::class.java
                ) as? HashMap<User, ArrayList<String>>
            @Suppress("UNCHECKED_CAST")
            val solutions = arguments?.getSerializable(ARG_SOLUTIONS, HashMap::class.java) as? HashMap<String, String>

            if (playersWords != null && solutions != null) {
                binding!!.playersWordsList.setContent {
                    PlayersScores(
                        solutions = solutions,
                        playersWords = playersWords as Map<User, List<String>>
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Set dialog width to match parent
        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!
                .setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure dialog layout remains consistent on resume
        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    companion object {
        /** Tag for identifying the fragment.  */
        const val TAG: String = "MultiplayerOnGameEndFragment"

        /** Argument key for the map of players to their found words.  */
        const val ARG_PLAYERS_WORDS: String = "playersWords"

        /** Argument key for the map of solution words to their paths.  */
        const val ARG_SOLUTIONS: String = "solutions"

        /**
         * Creates a new instance of MultiplayerOnGameEndFragment.
         * @param solutions Map of all valid words on the board and their paths.
         * @param playersWords Map of users and the words they found during the game.
         * @return A new fragment instance.
         */
        fun newInstance(
            solutions: Map<String, String>,
            playersWords: Map<User, List<String>>
        ): MultiplayerOnGameEndFragment {
            val fragment = MultiplayerOnGameEndFragment()
            val args = Bundle()
            args.putSerializable(ARG_PLAYERS_WORDS, HashMap(playersWords))
            args.putSerializable(ARG_SOLUTIONS, HashMap(solutions))
            fragment.setArguments(args)
            return fragment
        }
    }
}
