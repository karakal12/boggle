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
 * A DialogFragment that hosts the Compose-based SignUpDialog.
 */
class SignUpDialogFragment : DialogFragment() {
    private val viewModel: SignUpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BoggleTheme {
                SignUpDialog(
                    viewModel = viewModel,
                    onDismissRequest = { dismiss() }
                )
            }
        }
    }

    companion object {
        const val TAG = "SignUpDialogFragment"
    }
}
