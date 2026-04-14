import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Timestamp
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.*

fun Route.dataRoutes() {
    authenticate("auth-jwt") {
        post("/send-data") {
            val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
            val request = call.receive<SendDataRequest>()
            val conn = Database.connection

            conn.autoCommit = false
            try {
                conn.prepareStatement(
                    """
            INSERT INTO location_data (user_id, latitude, longitude, kalman_latitude, kalman_longitude, steps, recorded_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """
                ).use { stmt ->
                    request.locationDataList.forEach {
                        stmt.setInt(1, userId)
                        stmt.setDouble(2, it.latitude)
                        stmt.setDouble(3, it.longitude)
                        stmt.setDouble(4, it.kalmanLatitude)
                        stmt.setDouble(5, it.kalmanLongitude)
                        stmt.setInt(6, it.steps)
                        stmt.setTimestamp(7, Timestamp.valueOf(it.recordedAt))
                        stmt.addBatch()
                    }
                    stmt.executeBatch()
                }

                conn.prepareStatement(
                    """
            INSERT INTO gyroscope_data (user_id, x, y, z, recorded_at)
            VALUES (?, ?, ?, ?, ?)
        """
                ).use { stmt ->
                    request.gyroscopeDataList.forEach {
                        stmt.setInt(1, userId)
                        stmt.setDouble(2, it.x)
                        stmt.setDouble(3, it.y)
                        stmt.setDouble(4, it.z)
                        stmt.setTimestamp(5, Timestamp.valueOf(it.recordedAt))
                        stmt.addBatch()
                    }
                    stmt.executeBatch()
                }

                conn.prepareStatement(
                    """
            INSERT INTO accelerometer_data (user_id, x, y, z, recorded_at)
            VALUES (?, ?, ?, ?, ?)
        """
                ).use { stmt ->
                    request.accelerometerDataList.forEach {
                        stmt.setInt(1, userId)
                        stmt.setDouble(2, it.x)
                        stmt.setDouble(3, it.y)
                        stmt.setDouble(4, it.z)
                        stmt.setTimestamp(5, Timestamp.valueOf(it.recordedAt))
                        stmt.addBatch()
                    }
                    stmt.executeBatch()
                }

                conn.commit()
                call.respond(HttpStatusCode.OK, MessageResponse("Data uploaded successfully"))
            } catch (e: Exception) {
                conn.rollback()
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Failed to save data"))
            } finally {
                conn.autoCommit = true
            }
        }

        post("/mode1-1") {
            val requesterUserId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
            val request = call.receive<Mode1Request>()
            val conn = Database.connection

            try {
                if (!areUsersInSameGroup(conn, requesterUserId, request.targetUserId)) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("Users are not in the same group"))
                    return@post
                }

                val (latCol, lonCol) = if (request.kalmanEnabled) {
                    "kalman_latitude" to "kalman_longitude"
                } else {
                    "latitude" to "longitude"
                }

                val stmt = conn.prepareStatement(
                    """
                    SELECT $latCol, $lonCol
                    FROM location_data
                    WHERE user_id = ?
                    ORDER BY recorded_at DESC
                    LIMIT 1
                """
                )
                stmt.setInt(1, request.targetUserId)

                val rs = stmt.executeQuery()
                if (rs.next()) {
                    val latitude = rs.getDouble(1)
                    val longitude = rs.getDouble(2)
                    call.respond(LocationResponse(latitude, longitude))
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("No location data found"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Internal error"))
            }
        }

        post("/mode1-2") {
            val requesterUserId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
            val request = call.receive<Mode1Request>()
            val conn = Database.connection

            try {
                if (!areUsersInSameGroup(conn, requesterUserId, request.targetUserId)) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("Users are not in the same group"))
                    return@post
                }

                val latestTime = conn.prepareStatement("""
            SELECT recorded_at FROM location_data
            WHERE user_id = ?
            ORDER BY recorded_at DESC
            LIMIT 1
        """).use {
                    it.setInt(1, request.targetUserId)
                    val rs = it.executeQuery()
                    if (rs.next()) rs.getTimestamp(1) else null
                }

                if (latestTime == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("No location data found"))
                    return@post
                }

                val endTime = latestTime
                val start10s = Timestamp(endTime.time - 10_000)
                val start1h = Timestamp(endTime.time - 60 * 60 * 1000)

                fun fetchSensorData(table: String): List<SensorData> {
                    val stmt = conn.prepareStatement("""
                SELECT x, y, z, recorded_at FROM $table
                WHERE user_id = ? AND recorded_at BETWEEN ? AND ?
                ORDER BY recorded_at ASC
            """)
                    stmt.setInt(1, request.targetUserId)
                    stmt.setTimestamp(2, start10s)
                    stmt.setTimestamp(3, endTime)
                    val rs = stmt.executeQuery()
                    val list = mutableListOf<SensorData>()
                    while (rs.next()) {
                        list.add(
                            SensorData(
                                x = rs.getDouble("x"),
                                y = rs.getDouble("y"),
                                z = rs.getDouble("z"),
                                recordedAt = rs.getTimestamp("recorded_at").toString()
                            )
                        )
                    }
                    return list
                }

                val gyros = fetchSensorData("gyroscope_data")
                val accels = fetchSensorData("accelerometer_data")

                val locationDataList = conn.prepareStatement("""
            SELECT latitude, longitude, kalman_latitude, kalman_longitude, steps, recorded_at
            FROM location_data
            WHERE user_id = ? AND recorded_at BETWEEN ? AND ?
            ORDER BY recorded_at ASC
        """).use {
                    it.setInt(1, request.targetUserId)
                    it.setTimestamp(2, start1h)
                    it.setTimestamp(3, endTime)
                    val rs = it.executeQuery()
                    val list = mutableListOf<LocationData>()
                    while (rs.next()) {
                        list.add(
                            LocationData(
                                latitude = rs.getDouble("latitude"),
                                longitude = rs.getDouble("longitude"),
                                kalmanLatitude = rs.getDouble("kalman_latitude"),
                                kalmanLongitude = rs.getDouble("kalman_longitude"),
                                steps = rs.getInt("steps"),
                                recordedAt = rs.getTimestamp("recorded_at").toString()
                            )
                        )
                    }
                    list
                }

                call.respond(
                    Mode1Response(
                        locationRecordedAt = endTime.toString(),
                        gyroscopeDataList = gyros,
                        accelerometerDataList = accels,
                        locationDataList = locationDataList
                    )
                )

            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Internal error"))
            }
        }

        post("/get-period-locations") {
            val requesterUserId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
            val request = call.receive<PeriodLocationRequest>()
            val conn = Database.connection

            try {
                if (!areUsersInSameGroup(conn, requesterUserId, request.targetUserId)) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("Users are not in the same group"))
                    return@post
                }

                val stmt = conn.prepareStatement(
                    """
            SELECT latitude, longitude, kalman_latitude, kalman_longitude, steps, recorded_at
            FROM location_data
            WHERE user_id = ?
              AND recorded_at BETWEEN ?::timestamptz AND ?::timestamptz
            ORDER BY recorded_at ASC
            """
                )

                stmt.setInt(1, request.targetUserId)
                stmt.setString(2, request.from)
                stmt.setString(3, request.to)

                val rs = stmt.executeQuery()
                val rawLocations = mutableListOf<LocationData>()
                while (rs.next()) {
                    rawLocations.add(
                        LocationData(
                            latitude = rs.getDouble("latitude"),
                            longitude = rs.getDouble("longitude"),
                            kalmanLatitude = rs.getDouble("kalman_latitude"),
                            kalmanLongitude = rs.getDouble("kalman_longitude"),
                            steps = rs.getInt("steps"),
                            recordedAt = rs.getTimestamp("recorded_at").toInstant()
                                .atZone(ZoneOffset.UTC)
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
                        )
                    )
                }

                val filteredLocations = if (request.minIntervalMinutes > 0) {
                    val result = mutableListOf<LocationData>()
                    var lastTime: ZonedDateTime? = null
                    for (loc in rawLocations) {
                        val currentTime = ZonedDateTime.parse(loc.recordedAt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneOffset.UTC))
                        if (lastTime == null || Duration.between(lastTime, currentTime).toMinutes() >= request.minIntervalMinutes) {
                            result.add(loc)
                            lastTime = currentTime
                        }
                    }
                    result
                } else {
                    rawLocations
                }

                call.respond(PeriodLocationResponse(filteredLocations))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Internal error"))
            }
        }

        post("/get-period-clusters") {
            val requesterUserId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asInt()
            val request = call.receive<PeriodClusterRequest>()
            val conn = Database.connection

            try {
                if (!areUsersInSameGroup(conn, requesterUserId, request.targetUserId)) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("Users are not in the same group"))
                    return@post
                }

                val latCol = if (request.kalmanEnabled) "kalman_latitude" else "latitude"
                val lonCol = if (request.kalmanEnabled) "kalman_longitude" else "longitude"

                val stmt = conn.prepareStatement(
                    """
            SELECT $latCol, $lonCol
            FROM location_data
            WHERE user_id = ?
              AND recorded_at BETWEEN ?::timestamptz AND ?::timestamptz
              AND $latCol IS NOT NULL AND $lonCol IS NOT NULL
            ORDER BY recorded_at ASC
            """
                )

                stmt.setInt(1, request.targetUserId)
                stmt.setString(2, request.from)
                stmt.setString(3, request.to)

                val rs = stmt.executeQuery()
                val points = mutableListOf<Point>()
                while (rs.next()) {
                    points.add(Point(rs.getDouble(1), rs.getDouble(2)))
                }

                val clusterCenters = dbscan(points, request.eps, request.minPts)
                call.respond(PeriodClusterResponse(clusterCenters.map { ClusterCenter(it.first, it.second) }))

            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Internal error"))
            }
        }

    }
}

