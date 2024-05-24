package com.example.chargingstations.model

data class ChargingStation (
    val id: Int,
    val name: String,
    val address: String,
    val companyId: Int,
    val openingHours: String,
    val description: String?
)