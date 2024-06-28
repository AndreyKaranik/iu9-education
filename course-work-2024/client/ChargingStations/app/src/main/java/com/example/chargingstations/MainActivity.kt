package com.example.chargingstations

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.chargingstations.model.ChargingStation
import com.example.chargingstations.ui.theme.ChargingStationsTheme
import com.example.chargingstations.viewmodel.MainActivityViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private lateinit var mapView: MapView
    private lateinit var cameraListener: MyCameraListener
    private lateinit var list: List<PlacemarkMapObject>
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var cameraCallback: Map.CameraCallback

    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private val TAG: String = "MainActivity"
    private val placemarkMapObjectList = mutableListOf<PlacemarkMapObject>()

    private val tapListeners = mutableListOf<MapObjectTapListener>()


    private val mainActivityViewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("83258943-5e6f-4d87-b448-45553225d7e4")
        MapKitFactory.initialize(this)
        mapView = MapView(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        list = addMarkers()
        cameraListener = MyCameraListener(this, list, mapView.map.cameraPosition.zoom)
        mapView.map.addCameraListener(cameraListener)

        locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            Log.d(TAG, "locationPermissionRequest")
            if (permissions.getOrDefault(
                    Manifest.permission.ACCESS_FINE_LOCATION, false
                ) && permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
            ) {
                val manager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    mainActivityViewModel.showGPSDialog()
                } else {
                    move()
                }
            } else {
                Log.w(TAG, "permission problem")
            }
        }

        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            move()
        }

        cameraCallback = Map.CameraCallback {
            // Handle camera move finished ...
        }

        mapView.map.move(
            CameraPosition(Point(55.751244, 37.618423), 11.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 1f),
            cameraCallback
        )

        setContent {
            ChargingStationsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    val gpsProgressIndicatorIsShown by mainActivityViewModel.gpsProgressIndicatorIsShown.collectAsStateWithLifecycle()
                    val gpsDialogIsShown by mainActivityViewModel.gpsDialogIsShown.collectAsStateWithLifecycle()

//                    val searchQuery by mainActivityViewModel.searchQuery.collectAsState()
//                    val chargingStations by mainActivityViewModel.chargingStations.collectAsState()
//                    val filteredChargingStations by mainActivityViewModel.filteredChargingStations.collectAsState()
//                    val loading by mainActivityViewModel.loading.collectAsState()

                    when {
                        gpsDialogIsShown -> {
                            GPSDialog(onDismissRequest = {
                                mainActivityViewModel.hideGPSDialog()
                            }, onConfirmation = {
                                mainActivityViewModel.hideGPSDialog()
                                activityResultLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            })
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        ChargingStationsMap()
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            BasicIconButton(
                                onClick = { /*TODO*/ },
                                imageVector = Icons.Default.Settings
                            )
//                            FilledIconButton(
//                                onClick = {
//                                },
//                                modifier = Modifier
//                                    .size(48.dp)
//                                    .align(Alignment.TopStart)
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.Settings,
//                                    contentDescription = "description"
//                                )
//                            }
                            Spacer(modifier = Modifier.size(8.dp))

//                            Column {
//                                SearchBar(searchQuery = searchQuery,
//                                    onSearchQueryChanged = {
//                                        mainActivityViewModel.updateSearchQuery(
//                                            it
//                                        )
//                                    })
//                                LazyColumn(
//                                    Modifier.background(Color.White).height(120.dp),
//                                    verticalArrangement = Arrangement.spacedBy(8.dp),
//                                ) {
//                                    items(count = filteredChargingStations.size, key = {
//                                        filteredChargingStations[it].id
//                                    }, itemContent = { index ->
//                                        ChargingStationItem(filteredChargingStations[index]) {
//                                            //navController.navigate("chargingStationDetail/${filteredChargingStations[index].id}")
//                                        }
//                                        HorizontalDivider(color = Color.Gray, thickness = 1.dp)
//                                    })
//                                }
//                            }
                            Column(modifier = Modifier.align(Alignment.CenterEnd)) {
                                FilledIconButton(
                                    onClick = {
                                        mapView.map.apply {
                                            move(
                                                CameraPosition(
                                                    cameraPosition.target,
                                                    cameraPosition.zoom + 1,
                                                    cameraPosition.azimuth,
                                                    cameraPosition.tilt
                                                ),
                                                Animation(Animation.Type.SMOOTH, 0.25f),
                                                cameraCallback
                                            )
                                        }
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.baseline_add_24),
                                        contentDescription = "description"
                                    )
                                }
                                Spacer(modifier = Modifier.size(8.dp))
                                FilledIconButton(
                                    onClick = {
                                        mapView.map.apply {
                                            move(
                                                CameraPosition(
                                                    cameraPosition.target,
                                                    cameraPosition.zoom - 1,
                                                    cameraPosition.azimuth,
                                                    cameraPosition.tilt
                                                ),
                                                Animation(Animation.Type.SMOOTH, 0.25f),
                                                cameraCallback
                                            )
                                        }
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.baseline_remove_24),
                                        contentDescription = "description"
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(0.dp, 64.dp)
                            ) {
                                FilledIconButton(
                                    onClick = {
                                        //state = true
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "description"
                                    )
                                }
                                Spacer(modifier = Modifier.size(8.dp))
                                FilledIconButton(
                                    onClick = {
                                        //state = true
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.baseline_qr_code_2_24),
                                        contentDescription = "description"
                                    )
                                }
                                Spacer(modifier = Modifier.size(32.dp))
                                FilledIconButton(
                                    onClick = {
                                        requestPermissions()
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                ) {
                                    if (gpsProgressIndicatorIsShown) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.secondary,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(R.drawable.round_near_me_24),
                                            contentDescription = "description"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun move() {
        if (mainActivityViewModel.gpsProgressIndicatorIsShown.value) {
            mainActivityViewModel.hideGPSProgressIndicator()
        }
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "You don't have the permissions to get last known location.")
            return
        }

        val manager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.w(TAG, "GPS is disabled.")
            return
        }

        mainActivityViewModel.showGPSProgressIndicator()

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                mapView.map.move(
                    CameraPosition(
                        Point(location.latitude, location.longitude),
                        15.0f,
                        mapView.map.cameraPosition.azimuth,
                        mapView.map.cameraPosition.tilt
                    ),
                    Animation(Animation.Type.SMOOTH, 1f),
                    cameraCallback
                )
                mainActivityViewModel.hideGPSProgressIndicator()
            } else {
                lifecycleScope.launch {
                    delay(1000)
                    move()
                }
            }
        }
    }

    private fun requestPermissions() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

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
    fun ChargingStationsMap() {
        AndroidView(factory = {
            mapView
        })
    }

    private fun addMarkers(): List<PlacemarkMapObject> {
        val points = listOf(
            Point(55.751244, 37.618423) to "Station 1",
            Point(55.761244, 37.628423) to "Station 2",
            Point(55.771244, 37.638423) to "Station 3"
        )

        val marker = createBitmapFromVector(this, R.drawable.baseline_circle_24)
        val imageProvider = ImageProvider.fromBitmap(marker)

        points.forEach { (point, title) ->
            val placemarkMapObject = mapView.map.mapObjects.addPlacemark().apply {
                geometry = point
                setIcon(imageProvider)
                userData = title
            }
            val listener = MapObjectTapListener { _, _ ->
                showStationInfo(title)
                true
            }
            placemarkMapObject.addTapListener(listener)
            tapListeners.add(listener)
            placemarkMapObjectList.add(placemarkMapObject)
        }
        return placemarkMapObjectList
    }

    private fun showStationInfo(title: String) {
        AlertDialog.Builder(this).setTitle(title).setMessage("Описание для $title")
            .setPositiveButton("OK", null).show()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    @Composable
    fun ChargingStationListScreen() {
        val searchQuery by mainActivityViewModel.searchQuery.collectAsState()
        val chargingStations by mainActivityViewModel.chargingStations.collectAsState()
        val filteredChargingStations by mainActivityViewModel.filteredChargingStations.collectAsState()
        val loading by mainActivityViewModel.loading.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            SearchBar(searchQuery = searchQuery,
                onSearchQueryChanged = { mainActivityViewModel.updateSearchQuery(it) })
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (loading) {
                    CircularProgressIndicator()
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()//.height(300.dp)
                        ) {
                            items(count = filteredChargingStations.size, key = {
                                filteredChargingStations[it].id
                            }, itemContent = { index ->
                                ChargingStationItem(filteredChargingStations[index]) {
                                    //navController.navigate("chargingStationDetail/${filteredChargingStations[index].id}")
                                }
                                HorizontalDivider(color = Color.Gray, thickness = 1.dp)
                            })
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun BasicIconButton(
    onClick: () -> Unit,
    imageVector: ImageVector
) {

    Box (contentAlignment = Alignment.Center){
        Button(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .shadow(12.dp, shape = CircleShape)
        ) {}
        Icon(
            imageVector = imageVector,
            contentDescription = "description",
            tint = Color.White
        )
    }
}

//@OptIn(ExperimentalAnimationApi::class)
//@Composable
//fun MyApp() {
//    val navController = rememberAnimatedNavController()
//    val mainActivityViewModel: MainActivityViewModel = viewModel()
//
//    AnimatedNavHost(navController, startDestination = "chargingStationList") {
//        composable("chargingStationList", enterTransition = {
//            slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn()
//        }, exitTransition = {
//            slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut()
//        }) { ChargingStationListScreen(navController, mainActivityViewModel) }
//        composable("chargingStationDetail/{id}", enterTransition = {
//            slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn()
//        }, exitTransition = {
//            slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut()
//        }) { backStackEntry ->
//            val chargingStationId = backStackEntry.arguments?.getString("id")?.toIntOrNull()
//            ChargingStationDetailScreen(chargingStationId, mainActivityViewModel)
//        }
//    }
//}

@Composable
fun ChargingStationDetailScreen(
    chargingStationId: Int?, mainActivityViewModel: MainActivityViewModel
) {
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
            Text(
                text = (chargingStation.description ?: "null"), fontSize = 20.sp, color = Color.Blue
            )
        }
    } else {
        Text("not found", modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun SearchBar(searchQuery: String, onSearchQueryChanged: (String) -> Unit) {
    TextField(
        value = searchQuery,
        singleLine = true,
        onValueChange = onSearchQueryChanged,
        placeholder = { Text("Address") },
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
            .padding(16.dp)
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
    navController: NavHostController, startDestination: String, builder: NavGraphBuilder.() -> Unit
) {
    NavHost(
        navController = navController, startDestination = startDestination, builder = builder
    )
}