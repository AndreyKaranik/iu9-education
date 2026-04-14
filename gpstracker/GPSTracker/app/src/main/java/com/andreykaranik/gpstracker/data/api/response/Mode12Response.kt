package com.andreykaranik.gpstracker.data.api.response

import com.andreykaranik.gpstracker.domain.model.LocationData
import com.andreykaranik.gpstracker.domain.model.SensorData
import com.google.gson.annotations.SerializedName

data class Mode12Response(
    @SerializedName("location_recorded_at")
    val locationRecordedAt: String?,
    @SerializedName("gyroscope_data_list")
    val gyroscopeDataList: List<SensorData>?,
    @SerializedName("accelerometer_data_list")
    val accelerometerDataList: List<SensorData>?,
    @SerializedName("location_data_list")
    val locationDataList: List<LocationData>?,
    val message: String?,
    val error: String?
)
