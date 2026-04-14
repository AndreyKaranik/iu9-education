package com.andreykaranik.gpstracker.domain.model.result

sealed class LeaveGroupResult {
    object Success : LeaveGroupResult()
    object Unauthorized : LeaveGroupResult()
    object IsNotInGroup : LeaveGroupResult()
    object Failure : LeaveGroupResult()
    object Pending : LeaveGroupResult()
    object None : LeaveGroupResult()
}
