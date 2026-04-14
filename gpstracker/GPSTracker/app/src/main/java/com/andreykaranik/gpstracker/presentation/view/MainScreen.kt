package com.andreykaranik.gpstracker.presentation.view

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathSegment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.andreykaranik.gpstracker.R
import com.andreykaranik.gpstracker.domain.model.GroupData
import com.andreykaranik.gpstracker.domain.model.GroupMember
import com.andreykaranik.gpstracker.domain.model.LocationData
import com.andreykaranik.gpstracker.domain.model.ModeParameters
import com.andreykaranik.gpstracker.domain.model.UserData
import com.andreykaranik.gpstracker.domain.model.result.GetGroupDataResult
import com.andreykaranik.gpstracker.domain.model.result.GetGroupMembersResult
import com.andreykaranik.gpstracker.domain.model.result.LeaveGroupResult
import com.andreykaranik.gpstracker.domain.model.result.SaveUserDataResult
import com.andreykaranik.gpstracker.presentation.viewmodel.MainScreenViewModel
import com.github.tehras.charts.line.LineChart
import com.github.tehras.charts.line.LineChartData
import com.github.tehras.charts.line.renderer.line.SolidLineDrawer
import com.github.tehras.charts.line.renderer.point.FilledCircularPointDrawer
import com.github.tehras.charts.line.renderer.xaxis.SimpleXAxisDrawer
import com.github.tehras.charts.line.renderer.yaxis.SimpleYAxisDrawer
import com.github.tehras.charts.piechart.animation.simpleChartAnimation
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: MainScreenViewModel
) {

    val context = LocalContext.current

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            setMultiTouchControls(true)
            controller.setZoom(13.0)
            controller.setCenter(
                GeoPoint(
                    55.7522, 37.6156
                )
            )
        }
    }

    val getGroupDataResult by viewModel.getGroupDataResult.collectAsState()
    val groupData by viewModel.groupData.collectAsState()

    val leaveGroupResult by viewModel.leaveGroupResult.collectAsState()

    val saveUserDataResult by viewModel.saveUserDataResult.collectAsState()

    val getGroupMembersResult by viewModel.getGroupMembersResult.collectAsState()
    val groupMembers by viewModel.groupMembers.collectAsState()

    val userData by viewModel.userData.collectAsState()
    val selectedUserId by viewModel.selectedUserId.collectAsState()

    val selectedMode by viewModel.selectedMode.collectAsState()
    val parameters by viewModel.parameters.collectAsState()

    val showSettings = remember { mutableStateOf(false) }
    val showSelectGroupMemberSheet = remember { mutableStateOf(false) }
    val showModeSheet = remember { mutableStateOf(false) }

    val isUnauthorized by viewModel.isUnauthorized.collectAsState()

    if (isUnauthorized) {
        viewModel.setUnauthorized(false)
        navController.navigate("login_screen") {
            popUpTo("main_screen") { inclusive = true }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getUserData()
        viewModel.getGroupData()
    }

    when (saveUserDataResult) {
        is SaveUserDataResult.Success -> {
            viewModel.clearSaveUserDataResult()
            navController.navigate("login_screen") {
                popUpTo("main_screen") { inclusive = true }
            }
        }

        is SaveUserDataResult.Failure -> {
            viewModel.clearSaveUserDataResult()
        }

        else -> {}
    }

    when (getGroupDataResult) {
        is GetGroupDataResult.Success -> {
            viewModel.clearGetGroupDataResult()
        }

        is GetGroupDataResult.Unauthorized -> {
            viewModel.clearGetGroupDataResult()
            navController.navigate("login_screen") {
                popUpTo("main_screen") { inclusive = true }
            }
        }

        is GetGroupDataResult.IsNotInGroup -> {
            viewModel.clearGetGroupDataResult()
            navController.navigate("group_entry_screen") {
                popUpTo("main_screen") { inclusive = true }
            }
        }

        is GetGroupDataResult.Failure -> {
            viewModel.clearGetGroupDataResult()
            navController.navigate("login_screen") {
                popUpTo("main_screen") { inclusive = true }
            }
        }

        else -> {}
    }

    when (leaveGroupResult) {
        is LeaveGroupResult.Success -> {
            viewModel.clearLeaveGroupResult()
            navController.navigate("group_entry_screen") {
                popUpTo("main_screen") { inclusive = true }
            }
        }

        is LeaveGroupResult.Unauthorized -> {
            viewModel.clearLeaveGroupResult()
            navController.navigate("login_screen") {
                popUpTo("main_screen") { inclusive = true }
            }
        }

        is LeaveGroupResult.IsNotInGroup -> {
            viewModel.clearLeaveGroupResult()
            navController.navigate("group_entry_screen") {
                popUpTo("main_screen") { inclusive = true }
            }
        }

        is LeaveGroupResult.Failure -> {
            viewModel.clearLeaveGroupResult()
            navController.navigate("login_screen") {
                popUpTo("main_screen") { inclusive = true }
            }
        }

        else -> {}
    }

    when (getGroupMembersResult) {
        is GetGroupMembersResult.Success -> {
            viewModel.clearLeaveGroupResult()
        }

        is GetGroupMembersResult.Unauthorized -> {
            viewModel.clearGetGroupMembersResult()
            navController.navigate("login_screen") {
                popUpTo("main_screen") { inclusive = true }
            }
        }

        is GetGroupMembersResult.IsNotInGroup -> {
            viewModel.clearGetGroupMembersResult()
            navController.navigate("group_entry_screen") {
                popUpTo("main_screen") { inclusive = true }
            }
        }

        is GetGroupMembersResult.Failure -> {
            viewModel.clearGetGroupMembersResult()
            navController.navigate("login_screen") {
                popUpTo("main_screen") { inclusive = true }
            }
        }

        else -> {}
    }

    val locationPoints by viewModel.locationPointsFlow.collectAsState()

    LaunchedEffect(locationPoints) {
        mapView.overlays.clear()
        locationPoints.forEach { (lat, lon) ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(lat, lon)
                if (selectedMode == ModeType.CURRENT_LOCATION) {
                    icon = ContextCompat.getDrawable(context, R.drawable.person_pin_circle_icon)
                } else {
                    icon = ContextCompat.getDrawable(context, R.drawable.baseline_circle_16)
                }
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setOnMarkerClickListener { _, _ ->
                    viewModel.onMarkerClick()
                    true
                }
            }
            mapView.overlays.add(marker)
        }
        mapView.invalidate()
    }

    val bottomSheetVisible by viewModel.bottomSheetVisible.collectAsState()
    val mode12Data by viewModel.mode12Data.collectAsState()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (bottomSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.onBottomSheetDismiss()
            },
            sheetState = sheetState
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                mode12Data?.let {
                    val amplitudes = it.accelerometerDataList.map { d ->
                        sqrt(d.x * d.x + d.y * d.y + d.z * d.z)
                    }
                    Column {
                        Text(
                            text = "${stringResource(id = R.string.last_record_time)}:",
                            fontSize = 16.sp
                        )
                        Text(
                            text = it.locationRecordedAt,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "${stringResource(id = R.string.latitude)} ${ if (parameters.kalmanEnabled) it.locationDataList.last().kalmanLatitude else it.locationDataList.last().latitude}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "${stringResource(id = R.string.longitude)} ${ if (parameters.kalmanEnabled) it.locationDataList.last().kalmanLongitude else it.locationDataList.last().longitude}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = stringResource(id = R.string.accelerometer_amplitude_chart))
                        AmplitudeGraph(amplitudes = amplitudes)
                        var steps = 0
                        for (i in 1 until it.locationDataList.size) {
                            steps += it.locationDataList[i].steps
                        }
                        Text(text = "${stringResource(id = R.string.steps_last_hour)} $steps")

                    }
                }
            }
        }
    }

    val center = remember { GeoPoint(55.751244, 37.618423) } // Москва
    val radiusMeters = remember { 5000.0 } // 5 км

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        AndroidView(
            factory = { mapView },
            update = {}
        )

        if (showSettings.value) {
            SettingsBottomSheet(
                viewModel = viewModel,
                leaveGroupResult = leaveGroupResult,
                userData = userData,
                groupData = groupData,
                onDismiss = { showSettings.value = false }
            )
        }

        if (showSelectGroupMemberSheet.value) {
            SelectGroupMemberBottomSheet(
                groupMembers = groupMembers,
                selectedUserId = selectedUserId,
                onUserSelected = {id -> viewModel.selectUser(id)},
                onDismissRequest = { showSelectGroupMemberSheet.value = false }
            )
        }

        if (showModeSheet.value) {
            ModeSelectionBottomSheet(
                selectedMode = selectedMode,
                modeParameters = parameters,
                onModeSelected = { viewModel.selectMode(it) },
                onParametersChanged = { viewModel.updateParameters(it) },
                onDismissRequest = { showModeSheet.value = false }
            )
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

            SettingsButton(
                viewModel = viewModel,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(y = 32.dp),
                showSettings = showSettings
            )

            ModeButton(
                viewModel = viewModel,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(y = (-32).dp),
                showModeSheet = showModeSheet
            )

            SelectGroupMemberButton(
                viewModel = viewModel,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(y = (-32).dp),
                showSelectGroupMemberSheet = showSelectGroupMemberSheet
            )

            Column(
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                PlusButton(mapView = mapView)
                Spacer(modifier = Modifier.height(8.dp))
                MinusButton(mapView = mapView)
            }

        }
    }
}

