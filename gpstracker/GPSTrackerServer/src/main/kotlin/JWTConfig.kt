import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JWTConfig {
    private const val secret = "secret444"
    private const val issuer = "diploma2025"
    private const val audience = "users"
    private const val validityInMs = 60 * 60 * 1000L // 1 час

    val algorithm = Algorithm.HMAC256(secret)

    fun generateToken(userId: Int, email: String): String {
        val now = System.currentTimeMillis()
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + validityInMs))
            .sign(algorithm)
    }

    fun getVerifier(): JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()
}