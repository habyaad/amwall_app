package com.amwaal.amwall_app.infrastructure.security

import com.amwaal.amwall_app.users.Role
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureAlgorithm
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class TokenService {
    private val secretKey: SecretKey = Jwts.SIG.HS256.key().build()

    private val expirationPeriod: Long = 1000 * 60 * 60 * 24 

    fun generateToken(principal: TokenUserPrincipal): String {
        return Jwts.builder()
            .subject(principal.username)
            .claim("userId", principal.id)
            .claim("role", principal.role.name)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + 3600000)) // 1 hour
            .signWith(secretKey)
            .compact()
    }
    
     fun extractPrincipal(token: String): TokenUserPrincipal? {
       return try {
            val claims = extractAllClaims(token)
            val username = claims.subject ?: return null
            val id = claims.get("userId", Long::class.javaObjectType) ?: return null
            
            // Extract the string value and map it directly back to your Role enum
            val roleStr = claims.get("role", String::class.java) ?: return null
            val role = Role.valueOf(roleStr) 
            
            TokenUserPrincipal(id = id, username = username, role = role)
        } catch (e: Exception) {
            null // Catches malformed tokens, wrong enum mappings, or expiration failures safely
        }
    }

    fun extractUsername(token: String): String? {
        return extractAllClaims(token).subject
    }

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return username == userDetails.username && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean {
        return extractAllClaims(token).expiration.before(Date())
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }}