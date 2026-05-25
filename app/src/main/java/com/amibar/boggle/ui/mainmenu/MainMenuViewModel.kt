package com.amibar.boggle.ui.mainmenu

import androidx.lifecycle.ViewModel
import com.amibar.boggle.data.FirebaseHandler
import com.amibar.boggle.data.PlayerRole
import com.amibar.boggle.data.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class MainMenuDialog {
    None, Login, SignUp, JoinOrCreateRoom
}

data class MainMenuUiState(
    val currentUser: User? = null,
    val showingDialog: MainMenuDialog = MainMenuDialog.None,
    val initialRoomCode: String? = null,
    val initialPlayerRole: PlayerRole = PlayerRole.Guest,
    val navEvent: Pair<String, PlayerRole>? = null
)

class MainMenuViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainMenuUiState())
    val uiState = _uiState.asStateFlow()

    fun navigateToMultiplayer(roomCode: String, role: PlayerRole) {
        _uiState.value = _uiState.value.copy(navEvent = roomCode to role)
    }

    fun onNavigated() {
        _uiState.value = _uiState.value.copy(navEvent = null)
    }

    fun showDialog(dialog: MainMenuDialog, roomCode: String? = null, role: PlayerRole = PlayerRole.Guest) {
        _uiState.value = _uiState.value.copy(
            showingDialog = dialog,
            initialRoomCode = roomCode,
            initialPlayerRole = role
        )
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(showingDialog = MainMenuDialog.None)
    }

    fun updateCurrentUser(firebaseUser: FirebaseUser?) {
        if (firebaseUser == null) {
            _uiState.value = _uiState.value.copy(currentUser = null)
            return
        }

        // Initially populate with what we have from FirebaseUser
        val initialUser = User(
            uid = firebaseUser.uid,
            displayName = firebaseUser.displayName ?: "User",
            email = firebaseUser.email ?: ""
        )
        _uiState.value = _uiState.value.copy(currentUser = initialUser)

        // Then fetch full user data from database
        FirebaseHandler.userRef?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                val userData = task.result!!.getValue(User::class.java)
                if (userData != null) {
                    _uiState.value = _uiState.value.copy(currentUser = userData)
                }
            }
        }
    }
}
