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
                // Endpoints públicos
                it.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                it.requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                it.requestMatchers(HttpMethod.POST, "/api/v1/users/reset-password").permitAll()
                it.requestMatchers(HttpMethod.GET, "/api/v1/users/change-password").permitAll()
                it.requestMatchers("/actuator/**").permitAll()

                // Endpoints para reset de senha (apenas usuários com privilégio específico)
                it.requestMatchers(HttpMethod.POST, "/api/v1/users/change-password").hasRole("CHANGE_PASSWORD_PRIVILEGE")
                it.requestMatchers(HttpMethod.POST, "/api/v1/users/save-password").hasRole("CHANGE_PASSWORD_PRIVILEGE")

                // Endpoints de gerenciamento de authorities (apenas ADMIN)
                it.requestMatchers("/api/v1/authorities/**").hasRole("ADMIN")
                it.requestMatchers(HttpMethod.GET, "/api/v1/authorities/available").hasAnyRole("USER", "ADMIN")

                // Endpoints que requerem papel de ADMIN
                it.requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("ADMIN")
                it.requestMatchers(HttpMethod.GET, "/api/v1/users").hasRole("ADMIN")

                // Endpoints que requerem autenticação (USER ou ADMIN)
                it.requestMatchers("/api/v1/accounts/**").hasAnyRole("USER", "ADMIN")
                it.requestMatchers("/api/v1/users/{id}").hasAnyRole("USER", "ADMIN")
                it.requestMatchers("/api/v1/categories/**").hasAnyRole("USER", "ADMIN")
                it.requestMatchers("/api/v1/transactions/**").hasAnyRole("USER", "ADMIN")
                it.requestMatchers("/api/v1/budgets/**").hasAnyRole("USER", "ADMIN")
                it.requestMatchers("/api/v1/bank/**").hasAnyRole("USER", "ADMIN")

                it.anyRequest().permitAll()
            }
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling { ex ->
                ex.authenticationEntryPoint { _, response, _ ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                }
                ex.accessDeniedHandler { _, response, _ ->
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied")
                }
            }
            .build()
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }
}
