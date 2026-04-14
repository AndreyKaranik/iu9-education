package com.andreykaranik.gpstracker.data.api.request

import com.andreykaranik.gpstracker.domain.model.AccelerometerData
import com.andreykaranik.gpstracker.domain.model.GyroscopeData
import com.andreykaranik.gpstracker.domain.model.LocationData
import com.google.gson.annotations.SerializedName

data class SendDataRequest(
    @SerializedName("location_data_list")
    val locationDataList: List<LocationData>,
    @SerializedName("gyroscope_data_list")
    val gyroscopeDataList: List<GyroscopeData>,
    @SerializedName("accelerometer_data_list")
    val accelerometerDataList: List<AccelerometerData>
)