fun areUsersInSameGroup(conn: Connection, userId1: Int, userId2: Int): Boolean {
    val sql = """
        SELECT 1
        FROM group_members gm1
        JOIN group_members gm2 ON gm1.group_id = gm2.group_id
        WHERE gm1.user_id = ? AND gm2.user_id = ?
        LIMIT 1
    """.trimIndent()

    conn.prepareStatement(sql).use { stmt ->
        stmt.setInt(1, userId1)
        stmt.setInt(2, userId2)
        stmt.executeQuery().use { rs ->
            return rs.next()
        }
    }
}

@Serializable
data class SendDataRequest(
    @SerialName("location_data_list")
    val locationDataList: List<LocationData>,
    @SerialName("gyroscope_data_list")
    val gyroscopeDataList: List<GyroscopeData>,
    @SerialName("accelerometer_data_list")
    val accelerometerDataList: List<AccelerometerData>
)

@Serializable
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val kalmanLatitude: Double,
    val kalmanLongitude: Double,
    val steps: Int,
    @SerialName("recorded_at")
    val recordedAt: String
)

@Serializable
data class GyroscopeData(
    val x: Double,
    val y: Double,
    val z: Double,
    @SerialName("recorded_at")
    val recordedAt: String
)

@Serializable
data class AccelerometerData(
    val x: Double,
    val y: Double,
    val z: Double,
    @SerialName("recorded_at")
    val recordedAt: String
)

