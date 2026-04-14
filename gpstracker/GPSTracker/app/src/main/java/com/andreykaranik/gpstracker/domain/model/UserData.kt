package com.andreykaranik.gpstracker.domain.model

data class UserData(
    val name: String,
    val email: String,
    val accessToken: String,
    val refreshToken: String
)