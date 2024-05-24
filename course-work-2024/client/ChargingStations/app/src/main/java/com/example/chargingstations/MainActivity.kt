package com.example.chargingstations

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chargingstations.model.ChargingStation
import com.example.chargingstations.ui.theme.ChargingStationsTheme
import com.example.chargingstations.viewmodel.MainActivityViewModel
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MyApp() {
    val navController = rememberAnimatedNavController()
    val mainActivityViewModel: MainActivityViewModel = viewModel()

    AnimatedNavHost(navController, startDestination = "chargingStationList") {
        composable(
            "chargingStationList",
            enterTransition = {
                slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn()
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut()
            }
        ) { ChargingStationListScreen(navController, mainActivityViewModel) }
        composable(
            "chargingStationDetail/{id}",
            enterTransition = {
                slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn()
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut()
            }
        ) { backStackEntry ->
            val chargingStationId = backStackEntry.arguments?.getString("id")?.toIntOrNull()
            ChargingStationDetailScreen(chargingStationId, mainActivityViewModel)
        }
    }
}

@Composable
fun ChargingStationDetailScreen(chargingStationId: Int?, mainActivityViewModel: MainActivityViewModel) {
    val chargingStation = chargingStationId?.let { mainActivityViewModel.getChargingStation(it) }
    if (chargingStation != null) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(text = chargingStation.name, fontSize = 30.sp, color = Color.Black)
            Text(text = chargingStation.address, fontSize = 20.sp, color = Color.Gray)
            Text(text = (chargingStation.opening_hours), fontSize = 20.sp, color = Color.Blue)
            Text(text = (chargingStation.description ?: "null"), fontSize = 20.sp, color = Color.Blue)
        }
    } else {
        Text("not found", modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun ChargingStationListScreen(navController: NavHostController, mainActivityViewModel: MainActivityViewModel) {
    val searchQuery by mainActivityViewModel.searchQuery.collectAsState()
    val chargingStations by mainActivityViewModel.chargingStations.collectAsState()
    val filteredChargingStations by mainActivityViewModel.filteredChargingStations.collectAsState()

    val loading by mainActivityViewModel.loading.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChanged = { mainActivityViewModel.updateSearchQuery(it) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (loading) {
                CircularProgressIndicator()
            } else {
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
                        items(
                            count = filteredChargingStations.size,
                            key = {
                                filteredChargingStations[it].id
                            },
                            itemContent = { index ->
                                ChargingStationItem(filteredChargingStations[index]) {
                                    navController.navigate("chargingStationDetail/${filteredChargingStations[index].id}")
                                }
                                HorizontalDivider(color = Color.Gray, thickness = 1.dp)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(searchQuery: String, onSearchQueryChanged: (String) -> Unit) {
    OutlinedTextField(
        value = searchQuery,
        singleLine = true,
        onValueChange = onSearchQueryChanged,
        placeholder = { Text("Search") },
        label = { Text("Search") },
        leadingIcon = { IconButton(
            onClick = {
                //
            }) {
            Icon(imageVector = Icons.Filled.Search, contentDescription = "description")
        } },
        trailingIcon = {
            IconButton(
                onClick = {
                    onSearchQueryChanged("")
                }) {
                Icon(imageVector = Icons.Filled.Clear, contentDescription = "description")
            } },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ChargingStationItem(chargingStation: ChargingStation, onClick: () -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp)
            .fillMaxWidth()
        .clickable(onClick = onClick)
    ) {
        Text(text = chargingStation.name, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = chargingStation.address, fontSize = 16.sp)
    }
}

@Composable
fun AnimatedNavHost(
    navController: NavHostController,
    startDestination: String,
    builder: NavGraphBuilder.() -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        builder = builder
    )
}