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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import com.example.chargingstations.model.ChargingType
import com.example.chargingstations.model.Connector
import com.example.chargingstations.ui.BadQRCodeDialog
import com.example.chargingstations.ui.BasicIconButton
import com.example.chargingstations.ui.BasicIconButtonWithProgress
import com.example.chargingstations.ui.ChargingStationItem
import com.example.chargingstations.ui.ChargingStationNotFoundDialog
import com.example.chargingstations.ui.ChargingStationSearchBar
import com.example.chargingstations.ui.ConnectionProblemDialog
import com.example.chargingstations.ui.GPSDialog
import com.example.chargingstations.ui.InternetConnectionDialog

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
        connectivityManager.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (!mainActivityViewModel.chargingStationsFetched.value) {
                    mainActivityViewModel.fetchChargingStations()
                    mainActivityViewModel.hideInternetConnectionDialog()
                }
            }

            override fun onLost(network: Network) {

            }

            override fun onCapabilitiesChanged(
                network: Network, networkCapabilities: NetworkCapabilities
            ) {

            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {

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
                    val regex = "ChargingStationId=([0-9])+".toRegex()
                    if (regex.containsMatchIn(code)) {
                        val pair = code.split('=')
                        val chargingStation =
                            mainActivityViewModel.getChargingStationById(pair[1].toInt())
                        if (chargingStation != null) {
                            moveTo(
                                chargingStation.id,
                                Point(chargingStation.latitude, chargingStation.longitude)
                            )
                        } else {
                            mainActivityViewModel.showChargingStationNotFoundDialogIsShown()
                        }
                    } else {
                        mainActivityViewModel.showBadQRCodeDialogIsShown()
                    }
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
                    val badQRCodeDialogIsShown by mainActivityViewModel.badQRCodeDialogIsShown.collectAsState()
                    val chargingStationNotFoundDialogIsShown by mainActivityViewModel.chargingStationNotFoundDialogIsShown.collectAsState()
                    val connectionProblemDialogIsShown by mainActivityViewModel.connectionProblemDialogIsShown.collectAsState()


                    val chargingStationDetailsSheetIsShown by mainActivityViewModel.chargingStationDetailsSheetIsShown.collectAsState()
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

                        badQRCodeDialogIsShown -> {
                            BadQRCodeDialog(
                                onDismissRequest = {
                                    mainActivityViewModel.hideBadQRCodeDialogIsShown()
                                }, onConfirmation = {
                                    mainActivityViewModel.hideBadQRCodeDialogIsShown()
                                }
                            )
                        }

                        internetConnectionDialogIsShown -> {
                            InternetConnectionDialog()
                        }

                        chargingStationNotFoundDialogIsShown -> {
                            ChargingStationNotFoundDialog(
                                onDismissRequest = {
                                    mainActivityViewModel.hideChargingStationNotFoundDialogIsShown()
                                }, onConfirmation = {
                                    mainActivityViewModel.hideChargingStationNotFoundDialogIsShown()
                                }
                            )
                        }

                        connectionProblemDialogIsShown -> {
                            ConnectionProblemDialog(
                                onDismissRequest = {},
                                onConfirmation = {
                                    mainActivityViewModel.hideConnectionProblemDialog()
                                    mainActivityViewModel.fetchChargingStations()
                                }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (!chargingStationsFetched) {
                            if (!internetConnectionDialogIsShown && !connectionProblemDialogIsShown) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                            if (!chargingStationsFetching) {
                                if (!isNetworkAvailable(this@MainActivity)) {
                                    mainActivityViewModel.showInternetConnectionDialog()
                                } else {
                                    mainActivityViewModel.showConnectionProblemDialog()
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
                            if (chargingStationDetailsSheetIsShown) {
                                searchSheetIsShown = false
                                ChargingStationDetailsSheet(onDismissRequest = {
                                    mainActivityViewModel.hideChargingStationDetailsSheet()
                                })
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChargingStationDetailsSheet(onDismissRequest: () -> Unit) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = false,
        )

        ModalBottomSheet(modifier = Modifier
            .fillMaxHeight()
            .padding(0.dp, 32.dp, 0.dp, 0.dp),
            sheetState = sheetState,
            onDismissRequest = { onDismissRequest() }) {
            ChargingStationDetails()
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
    fun ChargingStationsMap() {
        AndroidView(factory = {
            mapView
        })
    }

    private fun addMarkers() {
        val marker = createBitmapFromVector(this, R.drawable.baseline_circle_24)
        val imageProvider = ImageProvider.fromBitmap(marker)

        val points = mutableListOf<Pair<Int, Point>>()
        mainActivityViewModel.chargingStations.value.forEach {
            points.add(it.id to Point(it.latitude, it.longitude))
        }

        points.forEach { pair ->
            val placemarkMapObject = mapView.map.mapObjects.addPlacemark().apply {
                geometry = pair.second
                setIcon(imageProvider)
                userData = title
            }
            val listener = MapObjectTapListener { _, _ ->
                moveTo(pair.first, pair.second)
                true
            }
            placemarkMapObject.addTapListener(listener)
            tapListeners.add(listener)
            placemarkMapObjectList.add(placemarkMapObject)
        }
    }

    private fun moveTo(chargingStationId: Int, point: Point) {
        mainActivityViewModel.showChargingStationDetailsSheet(chargingStationId)
        mapView.map.move(
            CameraPosition(
                Point(point.latitude, point.longitude),
                20.0f,
                mapView.map.cameraPosition.azimuth,
                mapView.map.cameraPosition.tilt
            ), Animation(Animation.Type.SMOOTH, 1f), cameraCallback
        )
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChargingStationList() {
        val searchQuery by mainActivityViewModel.searchQuery.collectAsState()
        val filteredChargingStations by mainActivityViewModel.filteredChargingStations.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp, 0.dp)
        ) {
//            var active by remember { mutableStateOf(false) }
//            SearchBar(
//                query = searchQuery,
//                onQueryChange = { mainActivityViewModel.updateSearchQuery(it) },
//                onSearch = {active = false},
//                active = active,
//                onActiveChange = {active = it},
//                placeholder = { Text("Name/Address...")},
//                leadingIcon = {Icon(imageVector = Icons.Filled.Search, contentDescription = "description")},
//            ) {
//                LazyColumn(
//                    verticalArrangement = Arrangement.spacedBy(8.dp),
//                    contentPadding = PaddingValues(bottom = 64.dp),
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .fillMaxHeight()
//
//                ) {
//                    items(filteredChargingStations, key = { it.id }) { station ->
//                        ChargingStationItem(station) {
//                            moveTo(station.id, Point(station.latitude, station.longitude))
//                        }
//                        HorizontalDivider(color = Color.Gray, thickness = 1.dp)
//                    }
//                }
//            }
            ChargingStationSearchBar(searchQuery = searchQuery,
                onSearchQueryChanged = { mainActivityViewModel.updateSearchQuery(it) })
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 64.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()

                ) {
                    items(filteredChargingStations, key = { it.id }) { station ->
                        ChargingStationItem(station) {
                            moveTo(station.id, Point(station.latitude, station.longitude))
                        }
                        HorizontalDivider(color = Color.Gray, thickness = 1.dp)
                    }
                }
            }
        }
    }

    @Composable
    fun ChargingStationDetails() {
        val chargingStationDetails by mainActivityViewModel.chargingStationDetails.collectAsState()
        if (chargingStationDetails != null) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(text = chargingStationDetails!!.name, fontSize = 24.sp, color = Color.Black)
                Text(text = chargingStationDetails!!.address, fontSize = 20.sp, color = Color.Gray)
                Text(text = "Connectors", fontSize = 24.sp, color = Color.Black)
                if (chargingStationDetails!!.connectors.isEmpty()) {
                    Text(text = "Empty", fontSize = 20.sp, color = Color.Gray)
                } else {
                    chargingStationDetails!!.connectors.forEach {
                        Box(
                            modifier = Modifier
                                .background(Color.Gray, shape = RoundedCornerShape(8.dp))
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            Column {
                                Text(text = it.chargingType.name, fontSize = 20.sp, color = Color.White)
                                Text(
                                    text = it.chargingType.currentType,
                                    fontSize = 16.sp,
                                    color = Color.LightGray
                                )
                                Text(
                                    text = it.rate.toInt().toString() + " kW/h",
                                    fontSize = 14.sp,
                                    color = Color.LightGray
                                )
                                if (it.status == 1) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color.Green, shape = RoundedCornerShape(8.dp))
                                            .padding(4.dp)
                                    ) {
                                        Text(
                                            text = "active",
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .background(Color.Red, shape = RoundedCornerShape(8.dp))
                                            .padding(4.dp)
                                    ) {
                                        Text(
                                            text = "inactive",
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Text(
                    text = "Opening hours",
                    fontSize = 24.sp
                )
                Box(
                    modifier = Modifier
                        .background(Color.Gray, shape = RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    Text(
                        text = chargingStationDetails!!.openingHours,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
                Text(
                    text = "Description",
                    fontSize = 24.sp
                )
                Box(
                    modifier = Modifier
                        .background(Color.Gray, shape = RoundedCornerShape(8.dp))
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 64.dp)
                        .padding(4.dp)
                ) {
                    Text(
                        text = chargingStationDetails!!.description ?: "No description",
                        fontSize = 16.sp,
                        color = Color.LightGray,
                    )
                }
                Text(
                    text = "Marks",
                    fontSize = 24.sp
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }
    }
}


//@Preview
//@Composable
//fun ChargingStationDetailsPreview() {
//    Surface(
//        modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
//    ) {
//        val connectors = listOf<Connector>(
//            Connector(0, 1, 0, ChargingType(0, "TYPE 2", "AC"), 22.0),
//            Connector(1, 1, 1, ChargingType(0, "GB/T", "DC"), 65.0)
//        )
//        Column(
//            verticalArrangement = Arrangement.spacedBy(6.dp),
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp)
//        ) {
//            Text(text = "Name", fontSize = 24.sp, color = Color.Black)
//            Text(text = "улица иванова", fontSize = 20.sp, color = Color.Gray)
//            Text(text = "Connectors", fontSize = 24.sp, color = Color.Black)
//            if (connectors.isEmpty()) {
//                Text(text = "Empty", fontSize = 20.sp, color = Color.Gray)
//            } else {
//                connectors.forEach {
//                    Box(
//                        modifier = Modifier
//                            .background(Color.Gray, shape = RoundedCornerShape(8.dp))
//                            .fillMaxWidth()
//                            .padding(4.dp)
//                    ) {
//                        Column {
//                            Text(text = it.chargingType.name, fontSize = 20.sp, color = Color.White)
//                            Text(
//                                text = it.chargingType.currentType,
//                                fontSize = 16.sp,
//                                color = Color.LightGray
//                            )
//                            Text(
//                                text = it.rate.toInt().toString() + " kW/h",
//                                fontSize = 14.sp,
//                                color = Color.LightGray
//                            )
//                            if (it.status == 1) {
//                                Box(
//                                    modifier = Modifier
//                                        .background(Color.Green, shape = RoundedCornerShape(8.dp))
//                                        .padding(4.dp)
//                                ) {
//                                    Text(
//                                        text = "active",
//                                        fontSize = 12.sp,
//                                        color = Color.White
//                                    )
//                                }
//                            } else {
//                                Box(
//                                    modifier = Modifier
//                                        .background(Color.Red, shape = RoundedCornerShape(8.dp))
//                                        .padding(4.dp)
//                                ) {
//                                    Text(
//                                        text = "inactive",
//                                        fontSize = 12.sp,
//                                        color = Color.White
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            Text(
//                text = "Opening hours",
//                fontSize = 24.sp
//            )
//            Box(
//                modifier = Modifier
//                    .background(Color.Gray, shape = RoundedCornerShape(8.dp))
//                    .padding(4.dp)
//            ) {
//                Text(
//                    text = "8-22",
//                    fontSize = 20.sp,
//                    color = Color.White
//                )
//            }
//            Text(
//                text = "Description",
//                fontSize = 24.sp
//            )
//            Box(
//                modifier = Modifier
//                    .background(Color.Gray, shape = RoundedCornerShape(8.dp))
//                    .fillMaxWidth()
//                    .defaultMinSize(minHeight = 64.dp)
//                    .padding(4.dp)
//            ) {
//                Text(
//                    text = "Super station",
//                    fontSize = 16.sp,
//                    color = Color.LightGray,
//                )
//            }
//            Text(
//                text = "Marks",
//                fontSize = 24.sp
//            )
//        }
//    }
//}