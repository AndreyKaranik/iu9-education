package com.example.chargingstations.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chargingstations.model.ChargingStation
import com.example.chargingstations.viewmodel.MainActivityViewModel

@Composable
fun BasicIconButton(
    onClick: () -> Unit, imageVector: ImageVector
) {
    Box(contentAlignment = Alignment.Center) {
        Button(
            onClick = onClick,
            elevation = ButtonDefaults.buttonElevation(3.dp),
            modifier = Modifier.size(48.dp)
        ) {}
        Icon(
            imageVector = imageVector, contentDescription = "description", tint = Color.White
        )
    }
}

@Composable
fun BasicIconButtonWithProgress(
    onClick: () -> Unit, imageVector: ImageVector, progressIsShown: Boolean
) {
    Box(contentAlignment = Alignment.Center) {
        Button(
            onClick = onClick,
            elevation = ButtonDefaults.buttonElevation(3.dp),
            modifier = Modifier.size(48.dp)
        ) {}
        if (progressIsShown) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        } else {
            Icon(
                imageVector = imageVector, contentDescription = "description", tint = Color.White
            )
        }
    }
}

@Composable
fun ChargingStationSearchBar(searchQuery: String, onSearchQueryChanged: (String) -> Unit) {
    OutlinedTextField(
        value = searchQuery,
        singleLine = true,
        onValueChange = onSearchQueryChanged,
        placeholder = { Text("Name/Address...") },
        label = { Text("Search") },
        leadingIcon = {
            IconButton(onClick = {
                //
            }) {
                Icon(imageVector = Icons.Filled.Search, contentDescription = "description")
            }
        },
        trailingIcon = {
            IconButton(onClick = {
                onSearchQueryChanged("")
            }) {
                Icon(imageVector = Icons.Filled.Clear, contentDescription = "description")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ChargingStationItem(chargingStation: ChargingStation, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(Color.White)
    ) {
        Text(text = chargingStation.name, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = chargingStation.address, fontSize = 16.sp)
    }
}