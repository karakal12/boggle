package com.amibar.boggle.ui.mainmenu

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amibar.boggle.data.FirebaseHandler
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DatabaseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Done : LoginUiState
    data class Loading(val message: String?) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val state: StateFlow<LoginUiState> = _uiState

    fun loginUser(email: String, password: String) {
        if (email.isBlank() || password.isBlank()){
            _uiState.value = LoginUiState.Error("Please fill all fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading("Logging in...")

            try {
                FirebaseHandler.auth.signInWithEmailAndPassword(email, password).await()
                updateFcmToken()
                _uiState.value = LoginUiState.Done
            } catch (e: FirebaseException) {
                val message = when(e) {
                    is FirebaseAuthInvalidUserException -> "User does not exist"
                    is FirebaseAuthInvalidCredentialsException -> "Invalid Password"
                    is FirebaseNetworkException -> "Network Error. Please check your connection"
                    else -> "An error occurred. Please try again later"
                }
                _uiState.value = LoginUiState.Error(message)
            }
        }
    }

    private suspend fun updateFcmToken() {
        try {
            val token: String = FirebaseHandler.messaging.token.await()
            FirebaseHandler.userRef?.child("fcmToken")?.setValue(token)?.await()
            FirebaseHandler.updateUserData()
        } catch (e: DatabaseException) {
            Log.e(TAG, "failed to write to database", e)
        } catch (e: FirebaseException) {
            Log.w(TAG, "Fetching FCM registration token failed", e)
        }
    }

    companion object {
        private const val TAG = "LoginDialogViewModel"
    }
}