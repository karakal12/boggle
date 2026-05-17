package com.amibar.boggle.ui.mainmenu

import android.app.ProgressDialog
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.fragment.app.DialogFragment
import com.amibar.boggle.R
import com.amibar.boggle.data.FirebaseHandler
import com.amibar.boggle.data.User
import com.amibar.boggle.databinding.FragmentSignUpBinding
import com.amibar.boggle.utils.ImageUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import java.io.IOException

/**
 * A DialogFragment that provides a sign-up interface for new users.
 * Handles user creation with Firebase Authentication, profile image selection,
 * and storing user metadata in the Realtime Database.
 */
class SignUpFragment
/**
 * Default constructor for SignUpFragment.
 */
    : DialogFragment() {
    /** View binding for the fragment layout.  */
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    /** Uri of the profile image selected from the gallery.  */
    private var selectedImageUri: Uri? = null

    /** Launcher for the system photo picker.  */
    private val pickMedia = registerForActivityResult(
        PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            binding.IVProfileImage.setImageURI(uri)
        } else {
            Log.d(TAG, "No media selected")
        }
    }

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
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    /**
     * Initializes UI components and sets up click listeners for image selection and sign-up.
     */
    private fun init() {
        binding.btnSelectImage.setOnClickListener {
            pickMedia.launch(
                PickVisualMediaRequest.Builder()
                    .setMediaType(ImageOnly)
                    .build()
            )
        }

        binding.signupButton.setOnClickListener { createUser() }
    }

    /**
     * Orchestrates the user creation process.
     * Validates inputs, creates an account with Firebase Auth, and initiates profile updates.
     */
    @Suppress("deprecation")
    private fun createUser() {
        val displayName = binding.ETDisplayName.text.toString().trim()
        val email = binding.ETEmail.text.toString().trim()
        val password = binding.ETPassword.text.toString().trim()

        if (displayName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val pd = ProgressDialog(requireContext())
        pd.setTitle("Connecting")
        pd.setMessage("Creating User...")
        pd.setCancelable(false)
        pd.show()

        FirebaseHandler.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(
                (requireActivity())
            ) { task: Task<AuthResult?>? ->
                if (task!!.isSuccessful) {
                    val user: FirebaseUser? =
                        FirebaseHandler.auth.currentUser
                    if (user != null) {
                        var base64Image: String? = null
                        if (selectedImageUri != null) {
                            try {
                                // Convert selected image to Base64 for database storage
                                base64Image = ImageUtils.uriToBase64(selectedImageUri!!, requireContext())
                            } catch (e: IOException) {
                                Log.e(TAG, "Error converting image to Base64", e)
                                // Use default person icon if conversion fails
                                base64Image = ImageUtils.bitmapToBase64(
                                    BitmapFactory.decodeResource(
                                        resources,
                                        R.drawable.ic_person
                                    )
                                )
                            }
                        }
                        updateProfile(user, displayName, base64Image, pd)
                    }
                } else {
                    pd.dismiss()
                    val toastMessage = when (task.exception) {
                        is FirebaseAuthWeakPasswordException -> "Password is too weak"
                        is FirebaseAuthInvalidCredentialsException -> "Invalid Email Address"
                        is FirebaseAuthUserCollisionException -> "User already exists"
                        is FirebaseNetworkException -> "Network Error. Please check your connection"
                        else -> "An error occurred. Please try again later"
                    }
                    Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }


    /**
     * Updates the user's Firebase Authentication profile with their chosen display name.
     * @param user         The created FirebaseUser.
     * @param displayName  The chosen display name.
     * @param base64Image  The encoded profile image.
     * @param pd           The progress dialog to update.
     */
    @Suppress("deprecation")
    private fun updateProfile(
        user: FirebaseUser,
        displayName: String,
        base64Image: String?,
        pd: ProgressDialog
    ) {
        pd.setMessage("Updating Profile...")
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task: Task<Void?>? ->
                if (task!!.isSuccessful()) {
                    fetchFcmTokenAndSaveUser(user, displayName, base64Image, pd)
                } else {
                    pd.dismiss()
                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    /**
     * Retrieves the FCM token for the device before saving the final user record.
     * This ensures the user is ready to receive notifications immediately.
     */
    @Suppress("deprecation")
    private fun fetchFcmTokenAndSaveUser(
        user: FirebaseUser,
        displayName: String,
        base64Image: String?,
        pd: ProgressDialog
    ) {
        pd.setMessage("Fetching FCM Token...")
        FirebaseHandler.messaging.getToken()
            .addOnCompleteListener { task: Task<String?>? ->
                var token: String? = null
                if (task!!.isSuccessful()) {
                    token = task.getResult()
                } else {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException())
                }
                saveUserToDatabase(user, displayName, base64Image, token, pd)
            }
    }

    /**
     * Saves the complete User object to the Firebase Realtime Database.
     * @param user         The FirebaseUser.
     * @param displayName  Display name.
     * @param base64Image  Encoded image.
     * @param fcmToken     Device token.
     * @param pd           The progress dialog to dismiss.
     */
    @Suppress("deprecation")
    private fun saveUserToDatabase(
        user: FirebaseUser,
        displayName: String,
        base64Image: String?,
        fcmToken: String?,
        pd: ProgressDialog
    ) {
        pd.setMessage("Saving User Data...")
        val newUser = User(user.uid, displayName, user.email!!, base64Image, fcmToken)

        val userRef: DatabaseReference =
            FirebaseHandler.rootRef.child("users").child(user.uid)
        userRef.setValue(newUser)
            .addOnCompleteListener { task: Task<Void?>? ->
                pd.dismiss()
                if (task!!.isSuccessful()) {
                    Toast.makeText(
                        requireContext(),
                        "User created successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    (activity as? MainActivity)?.updateUI()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "Failed to save user data", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    companion object {
        /** Tag used for logging.  */
        private const val TAG = "SignUpFragment"
    }
}
