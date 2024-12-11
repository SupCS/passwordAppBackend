package ua.asparian

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

object JwtConfig {
    private val dotenv = Dotenv.load()
    private val secret = dotenv["JWT_SECRET"] ?: throw IllegalStateException("JWT_SECRET not found in .env")
    private const val issuer = "ua.asparian"
    private const val audience = "ua.asparian.users"
    const val realm = "Access to Password Manager"

    private val algorithm = Algorithm.HMAC256(secret)

    val verifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun generateToken(username: String): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("username", username)
            .sign(algorithm)
    }
}

fun Application.configureSecurity() {
    install(Authentication) {
        jwt {
            realm = JwtConfig.realm
            verifier(JwtConfig.verifier)
            validate { credential ->
                if (credential.payload.getClaim("username").asString().isNotEmpty()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}
