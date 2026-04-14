package com.andreykaranik.gpstracker.data.api.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val name: String?,
    @SerializedName("access_token")
    val accessToken: String?,
    @SerializedName("refresh_token")
    val refreshToken: String?,
    val message: String?,
    val error: String?
)
