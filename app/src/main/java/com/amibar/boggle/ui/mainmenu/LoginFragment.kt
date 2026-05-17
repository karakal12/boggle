package com.amibar.boggle.ui.mainmenu

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.amibar.boggle.data.FirebaseHandler
import com.amibar.boggle.databinding.FragmentLoginBinding
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

/**
 * A DialogFragment that provides a login interface for existing users.
 * It handles Firebase Authentication, error reporting, and updates the user's FCM token upon success.
 */
class LoginFragment
/**
 * Default constructor for LoginFragment.
 */
    : DialogFragment() {
    /** View binding for the fragment layout.  */
    private lateinit var binding: FragmentLoginBinding

    override fun onStart() {
        super.onStart()
        // Set dialog width to match parent for a consistent UI
        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding!!.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    /**
     * Initializes UI components and sets up the login button listener.
     */
    private fun init() {
        binding.loginButton.setOnClickListener { view: View? -> this.loginUser(view) }
    }


    /**
     * Attempts to sign in the user using Firebase Authentication.
     * Validates input, shows a progress dialog, and handles common authentication errors.
     * @param view The clicked view.
     */
    private fun loginUser(view: View?) {
        val email = binding.ETEmail.getText().toString()
        val password = binding.ETPassword.getText().toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all the fields", Toast.LENGTH_SHORT)
                .show()
            return
        }

        @Suppress("deprecation") val pd = ProgressDialog(requireContext())
        pd.setTitle("Connecting")
        pd.setMessage("Logging in...")
        pd.show()

        FirebaseHandler.auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(
                requireActivity()
            ) { task: Task<AuthResult?>? ->
                val toastMessage: String?
                pd.dismiss()
                if (task!!.isSuccessful()) {
                    Log.i(TAG, "signInWithEmail:success")


                    // Update FCM Token on successful login for push notifications
                    updateFcmToken()

                    toastMessage = "User logged in successfully"
                    dismiss()
                } else {
                    // Map Firebase exceptions to user-friendly messages
                    toastMessage = when (task.exception) {
                        is FirebaseAuthInvalidUserException -> "User does not exist"
                        is FirebaseAuthInvalidCredentialsException -> "Invalid Password"
                        is FirebaseNetworkException -> "Network Error. Please check your connection"
                        else -> "An error occurred. Please try again later"
                    }
                }
                Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Fetches the current Firebase Cloud Messaging (FCM) token and saves it to the user's database entry.
     * This is necessary for the user to receive game invitations via notifications.
     */
    private fun updateFcmToken() {
        FirebaseHandler.messaging.getToken()
            .addOnCompleteListener { task: Task<String?>? ->
                if (task!!.isSuccessful && task.getResult() != null) {
                    val token = task.getResult()
                    FirebaseHandler.userRef?.child("fcmToken")
                        ?.setValue(token)
                        ?.addOnSuccessListener(OnSuccessListener { _ ->
                            // Refresh local user data to include the new token
                            FirebaseHandler.updateUserData()
                        })
                        ?.addOnFailureListener { e: Exception? ->
                            Log.e(
                                TAG,
                                "Failed to update FCM token",
                                e
                            )
                        }
                } else {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                }
            }
    }

    companion object {
        /** Tag used for logging.  */
        private const val TAG = "LoginFragment"
    }
}
