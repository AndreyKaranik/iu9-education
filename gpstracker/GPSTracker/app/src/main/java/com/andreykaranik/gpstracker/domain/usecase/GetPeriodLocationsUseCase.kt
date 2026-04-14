package com.andreykaranik.gpstracker.domain.usecase

import com.andreykaranik.gpstracker.domain.model.result.GetPeriodLocationsResult
import com.andreykaranik.gpstracker.domain.model.result.GetUserDataResult
import com.andreykaranik.gpstracker.domain.model.result.Mode11Result
import com.andreykaranik.gpstracker.domain.model.result.RefreshTokenResult
import com.andreykaranik.gpstracker.domain.model.result.SaveUserDataResult
import com.andreykaranik.gpstracker.domain.repository.DataRepository
import com.andreykaranik.gpstracker.domain.repository.UserRepository

class GetPeriodLocationsUseCase(
    private val dataRepository: DataRepository,
    private val userRepository: UserRepository
) {
    fun execute(
        userId: Int,
        from: String,
        to: String,
        minIntervalMinutes: Int
    ): GetPeriodLocationsResult {
        val getUserDataResult = userRepository.getUserData()
        if (getUserDataResult is GetUserDataResult.Success) {
            var userData = getUserDataResult.userData
            if (userData.accessToken.isNotBlank()) {
                val result = dataRepository.getPeriodLocations(
                    accessToken = userData.accessToken,
                    userId = userId,
                    from = from,
                    to = to,
                    minIntervalMinutes = minIntervalMinutes
                )
                if (result is GetPeriodLocationsResult.Unauthorized) {
                    val refreshTokenResult = userRepository.refreshToken(userData = userData)
                    if (refreshTokenResult is RefreshTokenResult.Success) {
                        userData = userData.copy(accessToken = refreshTokenResult.accessToken)
                        val saveUserDataResult = userRepository.saveUserData(userData = userData)
                        if (saveUserDataResult is SaveUserDataResult.Success) {
                            return dataRepository.getPeriodLocations(
                                accessToken = userData.accessToken,
                                userId = userId,
                                from = from,
                                to = to,
                                minIntervalMinutes = minIntervalMinutes
                            )
                        } else {
                            return GetPeriodLocationsResult.Unauthorized
                        }
                    } else {
                        return GetPeriodLocationsResult.Unauthorized
                    }
                } else {
                    return result
                }
            } else {
                return GetPeriodLocationsResult.Unauthorized
            }
        } else {
            return GetPeriodLocationsResult.Unauthorized
        }
    }
}