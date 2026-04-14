package com.andreykaranik.gpstracker.domain.usecase

import android.service.autofill.UserData
import com.andreykaranik.gpstracker.domain.model.result.GetGroupDataResult
import com.andreykaranik.gpstracker.domain.model.result.GetUserDataResult
import com.andreykaranik.gpstracker.domain.model.result.RefreshTokenResult
import com.andreykaranik.gpstracker.domain.model.result.SaveUserDataResult
import com.andreykaranik.gpstracker.domain.repository.GroupRepository
import com.andreykaranik.gpstracker.domain.repository.UserRepository

class GetGroupDataUseCase(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository
) {
    fun execute(): GetGroupDataResult {
        val getUserDataResult = userRepository.getUserData()
        if (getUserDataResult is GetUserDataResult.Success) {
            var userData = getUserDataResult.userData
            if (userData.accessToken.isNotBlank()) {
                val getGroupDataResult = groupRepository.getGroupData(accessToken = userData.accessToken)
                if (getGroupDataResult is GetGroupDataResult.Unauthorized) {
                    val refreshTokenResult = userRepository.refreshToken(userData = userData)
                    if (refreshTokenResult is RefreshTokenResult.Success) {
                        userData = userData.copy(accessToken = refreshTokenResult.accessToken)
                        val saveUserDataResult = userRepository.saveUserData(userData = userData)
                        if (saveUserDataResult is SaveUserDataResult.Success) {
                            return groupRepository.getGroupData(accessToken = userData.accessToken)
                        } else {
                            return GetGroupDataResult.Unauthorized
                        }
                    } else {
                        return GetGroupDataResult.Unauthorized
                    }
                } else {
                    return getGroupDataResult
                }
            } else {
                return GetGroupDataResult.Unauthorized
            }
        } else {
            return GetGroupDataResult.Unauthorized
        }
    }
}