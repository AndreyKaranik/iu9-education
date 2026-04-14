import java.sql.Connection
import java.sql.DriverManager

object Database {
    private val url = "jdbc:postgresql://$SERVER_HOST:5432/diploma2025"
    private val user = "diploma2025"
    private val password = "diploma2025_password"

    val connection: Connection by lazy {
        DriverManager.getConnection(url, user, password)
    }
}