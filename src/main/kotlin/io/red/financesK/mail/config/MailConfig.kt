package io.red.financesK.mail.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import java.util.*

@Configuration
class MailConfig {

    @Value("\${spring.mail.host}")
    private lateinit var host: String

    @Value("\${spring.mail.port}")
    private var port: Int = 587

    @Value("\${spring.mail.username}")
    private lateinit var username: String

    @Value("\${spring.mail.password}")
    private lateinit var password: String

    @Bean
    fun getJavaMailSender(): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = host
        mailSender.port = port
        mailSender.username = username
        mailSender.password = password

        val props: Properties = mailSender.javaMailProperties
        props["mail.transport.protocol"] = "smtp"
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.debug"] = "false"

        return mailSender
    }
}
