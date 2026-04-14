package com.andreykaranik.gpstracker.data.repository

import android.content.Context
import com.andreykaranik.gpstracker.data.api.ApiService
import com.andreykaranik.gpstracker.data.api.request.CreateGroupRequest
import com.andreykaranik.gpstracker.data.api.request.GetPeriodClustersRequest
import com.andreykaranik.gpstracker.data.api.request.GetPeriodLocationsRequest
import com.andreykaranik.gpstracker.data.api.request.Mode11Request
import com.andreykaranik.gpstracker.data.api.request.Mode12Request
import com.andreykaranik.gpstracker.data.api.request.SendDataRequest
import com.andreykaranik.gpstracker.data.api.response.GetPeriodClustersResponse
import com.andreykaranik.gpstracker.domain.model.AccelerometerData
import com.andreykaranik.gpstracker.domain.model.GyroscopeData
import com.andreykaranik.gpstracker.domain.model.LocationData
import com.andreykaranik.gpstracker.domain.model.Mode12Data
import com.andreykaranik.gpstracker.domain.model.result.CreateGroupResult
import com.andreykaranik.gpstracker.domain.model.result.GetPeriodClustersResult
import com.andreykaranik.gpstracker.domain.model.result.GetPeriodLocationsResult
import com.andreykaranik.gpstracker.domain.model.result.Mode11Result
import com.andreykaranik.gpstracker.domain.model.result.Mode12Result
import com.andreykaranik.gpstracker.domain.model.result.SendDataResult
import com.andreykaranik.gpstracker.domain.repository.DataRepository

class DataRepositoryImpl(
    private val context: Context,
    private val apiService: ApiService
) : DataRepository {
    override fun sendData(
        accessToken: String,
        locationDataList: List<LocationData>,
        gyroscopeDataList: List<GyroscopeData>,
        accelerometerDataList: List<AccelerometerData>
    ): SendDataResult {
        try {
            val response =
                apiService.sendData(
                    "Bearer $accessToken",
                    SendDataRequest(
                        locationDataList = locationDataList,
                        gyroscopeDataList = gyroscopeDataList,
                        accelerometerDataList = accelerometerDataList
                    )
                ).execute()

            when (response.code()) {
                200 -> {
                    return SendDataResult.Success
                }

                401 -> {
                    return SendDataResult.Unauthorized
                }

                else -> {
                    return SendDataResult.Failure
                }
            }
        } catch (e: Exception) {
            return SendDataResult.Failure
        }
    }

    override fun mode11(
        accessToken: String,
        userId: Int,
        kalmanEnabled: Boolean
    ): Mode11Result {
        try {
            val response =
                apiService.mode11(
                    "Bearer $accessToken",
                    Mode11Request(
                        targetUserId = userId,
                        kalmanEnabled = kalmanEnabled
                    )
                ).execute()

            when (response.code()) {
                200 -> {
                    val body = response.body()!!
                    return Mode11Result.Success(
                        latitude = body.latitude!!,
                        longitude = body.longitude!!
                    )
                }

                401 -> {
                    return Mode11Result.Unauthorized
                }

                403 -> {
                    return Mode11Result.AreNotInSameGroup
                }

                else -> {
                    return Mode11Result.Failure
                }
            }
        } catch (e: Exception) {
            return Mode11Result.Failure
        }
    }

    override fun mode12(
        accessToken: String,
        userId: Int,
        kalmanEnabled: Boolean
    ): Mode12Result {
        try {
            val response =
                apiService.mode12(
                    "Bearer $accessToken",
                    Mode12Request(
                        targetUserId = userId,
                        kalmanEnabled = kalmanEnabled
                    )
                ).execute()

            when (response.code()) {
                200 -> {
                    val body = response.body()!!
                    return Mode12Result.Success(
                        mode12Data = Mode12Data(
                            locationRecordedAt = body.locationRecordedAt!!,
                            gyroscopeDataList = body.gyroscopeDataList!!,
                            accelerometerDataList = body.accelerometerDataList!!,
                            locationDataList = body.locationDataList!!
                        )
                    )
                }

                401 -> {
                    return Mode12Result.Unauthorized
                }

                403 -> {
                    return Mode12Result.AreNotInSameGroup
                }

                else -> {
                    return Mode12Result.Failure
                }
            }
        } catch (e: Exception) {
            return Mode12Result.Failure
        }
    }

    override fun getPeriodLocations(
        accessToken: String,
        userId: Int,
        from: String,
        to: String,
        minIntervalMinutes: Int
    ): GetPeriodLocationsResult {
        try {
            val response =
                apiService.getPeriodLocations(
                    "Bearer $accessToken",
                    GetPeriodLocationsRequest(
                        targetUserId = userId,
                        from = from,
                        to = to,
                        minIntervalMinutes = minIntervalMinutes
                    )
                ).execute()

            when (response.code()) {
                200 -> {
                    val body = response.body()!!
                    return GetPeriodLocationsResult.Success(
                        locations = body.locations!!
                    )
                }

                401 -> {
                    return GetPeriodLocationsResult.Unauthorized
                }

                403 -> {
                    return GetPeriodLocationsResult.AreNotInSameGroup
                }

                else -> {
                    return GetPeriodLocationsResult.Failure
                }
            }
        } catch (e: Exception) {
            return GetPeriodLocationsResult.Failure
        }
    }

    override fun getPeriodClusters(
        accessToken: String,
        userId: Int,
        from: String,
        to: String,
        kalmanEnabled: Boolean,
        eps: Double,
        minPts: Int
    ): GetPeriodClustersResult {
        try {
            val response =
                apiService.getPeriodClusters(
                    "Bearer $accessToken",
                    GetPeriodClustersRequest(
                        targetUserId = userId,
                        from = from,
                        to = to,
                        kalmanEnabled = kalmanEnabled,
                        eps = eps,
                        minPts = minPts

                    )
                ).execute()

            when (response.code()) {
                200 -> {
                    val body = response.body()!!
                    return GetPeriodClustersResult.Success(
                        centers = body.centers!!
                    )
                }

                401 -> {
                    return GetPeriodClustersResult.Unauthorized
                }

                403 -> {
                    return GetPeriodClustersResult.AreNotInSameGroup
                }

                else -> {
                    return GetPeriodClustersResult.Failure
                }
            }
        } catch (e: Exception) {
            return GetPeriodClustersResult.Failure
        }
    }

}