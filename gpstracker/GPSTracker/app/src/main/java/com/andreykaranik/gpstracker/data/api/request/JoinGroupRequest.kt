package com.andreykaranik.gpstracker.data.api.request

import com.google.gson.annotations.SerializedName

data class JoinGroupRequest (
    val id: Int,
    @SerializedName("join_code")
    val joinCode: String
)