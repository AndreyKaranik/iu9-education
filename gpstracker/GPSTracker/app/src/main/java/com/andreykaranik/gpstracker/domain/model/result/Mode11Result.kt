package com.andreykaranik.gpstracker.domain.model.result

sealed class Mode11Result {
    data class Success(
        val latitude: Double,
        val longitude: Double
    ) : Mode11Result()
    object Unauthorized : Mode11Result()
    object AreNotInSameGroup : Mode11Result()
    object Failure : Mode11Result()
    object Pending : Mode11Result()
    object None : Mode11Result()
}

