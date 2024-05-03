package hr.algebra.domace.infrastructure.security.jwt

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable data class SecurityConfig(val issuer: String, val lasting: LastingConfig) {
    @Serializable data class LastingConfig(val access: Duration, val refresh: Duration)
}

@Serializable data class SecuritySecretConfig(val secret: String)
