package com.example.chargingstations.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chargingstations.data.ApiService
import com.example.chargingstations.domain.model.AuthData
import com.example.chargingstations.domain.model.RegistrationData
import com.example.chargingstations.domain.model.request.ChargeRequest
import com.example.chargingstations.domain.model.request.MarkRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory

class OrderActivityViewModel : ViewModel() {

    private val TAG: String = "OrderActivityViewModel"

    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val httpClient = OkHttpClient.Builder().apply {
        addInterceptor(logging)
    }.build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://194.67.88.154:8000/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    private val _connectorId = MutableStateFlow<Int?>(null)
    val connectorId: StateFlow<Int?> = _connectorId

    private val _chargingStationAddress = MutableStateFlow<String?>(null)
    val chargingStationAddress: StateFlow<String?> = _chargingStationAddress

    private val _connectorTypeName = MutableStateFlow<String?>(null)
    val connectorTypeName: StateFlow<String?> = _connectorTypeName

    private val _connectorRate = MutableStateFlow<Float?>(null)
    val connectorRate: StateFlow<Float?> = _connectorRate

    private val _amount = MutableStateFlow<Float>(0.0f)
    val amount: StateFlow<Float> = _amount

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    private val _orderId = MutableStateFlow<Int?>(null)
    val orderId: StateFlow<Int?> = _orderId

    private val _orderStatus = MutableStateFlow<Int?>(null)
    val orderStatus: StateFlow<Int?> = _orderStatus

    private val _orderProgress = MutableStateFlow<Float?>(null)
    val orderProgress: StateFlow<Float?> = _orderProgress

    fun setConnectorId(connectorId: Int?) {
        _connectorId.value = connectorId
    }

    fun setChargingStationAddress(chargingStationAddress: String?) {
        _chargingStationAddress.value = chargingStationAddress
    }

    fun setConnectorTypeName(typeName: String?) {
        _connectorTypeName.value = typeName
    }

    fun setConnectorRate(connectorRate: Float?) {
        _connectorRate.value = connectorRate
    }

    fun setAmount(amount: Float) {
        _amount.value = amount
    }

    fun setToken(token: String?) {
        _token.value = token
    }

    fun charge() {
        viewModelScope.launch {
            try {
                val response = apiService.charge(
                    ChargeRequest(connectorId = connectorId.value!!, amount = amount.value, token = token.value)
                ).awaitResponse()
                if (response.isSuccessful) {
                    response.body()?.let {
                        _orderId.value = it.orderId
                        while (_orderStatus.value != 1) {
                            val getOrderResponse = apiService.getOrder(orderId = it.orderId).awaitResponse()
                            if (getOrderResponse.isSuccessful) {
                                getOrderResponse.body()?.let { r ->
                                    _orderStatus.value = r.status
                                    _orderProgress.value = r.progress
                                }
                            } else {
                                Log.e(TAG, "error")
                            }
                            delay(200)
                        }
                    }
                } else {
                    Log.e(TAG, "error")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "exception")
            }
        }
    }

    fun mark(status: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.mark(
                    MarkRequest(a = 0)
                ).awaitResponse()
                if (response.isSuccessful) {
                    response.body()?.let {

                    }
                } else {
                    Log.e(TAG, "error")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "exception")
            }
        }
    }
}