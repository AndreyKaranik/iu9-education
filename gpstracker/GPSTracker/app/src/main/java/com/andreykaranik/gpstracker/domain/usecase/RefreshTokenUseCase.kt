package com.andreykaranik.gpstracker.domain.usecase

import com.andreykaranik.gpstracker.domain.model.UserData
import com.andreykaranik.gpstracker.domain.model.result.RefreshTokenResult
import com.andreykaranik.gpstracker.domain.repository.UserRepository

class RefreshTokenUseCase(
    private val userRepository: UserRepository
) {
    fun execute(userData: UserData): RefreshTokenResult {
        return userRepository.refreshToken(
            userData = userData
        )
    }
}