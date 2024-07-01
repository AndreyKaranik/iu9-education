package com.example.chargingstations
import com.example.chargingstations.model.ChargingStation
import com.example.chargingstations.model.ChargingStationDetails
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("charging-stations")
    fun getChargingStations(): Call<List<ChargingStation>>

    @GET("charging-stations/{id}")
    fun getChargingStationDetails(@Path("id") chargingStationId: Int): Call<ChargingStationDetails>
}