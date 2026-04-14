package com.andreykaranik.gpstracker.domain.model.result

import com.andreykaranik.gpstracker.domain.model.UserData

sealed class GetUserDataResult {
    data class Success(
        val userData: UserData
    ) : GetUserDataResult()
    object Failure : GetUserDataResult()
    object Pending : GetUserDataResult()
    object None : GetUserDataResult()
}