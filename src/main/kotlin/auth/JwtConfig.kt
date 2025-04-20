package com.apppillar.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtConfig {
    private const val secret = "secret" // используй из config в боевом режиме
    private const val issuer = "com.apppillar"
    private const val audience = "jwt-audience"
    private const val validityInMs = 60_000 // 24 часа

    private val algorithm = Algorithm.HMAC256(secret)

    fun generateToken(email: String): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("email", email)
        .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
        .sign(algorithm)
}