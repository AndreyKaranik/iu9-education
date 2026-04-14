package com.andreykaranik.gpstracker.domain.model

data class Mode12Data(
    val locationRecordedAt: String,
    val gyroscopeDataList: List<SensorData>,
    val accelerometerDataList: List<SensorData>,
    val locationDataList: List<LocationData>,
)
