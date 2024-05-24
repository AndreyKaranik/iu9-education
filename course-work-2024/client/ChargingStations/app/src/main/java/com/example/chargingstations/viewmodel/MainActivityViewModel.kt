package com.example.chargingstations.viewmodel
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chargingstations.ApiService
import com.example.chargingstations.model.ChargingStation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory

class MainActivityViewModel : ViewModel() {

    private val _chargingStations = MutableStateFlow<List<ChargingStation>>(emptyList())
    val chargingStations: StateFlow<List<ChargingStation>> = _chargingStations

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://89.111.172.144:8000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            try {
                val response = apiService.getChargingStations().awaitResponse()
                if (response.isSuccessful) {
                    response.body()?.let {
                        _chargingStations.value = it
                    }
                } else {
                    Log.e("E", "error")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("E", "exception")
            }
        }
    }
}