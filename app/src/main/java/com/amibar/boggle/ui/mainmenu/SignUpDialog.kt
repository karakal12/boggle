package com.amibar.boggle.ui.mainmenu

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.amibar.boggle.R
import com.amibar.boggle.utils.uriToBitmap

@Composable
fun SignUpDialog(
    viewModel: SignUpViewModel,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.state.collectAsState()

    val displayNameState = rememberTextFieldState()
    val emailState = rememberTextFieldState()
    val passwordState = rememberTextFieldState()
    val selectedImageState = remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(uiState) {
        if (uiState is SignUpUiState.Done) {
            onDismissRequest()
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = { Text(stringResource(R.string.sign_up)) },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.createUser(
                        displayName = displayNameState.text.toString(),
                        email = emailState.text.toString(),
                        password = passwordState.text.toString(),
                        selectedImage = selectedImageState.value,
                    )
                },
                enabled =   uiState !is SignUpUiState.Loading &&
                            displayNameState.text.isNotBlank() &&
                            emailState.text.isNotBlank() &&
                            passwordState.text.isNotBlank()
            ) {
                Text(stringResource(R.string.sign_up))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        text = {
            SignUpDialogContent(
                displayNameState = displayNameState,
                emailState = emailState,
                passwordState = passwordState,
                selectedImage = selectedImageState.value,
                onImageSelected = { selectedImageState.value = it },
                uiState = uiState
            )
        }
    )
}

@Composable
fun SignUpDialogContent(
    displayNameState: TextFieldState,
    emailState: TextFieldState,
    passwordState: TextFieldState,
    selectedImage: Uri?,
    onImageSelected: (Uri?) -> Unit,
    uiState: SignUpUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pickMedia = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) onImageSelected(uri)
    }

    val previewBitmap by produceState<Bitmap?>(initialValue = null, selectedImage) {
        value = try {
                uriToBitmap(selectedImage!!, context)
            } catch (_: Exception) {
                null
            }
    }

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(100.dp)
        ) {
            if (previewBitmap != null) {
                Image(
                    bitmap = previewBitmap!!.asImageBitmap(),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_person),
                    contentDescription = stringResource(R.string.profile_picture),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        TextButton(onClick = { pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) }) {
            Text(stringResource(R.string.profile_picture))
        }

        Spacer(Modifier.padding(16.dp))

        TextField(
            state = displayNameState,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            label = { Text(stringResource(R.string.prompt_display_name)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.padding(8.dp))

        TextField(
            state = emailState,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            label = { Text(stringResource(R.string.prompt_email)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.padding(8.dp))

        SecureTextField(
            state = passwordState,
            label = { Text(stringResource(R.string.prompt_password)) },
            modifier = Modifier.fillMaxWidth()
        )

        when (uiState) {
            is SignUpUiState.Loading -> {
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator()
                uiState.message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
            is SignUpUiState.Error -> {
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