package com.andreykaranik.gpstracker.domain.usecase

import com.andreykaranik.gpstracker.domain.model.result.GetPeriodClustersResult
import com.andreykaranik.gpstracker.domain.model.result.GetUserDataResult
import com.andreykaranik.gpstracker.domain.model.result.RefreshTokenResult
import com.andreykaranik.gpstracker.domain.model.result.SaveUserDataResult
import com.andreykaranik.gpstracker.domain.repository.DataRepository
import com.andreykaranik.gpstracker.domain.repository.UserRepository

class GetPeriodClustersUseCase(
    private val dataRepository: DataRepository,
    private val userRepository: UserRepository
) {
    fun execute(
        userId: Int,
        from: String,
        to: String,
        kalmanEnabled: Boolean,
        eps: Double,
        minPts: Int
    ): GetPeriodClustersResult {
        val getUserDataResult = userRepository.getUserData()
        if (getUserDataResult is GetUserDataResult.Success) {
            var userData = getUserDataResult.userData
            if (userData.accessToken.isNotBlank()) {
                val result = dataRepository.getPeriodClusters(
                    accessToken = userData.accessToken,
                    userId = userId,
                    from = from,
                    to = to,
                    kalmanEnabled = kalmanEnabled,
                    eps = eps,
                    minPts = minPts
                )
                if (result is GetPeriodClustersResult.Unauthorized) {
                    val refreshTokenResult = userRepository.refreshToken(userData = userData)
                    if (refreshTokenResult is RefreshTokenResult.Success) {
                        userData = userData.copy(accessToken = refreshTokenResult.accessToken)
                        val saveUserDataResult = userRepository.saveUserData(userData = userData)
                        if (saveUserDataResult is SaveUserDataResult.Success) {
                            return dataRepository.getPeriodClusters(
                                accessToken = userData.accessToken,
                                userId = userId,
                                from = from,
                                to = to,
                                kalmanEnabled = kalmanEnabled,
                                eps = eps,
                                minPts = minPts
                            )
                        } else {
                            return GetPeriodClustersResult.Unauthorized
                        }
                    } else {
                        return GetPeriodClustersResult.Unauthorized
                    }
                } else {
                    return result
                }
            } else {
                return GetPeriodClustersResult.Unauthorized
            }
        } else {
            return GetPeriodClustersResult.Unauthorized
        }
    }
}