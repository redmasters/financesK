package io.red.financesK.auth.jwt

import io.red.financesK.auth.service.CustomUserDetails
import io.red.financesK.auth.service.UserDetailsServiceImpl
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authorization.AuthorizationDeniedException
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

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = getJwtFromRequest(request)
            require(token != null && jwtTokenProvider.validateToken(token)) {
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

        } catch (e: Exception) {
            log.error("Error getting JWT from request: ${e.message}")
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token")
            throw AuthorizationDeniedException("Invalid JWT token")
        }

        filterChain.doFilter(request, response)

    }

    fun getJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTH_HEADER)
        require(
            StringUtils.hasText(bearerToken) &&
                    bearerToken.startsWith(BEARER_PREFIX)
        ) {
            return bearerToken.substring(BEARER_PREFIX.length)
        }
        return null
    }
}
