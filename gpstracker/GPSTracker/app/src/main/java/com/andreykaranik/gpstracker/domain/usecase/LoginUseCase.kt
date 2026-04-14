package com.andreykaranik.gpstracker.domain.usecase

import com.andreykaranik.gpstracker.domain.model.result.LoginResult
import com.andreykaranik.gpstracker.domain.model.result.RegisterResult
import com.andreykaranik.gpstracker.domain.repository.UserRepository

class LoginUseCase(
    private val userRepository: UserRepository
) {
    fun execute(email: String, password: String): LoginResult {
        return userRepository.login(
            email = email,
            password = password
        )
    }
}