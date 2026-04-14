package com.andreykaranik.gpstracker.data.api.response

import com.andreykaranik.gpstracker.domain.model.LocationData

data class GetPeriodLocationsResponse(
    val locations: List<LocationData>?
)