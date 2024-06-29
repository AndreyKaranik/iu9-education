package com.example.chargingstations.viewmodel
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chargingstations.ApiService
import com.example.chargingstations.model.ChargingStation
import com.yandex.mapkit.geometry.Point
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory

class MainActivityViewModel : ViewModel() {

    private val _chargingStations = MutableStateFlow<List<ChargingStation>>(emptyList())
    val chargingStations: StateFlow<List<ChargingStation>> = _chargingStations

    private val _chargingStationsFetching = MutableStateFlow(false)
    val chargingStationsFetching: StateFlow<Boolean> = _chargingStationsFetching

    private val _chargingStationsFetched = MutableStateFlow(false)
    val chargingStationsFetched: StateFlow<Boolean> = _chargingStationsFetched

    private val _internetConnectionDialogIsShown = MutableStateFlow(false)
    val internetConnectionDialogIsShown: StateFlow<Boolean> = _internetConnectionDialogIsShown

    private val _chargingStationDetailsSheetIsShown = MutableStateFlow(false)
    val chargingStationDetailsSheetIsShown: StateFlow<Boolean> = _chargingStationDetailsSheetIsShown

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _gpsProgressIndicatorIsShown = MutableStateFlow(false)
    val gpsProgressIndicatorIsShown: StateFlow<Boolean> = _gpsProgressIndicatorIsShown

    private val _gpsDialogIsShown = MutableStateFlow(false)
    val gpsDialogIsShown: StateFlow<Boolean> = _gpsDialogIsShown

    private val _badQRCodeDialogIsShown = MutableStateFlow(false)
    val badQRCodeDialogIsShown: StateFlow<Boolean> = _badQRCodeDialogIsShown

    private val _chargingStationNotFoundDialogIsShown = MutableStateFlow(false)
    val chargingStationNotFoundDialogIsShown: StateFlow<Boolean> = _chargingStationNotFoundDialogIsShown

    private val _selectedChargingStation = MutableStateFlow<ChargingStation?>(null)
    val selectedChargingStation: StateFlow<ChargingStation?> = _selectedChargingStation

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://89.111.172.144:8000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    init {
        fetchChargingStations()
    }

    fun showGPSDialog() {
        _gpsDialogIsShown.value = true
    }

    fun hideGPSDialog() {
        _gpsDialogIsShown.value = false
    }

    fun showInternetConnectionDialog() {
        _internetConnectionDialogIsShown.value = true
    }

    fun hideInternetConnectionDialog() {
        _internetConnectionDialogIsShown.value = false
    }

    fun showBadQRCodeDialogIsShown() {
        _badQRCodeDialogIsShown.value = true
    }

    fun hideBadQRCodeDialogIsShown() {
        _badQRCodeDialogIsShown.value = false
    }

    fun showChargingStationNotFoundDialogIsShown() {
        _chargingStationNotFoundDialogIsShown.value = true
    }

    fun hideChargingStationNotFoundDialogIsShown() {
        _chargingStationNotFoundDialogIsShown.value = false
    }


    fun showGPSProgressIndicator() {
        _gpsProgressIndicatorIsShown.value = true
    }

    fun hideGPSProgressIndicator() {
        _gpsProgressIndicatorIsShown.value = false
    }

    fun showChargingStationDetailsSheet(chargingStationId: Int) {
        _chargingStationDetailsSheetIsShown.value = true
        fetchChargingStation(chargingStationId)
    }

    fun fetchChargingStation(chargingStationId: Int) {
        viewModelScope.launch {
            delay(3000)
            _selectedChargingStation.value = getChargingStationById(chargingStationId)
        }
    }

    fun hideChargingStationDetailsSheet() {
        _chargingStationDetailsSheetIsShown.value = false
        _selectedChargingStation.value = null
    }

    fun getChargingStationById(chargingStationId: Int): ChargingStation? {
        return chargingStations.value.find {
            it.id == chargingStationId
        }
    }

    fun fetchChargingStations() {
        _chargingStationsFetching.value = true
        viewModelScope.launch {
            try {
                val response = apiService.getChargingStations().awaitResponse()
                if (response.isSuccessful) {
                    response.body()?.let {
                        _chargingStations.value = it
                        _chargingStationsFetched.value = true
                    }
                } else {
                    Log.e("E", "error")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("E", "exception")
            } finally {
                _chargingStationsFetching.value = false
            }
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