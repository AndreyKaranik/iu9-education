package com.andreykaranik.gpstracker.domain.model

import com.google.gson.annotations.SerializedName

data class AccelerometerData(
    val x: Double,
    val y: Double,
    val z: Double,
    @SerializedName("recorded_at")
    val recordedAt: String
)