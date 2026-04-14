package com.andreykaranik.gpstracker.data.api.response

import com.google.gson.annotations.SerializedName

data class RefreshTokenResponse(
    @SerializedName("access_token")
    val accessToken: String?,
    val message: String?,
    val error: String?
)
