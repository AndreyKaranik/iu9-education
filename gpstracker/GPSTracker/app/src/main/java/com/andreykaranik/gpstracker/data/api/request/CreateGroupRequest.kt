package com.andreykaranik.gpstracker.data.api.request

import com.google.gson.annotations.SerializedName

data class CreateGroupRequest(
    val name: String,
    val type: Int,
    @SerializedName("join_code")
    val joinCode: String
)
