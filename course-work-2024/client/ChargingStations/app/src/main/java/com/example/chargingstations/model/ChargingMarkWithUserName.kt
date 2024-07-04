package com.example.chargingstations.model

import com.google.gson.annotations.SerializedName

data class ChargingMarkWithUserName (
    val id: Int,
    @SerializedName("charging_station_id")
    val chargingStationId: Int,
    val status: Int,
    @SerializedName("user_id")
    val userId: Int?,
    @SerializedName("user_name")
    val userName: String?,
    @SerializedName("charging_type")
    val chargingType: ChargingType,
    val time: String
)