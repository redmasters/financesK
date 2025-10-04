package io.red.financesK.mail.service

import io.red.financesK.user.model.AppUser
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class MailService(
    private val mailSender: JavaMailSender
) {

    private val log = LoggerFactory.getLogger(MailService::class.java)

    @Value("\${spring.mail.support-mail}")
    private lateinit var supportEmail: String

    @Value("\${app.base-url}")
    private lateinit var baseUrl: String

    fun sendMailToken(simpleMailMessage: SimpleMailMessage) {
        log.info("m=sendMailToken, action=Sending email from: ${simpleMailMessage.from}, to: ${simpleMailMessage.to}")

        try {
            mailSender.send(simpleMailMessage)
            log.info("m=sendMailToken, action=Email sent successfully to: ${simpleMailMessage.to}")
        } catch (e: Exception) {
            log.error("m=sendMailToken, action=Failed to send email to: ${simpleMailMessage.to}, error: ${e.message}", e)
            throw e
        }
    }

    fun constructResetTokenEmail(
        token: String,
        user: AppUser
    ): SimpleMailMessage {
        val url = "$baseUrl/api/v1/users/change-password?token=$token"
        val message = "Reset your password using the following link: $url"
        return constructEmail("Password Reset", "$message \r\n$url", user)
    }

    fun constructWelcomeEmail(user: AppUser): SimpleMailMessage {
        val message = "Welcome to FinancesK! Your account has been successfully created."
        return constructEmail("Welcome to FinancesK", message, user)
    }

    fun constructGenericEmail(subject: String, body: String, user: AppUser): SimpleMailMessage {
        return constructEmail(subject, body, user)
    }

    private fun constructEmail(subject: String, body: String, user: AppUser): SimpleMailMessage {
        log.info("m=constructEmail, action=Constructing email to user: ${user.username}")

        val email = SimpleMailMessage()
        email.subject = subject
        email.text = body
        email.setTo(user.email)
        email.from = this.supportEmail

        return email
    }
}
