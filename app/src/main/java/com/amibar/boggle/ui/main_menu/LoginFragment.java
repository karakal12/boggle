package com.amibar.boggle.ui.main_menu;

import android.app.Dialog;
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

import com.amibar.boggle.R;
import com.amibar.boggle.data.FirebaseHandler;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends DialogFragment {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        return dialog;
    }

    private void init(View view){
        Button login_button = view.findViewById(R.id.login_button);
        ETPassword = view.findViewById(R.id.ETPassword);
        ETEmail = view.findViewById(R.id.ETEmail);

        login_button.setOnClickListener(this::loginUser);
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
