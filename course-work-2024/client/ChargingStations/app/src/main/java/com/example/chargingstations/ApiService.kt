package com.example.chargingstations
import com.example.chargingstations.model.ChargingStation
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("charging-stations")
    fun getChargingStations(): Call<List<ChargingStation>>
}