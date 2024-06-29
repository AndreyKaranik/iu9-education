package com.example.chargingstations.model

data class ChargingStation (
    val id: Int,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val company_id: Int,
    val opening_hours: String,
    val description: String?
)