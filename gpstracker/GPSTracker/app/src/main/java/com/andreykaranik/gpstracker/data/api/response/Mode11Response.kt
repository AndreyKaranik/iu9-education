package com.andreykaranik.gpstracker.data.api.response

import com.google.gson.annotations.SerializedName

data class Mode11Response(
    val latitude: Double?,
    val longitude: Double?,
    val message: String?,
    val error: String?
)