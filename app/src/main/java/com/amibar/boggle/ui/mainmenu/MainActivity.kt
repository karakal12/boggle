package com.amibar.boggle.ui.mainmenu

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amibar.boggle.R
import com.amibar.boggle.data.FirebaseHandler
import com.amibar.boggle.data.PlayerRole
import com.amibar.boggle.data.User
import com.amibar.boggle.databinding.ActivityMainBinding
import com.amibar.boggle.databinding.NavHeaderBinding
import com.amibar.boggle.ui.DonutActivity
import com.amibar.boggle.ui.game.multiplayer.JoinOrCreateRoomFragment
import com.amibar.boggle.ui.game.singleplayer.SingleplayerActivity
import com.amibar.boggle.utils.base64ToBitmap
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot

/**
 * The primary entry point of the application.
 * Manages the main navigation drawer, handles authentication state changes,
 * and provides access to different game modes and user features.
 */
@Suppress("unused")
class MainActivity : AppCompatActivity() {
    /** View binding for the activity layout.  */
    private lateinit var binding: ActivityMainBinding

    /** Listener for Firebase Authentication state changes.  */
    private lateinit var authStateListener: AuthStateListener

    /**
     * Launcher for SingleplayerActivity to receive the final score when the game ends.
     */
    private val singleplayerLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult? ->
        if (result!!.resultCode == RESULT_OK && result.data != null) {
            val score =
                result.data!!.getIntExtra(SingleplayerActivity.EXTRA_SCORE, 0)
            Toast.makeText(this, "Game finished! Your score: $score", Toast.LENGTH_LONG)
                .show()
        }
    }

    /**
     * Launcher for requesting notification permissions (Android 13+).
     */
    private val requestPermissionLauncher = registerForActivityResult(
        RequestPermission()
    ) { isGranted: Boolean? ->
        if (!isGranted!!) {
            Toast.makeText(
                this,
                "Notifications disabled. You won't receive game invites.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())


        // Enable edge-to-edge display
        this.enableEdgeToEdge()


        // Handle window insets for both the main content and the navigation drawer
        ViewCompat.setOnApplyWindowInsetsListener(
            binding.mainContent
        ) { v: View?, insets: WindowInsetsCompat? ->
            val systemBars = insets!!.getInsets(WindowInsetsCompat.Type.systemBars())
            v!!.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(
            binding.navView
        ) { v: View?, insets: WindowInsetsCompat? ->
            val systemBars = insets!!.getInsets(WindowInsetsCompat.Type.systemBars())
            v!!.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        askNotificationPermission()
        setupAuthStateListener()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    /**
     * Processes incoming intents, specifically for joining multiplayer rooms from notifications.
     * @param intent The intent to handle.
     */
    private fun handleIntent(intent: Intent?) {
        if (intent != null && intent.hasExtra("roomCode")) {
            val roomCode = intent.getStringExtra("roomCode")

            if (intent.hasExtra("invitationId")) {
                val invitationId = intent.getStringExtra("invitationId")
                val currentUserId = FirebaseAuth.getInstance().uid
                if (currentUserId != null && invitationId != null) {
                    FirebaseHandler.rootRef
                        .child("invitations")
                        .child(currentUserId)
                        .child(invitationId)
                        .removeValue()
                }
            }

            if (!roomCode.isNullOrEmpty()) {
                var role = PlayerRole.Guest // Default to Guest for invitations
                if (intent.hasExtra("action") && "host" == intent.getStringExtra("action")) {
                    role = PlayerRole.Host
                }
                JoinOrCreateRoomFragment.newInstance(roomCode, role)
                    .show(supportFragmentManager, JoinOrCreateRoomFragment.TAG)
            }
        }
    }

    /**
     * Initializes UI components, toolbar, and click listeners.
     */
    private fun init() {
        setSupportActionBar(binding.toolbar)

        // Navigation for Singleplayer
        binding.singleplayerButton.setOnClickListener { v: View? ->
            val intent = Intent(this, SingleplayerActivity::class.java)
            singleplayerLauncher.launch(intent)
        }

        // Navigation for Multiplayer - requires login
        binding.multiplayerButton.setOnClickListener { v: View? ->
            if (FirebaseHandler.auth.currentUser != null) {
                val fragment = JoinOrCreateRoomFragment()
                fragment.show(supportFragmentManager, JoinOrCreateRoomFragment.TAG)
            } else {
                Toast.makeText(this, "Please sign in to play multiplayer", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Navigation for Friend List
        binding.friendsListButton.setOnClickListener { v: View? ->
            val intent = Intent(this, FriendListActivity::class.java)
            startActivity(intent)
        }

        // Easter Egg / Bonus feature
        binding.donutButton.setOnClickListener { v: View? ->
            val intent = Intent(this, DonutActivity::class.java)
            startActivity(intent)
        }

        // Setup Drawer and Navigation View
        binding.navView.setNavigationItemSelectedListener { item: MenuItem? ->
            this.onNavigationItemSelected(
                item!!
            )
        }

        val toggle = ActionBarDrawerToggle(
            this, binding.main, binding.toolbar, R.string.open_nav, R.string.close_nav
        )
        binding.main.addDrawerListener(toggle)
        toggle.syncState()
    }

    /**
     * Sets up the listener that updates the UI when the user signs in or out.
     */
    private fun setupAuthStateListener() {
        authStateListener = AuthStateListener {
            updateUI()
            FirebaseHandler.updateUserData()
        }
    }

    override fun onStart() {
        super.onStart()
        FirebaseHandler.auth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        FirebaseHandler.auth.removeAuthStateListener(authStateListener)
    }

    /**
     * Updates the UI elements based on the current authentication state.
     * This includes menu visibility (login vs logout) and user profile info in the header.
     */
    fun updateUI() {
        val isLoggedIn = FirebaseHandler.currentUser != null
        val user: FirebaseUser? = FirebaseHandler.currentUser

        // Update navigation menu visibility
        val menu = binding.navView.menu
        val loginItem = menu.findItem(R.id.nav_login)
        val signupItem = menu.findItem(R.id.nav_signup)
        val logoutItem = menu.findItem(R.id.nav_logout)

        if (loginItem != null) loginItem.isVisible = !isLoggedIn
        if (signupItem != null) signupItem.isVisible = !isLoggedIn
        if (logoutItem != null) logoutItem.isVisible = isLoggedIn

        // Update navigation header with user info
        if (binding.navView.headerCount > 0) {
            val headerBinding = NavHeaderBinding.bind(binding.navView.getHeaderView(0))

            headerBinding.navHeaderTextViewName.text = if (user != null) user.displayName else "Not Logged In"
            headerBinding.navHeaderTextViewEmail.text = if (user != null) user.email else ""

            val imageView = headerBinding.navHeaderImageView
            if (user != null) {
                // Fetch additional user data (like profile image) from the database
                FirebaseHandler.userRef?.get()
                    ?.addOnCompleteListener { task: Task<DataSnapshot?>? ->
                        if (task!!.isSuccessful && task.getResult() != null) {
                            val userData = task.getResult()!!.getValue(User::class.java)
                            if (userData != null && userData.profileImageBase64 != null) {
                                val imageBitMap =
                                    base64ToBitmap(userData.profileImageBase64)
                                imageView.setImageBitmap(imageBitMap)
                            } else {
                                imageView.setImageResource(R.drawable.ic_person)
                            }
                        } else {
                            imageView.setImageResource(R.drawable.ic_person)
                        }
                    }
            } else {
                imageView.setImageResource(R.drawable.ic_person)
            }
        }
    }

    /**
     * Handles selection of items from the navigation drawer.
     * @param item The selected MenuItem.
     * @return True if the event was handled.
     */
    private fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (val id = item.itemId) {
            R.id.nav_logout -> {
                FirebaseHandler.signOut()
            }
            R.id.nav_login -> {
                val loginFragment = LoginFragment()
                loginFragment.show(supportFragmentManager, "LoginFragment")
            }
            R.id.nav_signup -> {
                val signUpFragment = SignUpDialogFragment()
                signUpFragment.show(supportFragmentManager, SignUpDialogFragment.TAG)
            }
        }

        binding.main.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Requests POST_NOTIFICATIONS permission for Android 13+.
     */
    private fun askNotificationPermission() {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
