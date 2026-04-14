package com.andreykaranik.gpstracker.domain.model.result

sealed class RefreshTokenResult {
    data class Success(
        val accessToken: String
    ) : RefreshTokenResult()
    object InvalidRefreshToken : RefreshTokenResult()
    object Failure : RefreshTokenResult()
    object Pending : RefreshTokenResult()
    object None : RefreshTokenResult()
}
