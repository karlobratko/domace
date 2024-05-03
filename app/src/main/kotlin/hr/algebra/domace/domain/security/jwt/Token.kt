package hr.algebra.domace.domain.security.jwt

import kotlin.time.Duration

/**
 * Represents a JWT token.
 *
 * This is a sealed interface, meaning that all implementations of this interface are nested within this interface.
 */
sealed interface Token {
    /**
     * The value of the JWT token.
     */
    val value: String

    /**
     * Represents a refresh JWT token.
     *
     * @property value The value of the refresh JWT token.
     */
    @JvmInline value class Refresh(override val value: String) : Token

    /**
     * Represents an access JWT token.
     *
     * @property value The value of the access JWT token.
     */
    @JvmInline value class Access(override val value: String) : Token

    /**
     * Represents the duration for which a JWT token lasts.
     *
     * @property value The duration value.
     */
    @JvmInline value class Lasting(val value: Duration)

    /**
     * Represents a pair of JWT tokens, including a refresh token and an access token.
     *
     * @property refresh The refresh JWT token.
     * @property access The access JWT token.
     */
    data class Pair(val refresh: Refresh, val access: Access)
}
