package com.example.chargingstations.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GPSDialog(
    onDismissRequest: () -> Unit, onConfirmation: () -> Unit
) {
    AlertDialog(icon = {
        Icon(Icons.Default.Info, contentDescription = "Icon")
    }, title = {
        Text(text = "GPS Dialog")
    }, text = {
        Text(text = "Your GPS seems to be disabled, do you want to enable it?")
    }, onDismissRequest = {
        onDismissRequest()
    }, confirmButton = {
        TextButton(onClick = {
            onConfirmation()
        }) {
            Text("Yes")
        }
    }, dismissButton = {
        TextButton(onClick = {
            onDismissRequest()
        }) {
            Text("No")
        }
    }, modifier = Modifier.padding(32.dp))
}

@Composable
fun InternetConnectionDialog() {
    AlertDialog(icon = {
        Icon(Icons.Default.Info, contentDescription = "Icon")
    }, title = {
        Text(text = "Internet Connection Dialog")
    }, text = {
        Text(text = "Your internet connection seems to be disabled. You need to enable it.")
    }, onDismissRequest = {}, confirmButton = {}, modifier = Modifier.padding(32.dp))
}