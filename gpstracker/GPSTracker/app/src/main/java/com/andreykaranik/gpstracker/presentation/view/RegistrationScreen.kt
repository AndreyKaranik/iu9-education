package com.andreykaranik.gpstracker.presentation.view

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.andreykaranik.gpstracker.R
import com.andreykaranik.gpstracker.domain.model.result.LoginResult
import com.andreykaranik.gpstracker.domain.model.result.RegisterResult
import com.andreykaranik.gpstracker.presentation.viewmodel.RegistrationScreenViewModel

@Composable
fun RegistrationScreen(
    navController: NavHostController,
    viewModel: RegistrationScreenViewModel
) {

    val context = LocalContext.current

    val registerResult by viewModel.registerResult.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }
    val isPasswordValid = password.length >= 4
    var passwordVisible by remember { mutableStateOf(false) }

    var acceptPrivacyPolicy by remember { mutableStateOf(false) }
    var acceptTerms by remember { mutableStateOf(false) }

    var errorText by remember { mutableStateOf("") }

    when (registerResult) {
        is RegisterResult.Success -> {
            showDialog = true
            viewModel.clearRegisterResult()
        }

        is RegisterResult.AlreadyExists -> {
            errorText = stringResource(R.string.error_email_already_in_use)
            viewModel.clearRegisterResult()
        }

        is RegisterResult.Failure -> {
            errorText = stringResource(R.string.error_request_failed)
            viewModel.clearRegisterResult()
        }

        else -> {}
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                navController.navigate("login_screen") {
                    popUpTo("login_screen") { inclusive = true }
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.email_confirmation_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.email_confirmation_message),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        navController.navigate("login_screen") {
                            popUpTo("login_screen") { inclusive = true }
                        }
                    },
                ) {
                    Text(
                        text = stringResource(R.string.accept_button)
                    )
                }
            },
            containerColor = Color.White
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(R.string.registration_title),
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    if (it.length < 50) {
                        name = it
                        nameError = false
                    }
                },
                label = { Text(stringResource(R.string.name_label)) },
                isError = nameError,
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (nameError) {
                        Text(
                            stringResource(R.string.name_error),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    if (it.length < 255) {
                        email = it
                        emailError = false
                    }
                },
                label = { Text(stringResource(R.string.email_label)) },
                isError = emailError,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                supportingText = {
                    if (emailError) {
                        Text(
                            stringResource(R.string.email_error),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    if (it.length < 255) {
                        password = it
                        passwordError = false
                    }
                },
                label = { Text(stringResource(R.string.password_label)) },
                isError = passwordError,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible)
                        painterResource(id = R.drawable.visibility_icon)
                    else
                        painterResource(id = R.drawable.visibility_off_icon)

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(painter = icon, contentDescription = null)
                    }
                },
                supportingText = {
                    if (passwordError) {
                        Text(
                            stringResource(R.string.password_error),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = acceptPrivacyPolicy,
                    onCheckedChange = { acceptPrivacyPolicy = it })
                Text(
                    text = stringResource(R.string.privacy_policy_checkbox),
                    fontSize = 14.sp,
                    lineHeight = 16.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = acceptTerms, onCheckedChange = { acceptTerms = it })
                Text(
                    text = stringResource(R.string.terms_checkbox),
                    fontSize = 14.sp,
                    lineHeight = 16.sp
                )
            }

            if (errorText.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = errorText,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(4.dp))
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    nameError = name.isBlank()
                    emailError = !isEmailValid
                    passwordError = !isPasswordValid
                    if (!nameError && !emailError && !passwordError) {
                        errorText = ""
                        viewModel.register(
                            name = name,
                            email = email,
                            password = password
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = acceptPrivacyPolicy && acceptTerms && registerResult !is RegisterResult.Pending
            ) {
                Text(stringResource(R.string.register_button))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.already_registered),
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    navController.navigate("login_screen") {
                        popUpTo("login_screen") { inclusive = true }
                    }
                },
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                Text(
                    text = stringResource(R.string.privacy_policy_link),
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        val url = "https://diploma2025.ru/privacy-policy"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    },
                    fontSize = 14.sp,
                    color = Color(0xFF007AFF),
                    textDecoration = TextDecoration.Underline
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.terms_link),
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        val url = "https://diploma2025.ru/terms-of-use"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    },
                    fontSize = 14.sp,
                    color = Color(0xFF007AFF),
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}