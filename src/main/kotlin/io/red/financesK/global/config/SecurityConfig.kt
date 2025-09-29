package io.red.financesK.global.config

import io.red.financesK.auth.jwt.JwtAuthenticationFilter
import io.red.financesK.auth.jwt.JwtTokenProvider
import io.red.financesK.auth.service.UserDetailsServiceImpl
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val userDetailsServiceImpl: UserDetailsServiceImpl,
    private val jwtTokenProvider: JwtTokenProvider
) {
    @Bean
    fun jwtAuthenticationFilter(): JwtAuthenticationFilter {
        return JwtAuthenticationFilter(jwtTokenProvider, userDetailsServiceImpl)
    }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        @Qualifier("corsConfigurationSource") corsConfigSource: CorsConfigurationSource
    ): SecurityFilterChain {
        return http.csrf { it.disable() }
            .cors { it.configurationSource(corsConfigSource) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                it.requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                it.requestMatchers("/actuator/**").permitAll()
                it.requestMatchers("/api/v1/accounts/**").authenticated()
                it.requestMatchers("/api/v1/users/{id}").authenticated()
                it.requestMatchers("/api/v1/categories/**").authenticated()
                it.requestMatchers("/api/v1/transactions/**").authenticated()
                it.requestMatchers("/api/v1/budgets/**").authenticated()
                it.requestMatchers("/api/v1/bank/**").authenticated()
                it.anyRequest().permitAll()
            }
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling { ex ->
                ex.authenticationEntryPoint { _, response, _ ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                }
            }
            .build()
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }
}
