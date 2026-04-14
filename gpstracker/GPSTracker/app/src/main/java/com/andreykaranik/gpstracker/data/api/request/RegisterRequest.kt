package com.andreykaranik.gpstracker.data.api.request

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)
