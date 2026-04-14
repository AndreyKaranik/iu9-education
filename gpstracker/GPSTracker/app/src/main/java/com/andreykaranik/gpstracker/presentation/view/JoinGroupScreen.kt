package com.andreykaranik.gpstracker.presentation.view

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
import androidx.compose.material3.OutlinedButton
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
import com.andreykaranik.gpstracker.domain.model.result.CreateGroupResult
import com.andreykaranik.gpstracker.domain.model.result.JoinGroupResult
import com.andreykaranik.gpstracker.presentation.viewmodel.GroupEntryScreenViewModel
import com.andreykaranik.gpstracker.presentation.viewmodel.JoinGroupScreenViewModel

@Composable
fun JoinGroupScreen(
    navController: NavHostController,
    viewModel: JoinGroupScreenViewModel
) {
    var groupId by remember { mutableStateOf("") }
    var groupIdError by remember { mutableStateOf(false) }

    var joinCode by remember { mutableStateOf("") }
    var joinCodeError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val joinGroupResult by viewModel.joinGroupResult.collectAsState()

    var errorText by remember { mutableStateOf("") }

    when (joinGroupResult) {
        is JoinGroupResult.Success -> {
            viewModel.clearJoinGroupResult()
            navController.navigate("main_screen") {
                popUpTo("group_entry_screen") { inclusive = true }
            }
        }
        is JoinGroupResult.Unauthorized -> {
            viewModel.clearJoinGroupResult()
            navController.navigate("login_screen") {
                popUpTo("group_entry_screen") { inclusive = true }
            }
        }
        is JoinGroupResult.IsAlreadyInGroup -> {
            viewModel.clearJoinGroupResult()
            errorText = stringResource(R.string.error_user_already_in_group)
        }
        is JoinGroupResult.InvalidGroupIdOrJoinCode -> {
            viewModel.clearJoinGroupResult()
            errorText = stringResource(R.string.error_invalid_group_id_or_code)
        }
        is JoinGroupResult.Failure -> {
            viewModel.clearJoinGroupResult()
            errorText = stringResource(R.string.error_request_failed)
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
                text = stringResource(R.string.join_group_title),
                fontSize = 28.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = groupId,
                onValueChange = {
                    if (it.all { char -> char.isDigit() } && it.length <= 10) {
                        groupId = it
                        groupIdError = false
                    }
                },
                label = { Text(stringResource(R.string.group_id_label)) },
                isError = groupIdError,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                supportingText = {
                    if (groupIdError) {
                        Text(
                            stringResource(R.string.group_id_error),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = joinCode,
                onValueChange = {
                    if (it.length <= 50) {
                        joinCode = it
                        joinCodeError = false
                    }
                },
                label = { Text(stringResource(R.string.group_code_label)) },
                isError = joinCodeError,
                modifier = Modifier.fillMaxWidth(),
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
                    if (joinCodeError) {
                        Text(
                            stringResource(R.string.group_code_error),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            if (errorText.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = errorText,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(6.dp))
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    groupIdError = groupId.isBlank()
                    joinCodeError = joinCode.isBlank()
                    errorText = ""
                    if (!groupIdError && !joinCodeError) {
                        viewModel.joinGroup(
                            id = groupId.toInt(),
                            joinCode = joinCode
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = joinGroupResult !is JoinGroupResult.Pending
            ) {
                Text(text = stringResource(R.string.join_group_button))
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    navController.navigate("group_entry_screen") {
                        popUpTo("group_entry_screen") { inclusive = true }
                    }
                }
            ) {
                Text(text = stringResource(R.string.go_back))
            }
        }
    }
}