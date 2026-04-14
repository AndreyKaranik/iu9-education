package com.andreykaranik.gpstracker.domain.usecase

import com.andreykaranik.gpstracker.domain.model.result.GetUserDataResult
import com.andreykaranik.gpstracker.domain.model.result.JoinGroupResult
import com.andreykaranik.gpstracker.domain.model.result.RefreshTokenResult
import com.andreykaranik.gpstracker.domain.model.result.SaveUserDataResult
import com.andreykaranik.gpstracker.domain.repository.GroupRepository
import com.andreykaranik.gpstracker.domain.repository.UserRepository

class JoinGroupUseCase(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository
) {
    fun execute(
        id: Int,
        joinCode: String
    ): JoinGroupResult {
        val getUserDataResult = userRepository.getUserData()
        if (getUserDataResult is GetUserDataResult.Success) {
            var userData = getUserDataResult.userData
            if (userData.accessToken.isNotBlank()) {
                val result = groupRepository.joinGroup(
                    accessToken = userData.accessToken,
                    id = id,
                    joinCode = joinCode
                )
                if (result is JoinGroupResult.Unauthorized) {
                    val refreshTokenResult = userRepository.refreshToken(userData = userData)
                    if (refreshTokenResult is RefreshTokenResult.Success) {
                        userData = userData.copy(accessToken = refreshTokenResult.accessToken)
                        val saveUserDataResult = userRepository.saveUserData(userData = userData)
                        if (saveUserDataResult is SaveUserDataResult.Success) {
                            return groupRepository.joinGroup(
                                accessToken = userData.accessToken,
                                id = id,
                                joinCode = joinCode
                            )
                        } else {
                            return JoinGroupResult.Unauthorized
                        }
                    } else {
                        return JoinGroupResult.Unauthorized
                    }
                } else {
                    return result
                }
            } else {
                return JoinGroupResult.Unauthorized
            }
        } else {
            return JoinGroupResult.Unauthorized
        }
    }
}