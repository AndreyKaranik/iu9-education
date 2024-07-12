package com.example.chargingstations

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.example.chargingstations.ui.ErrorDialog
import com.example.chargingstations.ui.GPSDialog
import com.example.chargingstations.ui.RegistrationConfirmDialog
import com.example.chargingstations.ui.RegistrationEmailDialog
import com.example.chargingstations.ui.RegistrationUsernameDialog
import com.example.chargingstations.ui.theme.ChargingStationsTheme
import com.example.chargingstations.viewmodel.AuthenticationActivityViewModel
import com.example.chargingstations.viewmodel.MainActivityViewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

class AuthenticationActivity : ComponentActivity() {

    private var skipButtonIsShown: Boolean = false

    private val authenticationActivityViewModel: AuthenticationActivityViewModel by viewModels()

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.extras?.let {
            skipButtonIsShown = it.getBoolean("skip_button")
        }

        setContent {
            ChargingStationsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val confirmDialogIsShown by authenticationActivityViewModel.confirmDialogIsShown.collectAsState()
                    val errorDialogIsShown by authenticationActivityViewModel.errorDialogIsShown.collectAsState()
                    val usernameDialogIsShown by authenticationActivityViewModel.usernameDialogIsShown.collectAsState()
                    val emailDialogIsShown by authenticationActivityViewModel.emailDialogIsShown.collectAsState()
                    val isAuth by authenticationActivityViewModel.isAuth.collectAsState()
                    val token by authenticationActivityViewModel.token.collectAsState()

                    if (isAuth) {
                        val sharedPref = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putBoolean("auth", true)
                            putString("token", token)
                            apply()
                        }
                        if (skipButtonIsShown) {
                            startActivity(
                                Intent(
                                    this@AuthenticationActivity,
                                    MainActivity::class.java
                                )
                            )
                        }
                        finish()
                    }

                    val navController = rememberAnimatedNavController()

