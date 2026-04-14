package com.andreykaranik.gpstracker.domain.usecase

import com.andreykaranik.gpstracker.domain.model.result.CreateGroupResult
import com.andreykaranik.gpstracker.domain.model.result.GetGroupDataResult
import com.andreykaranik.gpstracker.domain.model.result.GetUserDataResult
import com.andreykaranik.gpstracker.domain.model.result.RefreshTokenResult
import com.andreykaranik.gpstracker.domain.model.result.SaveUserDataResult
import com.andreykaranik.gpstracker.domain.repository.GroupRepository
import com.andreykaranik.gpstracker.domain.repository.UserRepository

class CreateGroupUseCase(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository
) {
    fun execute(
        name: String,
        type: Int,
        joinCode: String
    ): CreateGroupResult {
        val getUserDataResult = userRepository.getUserData()
        if (getUserDataResult is GetUserDataResult.Success) {
            var userData = getUserDataResult.userData
            if (userData.accessToken.isNotBlank()) {
                val result = groupRepository.createGroup(
                    accessToken = userData.accessToken,
                    name = name,
                    type = type,
                    joinCode = joinCode
                )
                if (result is CreateGroupResult.Unauthorized) {
                    val refreshTokenResult = userRepository.refreshToken(userData = userData)
                    if (refreshTokenResult is RefreshTokenResult.Success) {
                        userData = userData.copy(accessToken = refreshTokenResult.accessToken)
                        val saveUserDataResult = userRepository.saveUserData(userData = userData)
                        if (saveUserDataResult is SaveUserDataResult.Success) {
                            return groupRepository.createGroup(
                                accessToken = userData.accessToken,
                                name = name,
                                type = type,
                                joinCode = joinCode
                            )
                        } else {
                            return CreateGroupResult.Unauthorized
                        }
                    } else {
                        return CreateGroupResult.Unauthorized
                    }
                } else {
                    return result
                }
            } else {
                return CreateGroupResult.Unauthorized
            }
        } else {
            return CreateGroupResult.Unauthorized
        }
    }
}