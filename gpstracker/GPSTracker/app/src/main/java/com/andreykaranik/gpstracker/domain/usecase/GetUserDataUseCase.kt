package com.andreykaranik.gpstracker.domain.usecase

import com.andreykaranik.gpstracker.domain.model.result.GetUserDataResult
import com.andreykaranik.gpstracker.domain.repository.UserRepository

class GetUserDataUseCase(
    private val userRepository: UserRepository
) {
    fun execute(): GetUserDataResult {
        return userRepository.getUserData()
    }
}