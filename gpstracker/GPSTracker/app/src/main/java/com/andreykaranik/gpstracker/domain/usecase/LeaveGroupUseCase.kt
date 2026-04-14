package com.andreykaranik.gpstracker.domain.usecase

import com.andreykaranik.gpstracker.domain.model.result.GetGroupDataResult
import com.andreykaranik.gpstracker.domain.model.result.GetUserDataResult
import com.andreykaranik.gpstracker.domain.model.result.LeaveGroupResult
import com.andreykaranik.gpstracker.domain.model.result.RefreshTokenResult
import com.andreykaranik.gpstracker.domain.model.result.SaveUserDataResult
import com.andreykaranik.gpstracker.domain.repository.GroupRepository
import com.andreykaranik.gpstracker.domain.repository.UserRepository

class LeaveGroupUseCase(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository
) {
    fun execute(): LeaveGroupResult {
        val getUserDataResult = userRepository.getUserData()
        if (getUserDataResult is GetUserDataResult.Success) {
            var userData = getUserDataResult.userData
            if (userData.accessToken.isNotBlank()) {
                val result = groupRepository.leaveGroup(accessToken = userData.accessToken)
                if (result is LeaveGroupResult.Unauthorized) {
                    val refreshTokenResult = userRepository.refreshToken(userData = userData)
                    if (refreshTokenResult is RefreshTokenResult.Success) {
                        userData = userData.copy(accessToken = refreshTokenResult.accessToken)
                        val saveUserDataResult = userRepository.saveUserData(userData = userData)
                        if (saveUserDataResult is SaveUserDataResult.Success) {
                            return groupRepository.leaveGroup(accessToken = userData.accessToken)
                        } else {
                            return LeaveGroupResult.Unauthorized
                        }
                    } else {
                        return LeaveGroupResult.Unauthorized
                    }
                } else {
                    return result
                }
            } else {
                return LeaveGroupResult.Unauthorized
            }
        } else {
            return LeaveGroupResult.Unauthorized
        }
    }
}