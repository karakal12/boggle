package com.amibar.boggle.ui.mainmenu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.amibar.boggle.R;
import com.amibar.boggle.data.FirebaseHandler;
import com.amibar.boggle.data.User;
import com.amibar.boggle.ui.DonutActivity;
import com.amibar.boggle.ui.singleplayer.SinglePlayerActivity;
import com.amibar.boggle.utils.ImageUtils;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class MainActivity extends AppCompatActivity {
    private Button singlePlayerButton;
    private Button multiPlayerButton;
    private Button friendsButton;
    private Button leaderboardsButton;
    private ImageView donutButton;

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private FirebaseAuth.AuthStateListener authStateListener;

    private final ActivityResultLauncher<Intent> singlePlayerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    int score = result.getData().getIntExtra(SinglePlayerActivity.EXTRA_SCORE, 0);
                    Toast.makeText(this, "Game finished! Your score: " + score, Toast.LENGTH_LONG).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nav_view), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        setupAuthStateListener();
    }

    private void init(){
        singlePlayerButton = findViewById(R.id.singlePlayerButton);
        multiPlayerButton = findViewById(R.id.multiPlayerButton);
        friendsButton = findViewById(R.id.friendsListButton);
        leaderboardsButton = findViewById(R.id.leaderboardsButton);
        donutButton = findViewById(R.id.donutButton);
        drawerLayout = findViewById(R.id.main);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);

        singlePlayerButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SinglePlayerActivity.class);
            singlePlayerLauncher.launch(intent);
        });

        donutButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DonutActivity.class);
            startActivity(intent);
        });

        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupAuthStateListener() {
        authStateListener = firebaseAuth -> updateUI();
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

    private void updateUI() {
        boolean isLoggedIn = FirebaseHandler.getInstance().getCurrentUser() != null;
        FirebaseUser user = FirebaseHandler.getInstance().getCurrentUser();


        // Update navigation menu
        if (navigationView != null) {
            Menu menu = navigationView.getMenu();
            MenuItem loginItem = menu.findItem(R.id.nav_login);
            MenuItem signupItem = menu.findItem(R.id.nav_signup);
            MenuItem logoutItem = menu.findItem(R.id.nav_logout);

            if (loginItem != null) loginItem.setVisible(!isLoggedIn);
            if (signupItem != null) signupItem.setVisible(!isLoggedIn);
            if (logoutItem != null) logoutItem.setVisible(isLoggedIn);

            LinearLayout header = (LinearLayout) navigationView.getHeaderView(0);
            if (header != null) {
                ((TextView) header.findViewById(R.id.nav_header_textView_name)).setText
                        (user != null ? user.getDisplayName() : "Not Logged In");
                ((TextView) header.findViewById(R.id.nav_header_textView_email)).setText
                        (user != null ? user.getEmail() : "");
                // set profile image from firebase user
                ImageView imageView = header.findViewById(R.id.nav_header_imageView);
                if (user != null) {
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
    }

    private boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_logout) {
            FirebaseHandler.getAuth().signOut();
        } else if (id == R.id.nav_login) {
            LoginFragment loginFragment = new LoginFragment();
            loginFragment.show(getSupportFragmentManager(), "LoginFragment");
        } else if (id == R.id.nav_signup) {
            SignUpFragment signUpFragment = new SignUpFragment();
            signUpFragment.show(getSupportFragmentManager(), "SignUpFragment");
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
