package com.example.chargingstations

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.example.chargingstations.ui.theme.ChargingStationsTheme
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

class AuthenticationActivity : ComponentActivity() {

    private var skipButtonIsShown: Boolean = false

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
                    val navController = rememberAnimatedNavController()

                    AnimatedNavHost(navController, startDestination = "login") {
                        composable(
                            "login",
                            enterTransition = {
                                slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn()
                            },
                            exitTransition = {
                                slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut()
                            }
                        ) {
                            AuthorizationSheet(navController = navController)
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
                            "register",
                            enterTransition = {
                                slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn()
                            },
                            exitTransition = {
                                slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut()
                            }
                        ) {
                            RegistrationSheet(navController = navController)
                        }
                    }
                }
            }
        }
    }
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
fun RegistrationSheet(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 48.dp)
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(R.string.registration_title),
                fontSize = 32.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.size(24.dp))
            NameTextField()
            Spacer(modifier = Modifier.size(8.dp))
            EmailTextField()
            Spacer(modifier = Modifier.size(8.dp))
            PasswordTextField()
            Spacer(modifier = Modifier.size(24.dp))
            Button(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(),
                onClick = { /*TODO*/ }
            ) {
                Text(
                    text = stringResource(R.string.sign_up_button_label)
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            TextButton(
                onClick = {
                    navController.navigate("login")
                }
            ) {
                Text(
                    text = stringResource(R.string.already_have_an_account_button_label),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


@Composable
fun AuthorizationSheet(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 48.dp)
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(R.string.authorization_title),
                fontSize = 32.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.size(24.dp))
            NameTextField()
            Spacer(modifier = Modifier.size(8.dp))
            PasswordTextField()
            Spacer(modifier = Modifier.size(24.dp))
            Button(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(),
                onClick = { /*TODO*/ }
            ) {
                Text(
                    text = stringResource(R.string.sign_in_button_label)
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            TextButton(
                onClick = {
                    navController.navigate("register")
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
fun NameTextField() {
    var name by remember { mutableStateOf("") }
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
        value = name,
        singleLine = true,
        onValueChange = {
            if (it.length <= maxLength) {
                name = it
            }
            if (name.length < minLength) {
                validStatus = 1
            } else if (!it.matches(pattern)) {
                validStatus = 2
            } else {
                validStatus = 0
            }
        },
        isError = validStatus != 0 && name != "",
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
    if (validStatus != 0 && name != "") {
        ErrorText(text = validMessage)
    }
}

@Composable
fun PasswordTextField() {
    var password by remember { mutableStateOf("") }
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
        value = password,
        singleLine = true,
        onValueChange = {
            if (it.length <= maxLength) {
                password = it
            }
            if (password.length < minLength) {
                validStatus = 1
            } else if (!it.matches(pattern)) {
                validStatus = 2
            } else {
                validStatus = 0
            }
        },
        isError = validStatus != 0 && password != "",
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
    if (validStatus != 0 && password != "") {
        ErrorText(text = validMessage)
    }
}

@Composable
fun EmailTextField() {
    var email by remember { mutableStateOf("") }
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
        value = email,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email
        ),
        isError = !isValid && email != "",
        onValueChange = {
            if (it.length <= maxLength) {
                email = it
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

    if (!isValid && email != "") {
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