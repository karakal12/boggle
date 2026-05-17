package com.amibar.boggle.ui.mainmenu;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.amibar.boggle.data.FirebaseHandler;
import com.amibar.boggle.databinding.FragmentLoginBinding;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

/**
 * A DialogFragment that provides a login interface for existing users.
 * It handles Firebase Authentication, error reporting, and updates the user's FCM token upon success.
 */
public class LoginFragment extends DialogFragment {
    /** View binding for the fragment layout. */
    private FragmentLoginBinding binding;

    /** Tag used for logging. */
    private static final String TAG = "LoginFragment";

    /** Input field for user email. */
    private EditText ETEmail;
    /** Input field for user password. */
    private EditText ETPassword;

    /**
     * Default constructor for LoginFragment.
     */
    public LoginFragment() {
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
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    /**
     * Initializes UI components and sets up the login button listener.
     */
    private void init(){
        Button loginButton = binding.loginButton;
        ETPassword = binding.ETPassword;
        ETEmail = binding.ETEmail;

        loginButton.setOnClickListener(this::loginUser);
    }


    /**
     * Attempts to sign in the user using Firebase Authentication.
     * Validates input, shows a progress dialog, and handles common authentication errors.
     * @param view The clicked view.
     */
    private void loginUser(View view){
        String email = ETEmail.getText().toString();
        String password = ETPassword.getText().toString();
        
        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(requireContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        @SuppressWarnings("deprecation")
        ProgressDialog pd = new ProgressDialog(requireContext());
        pd.setTitle("Connecting");
        pd.setMessage("Logging in...");
        pd.show();
        
        FirebaseHandler.getAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    String toastMessage;
                    pd.dismiss();
                    if (task.isSuccessful()){
                        Log.i(TAG, "signInWithEmail:success");
                        
                        // Update FCM Token on successful login for push notifications
                        updateFcmToken();
                        
                        toastMessage = "User logged in successfully";
                        dismiss();
                    } else {
                        // Map Firebase exceptions to user-friendly messages
                        toastMessage = switch (task.getException()){
                            case FirebaseAuthInvalidUserException ignored -> "User does not exist";
                            case FirebaseAuthInvalidCredentialsException ignored -> "Invalid Password";
                            case FirebaseNetworkException ignored -> "Network Error. Please check your connection";
                            case null, default                              -> "An error occurred. Please try again later";
                        };
                    }
                    Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Fetches the current Firebase Cloud Messaging (FCM) token and saves it to the user's database entry.
     * This is necessary for the user to receive game invitations via notifications.
     */
    private void updateFcmToken() {
        FirebaseHandler.getMessaging().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String token = task.getResult();
                FirebaseHandler.getInstance().getUserRef().child("fcmToken").setValue(token)
                        .addOnSuccessListener(aVoid -> {
                            // Refresh local user data to include the new token
                            FirebaseHandler.getInstance().updateUserData();
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to update FCM token", e));
            } else {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
            }
        });
    }
}
