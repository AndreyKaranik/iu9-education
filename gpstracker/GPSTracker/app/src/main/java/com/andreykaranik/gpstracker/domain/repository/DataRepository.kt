package com.andreykaranik.gpstracker.domain.repository

import com.andreykaranik.gpstracker.data.api.response.GetPeriodClustersResponse
import com.andreykaranik.gpstracker.domain.model.AccelerometerData
import com.andreykaranik.gpstracker.domain.model.GyroscopeData
import com.andreykaranik.gpstracker.domain.model.LocationData
import com.andreykaranik.gpstracker.domain.model.result.GetPeriodClustersResult
import com.andreykaranik.gpstracker.domain.model.result.GetPeriodLocationsResult
import com.andreykaranik.gpstracker.domain.model.result.Mode11Result
import com.andreykaranik.gpstracker.domain.model.result.Mode12Result
import com.andreykaranik.gpstracker.domain.model.result.SendDataResult
import com.google.gson.annotations.SerializedName

interface DataRepository {
    fun sendData(
        accessToken: String,
        locationDataList: List<LocationData>,
        gyroscopeDataList: List<GyroscopeData>,
        accelerometerDataList: List<AccelerometerData>
    ): SendDataResult
    fun mode11(
        accessToken: String,
        userId: Int,
        kalmanEnabled: Boolean
    ): Mode11Result
    fun mode12(
        accessToken: String,
        userId: Int,
        kalmanEnabled: Boolean
    ): Mode12Result
    fun getPeriodLocations(
        accessToken: String,
        userId: Int,
        from: String,
        to: String,
        minIntervalMinutes: Int
    ): GetPeriodLocationsResult
    fun getPeriodClusters(
        accessToken: String,
        userId: Int,
        from: String,
        to: String,
        kalmanEnabled: Boolean,
        eps: Double,
        minPts: Int
    ): GetPeriodClustersResult
}