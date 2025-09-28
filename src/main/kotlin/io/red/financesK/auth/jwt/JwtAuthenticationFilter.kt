package io.red.financesK.auth.jwt

import io.red.financesK.auth.service.CustomUserDetails
import io.red.financesK.auth.service.UserDetailsServiceImpl
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userDetailsServiceImpl: UserDetailsServiceImpl
) : OncePerRequestFilter() {
    private val AUTH_HEADER = "Authorization"
    private val BEARER_PREFIX = "Bearer "
    private val log = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    // Define public endpoints that don't need JWT authentication
    private val publicEndpoints = listOf(
        "/api/v1/auth/login",
        "/api/v1/users"
    )

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            // Skip JWT processing for public endpoints
            if (isPublicEndpoint(request)) {
                log.debug("Skipping JWT authentication for public endpoint: ${request.requestURI}")
                filterChain.doFilter(request, response)
                return
            }

            val token = getJwtFromRequest(request)

            // Only process JWT if token is present and valid
            if (token != null && jwtTokenProvider.validateToken(token)) {
                val usernameEmail = jwtTokenProvider.getUsernameFromToken(token)
                val userDetails = userDetailsServiceImpl.loadUserByUsername(usernameEmail) as CustomUserDetails

                val authentication = UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.authorities
                )

                authentication.details = WebAuthenticationDetailsSource()
                    .buildDetails(request)

                SecurityContextHolder.getContext().authentication = authentication
                log.info("Authenticated user: ${userDetails.username} with JWT token")
            }
            // If no token or invalid token, just continue without authentication
            // Spring Security will handle authorization based on endpoint configuration

        } catch (e: Exception) {
            log.error("Error processing JWT token: ${e.cause?.message ?: e.message}")
            // Don't throw exception or send error response - let Spring Security handle it
        }

        filterChain.doFilter(request, response)
    }

    private fun isPublicEndpoint(request: HttpServletRequest): Boolean {
        val requestURI = request.requestURI
        val method = request.method

        // Check if it's a POST to /api/v1/users or /api/v1/auth/login
        return when {
            method == "POST" && requestURI == "/api/v1/users" -> true
            method == "POST" && requestURI == "/api/v1/auth/login" -> true
            else -> false
        }
    }

    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTH_HEADER)
        return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            bearerToken.substring(BEARER_PREFIX.length)
        } else {
            null
        }
    }
}
