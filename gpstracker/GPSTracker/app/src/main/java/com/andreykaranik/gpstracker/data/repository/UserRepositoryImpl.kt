package com.andreykaranik.gpstracker.data.repository

import android.content.Context
import com.andreykaranik.gpstracker.data.api.ApiService
import com.andreykaranik.gpstracker.data.api.request.LoginRequest
import com.andreykaranik.gpstracker.data.api.request.RefreshTokenRequest
import com.andreykaranik.gpstracker.data.api.request.RegisterRequest
import com.andreykaranik.gpstracker.domain.model.UserData
import com.andreykaranik.gpstracker.domain.model.result.GetUserDataResult
import com.andreykaranik.gpstracker.domain.model.result.LoginResult
import com.andreykaranik.gpstracker.domain.model.result.RefreshTokenResult
import com.andreykaranik.gpstracker.domain.model.result.RegisterResult
import com.andreykaranik.gpstracker.domain.model.result.SaveUserDataResult
import com.andreykaranik.gpstracker.domain.repository.UserRepository

class UserRepositoryImpl(
    private val context: Context,
    private val apiService: ApiService
) : UserRepository {
    override fun register(name: String, email: String, password: String): RegisterResult {
        try {
            val response =
                apiService.register(
                    RegisterRequest(
                        name = name,
                        email = email,
                        password = password
                    )
                ).execute()

            when (response.code()) {
                200 -> {
                    return RegisterResult.Success
                }
                201 -> {
                    return RegisterResult.Success
                }
                409 -> {
                    return RegisterResult.AlreadyExists
                }
                else -> {
                    return RegisterResult.Failure
                }
            }
        } catch (e: Exception) {
            return RegisterResult.Failure
        }
    }

    override fun login(email: String, password: String): LoginResult {
        try {
            val response =
                apiService.login(
                    LoginRequest(
                        email = email,
                        password = password
                    )
                ).execute()

            when (response.code()) {
                200 -> {
                    val body = response.body()!!
                    return LoginResult.Success(
                        name = body.name!!,
                        accessToken = body.accessToken!!,
                        refreshToken = body.refreshToken!!
                    )
                }
                401 -> {
                    return LoginResult.InvalidEmailOrPassword
                }
                403 -> {
                    return LoginResult.IsNotConfirmed
                }
                else -> {
                    return LoginResult.Failure
                }
            }
        } catch (e: Exception) {
            return LoginResult.Failure
        }
    }

    override fun refreshToken(userData: UserData): RefreshTokenResult {
        try {
            val response =
                apiService.refreshToken(
                    RefreshTokenRequest(
                        accessToken = userData.accessToken,
                        refreshToken = userData.refreshToken
                    )
                ).execute()

            when (response.code()) {
                200 -> {
                    val body = response.body()!!
                    return RefreshTokenResult.Success(
                        accessToken = body.accessToken!!
                    )
                }
                401 -> {
                    return RefreshTokenResult.InvalidRefreshToken
                }
                else -> {
                    return RefreshTokenResult.Failure
                }
            }
        } catch (e: Exception) {
            return RefreshTokenResult.Failure
        }
    }

    override fun saveUserData(userData: UserData): SaveUserDataResult {
        val sharedPref = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("name", userData.name)
            putString("email", userData.email)
            putString("access_token", userData.accessToken)
            putString("refresh_token", userData.refreshToken)
            apply()
        }
        return SaveUserDataResult.Success
    }

    override fun getUserData(): GetUserDataResult {
        val sharedPref = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val name = sharedPref.getString("name", "").toString()
        val email = sharedPref.getString("email", "").toString()
        val accessToken = sharedPref.getString("access_token", "").toString()
        val refreshToken = sharedPref.getString("refresh_token", "").toString()
        val userData = UserData(
            name = name,
            email = email,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
        return GetUserDataResult.Success(userData = userData)
    }
}