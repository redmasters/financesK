package io.red.financesK.mail.service

import io.red.financesK.user.model.AppUser
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.stereotype.Service
import java.util.*

@Service
class MailService(
    private val mailSender: JavaMailSender
) {

    private val log = LoggerFactory.getLogger(MailService::class.java)

    @Value("\${spring.mail.support-email}")
    val supportEmail: String? = null

    @Value("\${spring.mail.host}")
    val host: String? = null

    @Value("\${spring.mail.port}")
    val port: Int? = null

    @Value("\${spring.mail.username}")
    val username: String? = null

    @Value("\${spring.mail.password}")
    val password: String? = null

    fun sendMailToken(simpleMailMessage: SimpleMailMessage) {
        val message = SimpleMailMessage()
        message.from = username
        message.setTo("${simpleMailMessage.to}")
        message.subject = simpleMailMessage.subject
        message.text = simpleMailMessage.text

        mailSender.send(simpleMailMessage)
    }

    fun constructResetTokenEmail(
        contextPath: String,
        locale: Locale,
        token: String,
        user: AppUser
    ): SimpleMailMessage {
        val url = "$contextPath/user/change-password?token=$token"
        val message = "Reset your password using the following link: $url$locale"
        return constructEmail("Password Reset", "$message \r\n$url", user)
    }

    private fun constructEmail(subject: String, body: String, user: AppUser): SimpleMailMessage {
        log.info("Sending email to $subject to $body")
        val email = SimpleMailMessage()
        email.subject = subject
        email.text = body
        email.setTo(user.email)
        email.from = supportEmail
        return email
    }

    @Bean
    fun getMailSender(): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = host
        mailSender.port = port!!

        mailSender.username = username
        mailSender.password = password

        var props = mailSender.javaMailProperties
        props["mail.transport.protocol"] = "smtp"
        props["mail.smtp.auth"] = "true"
        props["mail.smtps.enable"] = "true"
        props["mail.debug"] = "true"
        return mailSender
    }
}
