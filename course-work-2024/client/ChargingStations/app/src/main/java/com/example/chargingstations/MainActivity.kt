package com.example.chargingstations

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chargingstations.model.ChargingStation
import com.example.chargingstations.ui.theme.ChargingStationsTheme
import com.example.chargingstations.viewmodel.MainActivityViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                UserListScreen()
            }
        }
    }
}

@Composable
fun MyApp(content: @Composable () -> Unit) {
    ChargingStationsTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}

@Composable
fun UserListScreen(mainActivityViewModel: MainActivityViewModel = viewModel()) {
    val searchQuery by mainActivityViewModel.searchQuery.collectAsState()
    val chargingStations by mainActivityViewModel.chargingStations.collectAsState()
    val filteredChargingStations by mainActivityViewModel.filteredChargingStations.collectAsState()


    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChanged = { mainActivityViewModel.updateSearchQuery(it) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()//.height(300.dp)
            ) {
                items(filteredChargingStations) { chargingStation ->
                    ChargingStationItem(chargingStation)
                }
            }
        }
    }
}

@Composable
fun SearchBar(searchQuery: String, onSearchQueryChanged: (String) -> Unit) {
    TextField(
        value = searchQuery,
        singleLine = true,
        onValueChange = onSearchQueryChanged,
        label = { Text("Search") },
        leadingIcon = { IconButton(
            onClick = {
                onSearchQueryChanged
            }) {
            Icon(imageVector = Icons.Filled.Search, contentDescription = "description")
        } },
        trailingIcon = {
            IconButton(
                onClick = {
                    value = ""
                }) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "description")
            } },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ChargingStationItem(charginStation: ChargingStation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = charginStation.name, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = charginStation.address, fontSize = 16.sp)
        }
    }
}

@Preview
@Composable
fun MyPreview() {
    ChargingStationsTheme {
        MyApp {
            UserListScreen()
        }
    }
}