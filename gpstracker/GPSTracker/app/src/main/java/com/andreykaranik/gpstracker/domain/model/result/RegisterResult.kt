package com.andreykaranik.gpstracker.domain.model.result

sealed class RegisterResult {
    object Success : RegisterResult()
    object AlreadyExists : RegisterResult()
    object Failure : RegisterResult()
    object Pending : RegisterResult()
    object None : RegisterResult()
}