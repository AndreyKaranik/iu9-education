package com.andreykaranik.gpstracker.domain.model.result

import com.andreykaranik.gpstracker.domain.model.GroupData

sealed class GetGroupDataResult {
    data class Success(
        val groupData: GroupData
    ) : GetGroupDataResult()
    object Unauthorized : GetGroupDataResult()
    object IsNotInGroup : GetGroupDataResult()
    object Failure : GetGroupDataResult()
    object Pending : GetGroupDataResult()
    object None : GetGroupDataResult()
}