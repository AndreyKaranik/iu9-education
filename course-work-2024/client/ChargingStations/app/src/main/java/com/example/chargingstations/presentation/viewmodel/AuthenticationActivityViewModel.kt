package com.example.chargingstations.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chargingstations.data.ApiService
import com.example.chargingstations.domain.model.AuthData
import com.example.chargingstations.domain.model.RegistrationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory

class AuthenticationActivityViewModel : ViewModel() {

    private val TAG: String = "AuthenticationActivityViewModel"

    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val httpClient = OkHttpClient.Builder().apply {
        addInterceptor(logging)
    }.build()

    private var lastChargingStationDetailsId: Int? = null

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://194.67.88.154:8000/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)
        .build()

    private val apiService = retrofit.create(ApiService::class.java)


    private val _usernameDialogIsShown = MutableStateFlow(false)
    val usernameDialogIsShown: StateFlow<Boolean> = _usernameDialogIsShown

    private val _emailDialogIsShown = MutableStateFlow(false)
    val emailDialogIsShown: StateFlow<Boolean> = _emailDialogIsShown

    private val _confirmDialogIsShown = MutableStateFlow(false)
    val confirmDialogIsShown: StateFlow<Boolean> = _confirmDialogIsShown

    private val _errorDialogIsShown = MutableStateFlow(false)
    val errorDialogIsShown: StateFlow<Boolean> = _errorDialogIsShown

    private val _isAuth = MutableStateFlow(false)
    val isAuth: StateFlow<Boolean> = _isAuth

    private val _token = MutableStateFlow("")
    val token: StateFlow<String> = _token

    fun hideConfirmDialog() {
        _confirmDialogIsShown.value = false;
    }

    fun hideErrorDialog() {
        _errorDialogIsShown.value = false;
    }

    fun hideUsernameDialog() {
        _usernameDialogIsShown.value = false;
    }
    fun hideEmailDialog() {
        _emailDialogIsShown.value = false;
    }


    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = apiService.register(RegistrationData(name, email, password)).awaitResponse()
                if (response.isSuccessful) {
                    response.body()?.let {
                        when (it.status) {
                            0 -> {
                                _confirmDialogIsShown.value = true
                            }
                            1 -> {
                                _errorDialogIsShown.value = true
                            }
                            2 -> {
                                _emailDialogIsShown.value = true
                            }
                        }
                    }
                } else {
                    _errorDialogIsShown.value = true
                    Log.e(TAG, "error")
                }
            } catch (e: Exception) {
                _errorDialogIsShown.value = true
                e.printStackTrace()
                Log.e(TAG, "exception")
            } finally {

            }
        }
    }

    fun auth(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = apiService.auth(AuthData(email, password)).awaitResponse()
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.token != "") {
                            _isAuth.value = true
                            _token.value = it.token
                        } else {
                            _errorDialogIsShown.value = true
                        }
                    }
                } else {
                    _errorDialogIsShown.value = true
                    Log.e(TAG, "error")
                }
            } catch (e: Exception) {
                _errorDialogIsShown.value = true
                e.printStackTrace()
                Log.e(TAG, "exception")
            } finally {

            }
        }
    }
}