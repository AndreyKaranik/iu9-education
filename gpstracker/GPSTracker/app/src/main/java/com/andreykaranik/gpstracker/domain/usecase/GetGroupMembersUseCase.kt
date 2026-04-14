package com.andreykaranik.gpstracker.domain.usecase

import com.andreykaranik.gpstracker.domain.model.result.GetGroupMembersResult
import com.andreykaranik.gpstracker.domain.model.result.GetUserDataResult
import com.andreykaranik.gpstracker.domain.model.result.LeaveGroupResult
import com.andreykaranik.gpstracker.domain.model.result.RefreshTokenResult
import com.andreykaranik.gpstracker.domain.model.result.SaveUserDataResult
import com.andreykaranik.gpstracker.domain.repository.GroupRepository
import com.andreykaranik.gpstracker.domain.repository.UserRepository

class GetGroupMembersUseCase(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository
) {
    fun execute(): GetGroupMembersResult {
        val getUserDataResult = userRepository.getUserData()
        if (getUserDataResult is GetUserDataResult.Success) {
            var userData = getUserDataResult.userData
            if (userData.accessToken.isNotBlank()) {
                val result = groupRepository.getGroupMembers(accessToken = userData.accessToken)
                if (result is GetGroupMembersResult.Unauthorized) {
                    val refreshTokenResult = userRepository.refreshToken(userData = userData)
                    if (refreshTokenResult is RefreshTokenResult.Success) {
                        userData = userData.copy(accessToken = refreshTokenResult.accessToken)
                        val saveUserDataResult = userRepository.saveUserData(userData = userData)
                        if (saveUserDataResult is SaveUserDataResult.Success) {
                            return groupRepository.getGroupMembers(accessToken = userData.accessToken)
                        } else {
                            return GetGroupMembersResult.Unauthorized
                        }
                    } else {
                        return GetGroupMembersResult.Unauthorized
                    }
                } else {
                    return result
                }
            } else {
                return GetGroupMembersResult.Unauthorized
            }
        } else {
            return GetGroupMembersResult.Unauthorized
        }
    }
}