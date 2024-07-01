package com.example.chargingstations.model

import com.google.gson.annotations.SerializedName

data class ConnectorDetails (
    val id: Int,
    @SerializedName("charging_station_id")
    val chargingStationId: Int,
    val status: Int,
    @SerializedName("charging_type")
    val chargingType: ChargingType,
    val rate: Double
)