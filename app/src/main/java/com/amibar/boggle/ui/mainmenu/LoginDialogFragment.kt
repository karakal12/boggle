package com.amibar.boggle.ui.mainmenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.amibar.boggle.ui.theme.BoggleTheme

/**
 * A DialogFragment that provides a login interface for existing users.
 * It handles Firebase Authentication, error reporting, and updates the user's FCM token upon success.
 */
class LoginDialogFragment : DialogFragment() {
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BoggleTheme {
                LoginDialog(
                    viewModel = viewModel,
                    onDismissRequest = { dismiss() }
                )
            }
        }
    }

    companion object {
        /** Tag used for logging.  */
        const val TAG = "LoginFragment"
    }
}
