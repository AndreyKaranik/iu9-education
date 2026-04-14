package com.andreykaranik.gpstracker.domain.model.result

import com.andreykaranik.gpstracker.domain.model.LocationData

sealed class GetPeriodLocationsResult {
    data class Success(
        val locations: List<LocationData>
    ) : GetPeriodLocationsResult()
    object Unauthorized : GetPeriodLocationsResult()
    object AreNotInSameGroup : GetPeriodLocationsResult()
    object Failure : GetPeriodLocationsResult()
    object Pending : GetPeriodLocationsResult()
    object None : GetPeriodLocationsResult()
}

