package com.example.chargingstations.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chargingstations.presentation.theme.ChargingStationsTheme

class OrderActivity : ComponentActivity() {

    private var connectorId: Int = -1
    private var chargingStationId: Int = -1
    private var chargingStationAddress: String = ""
    private var connectorTypeName: String = ""
    private var connectorRate: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.extras?.let {
            connectorId = it.getInt("connector_id")
            chargingStationId = it.getInt("charging_station_id")
            chargingStationAddress = it.getString("charging_station_address")!!
            connectorTypeName = it.getString("charging_type_name")!!
            connectorRate = it.getDouble("connector_rate")
        }
        setContent {
            ChargingStationsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth()
                                .padding(horizontal = 64.dp),
                        ) {
                            Text(
                                text = "Укажите объем зарядки (kW/h): "
                            )
                            TextField(
                                value = "0",
                                onValueChange = {}
                            )
                            Spacer(modifier = Modifier.size(16.dp))
                            Text(
                                text = "Адрес: "
                            )
                            Text(
                                text = chargingStationAddress
                            )
                            Text(
                                text = "Тип зарядки: "
                            )
                            Text(
                                text = connectorTypeName
                            )
                            Text(
                                text = "Скорость зарядки: "
                            )
                            Text(
                                text = "$connectorRate kW/h"
                            )
                            Spacer(modifier = Modifier.size(16.dp))
                            Button(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .fillMaxWidth(),
                                onClick = {

                                }
                            ) {
                                Text(text = "Зарядить")
                            }
                        }
                    }
                }
            }
        }
    }
}