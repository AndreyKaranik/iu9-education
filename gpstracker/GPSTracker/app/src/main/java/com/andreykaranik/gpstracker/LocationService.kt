package com.andreykaranik.gpstracker

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.andreykaranik.gpstracker.domain.model.AccelerometerData
import com.andreykaranik.gpstracker.domain.model.GyroscopeData
import com.andreykaranik.gpstracker.domain.model.LocationData
import com.andreykaranik.gpstracker.domain.model.result.SendDataResult
import com.andreykaranik.gpstracker.domain.usecase.SendDataUseCase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.sqrt

@AndroidEntryPoint
class LocationService : Service(), SensorEventListener {

    @Inject
    lateinit var sendDataUseCase: SendDataUseCase

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var sensorManager: SensorManager

    private var previousStepCount = 0
    private var currentStepCount = 0

    private val locationDataList = mutableListOf<LocationData>()
    private val gyroscopeDataList = mutableListOf<GyroscopeData>()
    private val accelerometerDataList = mutableListOf<AccelerometerData>()

    private val maxLocationSize = 10000
    private val maxSensorSize = 10000

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val kalman = KalmanGpsFilter()

    private val accelerationWindow = mutableListOf<Double>()
    private val peakVector = mutableListOf<Double>()
    private var stepThreshold = 11.5
    private var isRising = false
    private var lastStepTime: Long = 0
    private val stepCooldown = 400

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, createNotification())

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val lat = location.latitude
                    val lon = location.longitude
                    val steps = if (previousStepCount == -1) 0 else currentStepCount - previousStepCount
                    if (currentStepCount != -1) {
                        previousStepCount = currentStepCount
                    }

                    val nowMillis = System.currentTimeMillis()
                    val (filteredLat, filteredLon) = kalman.update(lat, lon, nowMillis)

                    val now = Instant.now()
                        .atZone(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
                    val data = LocationData(
                        latitude = lat,
                        longitude = lon,
                        kalmanLatitude = filteredLat,
                        kalmanLongitude = filteredLon,
                        steps = steps,
                        recordedAt = now
                    )

                    synchronized(locationDataList) {
                        locationDataList.add(data)
                        if (locationDataList.size > maxLocationSize) {
                            locationDataList.subList(0, maxLocationSize / 2).clear()
                        }
                    }
                }
            }
        }

        startLocationUpdates()
        startSendingDataPeriodically()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        val now = Instant.now()
            .atZone(ZoneOffset.UTC)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
        when (event.sensor.type) {
            Sensor.TYPE_GYROSCOPE -> {
                val data = GyroscopeData(event.values[0].toDouble(), event.values[1].toDouble(), event.values[2].toDouble(), now)
                synchronized(gyroscopeDataList) {
                    gyroscopeDataList.add(data)
                    if (gyroscopeDataList.size > maxSensorSize) {
                        gyroscopeDataList.subList(0, maxSensorSize / 2).clear()
                    }
                }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val data = AccelerometerData(event.values[0].toDouble(), event.values[1].toDouble(), event.values[2].toDouble(), now)
                synchronized(accelerometerDataList) {
                    accelerometerDataList.add(data)
                    if (accelerometerDataList.size > maxSensorSize) {
                        accelerometerDataList.subList(0, maxSensorSize / 2).clear()
                    }
                }

                val noww = System.currentTimeMillis()
                val ax = event.values[0].toDouble()
                val ay = event.values[1].toDouble()
                val az = event.values[2].toDouble()

                val magnitude = sqrt(ax * ax + ay * ay + az * az)

                accelerationWindow.add(magnitude)
                if (accelerationWindow.size > 3) {
                    accelerationWindow.removeAt(0)
                }

                if (accelerationWindow.size == 3) {
                    val (a, b, c) = accelerationWindow

                    if (a < b && b < c) {
                        isRising = true
                    }

                    if (isRising && magnitude > stepThreshold) {
                        peakVector.add(magnitude)
                    }

                    if (isRising && peakVector.isNotEmpty() && a > b && b > c) {
                        val realPeak = peakVector.maxOrNull() ?: 0.0
                        if (realPeak > stepThreshold && noww - lastStepTime > stepCooldown) {
                            lastStepTime = noww
                            currentStepCount++
                        }
                        isRising = false
                        peakVector.clear()
                        accelerationWindow.clear()
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val channelId = "location_channel"
        val channel = NotificationChannel(channelId, "GPS Tracking", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Отслеживание местоположения")
            .setSmallIcon(R.drawable.visibility_icon)
            .build()
    }

    companion object {
        const val NOTIF_ID = 1001
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    private fun startSendingDataPeriodically() {
        serviceScope.launch {
            while (isActive) {
                delay(1_000)

                val locationsToSend = synchronized(locationDataList) {
                    val copy = locationDataList.toList()
                    copy
                }

                val gyrosToSend = synchronized(gyroscopeDataList) {
                    val copy = gyroscopeDataList.toList()
                    copy
                }

                val accelsToSend = synchronized(accelerometerDataList) {
                    val copy = accelerometerDataList.toList()
                    copy
                }

                val result = sendDataUseCase.execute(
                    locationDataList = locationsToSend,
                    gyroscopeDataList = gyrosToSend,
                    accelerometerDataList = accelsToSend
                )

                Log.d("SendData", "Result: $result")

                if (result is SendDataResult.Success) {
                    locationDataList.clear()
                    gyroscopeDataList.clear()
                    accelerometerDataList.clear()
                }
            }
        }
    }
}

class KalmanGpsFilter(
    private val processNoise: Double = 1.0,
    private val measurementNoise: Double = 10.0
) {
    private var x = DoubleArray(4) { 0.0 }
    private var p = Array(4) { DoubleArray(4) }

    private var lastTimestamp: Long = -1L
    private var isFirst = true

    fun update(lat: Double, lon: Double, timestamp: Long): Pair<Double, Double> {
        if (isFirst) {
            x[0] = lat
            x[1] = lon
            x[2] = 0.0
            x[3] = 0.0
            for (i in 0..3) p[i][i] = 1.0
            lastTimestamp = timestamp
            isFirst = false
            return lat to lon
        }

        val dt = (timestamp - lastTimestamp) / 1000.0
        lastTimestamp = timestamp

        val F = arrayOf(
            doubleArrayOf(1.0, 0.0, dt, 0.0),
            doubleArrayOf(0.0, 1.0, 0.0, dt),
            doubleArrayOf(0.0, 0.0, 1.0, 0.0),
            doubleArrayOf(0.0, 0.0, 0.0, 1.0)
        )

        val Q = Array(4) { DoubleArray(4) }
        val q = processNoise
        for (i in 0..3) Q[i][i] = q

        val z = doubleArrayOf(lat, lon)
        val H = arrayOf(
            doubleArrayOf(1.0, 0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 1.0, 0.0, 0.0)
        )

        val R = arrayOf(
            doubleArrayOf(measurementNoise, 0.0),
            doubleArrayOf(0.0, measurementNoise)
        )

        x = multiplyMatrixVector(F, x)

        p = addMatrices(
            multiplyMatrices(F, multiplyMatrices(p, transpose(F))),
            Q
        )

        val y = subtractVectors(z, multiplyMatrixVector(H, x))

        val S = addMatrices(
            multiplyMatrices(H, multiplyMatrices(p, transpose(H))),
            R
        )

        val K = multiplyMatrices(p, multiplyMatrices(transpose(H), invert2x2(S)))

        for (i in 0..3) {
            for (j in 0..1) {
                x[i] += K[i][j] * y[j]
            }
        }

        val KH = multiplyMatrices(K, H)
        val I = identity(4)
        val temp = subtractMatrices(I, KH)
        p = multiplyMatrices(temp, p)

        return x[0] to x[1]
    }

    private fun identity(n: Int) = Array(n) { i -> DoubleArray(n) { j -> if (i == j) 1.0 else 0.0 } }

    private fun transpose(m: Array<DoubleArray>) =
        Array(m[0].size) { i -> DoubleArray(m.size) { j -> m[j][i] } }

    private fun multiplyMatrices(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray> {
        return Array(a.size) { i ->
            DoubleArray(b[0].size) { j ->
                (0 until b.size).sumOf { k -> a[i][k] * b[k][j] }
            }
        }
    }

    private fun multiplyMatrixVector(a: Array<DoubleArray>, x: DoubleArray): DoubleArray {
        return DoubleArray(a.size) { i ->
            (0 until x.size).sumOf { j -> a[i][j] * x[j] }
        }
    }

    private fun subtractVectors(a: DoubleArray, b: DoubleArray): DoubleArray {
        return DoubleArray(a.size) { i -> a[i] - b[i] }
    }

    private fun addMatrices(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray> {
        return Array(a.size) { i -> DoubleArray(a[0].size) { j -> a[i][j] + b[i][j] } }
    }

    private fun subtractMatrices(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray> {
        return Array(a.size) { i -> DoubleArray(a[0].size) { j -> a[i][j] - b[i][j] } }
    }

    private fun invert2x2(m: Array<DoubleArray>): Array<DoubleArray> {
        val det = m[0][0] * m[1][1] - m[0][1] * m[1][0]
        if (det == 0.0) throw IllegalArgumentException("Singular matrix")
        val invDet = 1.0 / det
        return arrayOf(
            doubleArrayOf(m[1][1] * invDet, -m[0][1] * invDet),
            doubleArrayOf(-m[1][0] * invDet, m[0][0] * invDet)
        )
    }
}