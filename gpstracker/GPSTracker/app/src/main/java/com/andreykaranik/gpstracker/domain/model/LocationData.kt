package com.andreykaranik.gpstracker.domain.model

import com.google.gson.annotations.SerializedName

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val kalmanLatitude: Double,
    val kalmanLongitude: Double,
    val steps: Int,
    @SerializedName("recorded_at")
    val recordedAt: String
)
