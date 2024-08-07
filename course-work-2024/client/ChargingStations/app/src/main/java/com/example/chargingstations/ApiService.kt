package com.example.chargingstations
import com.example.chargingstations.model.AuthData
import com.example.chargingstations.model.ChargingStationDetails
import com.example.chargingstations.model.ChargingStationImage
import com.example.chargingstations.model.IntStatus
import com.example.chargingstations.model.JsonChargingStation
import com.example.chargingstations.model.RegistrationData
import com.example.chargingstations.model.Token
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("charging-stations")
    fun getChargingStations(
        @Query("level") level: String?,
        @Query("query") query: String?
    ): Call<List<JsonChargingStation>>

    @GET("charging-stations/{id}")
    fun getChargingStationDetails(@Path("id") chargingStationId: Int): Call<ChargingStationDetails>

    @GET("charging-station-images/{id}")
    fun getChargingStationImage(@Path("id") chargingStationImageId: Int): Call<ChargingStationImage>

    @POST("register")
    fun register(@Body body: RegistrationData): Call<IntStatus>
    @POST("auth")
    fun auth(@Body body: AuthData): Call<Token>
}