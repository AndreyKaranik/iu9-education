package com.andreykaranik.gpstracker.domain.model.result

sealed class JoinGroupResult {
    object Success : JoinGroupResult()
    object Unauthorized : JoinGroupResult()
    object IsAlreadyInGroup : JoinGroupResult()
    object InvalidGroupIdOrJoinCode : JoinGroupResult()
    object Failure : JoinGroupResult()
    object Pending : JoinGroupResult()
    object None : JoinGroupResult()
}