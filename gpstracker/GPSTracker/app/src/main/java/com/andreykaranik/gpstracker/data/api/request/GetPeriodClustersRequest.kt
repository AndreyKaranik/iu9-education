package com.andreykaranik.gpstracker.data.api.request

import com.google.gson.annotations.SerializedName

data class GetPeriodClustersRequest(
    @SerializedName("user_id")
    val targetUserId: Int,
    val from: String,
    val to: String,
    @SerializedName("kalman_enabled")
    val kalmanEnabled: Boolean,
    val eps: Double,
    @SerializedName("min_pts")
    val minPts: Int
)