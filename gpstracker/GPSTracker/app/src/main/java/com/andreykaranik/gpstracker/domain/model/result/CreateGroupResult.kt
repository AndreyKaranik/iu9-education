package com.andreykaranik.gpstracker.domain.model.result

sealed class CreateGroupResult {
    object Success : CreateGroupResult()
    object Unauthorized : CreateGroupResult()
    object IsAlreadyInGroup : CreateGroupResult()
    object Failure : CreateGroupResult()
    object Pending : CreateGroupResult()
    object None : CreateGroupResult()
}