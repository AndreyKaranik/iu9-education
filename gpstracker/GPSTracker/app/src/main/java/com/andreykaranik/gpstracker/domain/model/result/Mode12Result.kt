package com.andreykaranik.gpstracker.domain.model.result

import com.andreykaranik.gpstracker.domain.model.Mode12Data

sealed class Mode12Result {
    data class Success(
        val mode12Data: Mode12Data
    ) : Mode12Result()
    object Unauthorized : Mode12Result()
    object AreNotInSameGroup : Mode12Result()
    object Failure : Mode12Result()
    object Pending : Mode12Result()
    object None : Mode12Result()
}