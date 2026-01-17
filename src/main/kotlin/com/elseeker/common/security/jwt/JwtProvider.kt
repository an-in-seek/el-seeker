package com.elseeker.common.security.jwt

import com.elseeker.common.config.ElSeekerProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtProvider(
    private val elSeekerProperties: ElSeekerProperties,
) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(elSeekerProperties.jwt.secret))

    // 최적화 1: Parser는 Thread-Safe 하므로 한 번만 생성하여 재사용합니다.
    private val jwtParser: JwtParser = Jwts.parser()
        .verifyWith(secretKey)
        .build()

    fun generateAccessToken(memberUid: String, email: String, role: String): String {
        val now = Instant.now()
        val expiry = now.plusSeconds(elSeekerProperties.jwt.accessTokenTtl.seconds)

        return Jwts.builder()
            .subject(memberUid)
            .claim("email", email)
            .claim("role", role)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    fun generateRefreshToken(memberUid: String): String {
        val now = Instant.now()
        val expiry = now.plusSeconds(elSeekerProperties.jwt.refreshTokenTtl.seconds)
        return Jwts.builder()
            .subject(memberUid)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    fun resolveAccessToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7)
        }
        return request.cookies
            ?.firstOrNull { it.name == ACCESS_TOKEN_COOKIE_NAME }
            ?.value
    }

    /**
     * 최적화 2: 토큰 검증과 파싱을 동시에 수행합니다.
     * 유효한 토큰이면 Claims를 반환하고, 유효하지 않거나 만료되었으면 null을 반환합니다.
     */
    fun resolveClaims(token: String): Claims? {
        return try {
            val claims = jwtParser.parseSignedClaims(token).payload
            // 만료 시간 검사는 parser가 자동으로 수행하지만, 명시적으로 확인하고 싶다면 아래 코드 유지
            if (claims.expiration.before(Date.from(Instant.now()))) {
                null
            } else {
                claims
            }
        } catch (e: Exception) {
            // Log.error("Invalid JWT token: ${e.message}") // 필요 시 로깅
            null
        }
    }

    // 단순 검증용 메서드가 필요하다면 재활용
    fun validateToken(token: String): Boolean {
        return resolveClaims(token) != null
    }

    companion object {
        const val ACCESS_TOKEN_COOKIE_NAME = "ACCESS_TOKEN"
        const val REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN"
    }
}