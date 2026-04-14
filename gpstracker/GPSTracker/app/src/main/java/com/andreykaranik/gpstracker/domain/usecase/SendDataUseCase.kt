package com.andreykaranik.gpstracker.domain.usecase

import com.andreykaranik.gpstracker.domain.model.AccelerometerData
import com.andreykaranik.gpstracker.domain.model.GyroscopeData
import com.andreykaranik.gpstracker.domain.model.LocationData
import com.andreykaranik.gpstracker.domain.model.result.GetUserDataResult
import com.andreykaranik.gpstracker.domain.model.result.RefreshTokenResult
import com.andreykaranik.gpstracker.domain.model.result.SaveUserDataResult
import com.andreykaranik.gpstracker.domain.model.result.SendDataResult
import com.andreykaranik.gpstracker.domain.repository.DataRepository
import com.andreykaranik.gpstracker.domain.repository.UserRepository

class SendDataUseCase(
    private val dataRepository: DataRepository,
    private val userRepository: UserRepository
) {
    fun execute(
        locationDataList: List<LocationData>,
        gyroscopeDataList: List<GyroscopeData>,
        accelerometerDataList: List<AccelerometerData>
    ): SendDataResult {
        val getUserDataResult = userRepository.getUserData()
        if (getUserDataResult is GetUserDataResult.Success) {
            var userData = getUserDataResult.userData
            if (userData.accessToken.isNotBlank()) {
                val result = dataRepository.sendData(
                    accessToken = userData.accessToken,
                    locationDataList = locationDataList,
                    gyroscopeDataList = gyroscopeDataList,
                    accelerometerDataList = accelerometerDataList
                )
                if (result is SendDataResult.Unauthorized) {
                    val refreshTokenResult = userRepository.refreshToken(userData = userData)
                    if (refreshTokenResult is RefreshTokenResult.Success) {
                        userData = userData.copy(accessToken = refreshTokenResult.accessToken)
                        val saveUserDataResult = userRepository.saveUserData(userData = userData)
                        if (saveUserDataResult is SaveUserDataResult.Success) {
                            return dataRepository.sendData(
                                accessToken = userData.accessToken,
                                locationDataList = locationDataList,
                                gyroscopeDataList = gyroscopeDataList,
                                accelerometerDataList = accelerometerDataList
                            )
                        } else {
                            return SendDataResult.Unauthorized
                        }
                    } else {
                        return SendDataResult.Unauthorized
                    }
                } else {
                    return result
                }
            } else {
                return SendDataResult.Unauthorized
            }
        } else {
            return SendDataResult.Unauthorized
        }
    }
}