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

public class LoginFragment extends DialogFragment {
    FragmentLoginBinding binding;

    private static final String TAG = "LoginFragment";

    private EditText ETEmail;
    private EditText ETPassword;

    public LoginFragment() {
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void init(){
        Button loginButton = binding.loginButton;
        ETPassword = binding.ETPassword;
        ETEmail = binding.ETEmail;

        loginButton.setOnClickListener(this::loginUser);
    }



    private void loginUser(View view){
        String email = ETEmail.getText().toString();
        String password = ETPassword.getText().toString();
        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(requireContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }
        ProgressDialog pd = new ProgressDialog(requireContext());
        pd.setTitle("Connecting");
        pd.setMessage("Logging in...");
        pd.show();
        FirebaseHandler.getAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    String toastMessage;
                    pd.dismiss();
                    if (task.isSuccessful()){
                        Log.i("LoginFragment", "signInWithEmail:success");
                        FirebaseUser user = FirebaseHandler.getAuth().getCurrentUser();
                        assert user != null;
                        toastMessage = "User logged in successfully\nUid: "+user.getUid();
                        dismiss();
                    } else {
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
}
