package com.example.chargingstations.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chargingstations.R
import com.example.chargingstations.model.ChargingMarkWithUserName
import com.example.chargingstations.model.ChargingStationDetails
import com.example.chargingstations.model.ChargingType
import com.example.chargingstations.model.ConnectorDetails
import com.example.chargingstations.ui.theme.ChargingStationsTheme
import com.example.chargingstations.ui.theme.Gray1
import com.example.chargingstations.ui.theme.Gray2
import com.example.chargingstations.viewmodel.MainActivityViewModel

@Composable
fun ChargingStationDetailsView(mainActivityViewModel: MainActivityViewModel) {
    val chargingStationDetails by mainActivityViewModel.chargingStationDetails.collectAsState()
    val chargingStationImageBitmap by mainActivityViewModel.chargingStationImageBitmap.collectAsState()
    if (chargingStationDetails != null) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(color = Color.LightGray, shape = RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (chargingStationImageBitmap != null) {
                        Image(
                            modifier = Modifier
                                .size(256.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            bitmap = chargingStationImageBitmap!!,
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            modifier = Modifier.size(32.dp),
                            tint = Color.Gray,
                            imageVector = ImageVector.vectorResource(R.drawable.baseline_image_24),
                            contentDescription = null
                        )
                    }
                }
                Spacer(modifier = Modifier.size(16.dp))
                Column {
                    Text(
                        text = chargingStationDetails!!.name,
                        fontSize = 24.sp,
                        color = Color.Black
                    )
                    Text(
                        text = chargingStationDetails!!.address,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Row(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(4.dp, 0.dp)
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.CenterVertically),
                            imageVector = ImageVector.vectorResource(R.drawable.baseline_access_time_24),
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.size(2.dp))
                        Text(
                            text = chargingStationDetails!!.openingHours,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
            HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(R.string.charging_station_details_connectors_title),
                fontSize = 16.sp,
                color = Color.LightGray
            )
            if (chargingStationDetails!!.connectors.isEmpty()) {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(R.string.charging_station_details_no_connectors),
                    fontSize = 20.sp,
                    color = Color.Gray
                )
            } else {
                chargingStationDetails!!.connectors.forEach {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Gray1, shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 0.5.dp, color = Gray2, shape = RoundedCornerShape(8.dp)
                            )
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Row {
                            Column {
                                Text(
                                    text = it.chargingType.name,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = it.rate.toInt().toString() + " kW/h",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.CenterVertically),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    val connectorStatusName: Int
                                    val connectorStatusColor: Color
                                    when (it.status) {
                                        1 -> {
                                            connectorStatusName =
                                                R.string.charging_station_details_connector_active
                                            connectorStatusColor = Color.Green
                                        }

                                        2 -> {
                                            connectorStatusName =
                                                R.string.charging_station_details_connector_active
                                            connectorStatusColor = Color.Yellow
                                        }

                                        else -> {
                                            connectorStatusName =
                                                R.string.charging_station_details_connector_inactive
                                            connectorStatusColor = Color.Red
                                        }
                                    }

                                    Text(
                                        text = stringResource(connectorStatusName),
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                connectorStatusColor,
                                                shape = RoundedCornerShape(percent = 50)
                                            )
                                            .size(12.dp)
                                            .border(
                                                width = 1.dp,
                                                color = Color.LightGray,
                                                shape = RoundedCornerShape(percent = 50)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(R.string.charging_station_details_description_title),
                fontSize = 16.sp,
                color = Color.LightGray
            )


            if (chargingStationDetails!!.description == null) {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(R.string.charging_station_details_no_description),
                    fontSize = 20.sp,
                    color = Color.Gray
                )
            } else {
                Box(
                    modifier = Modifier
                        .background(
                            color = Gray1, shape = RoundedCornerShape(8.dp)
                        )
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 64.dp)
                        .padding(8.dp)
                ) {
                    Text(
                        text = chargingStationDetails!!.description!!,
                        fontSize = 14.sp,
                        color = Color.Black,
                    )
                }
            }
            HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(R.string.charging_station_details_marks_title),
                fontSize = 16.sp,
                color = Color.LightGray
            )
            if (chargingStationDetails!!.chargingMarksWithUserName.isEmpty()) {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(R.string.charging_station_details_no_marks),
                    fontSize = 20.sp,
                    color = Color.Gray
                )
            } else {
                chargingStationDetails!!.chargingMarksWithUserName.forEach {
                    Box(
                        modifier = Modifier
                            .background(
                                Color.Gray, shape = RoundedCornerShape(8.dp)
                            )
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Column {
                            if (it.userId != null) {
                                Text(
                                    text = it.userName!!, fontSize = 20.sp, color = Color.White
                                )
                            } else {
                                Text(text = "Anonymous", fontSize = 20.sp, color = Color.White)
                            }
                            if (it.status == 1) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Color.Green, shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = "Success", fontSize = 12.sp, color = Color.White
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Color.Red, shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = "Failed", fontSize = 12.sp, color = Color.White
                                    )
                                }
                            }
                            Text(text = it.chargingType.name)
                            Text(text = it.time)
                        }
                    }
                }
            }
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

