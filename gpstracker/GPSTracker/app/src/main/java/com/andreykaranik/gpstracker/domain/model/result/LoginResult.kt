package com.andreykaranik.gpstracker.domain.model.result

sealed class LoginResult {
    data class Success(
        val name: String,
        val accessToken: String,
        val refreshToken: String
    ) : LoginResult()
    object InvalidEmailOrPassword : LoginResult()
    object IsNotConfirmed : LoginResult()
    object Failure : LoginResult()
    object Pending : LoginResult()
    object None : LoginResult()

}