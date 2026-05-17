package com.amibar.boggle.ui.game.multiplayer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.amibar.boggle.data.PlayerRole
import com.amibar.boggle.databinding.FragmentJoinOrCreateRoomBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

/**
 * A DialogFragment that allows users to either create a new multiplayer room or join an existing one.
 * It handles input validation and checks for the existence of a room code in Firebase before joining.
 */
class JoinOrCreateRoomFragment
/**
 * Required empty public constructor.
 */
    : DialogFragment() {
    /** View binding for fragment layout.  */
    private var binding: FragmentJoinOrCreateRoomBinding? = null

    override fun onStart() {
        super.onStart()
        // Set dialog width to match parent for better usability
        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentJoinOrCreateRoomBinding.inflate(inflater, container, false)
        return binding!!.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()


        // Handle arguments if they exist (e.g. when opening from a notification)
        if (arguments == null) return
        if (!requireArguments().containsKey(ARG_INITIAL_ROOM_CODE)) return

        val role = PlayerRole.valueOf(
            requireArguments().getString(
                ARG_INITIAL_PLAYER_ROLE,
                PlayerRole.Host.toString()
            )
        )
        val initialRoomCode = requireArguments().getString(ARG_INITIAL_ROOM_CODE)

        binding!!.roomCodeTV.setText(initialRoomCode)
        binding!!.roomCodeTIL.visibility = View.VISIBLE


//        // Toggle visibility based on the intended role
//        binding.createRoom.setVisibility(role == PlayerRole.host ? View.VISIBLE: View.GONE);
//        binding.createRoom.setOnClickListener(this::createRoom);
//        binding.joinRoom.setVisibility(role == PlayerRole.Guest ? View.VISIBLE: View.GONE);
//        binding.joinRoom.setOnClickListener(this::joinRoom);
        if (role == PlayerRole.Host) {
            createRoom(view)
        } else {
            joinRoom(view)
        }
    }

    /**
     * Initializes UI state and basic button listeners.
     */
    private fun init() {
        binding!!.createRoom.setOnClickListener { view: View? ->
            binding!!.roomCodeTIL.setVisibility(View.VISIBLE)
            binding!!.joinRoom.setVisibility(View.GONE)
            requireView().setOnClickListener { view: View? -> this.createRoom(view) }
        }

        binding!!.joinRoom.setOnClickListener { view: View? ->
            binding!!.roomCodeTIL.setVisibility(View.VISIBLE)
            binding!!.createRoom.setVisibility(View.GONE)
            requireView().setOnClickListener { view: View? -> this.joinRoom(view) }
        }
    }

    /**
     * Logic for creating a room. Transitions to MultiplayerActivity as HOST.
     * @param view The clicked view.
     */
    private fun createRoom(view: View?) {
        val intent = makeIntent(PlayerRole.Host)
        if (intent != null) {
            startActivity(intent)
        }
        dismiss()
    }

    /**
     * Logic for joining a room. Validates room code exists in Firebase before transitioning.
     * @param view The clicked view.
     */
    private fun joinRoom(view: View?) {
        val roomCodeTVText = binding!!.roomCodeTV.getText()
        if (roomCodeTVText == null || roomCodeTVText.toString().isEmpty()) return

        val roomCode = roomCodeTVText.toString()
        val roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomCode)

        // Verify if room exists in Realtime Database
        roomRef.get().addOnCompleteListener { task: Task<DataSnapshot?>? ->
            if (task!!.isSuccessful() && task.getResult()!!.exists()) {
                // Room exists, proceed to join as GUEST
                val intent = makeIntent(PlayerRole.Guest)
                if (intent != null) {
                    startActivity(intent)
                    dismiss()
                }
            } else {
                // Room doesn't exist, show error to user
                binding!!.roomCodeTIL.setError("Room not found")
            }
        }
    }


    /**
     * Helper to create an Intent for MultiplayerActivity.
     * @param role The role to pass to the activity.
     * @return The configured Intent or null if room code is missing.
     */
    private fun makeIntent(role: PlayerRole?): Intent? {
        val roomCodeTVText = binding!!.roomCodeTV.getText()
        if (roomCodeTVText != null && !roomCodeTVText.toString().isEmpty()) {
            val intent = Intent(requireContext(), MultiplayerActivity::class.java)
            intent.putExtra(MultiplayerActivity.ARG_ROOM_CODE, roomCodeTVText.toString())
            intent.putExtra(MultiplayerActivity.ARG_PLAYER_ROLE, role)
            return intent
        }
        return null
    }

    companion object {
        /** Tag used for identifying this fragment in the FragmentManager.  */
        const val TAG: String = "JoinOrCreateRoomFragment"

        /** Key for the initial room code passed in arguments.  */
        private const val ARG_INITIAL_ROOM_CODE = "initial_room_code"

        /** Key for the initial player role passed in arguments.  */
        private const val ARG_INITIAL_PLAYER_ROLE = "initial_player_role"

        /**
         * Creates a new instance of this fragment with optional initial room code and role.
         * @param roomCode The initial room code to display.
         * @param playerRole The player's role (HOST or GUEST).
         * @return A configured fragment instance.
         */
        fun newInstance(roomCode: String?, playerRole: PlayerRole): JoinOrCreateRoomFragment {
            val fragment = JoinOrCreateRoomFragment()
            val args = Bundle()
            args.putString(ARG_INITIAL_ROOM_CODE, roomCode)
            args.putString(ARG_INITIAL_PLAYER_ROLE, playerRole.toString())
            fragment.setArguments(args)
            return fragment
        }
    }
}
