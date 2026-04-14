package com.andreykaranik.gpstracker.data.api.request

import com.google.gson.annotations.SerializedName

data class GetPeriodLocationsRequest(
    @SerializedName("user_id")
    val targetUserId: Int,
    val from: String,
    val to: String,
    @SerializedName("min_interval_minutes")
    val minIntervalMinutes: Int
)