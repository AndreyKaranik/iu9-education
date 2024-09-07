package com.example.chargingstations.presentation.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.chargingstations.presentation.theme.ChargingStationsTheme
import com.example.chargingstations.presentation.viewmodel.AuthenticationActivityViewModel
import com.example.chargingstations.presentation.viewmodel.OrderActivityViewModel

class OrderActivity : ComponentActivity() {

    private val viewModel: OrderActivityViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.extras?.let {
            viewModel.setConnectorId(it.getInt("connector_id"))
            viewModel.setChargingStationAddress(it.getString("charging_station_address"))
            viewModel.setConnectorTypeName(it.getString("charging_type_name"))
            viewModel.setConnectorRate(it.getFloat("connector_rate"))
            viewModel.setToken(it.getString("token"))
        }


        setContent {
            ChargingStationsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val chargingStationAddress by viewModel.chargingStationAddress.collectAsState()
                    val connectorTypeName by viewModel.connectorTypeName.collectAsState()
                    val connectorRate by viewModel.connectorRate.collectAsState()
                    val amount by viewModel.amount.collectAsState()
                    val orderStatus by viewModel.orderStatus.collectAsState()
                    val orderProgress by viewModel.orderProgress.collectAsState()

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when (orderStatus) {
                            null -> {
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
                                        value = amount.toString(),
                                        onValueChange = {
                                            viewModel.setAmount(it.toFloat())
                                        },
                                        keyboardOptions = KeyboardOptions.Default.copy(
                                            keyboardType = KeyboardType.Number
                                        )
                                    )
                                    Spacer(modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "Адрес: "
                                    )
                                    Text(
                                        text = chargingStationAddress ?: "null"
                                    )
                                    Text(
                                        text = "Тип зарядки: "
                                    )
                                    Text(
                                        text = connectorTypeName ?: "null"
                                    )
                                    Text(
                                        text = "Скорость зарядки: "
                                    )
                                    Text(
                                        text = "${connectorRate ?: "null"} kW/h"
                                    )
                                    Spacer(modifier = Modifier.size(16.dp))
                                    Button(
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .fillMaxWidth(),
                                        onClick = {
                                            viewModel.charge()
                                        }
                                    ) {
                                        Text(text = "Зарядить")
                                    }
                                }
                            }
                            0 -> {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .fillMaxWidth()
                                        .padding(horizontal = 64.dp),
                                ) {
                                    Text(text = "progress: $orderProgress")
                                }
                            }
                            1 -> {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .fillMaxWidth()
                                        .padding(horizontal = 64.dp),
                                ) {
                                    Text(text = "completed")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}