                    AnimatedNavHost(navController, startDestination = "sign_in") {
                        composable(
                            "sign_in",
                            enterTransition = {
                                slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn()
                            },
                            exitTransition = {
                                slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut()
                            }
                        ) {
                            AuthorizationSheet(authenticationActivityViewModel, navController = navController)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 32.dp),
                                contentAlignment = Alignment.BottomCenter,
                            ) {
                                if (skipButtonIsShown) {
                                    SkipButton {
                                        startActivity(
                                            Intent(
                                                this@AuthenticationActivity,
                                                MainActivity::class.java
                                            )
                                        )
                                        finish()
                                    }
                                }
                            }
                        }
                        composable(
                            "sign_up",
                            enterTransition = {
                                slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn()
                            },
                            exitTransition = {
                                slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut()
                            }
                        ) {
                            RegistrationSheet(authenticationActivityViewModel,this@AuthenticationActivity, navController = navController)
                        }
                    }

                    when {
                        confirmDialogIsShown -> {
                            RegistrationConfirmDialog(
                                onDismissRequest = {
                                    authenticationActivityViewModel.hideConfirmDialog()
                                    navController.popBackStack()
                                }, onConfirmRequest = {
                                    authenticationActivityViewModel.hideConfirmDialog()
                                    navController.popBackStack()
                                }
                            )
                        }
                        errorDialogIsShown -> {
                            ErrorDialog(
                                onDismissRequest = {
                                    authenticationActivityViewModel.hideErrorDialog()
                                }, onConfirmRequest = {
                                    authenticationActivityViewModel.hideErrorDialog()
                                }
                            )
                        }
                        usernameDialogIsShown -> {
                            RegistrationUsernameDialog(
                                onDismissRequest = {
                                    authenticationActivityViewModel.hideUsernameDialog()
                                }, onConfirmRequest = {
                                    authenticationActivityViewModel.hideUsernameDialog()
                                }
                            )
                        }
                        emailDialogIsShown -> {
                            RegistrationEmailDialog(
                                onDismissRequest = {
                                    authenticationActivityViewModel.hideEmailDialog()
                                }, onConfirmRequest = {
                                    authenticationActivityViewModel.hideEmailDialog()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun openUrlInBrowser(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

@Composable
fun SkipButton(onClick: () -> Unit) {
    Button(
        colors = ButtonDefaults.buttonColors().copy(containerColor = Color.Gray),
        onClick = {
            onClick()
        }
    ) {
        Text(
            text = stringResource(R.string.skip_button_label)
        )
    }
}

@Composable
fun RegistrationSheet(authenticationActivityViewModel: AuthenticationActivityViewModel, context: Context, navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 48.dp)
        ) {
            var name = remember { mutableStateOf("") }
            var password = remember { mutableStateOf("") }
            var email = remember { mutableStateOf("") }
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(R.string.registration_title),
                fontSize = 32.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.size(24.dp))
            NameTextField(name)
            Spacer(modifier = Modifier.size(8.dp))
            EmailTextField(email)
            Spacer(modifier = Modifier.size(8.dp))
            PasswordTextField(password)
            Spacer(modifier = Modifier.size(24.dp))
            Button(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(),
                onClick = {
                    authenticationActivityViewModel.register(name.value, email.value, password.value)
                }
            ) {
                Text(
                    text = stringResource(R.string.sign_up_button_label)
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            TextButton(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Text(
                    text = stringResource(R.string.already_have_an_account_button_label),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 8.dp, start = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.privacy_policy_text),
                fontSize = 12.sp,
                color = Color.Black
            )
            ClickableText(
                text = AnnotatedString(stringResource(R.string.privacy_policy_button_label)),
                onClick = {
                    val url = "http://89.111.172.144:8000/privacy-policy"
                    openUrlInBrowser(context, url)
                },
                style = TextStyle.Default.copy(textDecoration = TextDecoration.Underline, color = Color.Gray),
            )
        }
    }
}


@Composable
fun AuthorizationSheet(authenticationActivityViewModel: AuthenticationActivityViewModel, navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 48.dp)
        ) {
            var name = remember { mutableStateOf("") }
            var password = remember { mutableStateOf("") }
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(R.string.authorization_title),
                fontSize = 32.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.size(24.dp))
            NameTextField(name)
            Spacer(modifier = Modifier.size(8.dp))
            PasswordTextField(password)
            Spacer(modifier = Modifier.size(24.dp))
            Button(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(),
                onClick = { authenticationActivityViewModel.auth(name.value, password.value) }
            ) {
                Text(
                    text = stringResource(R.string.sign_in_button_label)
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            TextButton(
                onClick = {
                    navController.navigate("sign_up")
                }
            ) {
                Text(
                    text = stringResource(R.string.create_a_new_account_button_label),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun NameTextField(name: MutableState<String>) {
    var validStatus by remember { mutableStateOf(0) }
    val minLength = 5
    val maxLength = 30
    val pattern = "^[a-zA-Z0-9]*$".toRegex()
    TextField(
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors().copy(
            focusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            errorContainerColor = Color.White
        ),
        value = name.value,
        singleLine = true,
        onValueChange = {
            if (it.length <= maxLength) {
                name.value = it
            }
            if (name.value.length < minLength) {
                validStatus = 1
            } else if (!it.matches(pattern)) {
                validStatus = 2
            } else {
                validStatus = 0
            }
        },
        isError = validStatus != 0 && name.value != "",
        placeholder = {
            Text(
                text = stringResource(R.string.user_name_text_field_placeholder),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        label = {
            Text(
                text = stringResource(R.string.user_name_text_field_label)
            )
        }
    )
    val validMessage = when (validStatus) {
        1 -> stringResource(R.string.user_name_must_contain_at_least_n_characters_valid_message)
        2 -> stringResource(R.string.only_latin_characters_and_numbers_are_allowed_valid_message)
        else -> ""
    }
    if (validStatus != 0 && name.value != "") {
        ErrorText(text = validMessage)
    }
}

@Composable
fun PasswordTextField(password: MutableState<String>) {
    var passwordVisible by remember { mutableStateOf(false) }
    var validStatus by remember { mutableStateOf(0) }
    val minLength = 5
    val maxLength = 30
    val pattern = "^[a-zA-Z0-9]*$".toRegex()
    TextField(
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors().copy(
            focusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            errorContainerColor = Color.White
        ),
        value = password.value,
        singleLine = true,
        onValueChange = {
            if (it.length <= maxLength) {
                password.value = it
            }
            if (password.value.length < minLength) {
                validStatus = 1
            } else if (!it.matches(pattern)) {
                validStatus = 2
            } else {
                validStatus = 0
            }
        },
        isError = validStatus != 0 && password.value != "",
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            val image =
                if (passwordVisible) ImageVector.vectorResource(R.drawable.baseline_visibility_24) else ImageVector.vectorResource(
                    R.drawable.baseline_visibility_off_24
                )
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = image,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                )
            }
        },
        placeholder = {
            Text(
                text = stringResource(R.string.user_password_text_field_placeholder),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        label = {
            Text(
                text = stringResource(R.string.user_password_text_field_label)
            )
        }
    )
    val validMessage = when (validStatus) {
        1 -> stringResource(R.string.password_must_contain_at_least_n_characters_valid_message)
        2 -> stringResource(R.string.only_latin_characters_and_numbers_are_allowed_valid_message)
        else -> ""
    }
    if (validStatus != 0 && password.value != "") {
        ErrorText(text = validMessage)
    }
}

@Composable
fun EmailTextField(email: MutableState<String>) {
    var isValid by remember { mutableStateOf(true) }
    val maxLength = 319
    TextField(
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors().copy(
            focusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            errorContainerColor = Color.White
        ),
        value = email.value,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email
        ),
        isError = !isValid && email.value != "",
        onValueChange = {
            if (it.length <= maxLength) {
                email.value = it
            }
            isValid = Patterns.EMAIL_ADDRESS.matcher(it).matches()
        },
        placeholder = {
            Text(
                text = stringResource(R.string.user_email_text_field_placeholder),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        label = {
            Text(
                text = stringResource(R.string.user_email_text_field_label)
            )
        }
    )

    if (!isValid && email.value != "") {
        ErrorText(text = stringResource(R.string.incorrect_email_valid_message))
    }
}

@Composable
fun ErrorText(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
    )
}