package com.amibar.boggle.ui.mainmenu

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amibar.boggle.R
import com.amibar.boggle.data.FirebaseHandler
import com.amibar.boggle.data.PlayerRole
import com.amibar.boggle.ui.donuteasteregg.DonutActivity
import com.amibar.boggle.ui.game.multiplayer.JoinOrCreateRoomDialog
import com.amibar.boggle.ui.game.singleplayer.SingleplayerActivity
import com.amibar.boggle.ui.theme.BoggleTheme
import com.amibar.boggle.utils.base64ToBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import kotlinx.coroutines.launch

/**
 * The primary entry point of the application.
 * Manages the main navigation drawer, handles authentication state changes,
 * and provides access to different game modes and user features.
 */
class MainActivity : AppCompatActivity() {

    private val viewModel: MainMenuViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private val signUpViewModel: SignUpViewModel by viewModels()

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
        enableEdgeToEdge()

        setContent {
            BoggleTheme {
                val uiState by viewModel.uiState.collectAsState()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

                MainMenuScreenContent(
                    uiState = uiState,
                    drawerState = drawerState,
                    loginViewModel = loginViewModel,
                    signUpViewModel = signUpViewModel,
                    onLogoutClick = { FirebaseHandler.signOut() },
                    onLoginClick = { viewModel.showDialog(MainMenuDialog.Login) },
                    onSignUpClick = { viewModel.showDialog(MainMenuDialog.SignUp) },
                    onDismissDialog = { viewModel.dismissDialog() },
                    onSingleplayerClick = {
                        val intent = Intent(this, SingleplayerActivity::class.java)
                        singleplayerLauncher.launch(intent)
                    },
                    onMultiplayerClick = {
                        if (FirebaseHandler.auth.currentUser != null) {
                            viewModel.showDialog(MainMenuDialog.JoinOrCreateRoom)
                        } else {
                            Toast.makeText(this, "Please sign in to play multiplayer", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onFriendsListClick = {
                        startActivity(Intent(this, FriendListActivity::class.java))
                    },
                    onDonutClick = {
                        startActivity(Intent(this, DonutActivity::class.java))
                    }
                )
            }
        }

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
                viewModel.showDialog(MainMenuDialog.JoinOrCreateRoom, roomCode, role)
            }
        }
    }

    /**
     * Sets up the listener that updates the UI when the user signs in or out.
     */
    private fun setupAuthStateListener() {
        authStateListener = AuthStateListener { auth ->
            viewModel.updateCurrentUser(auth.currentUser)
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
     * Requests POST_NOTIFICATIONS permission for Android 13+.
     */
    private fun askNotificationPermission() {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
fun MainMenuScreenContent(
    uiState: MainMenuUiState,
    modifier: Modifier = Modifier,
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    loginViewModel: LoginViewModel? = null,
    signUpViewModel: SignUpViewModel? = null,
    onLogoutClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    onDismissDialog: () -> Unit = {},
    onSingleplayerClick: () -> Unit = {},
    onMultiplayerClick: () -> Unit = {},
    onFriendsListClick: () -> Unit = {},
    onDonutClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            val user = uiState.currentUser
            val profileBitmap = remember(user?.profileImageBase64) {
                user?.profileImageBase64?.let { base64ToBitmap(it)?.asImageBitmap() }
            }
            ModalDrawerSheet {
                Column {
                    DrawerHeader(
                        profilePicture = profileBitmap,
                        name = user?.displayName,
                        email = user?.email
                    )
                    Spacer(Modifier.height(12.dp))
                    if (uiState.currentUser != null) {
                        NavigationDrawerItem(
                            icon = { Icon(painterResource(R.drawable.ic_logout), null) },
                            label = { Text("Logout") },
                            selected = false,
                            onClick = {
                                onLogoutClick()
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    } else {
                        NavigationDrawerItem(
                            icon = { Icon(painterResource(R.drawable.ic_login), null) },
                            label = { Text("Login") },
                            selected = false,
                            onClick = {
                                onLoginClick()
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.PersonAdd, null) },
                            label = { Text("Sign Up") },
                            selected = false,
                            onClick = {
                                onSignUpClick()
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            modifier = modifier,
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .height(TopAppBarDefaults.MediumAppBarCollapsedHeight)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                scope.launch { drawerState.open() }
                            }
                        ) {
                            Icon(Icons.Default.Menu, "Open Menu")
                        }
                        Spacer(Modifier.weight(1f))
                    }
                }
            },
            // secret donut
            floatingActionButton = {
                IconButton(
                    onClick = onDonutClick,
                    modifier = Modifier.size(75.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_donut),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = stringResource(R.string.boggle_welcome_message),
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onSingleplayerClick
                ) {
                    Text(stringResource(R.string.single_player_button))
                }
                Button(
                    onClick = onMultiplayerClick
                ) {
                    Text(stringResource(R.string.multiplayer_button))
                }
                Button(
                    onClick = onFriendsListClick
                ) {
                    Text(stringResource(R.string.friends_list_button))
                }
            }
        }
    }

    when (uiState.showingDialog) {
        MainMenuDialog.Login -> {
            loginViewModel?.let {
                LoginDialog(
                    viewModel = it,
                    onDismissRequest = onDismissDialog
                )
            }
        }
        MainMenuDialog.SignUp -> {
            signUpViewModel?.let {
                SignUpDialog(
                    viewModel = it,
                    onDismissRequest = onDismissDialog
                )
            }
        }
        MainMenuDialog.JoinOrCreateRoom -> {
            JoinOrCreateRoomDialog(
                onDismissRequest = onDismissDialog,
                initialRoomCode = uiState.initialRoomCode,
                initialPlayerRole = uiState.initialPlayerRole
            )
        }
        MainMenuDialog.None -> {}
    }
}

@Composable
fun DrawerHeader(profilePicture: ImageBitmap?, name: String?, email: String?, modifier: Modifier = Modifier) {
    BoggleTheme(
        darkTheme = true
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(176.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                if (profilePicture != null && !name.isNullOrEmpty() && !email.isNullOrEmpty()) {
                    Icon(
                        bitmap = profilePicture,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(64.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_person),
                        contentDescription = "Default Profile Picture",
                        modifier = Modifier.size(64.dp)
                    )
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    text = name ?: "Not Logged In",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = email ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainMenuScreenContentPreview() {
    BoggleTheme {
        MainMenuScreenContent(
            MainMenuUiState()
        )
    }
}
