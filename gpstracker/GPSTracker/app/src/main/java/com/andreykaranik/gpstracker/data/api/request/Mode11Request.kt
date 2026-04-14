package com.andreykaranik.gpstracker.data.api.request

import com.google.gson.annotations.SerializedName

data class Mode11Request(
    @SerializedName("user_id")
    val targetUserId: Int,
    @SerializedName("kalman_enabled")
    val kalmanEnabled: Boolean
)
