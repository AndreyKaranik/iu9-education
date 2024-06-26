package com.example.chargingstations.model

import com.google.gson.annotations.SerializedName

data class ChargingStation (
    val id: Int,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("company_id")
    val companyId: Int,
    @SerializedName("opening_hours")
    val openingHours: String,
    val description: String?
)