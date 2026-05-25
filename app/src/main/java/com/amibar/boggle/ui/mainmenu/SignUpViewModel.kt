package com.amibar.boggle.ui.mainmenu

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.res.imageResource
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amibar.boggle.R
import com.amibar.boggle.data.FirebaseHandler
import com.amibar.boggle.data.FirebaseHandler.auth
import com.amibar.boggle.data.FirebaseHandler.currentUser
import com.amibar.boggle.data.User
import com.amibar.boggle.utils.bitmapToBase64
import com.amibar.boggle.utils.uriToBase64
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface SignUpUiState {
    object Idle : SignUpUiState
    object Done : SignUpUiState
    data class Loading(val message: String?) : SignUpUiState
    data class Error(val message: String) : SignUpUiState
}

class SignUpViewModel(
    application: Application
) : AndroidViewModel(application){
    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val state: StateFlow<SignUpUiState> = _uiState

    private val defaultBase64Image: String? by lazy {
        bitmapToBase64(
            ImageBitmap.imageResource(
                res = getApplication<Application>().resources,
                id = R.drawable.ic_person
            ).asAndroidBitmap()
        )
    }


    fun createUser(displayName: String, email: String, password: String, selectedImage: Uri?) {
        if (displayName.isEmpty() || email.isEmpty() || password.isEmpty()){
            _uiState.value = SignUpUiState.Error("Please fill all fields")
            return
        }

        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext

            _uiState.value = SignUpUiState.Loading("Creating User...")
            try {
                    auth.createUserWithEmailAndPassword(email, password).await()
                    val base64Image = if (selectedImage != null) {
                        uriToBase64(selectedImage, context)
                    } else {
                        defaultBase64Image
                    }
                    updateProfile(currentUser!!, displayName, base64Image)
                    _uiState.value = SignUpUiState.Done

            } catch (e: FirebaseException) {
                val message = when(e) {
                    is FirebaseAuthWeakPasswordException -> "Password is too weak"
                    is FirebaseAuthInvalidCredentialsException -> "Invalid Email Address"
                    is FirebaseAuthUserCollisionException -> "User already exists"
                    is FirebaseNetworkException -> "Network Error. Please check your connection"
                    else -> "An error occurred. Please try again later"
                }
                _uiState.value = SignUpUiState.Error(message)
            }
        }
    }


    /**
     * Updates the user's Firebase Authentication profile with their chosen display name.
     * @param user         The created FirebaseUser.
     * @param displayName  The chosen display name.
     * @param base64Image  The encoded profile image.
     */
    private suspend fun updateProfile(
        user: FirebaseUser,
        displayName: String,
        base64Image: String?,
    ) {
        _uiState.value = SignUpUiState.Loading("Updating User...")

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()

        try {
            user.updateProfile(profileUpdates).await()
            fetchFcmTokenAndSaveUser(user, displayName, base64Image)
        } catch (_: Exception) {
            _uiState.value = SignUpUiState.Error("Failed to update profile")
        }
    }

    /**
     * Retrieves the FCM token for the device before saving the final user record.
     * This ensures the user is ready to receive notifications immediately.
     */
    private suspend fun fetchFcmTokenAndSaveUser(
        user: FirebaseUser,
        displayName: String,
        base64Image: String?
    ) {
        _uiState.value = SignUpUiState.Loading("Fetching FCM Token...")

        try {
            val token: String = FirebaseHandler.messaging.token.await()
            saveUserToDatabase(user, displayName, base64Image, token)
        } catch (e: Exception) {
            Log.e(TAG, "Fetching FCM registration token failed", e)
        }
    }

    /**
     * Saves the complete User object to the Firebase Realtime Database.
     * @param user         The FirebaseUser.
     * @param displayName  Display name.
     * @param base64Image  Encoded image.
     * @param fcmToken     Device token.
     */
    private suspend fun saveUserToDatabase(
        user: FirebaseUser,
        displayName: String,
        base64Image: String?,
        fcmToken: String?
    ) {
        _uiState.value = SignUpUiState.Loading("Saving User Data...")
        val newUser = User(user.uid, displayName, user.email!!, base64Image, fcmToken)

        val userRef: DatabaseReference =
            FirebaseHandler.rootRef.child("users").child(user.uid)
        try {
            userRef.setValue(newUser).await()
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = SignUpUiState.Error("Failed to save user data")
        }
    }

    companion object {
        /** Tag used for logging.  */
        const val TAG = "SignUpViewModel"
    }
}
