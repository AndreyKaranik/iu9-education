package com.andreykaranik.gpstracker.data.api.request

import com.google.gson.annotations.SerializedName

data class RefreshTokenRequest(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String
)
