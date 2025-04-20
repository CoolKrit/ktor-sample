package com.apppillar.services

import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailService(
    private val smtpHost: String,
    private val smtpPort: String,
    private val username: String,
    private val password: String
) {

    fun sendResetEmail(to: String, resetLink: String) {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", smtpHost)
            put("mail.smtp.port", smtpPort)
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })

        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(username))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                subject = "Восстановление пароля"
                setText("Чтобы восстановить пароль, перейдите по ссылке: $resetLink")
            }

            Transport.send(message)
            println("✅ Email отправлен на $to")
        } catch (e: MessagingException) {
            e.printStackTrace()
        }
    }
}