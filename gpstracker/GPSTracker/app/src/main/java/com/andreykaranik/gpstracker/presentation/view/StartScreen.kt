package com.andreykaranik.gpstracker.presentation.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.ConnectivityManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.andreykaranik.gpstracker.R
import com.andreykaranik.gpstracker.domain.model.result.GetUserDataResult
import com.andreykaranik.gpstracker.LocationService
import com.andreykaranik.gpstracker.presentation.viewmodel.StartScreenViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun StartScreen(
    navController: NavHostController,
    viewModel: StartScreenViewModel
) {
    val context = LocalContext.current
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    val permissionsGranted = remember { mutableStateOf(false) }
    val locationEnabled = remember { mutableStateOf(false) }
    val internetAvailable = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            locationEnabled.value = isLocationEnabled(context)
            internetAvailable.value = isInternetAvailable(context)
            delay(1000)
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionState.launchPermissionRequest()
    }

    LaunchedEffect(locationPermissionState.status, locationEnabled.value, internetAvailable.value) {
        if (
            locationPermissionState.status.isGranted &&
            locationEnabled.value &&
            internetAvailable.value &&
            !permissionsGranted.value
        ) {
            val intent = Intent(context, LocationService::class.java)
            ContextCompat.startForegroundService(context, intent)
            permissionsGranted.value = true
        }
    }

    val getUserDataResult by viewModel.getUserDataResult.collectAsState()

    if (permissionsGranted.value) {
        when (getUserDataResult) {
            is GetUserDataResult.Success -> {
                val result = getUserDataResult as GetUserDataResult.Success
                val target = if (result.userData.email.isBlank()) "login_screen" else "main_screen"
                navController.navigate(target) {
                    popUpTo("start_screen") { inclusive = true }
                }
                viewModel.clearGetUserDataResult()
            }
            is GetUserDataResult.Failure -> {
                viewModel.clearGetUserDataResult()
            }
            else -> {}
        }
    }

    val errorMessage = when {
        !locationPermissionState.status.isGranted -> stringResource(R.string.error_location_permission)
        !locationEnabled.value -> stringResource(R.string.error_location_disabled)
        !internetAvailable.value -> stringResource(R.string.error_no_internet)
        else -> ""
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
        Text(
            text = errorMessage,
            color = Color.Black,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .offset(y = 48.dp),
            textAlign = TextAlign.Center
        )
    }
}

fun isLocationEnabled(context: Context): Boolean {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

fun isInternetAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.activeNetworkInfo?.isConnectedOrConnecting == true
}