@Composable
fun PlusButton(mapView: MapView) {
    FilledIconButton(
        onClick = {
            mapView.apply {
                controller.zoomTo(zoomLevelDouble + 0.5, 100)
            }
        },
        Modifier.size(48.dp),
    ) {
        Icon(
            painter = painterResource(id = R.drawable.plus_icon),
            contentDescription = null
        )
    }
}

@Composable
fun MinusButton(mapView: MapView) {
    FilledIconButton(
        onClick = {
            mapView.apply {
                controller.zoomTo(zoomLevelDouble - 0.5, 100)
            }
        },
        Modifier.size(48.dp),
    ) {
        Icon(
            painter = painterResource(id = R.drawable.minus_icon),
            contentDescription = null
        )
    }
}

@Composable
fun SettingsButton(
    viewModel: MainScreenViewModel,
    modifier: Modifier,
    showSettings: MutableState<Boolean>
) {
    FilledIconButton(
        onClick = {
            viewModel.getGroupMembers()
            showSettings.value = true
        },
        modifier = modifier.size(56.dp)
    ) {
        Icon(
            modifier = Modifier.size(28.dp),
            painter = painterResource(id = R.drawable.settings_icon),
            contentDescription = null
        )
    }
}

@Composable
fun ModeButton(
    viewModel: MainScreenViewModel,
    modifier: Modifier,
    showModeSheet: MutableState<Boolean>
) {
    FilledIconButton(
        onClick = {
            showModeSheet.value = true
        },
        modifier = modifier.size(56.dp)
    ) {
        Icon(
            modifier = Modifier.size(28.dp),
            painter = painterResource(id = R.drawable.mode_icon),
            contentDescription = null
        )
    }
}

