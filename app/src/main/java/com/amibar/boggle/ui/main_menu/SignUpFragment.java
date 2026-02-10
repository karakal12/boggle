package com.amibar.boggle.ui.main_menu;

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
import com.amibar.boggle.utils.ImageUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.io.IOException;
import java.util.Objects;

public class SignUpFragment extends DialogFragment {

    private static final String TAG = "SignUpFragment";

    private ImageView IVProfileImage;
    private TextInputEditText ETDisplayName, ETEmail, ETPassword;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    IVProfileImage.setImageURI(uri);
                } else {
                    Log.d(TAG, "No media selected");
                }
            });

    public SignUpFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }

    private void init(View view) {
        IVProfileImage = view.findViewById(R.id.IVProfileImage);
        Button btnSelectImage = view.findViewById(R.id.btnSelectImage);
        ETDisplayName = view.findViewById(R.id.ETDisplayName);
        ETEmail = view.findViewById(R.id.ETEmail);
        ETPassword = view.findViewById(R.id.ETPassword);
        Button signup_button = view.findViewById(R.id.signup_button);

        btnSelectImage.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        signup_button.setOnClickListener(v -> createUser());
    }

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
                                    base64Image = ImageUtils.uriToBase64(selectedImageUri, requireContext());
                                } catch (IOException e) {
                                    // Handle the exception by logging it and setting a default image
                                    Log.e(TAG, "Error converting image to Base64", e);
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


    @SuppressWarnings("deprecation")
    private void updateProfile(FirebaseUser user, String displayName, String base64Image, ProgressDialog pd) {
        pd.setMessage("Updating Profile...");
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserToDatabase(user, displayName, base64Image, pd);
                    } else {
                        pd.dismiss();
                        Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressWarnings("deprecation")
    private void saveUserToDatabase(FirebaseUser user, String displayName, String base64Image, ProgressDialog pd) {
        pd.setMessage("Saving User Data...");
        User newUser = new User(displayName, user.getEmail(), base64Image);

        FirebaseHandler.getInstance().getUserRef().setValue(newUser)
                .addOnCompleteListener(task -> {
                    pd.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(), "User created successfully!", Toast.LENGTH_SHORT).show();
                        dismiss();
                    } else {
                        Toast.makeText(requireContext(), "Failed to save user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
