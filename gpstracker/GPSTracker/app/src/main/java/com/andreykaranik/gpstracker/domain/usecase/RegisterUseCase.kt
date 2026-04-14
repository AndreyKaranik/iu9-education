package com.andreykaranik.gpstracker.domain.usecase

import com.andreykaranik.gpstracker.domain.model.result.RegisterResult
import com.andreykaranik.gpstracker.domain.repository.UserRepository

class RegisterUseCase(
    private val userRepository: UserRepository
) {
    fun execute(name: String, email: String, password: String): RegisterResult {
        return userRepository.register(
            name = name,
            email = email,
            password = password
        )
    }
}