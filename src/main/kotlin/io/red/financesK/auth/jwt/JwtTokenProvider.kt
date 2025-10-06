package io.red.financesK.auth.jwt

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import io.red.financesK.auth.service.CustomUserDetails
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider {
    private val log = LoggerFactory.getLogger(JwtTokenProvider::class.java)

    @Value("\${app.jwt.secret}")
    private lateinit var secretKey: String

    @Value("\${app.jwt.expiration-ms}")
    private var jwtExpirationMs: Long = 0

    fun generateToken(customUserDetails: CustomUserDetails): String {
        log.info("Generating JWT token for user: ${customUserDetails.username}")

        return Jwts
            .builder()
            .subject(customUserDetails.username)
            .claim("id", customUserDetails.getAppUser().id)
            .claim("authorities", customUserDetails.authorities)
            .issuedAt(Date())
            .expiration(Date.from(Instant.now().plusMillis(jwtExpirationMs)))
            .signWith(getSignatureKey())
            .compact()

    }

    fun getSignatureKey(): SecretKey {
        log.info("Generating secret key for JWT token")
        val keyBytes = Decoders.BASE64.decode(secretKey)
        require(keyBytes.size >= 32) {
            "Secret key must be at least 32 bytes long, but was ${keyBytes.size} bytes"
        }
        return Keys.hmacShaKeyFor(keyBytes)

    }

    fun getUsernameFromToken(token: String?): String {
        log.info("Extracting username from JWT token")
        return Jwts.parser()
            .verifyWith(getSignatureKey())
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
    }

    fun validateToken(token: String): Boolean {
        log.info("Validating JWT token")
        return try {
            Jwts.parser()
                .verifyWith(getSignatureKey())
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: MalformedJwtException) {
            log.error("Malformed JWT token exception: ${e.message}")
            false
        } catch (e: ExpiredJwtException) {
            log.error("Expired JWT token exception: ${e.message}")
            false
        } catch (e: UnsupportedJwtException) {
            log.error("Unsupported JWT token exception: ${e.message}")
            false
        } catch (e: IllegalArgumentException) {
            log.error("JWT token is empty or invalid : ${e.message}")
            false
        } catch (e: Exception) {
            log.error("Invalid JWT token: ${e.message}")
            false
        }
    }
}