@Preview
@Composable
fun ChargingStationDetailsPreview() {
    ChargingStationsTheme {
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {
            val connectors = listOf<ConnectorDetails>(
                ConnectorDetails(0, 1, 0, ChargingType(0, "TYPE 2", "AC"), 22.0),
                ConnectorDetails(1, 1, 1, ChargingType(0, "GB/T", "DC"), 65.0)
            )
            val marks = listOf<ChargingMarkWithUserName>(
                ChargingMarkWithUserName(0, 1, 0, 1, "John", ChargingType(0, "TYPE 2", "AC"), "12:12:12"),
                ChargingMarkWithUserName(1, 1, 1, null, null, ChargingType(0, "GB/T", "DC"), "12:12:12")
            )
            val chargingStationImageBitmap = null
            val chargingStationDetails = ChargingStationDetails(
                0,
                "SuperCharger",
                "ул. Иванова, Москва",
                0.0,
                0.0,
                0,
                "8-22",
                "Отличная зарядная станция в Москве не улице Иванова.",
                connectors,
                marks,
                emptyList()
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(color = Color.LightGray, shape = RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (chargingStationImageBitmap != null) {
                            Image(
                                modifier = Modifier
                                    .size(256.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                bitmap = chargingStationImageBitmap!!,
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                modifier = Modifier.size(32.dp),
                                tint = Color.Gray,
                                imageVector = ImageVector.vectorResource(R.drawable.baseline_image_24),
                                contentDescription = null
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(16.dp))
                    Column {
                        Text(
                            text = chargingStationDetails!!.name,
                            fontSize = 24.sp,
                            color = Color.Black
                        )
                        Text(
                            text = chargingStationDetails!!.address,
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Row(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(4.dp, 0.dp)
                        ) {
                            Icon(
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.CenterVertically),
                                imageVector = ImageVector.vectorResource(R.drawable.baseline_access_time_24),
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.size(2.dp))
                            Text(
                                text = chargingStationDetails!!.openingHours,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(R.string.charging_station_details_connectors_title),
                    fontSize = 16.sp,
                    color = Color.LightGray
                )
                if (chargingStationDetails!!.connectors.isEmpty()) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = stringResource(R.string.charging_station_details_no_connectors),
                        fontSize = 20.sp,
                        color = Color.Gray
                    )
                } else {
                    chargingStationDetails!!.connectors.forEach {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Gray1, shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 0.5.dp, color = Gray2, shape = RoundedCornerShape(8.dp)
                                )
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Row {
                                Column {
                                    Text(
                                        text = it.chargingType.name,
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = it.rate.toInt().toString() + " kW/h",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.CenterVertically),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        val connectorStatusName: Int
                                        val connectorStatusColor: Color
                                        when (it.status) {
                                            1 -> {
                                                connectorStatusName =
                                                    R.string.charging_station_details_connector_active
                                                connectorStatusColor = Color.Green
                                            }

                                            2 -> {
                                                connectorStatusName =
                                                    R.string.charging_station_details_connector_active
                                                connectorStatusColor = Color.Yellow
                                            }

                                            else -> {
                                                connectorStatusName =
                                                    R.string.charging_station_details_connector_inactive
                                                connectorStatusColor = Color.Red
                                            }
                                        }

                                        Text(
                                            text = stringResource(connectorStatusName),
                                            fontSize = 14.sp,
                                            color = Color.Black
                                        )
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    connectorStatusColor,
                                                    shape = RoundedCornerShape(percent = 50)
                                                )
                                                .size(12.dp)
                                                .border(
                                                    width = 1.dp,
                                                    color = Color.LightGray,
                                                    shape = RoundedCornerShape(percent = 50)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(R.string.charging_station_details_description_title),
                    fontSize = 16.sp,
                    color = Color.LightGray
                )


                if (chargingStationDetails!!.description == null) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = stringResource(R.string.charging_station_details_no_description),
                        fontSize = 20.sp,
                        color = Color.Gray
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Gray1, shape = RoundedCornerShape(8.dp)
                            )
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 64.dp)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = chargingStationDetails!!.description!!,
                            fontSize = 14.sp,
                            color = Color.Black,
                        )
                    }
                }
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(R.string.charging_station_details_marks_title),
                    fontSize = 16.sp,
                    color = Color.LightGray
                )
                if (chargingStationDetails!!.chargingMarksWithUserName.isEmpty()) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = stringResource(R.string.charging_station_details_no_marks),
                        fontSize = 20.sp,
                        color = Color.Gray
                    )
                } else {
                    chargingStationDetails!!.chargingMarksWithUserName.forEach {
                        Box(
                            modifier = Modifier
                                .background(
                                    Color.Gray, shape = RoundedCornerShape(8.dp)
                                )
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            Column {
                                if (it.userId != null) {
                                    Text(
                                        text = it.userName!!, fontSize = 20.sp, color = Color.White
                                    )
                                } else {
                                    Text(text = "Anonymous", fontSize = 20.sp, color = Color.White)
                                }
                                if (it.status == 1) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                Color.Green, shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(4.dp)
                                    ) {
                                        Text(
                                            text = "Success", fontSize = 12.sp, color = Color.White
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                Color.Red, shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(4.dp)
                                    ) {
                                        Text(
                                            text = "Failed", fontSize = 12.sp, color = Color.White
                                        )
                                    }
                                }
                                Text(text = it.chargingType.name)
                                Text(text = it.time)
                            }
                        }
                    }
                }
            }
        }
    }
}