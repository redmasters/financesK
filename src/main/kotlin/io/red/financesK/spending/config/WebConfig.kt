package io.red.financesK.spending.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig {
    @Bean
    fun corsConfig(): WebMvcConfigurer {
        return  object : WebMvcConfigurer {
            override fun addCorsMappings(registry: org.springframework.web.servlet.config.annotation.CorsRegistry) {
                registry.addMapping("/**")
                    .allowedOrigins("https://finances-k-frontend.vercel.app/")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
            }

        }
    }
}