@Composable
fun SelectGroupMemberButton(
    viewModel: MainScreenViewModel,
    modifier: Modifier,
    showSelectGroupMemberSheet: MutableState<Boolean>
) {
    FilledIconButton(
        onClick = {
            viewModel.getGroupMembers()
            showSelectGroupMemberSheet.value = true
        },
        modifier = modifier.size(64.dp)
    ) {
        Icon(
            modifier = Modifier.size(32.dp),
            painter = painterResource(id = R.drawable.select_icon),
            contentDescription = null
        )
    }
}

@Composable
fun AmplitudeGraph(amplitudes: List<Double>) {
    val maxAmplitude = amplitudes.max()
    val lineColor = Color.Red


    Column {
        Spacer(modifier = Modifier.height(50.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 16.dp)
        ) {
            val graphWidth = size.width
            val graphHeight = size.height
            val pointGap = graphWidth / (amplitudes.size - 1).coerceAtLeast(1)

            val points = amplitudes.mapIndexed { index, amp ->
                val x = index * pointGap
                val y = graphHeight - (amp / maxAmplitude).toFloat() * graphHeight
                Offset(x, y)
            }

            for (i in 0 until points.size - 1) {
                drawLine(
                    color = lineColor,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 2f
                )
            }
        }
    }
}