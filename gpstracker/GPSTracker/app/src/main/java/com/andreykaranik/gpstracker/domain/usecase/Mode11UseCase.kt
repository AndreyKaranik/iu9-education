package com.andreykaranik.gpstracker.domain.usecase

import com.andreykaranik.gpstracker.domain.model.AccelerometerData
import com.andreykaranik.gpstracker.domain.model.GyroscopeData
import com.andreykaranik.gpstracker.domain.model.LocationData
import com.andreykaranik.gpstracker.domain.model.result.GetUserDataResult
import com.andreykaranik.gpstracker.domain.model.result.Mode11Result
import com.andreykaranik.gpstracker.domain.model.result.RefreshTokenResult
import com.andreykaranik.gpstracker.domain.model.result.SaveUserDataResult
import com.andreykaranik.gpstracker.domain.model.result.SendDataResult
import com.andreykaranik.gpstracker.domain.repository.DataRepository
import com.andreykaranik.gpstracker.domain.repository.UserRepository

class Mode11UseCase(
    private val dataRepository: DataRepository,
    private val userRepository: UserRepository
) {
    fun execute(
        userId: Int,
        kalmanEnabled: Boolean
    ): Mode11Result {
        val getUserDataResult = userRepository.getUserData()
        if (getUserDataResult is GetUserDataResult.Success) {
            var userData = getUserDataResult.userData
            if (userData.accessToken.isNotBlank()) {
                val result = dataRepository.mode11(
                    accessToken = userData.accessToken,
                    userId = userId,
                    kalmanEnabled = kalmanEnabled
                )
                if (result is Mode11Result.Unauthorized) {
                    val refreshTokenResult = userRepository.refreshToken(userData = userData)
                    if (refreshTokenResult is RefreshTokenResult.Success) {
                        userData = userData.copy(accessToken = refreshTokenResult.accessToken)
                        val saveUserDataResult = userRepository.saveUserData(userData = userData)
                        if (saveUserDataResult is SaveUserDataResult.Success) {
                            return dataRepository.mode11(
                                accessToken = userData.accessToken,
                                userId = userId,
                                kalmanEnabled = kalmanEnabled
                            )
                        } else {
                            return Mode11Result.Unauthorized
                        }
                    } else {
                        return Mode11Result.Unauthorized
                    }
                } else {
                    return result
                }
            } else {
                return Mode11Result.Unauthorized
            }
        } else {
            return Mode11Result.Unauthorized
        }
    }
}