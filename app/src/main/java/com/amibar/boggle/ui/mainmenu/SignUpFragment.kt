package com.amibar.boggle.ui.mainmenu;

import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.amibar.boggle.R;
import com.amibar.boggle.data.FirebaseHandler;
import com.amibar.boggle.data.User;
import com.amibar.boggle.databinding.FragmentSignUpBinding;
import com.amibar.boggle.utils.ImageUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;

import java.io.IOException;
import java.util.Objects;

/**
 * A DialogFragment that provides a sign-up interface for new users.
 * Handles user creation with Firebase Authentication, profile image selection, 
 * and storing user metadata in the Realtime Database.
 */
public class SignUpFragment extends DialogFragment {
    /** View binding for the fragment layout. */
    private FragmentSignUpBinding binding;

    /** Tag used for logging. */
    private static final String TAG = "SignUpFragment";

    /** View for displaying the selected profile image. */
    private ImageView IVProfileImage;
    /** Input field for the display name. */
    private TextInputEditText ETDisplayName;
    /** Input field for the email address. */
    private TextInputEditText ETEmail;
    /** Input field for the password. */
    private TextInputEditText ETPassword;
    /** Uri of the profile image selected from the gallery. */
    private Uri selectedImageUri;

    /** Launcher for the system photo picker. */
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    IVProfileImage.setImageURI(uri);
                } else {
                    Log.d(TAG, "No media selected");
                }
            });

    /**
     * Default constructor for SignUpFragment.
     */
    public SignUpFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        // Set dialog width to match parent for a consistent UI
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    /**
     * Initializes UI components and sets up click listeners for image selection and sign-up.
     */
    private void init() {
        IVProfileImage = binding.IVProfileImage;
        Button btnSelectImage = binding.btnSelectImage;
        ETDisplayName = binding.ETDisplayName;
        ETEmail = binding.ETEmail;
        ETPassword = binding.ETPassword;
        Button signup_button = binding.signupButton;

        btnSelectImage.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        signup_button.setOnClickListener(v -> createUser());
    }

    /**
     * Orchestrates the user creation process.
     * Validates inputs, creates an account with Firebase Auth, and initiates profile updates.
     */
    @SuppressWarnings("deprecation")
    private void createUser() {
        String displayName = Objects.requireNonNull(ETDisplayName.getText()).toString().trim();
        String email = Objects.requireNonNull(ETEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(ETPassword.getText()).toString().trim();

        if (displayName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog pd = new ProgressDialog(requireContext());
        pd.setTitle("Connecting");
        pd.setMessage("Creating User...");
        pd.setCancelable(false);
        pd.show();

        FirebaseHandler.getAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener((requireActivity()), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseHandler.getAuth().getCurrentUser();
                        if (user != null) {
                            String base64Image = null;
                            if (selectedImageUri != null) {
                                try {
                                    // Convert selected image to Base64 for database storage
                                    base64Image = ImageUtils.uriToBase64(selectedImageUri, requireContext());
                                } catch (IOException e) {
                                    Log.e(TAG, "Error converting image to Base64", e);
                                    // Use default person icon if conversion fails
                                    base64Image = ImageUtils.bitmapToBase64(BitmapFactory.decodeResource(getResources(), R.drawable.ic_person));
                                }
                            }
                            updateProfile(user, displayName, base64Image, pd);
                        }
                    } else {
                        pd.dismiss();
                        String toastMessage = switch (task.getException()){
                            case FirebaseAuthWeakPasswordException ignored -> "Password is too weak";
                            case FirebaseAuthInvalidCredentialsException ignored -> "Invalid Email Address";
                            case FirebaseAuthUserCollisionException ignored -> "User already exists";
                            case FirebaseNetworkException ignored -> "Network Error. Please check your connection";
                            case null, default -> "An error occurred. Please try again later";
                        };
                        Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /**
     * Updates the user's Firebase Authentication profile with their chosen display name.
     * @param user         The created FirebaseUser.
     * @param displayName  The chosen display name.
     * @param base64Image  The encoded profile image.
     * @param pd           The progress dialog to update.
     */
    @SuppressWarnings("deprecation")
    private void updateProfile(FirebaseUser user, String displayName, String base64Image, ProgressDialog pd) {
        pd.setMessage("Updating Profile...");
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        fetchFcmTokenAndSaveUser(user, displayName, base64Image, pd);
                    } else {
                        pd.dismiss();
                        Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Retrieves the FCM token for the device before saving the final user record.
     * This ensures the user is ready to receive notifications immediately.
     */
    @SuppressWarnings("deprecation")
    private void fetchFcmTokenAndSaveUser(FirebaseUser user, String displayName, String base64Image, ProgressDialog pd) {
        pd.setMessage("Fetching FCM Token...");
        FirebaseHandler.getMessaging().getToken().addOnCompleteListener(task -> {
            String token = null;
            if (task.isSuccessful()) {
                token = task.getResult();
            } else {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
            }
            saveUserToDatabase(user, displayName, base64Image, token, pd);
        });
    }

    /**
     * Saves the complete User object to the Firebase Realtime Database.
     * @param user         The FirebaseUser.
     * @param displayName  Display name.
     * @param base64Image  Encoded image.
     * @param fcmToken     Device token.
     * @param pd           The progress dialog to dismiss.
     */
    @SuppressWarnings("deprecation")
    private void saveUserToDatabase(FirebaseUser user, String displayName, String base64Image, String fcmToken, ProgressDialog pd) {
        pd.setMessage("Saving User Data...");
        User newUser = new User(user.getUid(), displayName, user.getEmail(), base64Image, fcmToken);

        DatabaseReference userRef = FirebaseHandler.getInstance().getRootRef().child("users").child(user.getUid());
        userRef.setValue(newUser)
                .addOnCompleteListener(task -> {
                    pd.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(), "User created successfully!", Toast.LENGTH_SHORT).show();
                        if (getActivity() instanceof MainActivity mainActivity){
                            mainActivity.updateUI();
                        }
                        dismiss();
                    } else {
                        Toast.makeText(requireContext(), "Failed to save user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
