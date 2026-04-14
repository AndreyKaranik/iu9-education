package com.andreykaranik.gpstracker.domain.model.result

sealed class SaveUserDataResult {
    object Success : SaveUserDataResult()
    object Failure : SaveUserDataResult()
    object Pending : SaveUserDataResult()
    object None : SaveUserDataResult()
}