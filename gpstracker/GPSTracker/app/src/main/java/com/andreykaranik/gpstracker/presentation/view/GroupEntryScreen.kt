package com.andreykaranik.gpstracker.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.andreykaranik.gpstracker.R
import com.andreykaranik.gpstracker.domain.model.result.SaveUserDataResult
import com.andreykaranik.gpstracker.presentation.viewmodel.GroupEntryScreenViewModel

@Composable
fun GroupEntryScreen(
    navController: NavHostController,
    viewModel: GroupEntryScreenViewModel
) {
    val saveUserDataResult by viewModel.saveUserDataResult.collectAsState()

    when (saveUserDataResult) {
        is SaveUserDataResult.Success -> {
            viewModel.clearSaveUserDataResult()
            navController.navigate("login_screen") {
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
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = getStatusBarHeight(),
                bottom = getSystemBarHeight()
            )
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = stringResource(id = R.string.getting_started),
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                SquareIconButton(
                    text = stringResource(id = R.string.create_group),
                    icon = painterResource(id = R.drawable.create_group_icon),
                    onClick = {
                        navController.navigate("create_group_screen")
                    }
                )
                Spacer(modifier = Modifier.width(32.dp))
                SquareIconButton(
                    text = stringResource(id = R.string.join_group),
                    icon = painterResource(id = R.drawable.join_group_icon),
                    onClick = {
                        navController.navigate("join_group_screen")
                    }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedButton(
                onClick = {
                    viewModel.leave()
                }
            ) {
                Text(text = stringResource(id = R.string.sign_out))
            }
        }
    }
}

@Composable
fun SquareIconButton(
    text: String,
    icon: Painter,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.size(144.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
            )
            Text(
                text = text,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
