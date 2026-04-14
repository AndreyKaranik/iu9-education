package com.andreykaranik.gpstracker.presentation.view

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.andreykaranik.gpstracker.R
import com.andreykaranik.gpstracker.domain.model.result.LoginResult
import com.andreykaranik.gpstracker.domain.model.result.SaveUserDataResult
import com.andreykaranik.gpstracker.presentation.viewmodel.LoginScreenViewModel

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginScreenViewModel
) {

    val loginResult by viewModel.loginResult.collectAsState()
    val saveUserDataResult by viewModel.saveUserDataResult.collectAsState()

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var errorText by remember { mutableStateOf("") }

    when (loginResult) {
        is LoginResult.Success -> {
            viewModel.saveUserData(
                email = email,
                loginResult = loginResult as LoginResult.Success
            )
            viewModel.clearLoginResult()
        }

        is LoginResult.IsNotConfirmed -> {
            errorText = stringResource(R.string.error_account_not_verified)
            viewModel.clearLoginResult()
        }

        is LoginResult.InvalidEmailOrPassword -> {
            errorText = stringResource(R.string.error_invalid_credentials)
            viewModel.clearLoginResult()
        }

        is LoginResult.Failure -> {
            errorText = stringResource(R.string.error_request_failed)
            viewModel.clearLoginResult()
        }

        else -> {}
    }

    when (saveUserDataResult) {
        is SaveUserDataResult.Success -> {
            viewModel.clearSaveUserDataResult()
            navController.navigate("main_screen") {
                popUpTo("login_screen") { inclusive = true }
            }
        }

        is SaveUserDataResult.Failure -> {
            viewModel.clearSaveUserDataResult()
        }

        else -> {}
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
                text = stringResource(R.string.login_title),
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = email,
                onValueChange = {
                    email = it
                    emailError = false
                },
                label = { Text(stringResource(R.string.email_label)) },
                isError = emailError,
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
                modifier = Modifier.fillMaxWidth(),
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password_label)) },
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
                }
            )

            if (errorText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorText,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (isEmailValid) {
                        errorText = ""
                        viewModel.login(
                            email = email,
                            password = password
                        )
                    } else {
                        emailError = true
                    }
                },
                enabled = loginResult !is LoginResult.Pending
            ) {
                Text(stringResource(R.string.login_button))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.not_registered),
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    navController.navigate("registration_screen")
                },
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                lineHeight = 18.sp
            )
        }
    }
}