package com.amibar.boggle.ui.mainmenu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amibar.boggle.R
import com.amibar.boggle.ui.theme.BoggleTheme

@Composable
fun LoginDialog(
    viewModel: LoginViewModel,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.state.collectAsState()

    val emailState = rememberTextFieldState()
    val passwordState = rememberTextFieldState()

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Done){
            onDismissRequest()
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        confirmButton = {
            Button(
                onClick = {
                    viewModel.loginUser(
                        email = emailState.text.toString(),
                        password = passwordState.text.toString()
                    )
                },
                enabled =   uiState !is LoginUiState.Loading &&
                            emailState.text.isNotBlank() &&
                            passwordState.text.isNotBlank()
            ) {
                Text(stringResource(R.string.login))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        text = {
            LoginDialogContent(
                emailState = emailState,
                passwordState = passwordState,
                uiState = uiState
            )
        }
    )
}

@Composable
fun LoginDialogContent(
    emailState: TextFieldState,
    passwordState: TextFieldState,
    uiState: LoginUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 32.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_login),
            contentDescription = "Login Icon",
            modifier = Modifier.size(100.dp)
        )

        TextField(
            state = emailState,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            label = { Text(stringResource(R.string.prompt_email)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        SecureTextField(
            state = passwordState,
            label = { Text(stringResource(R.string.prompt_password)) },
            modifier = Modifier.fillMaxWidth()
        )

        when(uiState) {
            is LoginUiState.Loading -> {
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator()
                uiState.message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
            is LoginUiState.Error -> {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            else -> {}
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginDialogContentPreview() {
    BoggleTheme {
        LoginDialogContent(
            emailState = rememberTextFieldState("john@example.com"),
            passwordState = rememberTextFieldState("password123"),
            uiState = LoginUiState.Idle
        )
    }
}

