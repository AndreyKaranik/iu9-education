import com.auth0.jwt.JWT
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.mindrot.jbcrypt.BCrypt
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*

fun Route.authRoutes() {

    post("/register") {
        val request = call.receive<RegisterRequest>()

        val validationErrors = request.validate()
        if (validationErrors.isNotEmpty()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(validationErrors.joinToString("; ")))
            return@post
        }

        val hashedPassword = hash(request.password)
        val confirmationToken = UUID.randomUUID().toString()
        val confirmationExpires = LocalDateTime.now().plusHours(24)

        val conn = Database.connection

        val existingUser = conn.prepareStatement(
            "SELECT id, is_confirmed FROM users WHERE email = ?"
        ).use { stmt ->
            stmt.setString(1, request.email)
            val rs = stmt.executeQuery()
            if (rs.next()) {
                Pair(rs.getInt("id"), rs.getBoolean("is_confirmed"))
            } else null
        }

        if (existingUser != null) {
            val (id, isConfirmed) = existingUser
            if (isConfirmed) {
                call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse("User already exists and is confirmed")
                )
                return@post
            } else {
                val newToken = UUID.randomUUID().toString()
                val newExpires = LocalDateTime.now().plusHours(24)

                conn.prepareStatement(
                    """
                    UPDATE users SET confirmation_token = ?, confirmation_token_expires_at = ?,
                    name = ?, email = ?, password_hash = ?
                    WHERE id = ?
                    """
                ).use { stmt ->
                    stmt.setString(1, newToken)
                    stmt.setTimestamp(2, Timestamp.valueOf(newExpires))
                    stmt.setString(3, request.name)
                    stmt.setString(4, request.email)
                    stmt.setString(5, hashedPassword)
                    stmt.setInt(6, id)
                    stmt.executeUpdate()
                }

                sendConfirmationMessage(request.name, request.email, newToken)

                call.respond(
                    HttpStatusCode.OK,
                    MessageResponse("Confirmation email resent. Please check your inbox.")
                )
                return@post
            }
        }

        conn.prepareStatement(
            """
            INSERT INTO users (name, email, password_hash, confirmation_token, confirmation_token_expires_at)
            VALUES (?, ?, ?, ?, ?)
            """
        ).use { stmt ->
            stmt.setString(1, request.name)
            stmt.setString(2, request.email)
            stmt.setString(3, hashedPassword)
            stmt.setString(4, confirmationToken)
            stmt.setTimestamp(5, Timestamp.valueOf(confirmationExpires))
            stmt.executeUpdate()
        }

        sendConfirmationMessage(request.name, request.email, confirmationToken)

        call.respond(
            HttpStatusCode.Created,
            MessageResponse("User registered. Please confirm your email.")
        )
    }

    get("/confirm") {
        val token = call.parameters["token"]
        if (token.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing token"))
            return@get
        }

        val conn = Database.connection

        val user = conn.prepareStatement(
            """
        SELECT id, is_confirmed, confirmation_token_expires_at
        FROM users WHERE confirmation_token = ?
        """
        ).use { stmt ->
            stmt.setString(1, token)
            val rs = stmt.executeQuery()
            if (rs.next()) {
                val expiresAt = rs.getTimestamp("confirmation_token_expires_at")?.toLocalDateTime()
                UserInfo(
                    id = rs.getInt("id"),
                    isConfirmed = rs.getBoolean("is_confirmed"),
                    tokenExpiresAt = expiresAt
                )
            } else null
        }

        if (user == null) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid confirmation token"))
            return@get
        }

        if (user.isConfirmed) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("User is already confirmed"))
            return@get
        }

        val now = LocalDateTime.now()
        if (user.tokenExpiresAt == null || now.isAfter(user.tokenExpiresAt)) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Confirmation token expired"))
            return@get
        }

        conn.prepareStatement(
            """
        UPDATE users
        SET is_confirmed = TRUE
        WHERE id = ?
        """
        ).use { stmt ->
            stmt.setInt(1, user.id)
            stmt.executeUpdate()
        }

        call.respond(HttpStatusCode.OK, MessageResponse("Email confirmed successfully"))
    }

    post("/login") {
        val request = call.receive<LoginRequest>()
        val conn = Database.connection

        val user = conn.prepareStatement(
            "SELECT id, name, email, password_hash, is_confirmed FROM users WHERE email = ?"
        ).use { stmt ->
            stmt.setString(1, request.email)
            val rs = stmt.executeQuery()
            if (rs.next()) {
                UserLoginInfo(
                    id = rs.getInt("id"),
                    name = rs.getString("name"),
                    email = rs.getString("email"),
                    passwordHash = rs.getString("password_hash"),
                    isConfirmed = rs.getBoolean("is_confirmed")
                )
            } else null
        }

        if (user == null || !BCrypt.checkpw(request.password, user.passwordHash)) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid email or password"))
            return@post
        }

        if (!user.isConfirmed) {
            call.respond(HttpStatusCode.Forbidden, ErrorResponse("Email not confirmed"))
            return@post
        }

        val accessToken = JWTConfig.generateToken(user.id, user.email)
        val refreshToken = UUID.randomUUID().toString()
        val refreshTokenExpiresAt = LocalDateTime.now().plusDays(30)

        val refreshTokenHash = hash(refreshToken)

        conn.prepareStatement(
            "UPDATE users SET refresh_token_hash = ?, refresh_token_expires_at = ? WHERE id = ?"
        ).use { stmt ->
            stmt.setString(1, refreshTokenHash)
            stmt.setTimestamp(2, Timestamp.valueOf(refreshTokenExpiresAt))
            stmt.setInt(3, user.id)
            stmt.executeUpdate()
        }

        call.respond(
            HttpStatusCode.OK,
            LoginResponse(name = user.name, accessToken = accessToken, refreshToken = refreshToken)
        )
    }

    post("/refresh-token") {
        val request = call.receive<RefreshTokenRequest>()

        val decodedJWT = try {
            JWT.decode(request.accessToken)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid access token format"))
            return@post
        }

        val algorithm = JWTConfig.algorithm
        try {
            algorithm.verify(decodedJWT)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token signature"))
            return@post
        }

        val userId = decodedJWT.getClaim("userId").asInt()

        val conn = Database.connection
        val user = conn.prepareStatement(
            "SELECT id, email, refresh_token_hash, refresh_token_expires_at FROM users WHERE id = ?"
        ).use { stmt ->
            stmt.setInt(1, userId)
            val rs = stmt.executeQuery()
            if (rs.next()) {
                val storedHash = rs.getString("refresh_token_hash")
                if (!BCrypt.checkpw(request.refreshToken, storedHash)) return@use null

                val expiresAt = rs.getTimestamp("refresh_token_expires_at")?.toLocalDateTime()
                UserRefreshInfo(
                    id = rs.getInt("id"),
                    email = rs.getString("email"),
                    refreshTokenExpiresAt = expiresAt
                )
            } else null
        }

        if (user == null || user.refreshTokenExpiresAt == null || LocalDateTime.now().isAfter(user.refreshTokenExpiresAt)) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid or expired refresh token"))
            return@post
        }

        val newAccessToken = JWTConfig.generateToken(user.id, user.email)
        call.respond(AccessTokenResponse(accessToken = newAccessToken))
    }
}

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RefreshTokenRequest(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String
)

@Serializable
data class LoginResponse(
    val name: String,
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String
)

@Serializable
data class AccessTokenResponse(
    @SerialName("access_token")
    val accessToken: String
)

@Serializable
data class RegisterRequest(val name: String, val email: String, val password: String) {
    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        if (name.isBlank()) errors.add("Name must not be blank")
        if (!email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$")))
            errors.add("Invalid email format")
        if (password.length < 4)
            errors.add("Password must be at least 4 characters")

        return errors
    }
}

@Serializable
data class UserInfo(
    val id: Int,
    val isConfirmed: Boolean,
    @Contextual val tokenExpiresAt: LocalDateTime?
)

data class UserLoginInfo(val id: Int, val name: String, val email: String, val passwordHash: String, val isConfirmed: Boolean)

data class UserRefreshInfo(val id: Int, val email: String, val refreshTokenExpiresAt: LocalDateTime?)