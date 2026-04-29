package com.amibar.boggle.ui.mainmenu;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amibar.boggle.R;
import com.amibar.boggle.data.FirebaseHandler;
import com.amibar.boggle.data.PlayerRole;
import com.amibar.boggle.data.User;
import com.amibar.boggle.databinding.ActivityMainBinding;
import com.amibar.boggle.databinding.NavHeaderBinding;
import com.amibar.boggle.ui.DonutActivity;
import com.amibar.boggle.ui.game.multiplayer.JoinOrCreateRoomFragment;
import com.amibar.boggle.ui.game.singleplayer.SingleplayerActivity;
import com.amibar.boggle.utils.ImageUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * The primary entry point of the application.
 * Manages the main navigation drawer, handles authentication state changes,
 * and provides access to different game modes and user features.
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class MainActivity extends AppCompatActivity {
    /** View binding for the activity layout. */
    private ActivityMainBinding binding;

    /** Listener for Firebase Authentication state changes. */
    private FirebaseAuth.AuthStateListener authStateListener;

    /**
     * Launcher for SingleplayerActivity to receive the final score when the game ends.
     */
    private final ActivityResultLauncher<Intent> singleplayerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    int score = result.getData().getIntExtra(SingleplayerActivity.EXTRA_SCORE, 0);
                    Toast.makeText(this, "Game finished! Your score: " + score, Toast.LENGTH_LONG).show();
                }
            }
    );

    /**
     * Launcher for requesting notification permissions (Android 13+).
     */
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Notifications disabled. You won't receive game invites.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Enable edge-to-edge display
        EdgeToEdge.enable(this);
        
        // Handle window insets for both the main content and the navigation drawer
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContent, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(binding.navView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        askNotificationPermission();
        setupAuthStateListener();
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    /**
     * Processes incoming intents, specifically for joining multiplayer rooms from notifications.
     * @param intent The intent to handle.
     */
    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("roomCode")) {
            String roomCode = intent.getStringExtra("roomCode");
            if (roomCode != null && !roomCode.isEmpty()) {
                PlayerRole role = PlayerRole.host;
                if (intent.hasExtra("action") && "join".equals(intent.getStringExtra("action"))){
                    role = PlayerRole.guest;
                }
                JoinOrCreateRoomFragment.newInstance(roomCode, role)
                        .show(getSupportFragmentManager(), JoinOrCreateRoomFragment.TAG);
            }
        }
    }

    /**
     * Initializes UI components, toolbar, and click listeners.
     */
    private void init(){
        setSupportActionBar(binding.toolbar);

        // Navigation for Singleplayer
        binding.singleplayerButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SingleplayerActivity.class);
            singleplayerLauncher.launch(intent);
        });

        // Navigation for Multiplayer - requires login
        binding.multiplayerButton.setOnClickListener(v -> {
            if (FirebaseHandler.getAuth().getCurrentUser() != null){
                JoinOrCreateRoomFragment fragment = new JoinOrCreateRoomFragment();
                fragment.show(getSupportFragmentManager(), JoinOrCreateRoomFragment.TAG);
            } else {
                Toast.makeText(this, "Please sign in to play multiplayer", Toast.LENGTH_SHORT).show();
            }
        });

        // Navigation for Friend List
        binding.friendsListButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, FriendListActivity.class);
            startActivity(intent);
        });

        // Easter Egg / Bonus feature
        binding.donutButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DonutActivity.class);
            startActivity(intent);
        });

        // Setup Drawer and Navigation View
        binding.navView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.main, binding.toolbar, R.string.open_nav, R.string.close_nav);
        binding.main.addDrawerListener(toggle);
        toggle.syncState();
    }

    /**
     * Sets up the listener that updates the UI when the user signs in or out.
     */
    private void setupAuthStateListener() {
        authStateListener = firebaseAuth -> {
            updateUI();
            FirebaseHandler.getInstance().updateUserData();
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseHandler.getAuth().addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            FirebaseHandler.getAuth().removeAuthStateListener(authStateListener);
        }
    }

    /**
     * Updates the UI elements based on the current authentication state.
     * This includes menu visibility (login vs logout) and user profile info in the header.
     */
    void updateUI() {
        boolean isLoggedIn = FirebaseHandler.getInstance().getCurrentUser() != null;
        FirebaseUser user = FirebaseHandler.getInstance().getCurrentUser();

        // Update navigation menu visibility
        Menu menu = binding.navView.getMenu();
        MenuItem loginItem = menu.findItem(R.id.nav_login);
        MenuItem signupItem = menu.findItem(R.id.nav_signup);
        MenuItem logoutItem = menu.findItem(R.id.nav_logout);

        if (loginItem != null) loginItem.setVisible(!isLoggedIn);
        if (signupItem != null) signupItem.setVisible(!isLoggedIn);
        if (logoutItem != null) logoutItem.setVisible(isLoggedIn);

        // Update navigation header with user info
        if (binding.navView.getHeaderCount() > 0) {
            NavHeaderBinding headerBinding = NavHeaderBinding.bind(binding.navView.getHeaderView(0));

            headerBinding.navHeaderTextViewName
                    .setText(user != null ? user.getDisplayName() : "Not Logged In");
            headerBinding.navHeaderTextViewEmail
                    .setText(user != null ? user.getEmail() : "");

            ImageView imageView = headerBinding.navHeaderImageView;
            if (user != null) {
                // Fetch additional user data (like profile image) from the database
                FirebaseHandler.getInstance().getUserRef().get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        User userData = task.getResult().getValue(User.class);
                        if (userData != null && userData.getProfileImageBase64() != null) {
                            Bitmap imageBitMap = ImageUtils.base64ToBitmap(userData.getProfileImageBase64());
                            imageView.setImageBitmap(imageBitMap);
                        } else {
                            imageView.setImageResource(R.drawable.ic_person);
                        }
                    } else {
                        imageView.setImageResource(R.drawable.ic_person);
                    }
                });
            } else {
                imageView.setImageResource(R.drawable.ic_person);
            }
        }
    }

    /**
     * Handles selection of items from the navigation drawer.
     * @param item The selected MenuItem.
     * @return True if the event was handled.
     */
    private boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_logout) {
            FirebaseHandler.getInstance().signOut();
        } else if (id == R.id.nav_login) {
            LoginFragment loginFragment = new LoginFragment();
            loginFragment.show(getSupportFragmentManager(), "LoginFragment");
        } else if (id == R.id.nav_signup) {
            SignUpFragment signUpFragment = new SignUpFragment();
            signUpFragment.show(getSupportFragmentManager(), "SignUpFragment");
        }

        binding.main.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Requests POST_NOTIFICATIONS permission for Android 13+.
     */
    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
