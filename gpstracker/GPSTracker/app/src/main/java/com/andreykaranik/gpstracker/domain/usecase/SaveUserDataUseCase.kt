package com.andreykaranik.gpstracker.domain.usecase

import com.andreykaranik.gpstracker.domain.model.UserData
import com.andreykaranik.gpstracker.domain.model.result.SaveUserDataResult
import com.andreykaranik.gpstracker.domain.repository.UserRepository

class SaveUserDataUseCase(
    private val userRepository: UserRepository
) {
    fun execute(userData: UserData): SaveUserDataResult {
        return userRepository.saveUserData(
            userData = userData
        )
    }
}