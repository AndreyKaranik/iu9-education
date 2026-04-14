package com.andreykaranik.gpstracker.presentation.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
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
import com.andreykaranik.gpstracker.domain.model.result.CreateGroupResult
import com.andreykaranik.gpstracker.domain.model.result.GetGroupDataResult
import com.andreykaranik.gpstracker.presentation.viewmodel.CreateGroupScreenViewModel
import com.andreykaranik.gpstracker.presentation.viewmodel.GroupEntryScreenViewModel

@Composable
fun CreateGroupScreen(
    navController: NavHostController,
    viewModel: CreateGroupScreenViewModel
) {
    val context = LocalContext.current

    var groupName by remember { mutableStateOf("") }
    var groupNameError by remember { mutableStateOf(false) }

    val groupTypes = listOf(
        stringResource(R.string.group_type_unspecified),
        stringResource(R.string.group_type_family),
        stringResource(R.string.group_type_friends),
        stringResource(R.string.group_type_travel)
    )

    var selectedTypeIndex by remember { mutableStateOf(0) }
    var joinCode by remember { mutableStateOf("") }
    var joinCodeError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val createGroupResult by viewModel.createGroupResult.collectAsState()

    var errorText by remember { mutableStateOf("") }

    when (createGroupResult) {
        is CreateGroupResult.Success -> {
            viewModel.clearCreateGroupResult()
            navController.navigate("main_screen") {
                popUpTo("group_entry_screen") { inclusive = true }
            }
        }
        is CreateGroupResult.Unauthorized -> {
            viewModel.clearCreateGroupResult()
            navController.navigate("login_screen") {
                popUpTo("group_entry_screen") { inclusive = true }
            }
        }
        is CreateGroupResult.IsAlreadyInGroup -> {
            viewModel.clearCreateGroupResult()
            errorText = stringResource(R.string.error_user_already_in_group)
        }
        is CreateGroupResult.Failure -> {
            viewModel.clearCreateGroupResult()
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
                text = stringResource(R.string.create_group_title),
                fontSize = 28.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = groupName,
                onValueChange = {
                    if (it.length < 50) {
                        groupName = it
                        groupNameError = false
                    }
                },
                label = { Text(stringResource(R.string.group_name_label)) },
                isError = groupNameError,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                supportingText = {
                    if (groupNameError) {
                        Text(
                            stringResource(R.string.group_name_error),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            DropdownMenuBox(
                label = stringResource(R.string.group_type_label),
                options = groupTypes,
                selectedIndex = selectedTypeIndex,
                onSelectedChange = { selectedTypeIndex = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = joinCode,
                onValueChange = {
                    if (it.length < 50) {
                        joinCode = it
                        joinCodeError = false
                    }
                },
                label = { Text(stringResource(R.string.group_code_label)) },
                modifier = Modifier.fillMaxWidth(),
                isError = joinCodeError,
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
                    groupNameError = groupName.isBlank()
                    joinCodeError = joinCode.isBlank()
                    errorText = ""
                    if (!groupNameError && !joinCodeError) {
                        viewModel.createGroup(
                            name = groupName,
                            type = selectedTypeIndex,
                            joinCode = joinCode
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = createGroupResult !is CreateGroupResult.Pending
            ) {
                Text(text = stringResource(R.string.create_group_button))
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    navController.navigate("group_entry_screen") {
                        popUpTo("group_entry_screen") { inclusive = true }
                    }
                },
            ) {
                Text(text = stringResource(R.string.go_back))
            }
        }
    }
}

@Composable
fun DropdownMenuBox(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = options.getOrNull(selectedIndex) ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            },
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(),
            containerColor = Color.White
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelectedChange(index)
                        expanded = false
                    }
                )
            }
        }
    }
}