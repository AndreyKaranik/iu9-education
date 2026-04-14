import java.util.Properties
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

fun sendConfirmationMessage(name: String, email: String, token: String) {
    val username = "noreply.diploma2025@yandex.ru"
    val password = "dhvypowkoklzfhmt"

    val props = Properties().apply {
        put("mail.smtp.auth", "true")
        put("mail.smtp.starttls.enable", "true")
        put("mail.smtp.host", "smtp.yandex.ru")
        put("mail.smtp.port", "587")
    }

    val session = Session.getInstance(props, object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(username, password)
        }
    })

    try {
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(username))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(email))
            subject = "Подтверждение регистрации от приложения GPSTracker"
            setText("Здравствуйте, $name!\n\nПерейдите по ссылке для подтверждения: http://$SERVER_HOST:$SERVER_PORT/api/auth/confirm?token=$token\nЕсли это были не Вы, то проигнорируйте данное письмо.")
        }
        Transport.send(message)
        println("Письмо успешно отправлено")
    } catch (e: MessagingException) {
        e.printStackTrace()
    }
}