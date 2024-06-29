package com.example.chargingstations

import android.Manifest
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.lifecycleScope
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
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var cameraCallback: Map.CameraCallback

    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var qrScannerActivityResultLauncher: ActivityResultLauncher<Intent>


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
        cameraListener =
            MyCameraListener(this, placemarkMapObjectList, mapView.map.cameraPosition.zoom)
        mapView.map.addCameraListener(cameraListener)

        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network : Network) {
                if (!mainActivityViewModel.chargingStationsFetched.value) {
                    mainActivityViewModel.fetchChargingStations()
                }
            }

            override fun onLost(network : Network) {
            }

            override fun onCapabilitiesChanged(network : Network, networkCapabilities : NetworkCapabilities) {
            }

            override fun onLinkPropertiesChanged(network : Network, linkProperties : LinkProperties) {
            }
        })

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

        qrScannerActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                val code = it.data?.getStringExtra("code")
                if (code != null) {
                    Log.e(TAG, code)
                }
            }
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
                    val chargingStationsFetched by mainActivityViewModel.chargingStationsFetched.collectAsState()
                    val chargingStationsFetching by mainActivityViewModel.chargingStationsFetching.collectAsState()
                    val gpsProgressIndicatorIsShown by mainActivityViewModel.gpsProgressIndicatorIsShown.collectAsState()
                    val gpsDialogIsShown by mainActivityViewModel.gpsDialogIsShown.collectAsState()
                    val internetConnectionDialogIsShown by mainActivityViewModel.internetConnectionDialogIsShown.collectAsState()
                    var searchSheetIsShown by remember { mutableStateOf(false) }

                    if (chargingStationsFetched && placemarkMapObjectList.isEmpty()) {
                        addMarkers()
                    }

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
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (!chargingStationsFetched) {
                            if (!chargingStationsFetching) {
                                mainActivityViewModel.showInternetConnectionDialog()
                            }
                            if (!internetConnectionDialogIsShown) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            } else {
                                if (!isNetworkAvailable(this@MainActivity)) {
                                    InternetConnectionDialog()
                                } else {
                                    //InternetConnectionDialog()
                                }
                            }
                        } else {
                            ChargingStationsMap()
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                BasicIconButton(
                                    onClick = { /*TODO*/ }, imageVector = Icons.Default.Settings
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Column(modifier = Modifier.align(Alignment.CenterEnd)) {
                                    BasicIconButton(
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
                                        imageVector = ImageVector.vectorResource(R.drawable.baseline_add_24)
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                    BasicIconButton(
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
                                        imageVector = ImageVector.vectorResource(R.drawable.baseline_remove_24)
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(0.dp, 64.dp)
                                ) {
                                    BasicIconButton(
                                        onClick = {
                                            searchSheetIsShown = true
                                        }, imageVector = Icons.Default.Search
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                    BasicIconButton(
                                        onClick = {
                                            qrScannerActivityResultLauncher.launch(
                                                Intent(
                                                    this@MainActivity, QRScannerActivity::class.java
                                                )
                                            )
                                        },
                                        imageVector = ImageVector.vectorResource(R.drawable.baseline_qr_code_2_24)
                                    )
                                    Spacer(modifier = Modifier.size(32.dp))
                                    BasicIconButtonWithProgress(
                                        onClick = {
                                            requestPermissions()
                                        },
                                        imageVector = ImageVector.vectorResource(R.drawable.round_near_me_24),
                                        gpsProgressIndicatorIsShown
                                    )
                                }
                            }
                            if (searchSheetIsShown) {
                                SearchSheet(onDismissRequest = {
                                    searchSheetIsShown = false
                                })
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SearchSheet(onDismissRequest: () -> Unit) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )

        ModalBottomSheet(modifier = Modifier
            .fillMaxHeight()
            .padding(0.dp, 32.dp, 0.dp, 0.dp),
            sheetState = sheetState,
            onDismissRequest = { onDismissRequest() }) {
            ChargingStationList()
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
                    ), Animation(Animation.Type.SMOOTH, 1f), cameraCallback
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
    fun InternetConnectionDialog() {
        AlertDialog(icon = {
            Icon(Icons.Default.Info, contentDescription = "Icon")
        }, title = {
            Text(text = "Internet Connection Dialog")
        }, text = {
            Text(text = "Your internet connection seems to be disabled. You need to enable it.")
        }, onDismissRequest = {}, confirmButton = {}, modifier = Modifier.padding(32.dp))
    }

    @Composable
    fun ChargingStationsMap() {
        AndroidView(factory = {
            mapView
        })
    }

    private fun addMarkers() {
        val marker = createBitmapFromVector(this, R.drawable.baseline_circle_24)
        val imageProvider = ImageProvider.fromBitmap(marker)

        val points = mutableListOf<Point>()
        mainActivityViewModel.chargingStations.value.forEach {
            points.add(Point(it.latitude, it.longitude))
        }

        points.forEach { point ->
            val placemarkMapObject = mapView.map.mapObjects.addPlacemark().apply {
                geometry = point
                setIcon(imageProvider)
                userData = title
            }
            val listener = MapObjectTapListener { _, _ ->
                showStationInfo("title")
                true
            }
            placemarkMapObject.addTapListener(listener)
            tapListeners.add(listener)
            placemarkMapObjectList.add(placemarkMapObject)
        }
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
    fun ChargingStationList() {
        val searchQuery by mainActivityViewModel.searchQuery.collectAsState()
        val filteredChargingStations by mainActivityViewModel.filteredChargingStations.collectAsState()

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            SearchBar(searchQuery = searchQuery,
                onSearchQueryChanged = { mainActivityViewModel.updateSearchQuery(it) })
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxSize()
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

                        }
                        HorizontalDivider(color = Color.Gray, thickness = 1.dp)
                    })
                }
            }
        }
    }
}


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
    OutlinedTextField(
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
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(Color.White)
    ) {
        Text(text = chargingStation.name, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = chargingStation.address, fontSize = 16.sp)
    }
}