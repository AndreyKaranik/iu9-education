package com.andreykaranik.gpstracker.domain.model.result

sealed class SendDataResult {
    object Success : SendDataResult()
    object Unauthorized : SendDataResult()
    object Failure : SendDataResult()
    object Pending : SendDataResult()
    object None : SendDataResult()
}
