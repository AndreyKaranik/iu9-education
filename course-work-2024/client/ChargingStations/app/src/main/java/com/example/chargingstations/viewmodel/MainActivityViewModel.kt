package com.example.chargingstations.viewmodel
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chargingstations.ApiService
import com.example.chargingstations.model.ChargingStation
import com.yandex.mapkit.geometry.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory

class MainActivityViewModel : ViewModel() {

    private val _chargingStations = MutableStateFlow<List<ChargingStation>>(emptyList())
    val chargingStations: StateFlow<List<ChargingStation>> = _chargingStations

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _searchQuery = MutableStateFlow(" ")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _gpsProgressIndicatorIsShown = MutableStateFlow(false)
    val gpsProgressIndicatorIsShown: StateFlow<Boolean> = _gpsProgressIndicatorIsShown

    private val _gpsDialogIsShown = MutableStateFlow<Boolean>(false)
    val gpsDialogIsShown: StateFlow<Boolean> = _gpsDialogIsShown

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://89.111.172.144:8000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    fun showGPSDialog() {
        _gpsDialogIsShown.value = true
    }

    fun hideGPSDialog() {
        _gpsDialogIsShown.value = false
    }

    fun showGPSProgressIndicator() {
        _gpsProgressIndicatorIsShown.value = true
    }

    fun hideGPSProgressIndicator() {
        _gpsProgressIndicatorIsShown.value = false
    }

    init {
        _loading.value = true
        fetchChargingStations()
    }

    private fun fetchChargingStations() {
        viewModelScope.launch {
            try {
                val response = apiService.getChargingStations().awaitResponse()
                if (response.isSuccessful) {
                    response.body()?.let {
                        _chargingStations.value = it
                        updateSearchQuery("")
                    }
                } else {
                    Log.e("E", "error")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("E", "exception")
            }
            _loading.value = false
        }
    }

    val filteredChargingStations: StateFlow<List<ChargingStation>> = _searchQuery
        .map { query ->
            if (query.isBlank()) {
                _chargingStations.value
            } else {
                _chargingStations.value.filter { chargingStation ->
                    chargingStation.name.contains(query, ignoreCase = true) || chargingStation.address.contains(query, ignoreCase = true)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, _chargingStations.value)

    fun updateSearchQuery(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun getChargingStation(chargingStationId: Int) : ChargingStation? {
        return chargingStations.value.find { it.id == chargingStationId }
    }
}