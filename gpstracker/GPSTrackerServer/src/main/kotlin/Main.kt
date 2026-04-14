import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.mindrot.jbcrypt.BCrypt
import java.sql.Connection
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

const val SERVER_HOST = "diploma2025.ru"
const val SERVER_PORT = 8200

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, module = Application::module).start(wait = true)
}

fun Application.module() {

    install(ContentNegotiation) {
        json(Json {
                serializersModule = SerializersModule {
                    contextual(LocalDateTime::class, LocalDateTimeSerializer)
                }
            }
        )
    }

    install(StatusPages) {
        exception<Exception> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(cause.message.toString())
            )
        }
    }

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JWTConfig.getVerifier())
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asInt()
                if (userId != null) JWTPrincipal(credential.payload) else null
            }
        }
    }

    routing {
        route("/api/auth") {
            authRoutes()
        }
        route("/api/groups") {
            groupRoutes()
        }
        route("/api/data") {
            dataRoutes()
        }
        legalRoutes()
    }

    CoroutineScope(Dispatchers.IO).launch {
        while (true) {
            val conn = Database.connection

            val zoneId = ZoneId.of("UTC")
            val now = ZonedDateTime.now(zoneId)
            val oneWeekAgo = Timestamp.from(now.minusWeeks(1).toInstant())
            val fiveMinutesAgo = Timestamp.from(now.minusMinutes(5).toInstant())

            try {
                conn.prepareStatement("DELETE FROM location_data WHERE recorded_at < ?").use {
                    it.setTimestamp(1, oneWeekAgo)
                    it.executeUpdate()
                }

                conn.prepareStatement("DELETE FROM gyroscope_data WHERE recorded_at < ?").use {
                    it.setTimestamp(1, fiveMinutesAgo)
                    it.executeUpdate()
                }

                conn.prepareStatement("DELETE FROM accelerometer_data WHERE recorded_at < ?").use {
                    it.setTimestamp(1, fiveMinutesAgo)
                    it.executeUpdate()
                }

                val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                println("[$currentTime] записи удалены")
            } catch (e: Exception) {
                e.printStackTrace()
            }

            delay(5 * 60 * 1000L)
        }
    }
}

@Serializable
data class ErrorResponse(val error: String)

@Serializable
data class MessageResponse(val message: String)

fun hash(string: String): String {
    return BCrypt.hashpw(string, BCrypt.gensalt())
}