@Serializable
data class Mode1Request(
    @SerialName("user_id")
    val targetUserId: Int,
    @SerialName("kalman_enabled")
    val kalmanEnabled: Boolean
)

@Serializable
data class LocationResponse(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class SensorData(
    val x: Double,
    val y: Double,
    val z: Double,
    @SerialName("recorded_at")
    val recordedAt: String
)

@Serializable
data class Mode1Response(
    @SerialName("location_recorded_at")
    val locationRecordedAt: String,
    @SerialName("gyroscope_data_list")
    val gyroscopeDataList: List<SensorData>,
    @SerialName("accelerometer_data_list")
    val accelerometerDataList: List<SensorData>,
    @SerialName("location_data_list")
    val locationDataList: List<LocationData>,
)

@Serializable
data class PeriodLocationRequest(
    @SerialName("user_id")
    val targetUserId: Int,
    val from: String,
    val to: String,
    @SerialName("min_interval_minutes")
    val minIntervalMinutes: Int = 0
)

@Serializable
data class PeriodLocationResponse(
    val locations: List<LocationData>
)

@Serializable
data class PeriodClusterRequest(
    @SerialName("user_id")
    val targetUserId: Int,
    val from: String,
    val to: String,
    @SerialName("kalman_enabled")
    val kalmanEnabled: Boolean,
    val eps: Double,
    @SerialName("min_pts")
    val minPts: Int
)

@Serializable
data class PeriodClusterResponse(
    val centers: List<ClusterCenter>
)