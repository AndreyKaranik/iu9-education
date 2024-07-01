package com.example.chargingstations.model

import com.google.gson.annotations.SerializedName

data class ChargingMark (
    val id: Int,
    @SerializedName("charging_station_id")
    val chargingStationId: Int,
    val status: Int,
    @SerializedName("user_id")
    val userId: Int?
)