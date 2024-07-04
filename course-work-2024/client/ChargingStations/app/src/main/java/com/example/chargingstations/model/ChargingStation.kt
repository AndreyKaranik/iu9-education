package com.example.chargingstations.model

import com.google.gson.annotations.SerializedName

data class ChargingStationMin (
    val id: Int,
    val latitude: Double,
    val longitude: Double,
)

data class ChargingStationMedium (
    val id: Int,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("charging_types")
    val chargingTypes: List<ChargingType>
)

data class JsonChargingStation (
    val id: Int?,
    val name: String?,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("opening_hours")
    val openingHours: String?,
    val description: String?,
    @SerializedName("charging_types")
    val chargingTypes: List<ChargingType>?
)