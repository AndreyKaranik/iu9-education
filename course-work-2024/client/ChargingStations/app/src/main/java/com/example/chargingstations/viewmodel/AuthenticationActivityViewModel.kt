package com.example.chargingstations.viewmodel

import androidx.lifecycle.ViewModel
import com.example.chargingstations.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthenticationActivityViewModel : ViewModel() {

    private val TAG: String = "AutenticationActivityViewModel"

    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val httpClient = OkHttpClient.Builder().apply {
        addInterceptor(logging)
    }.build()

    private var lastChargingStationDetailsId: Int? = null

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://89.111.172.144:8000/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)
        .build()

    private val apiService = retrofit.create(ApiService::class.java)
}