package hr.algebra.domace.domain.model

import hr.algebra.domace.domain.model.RefreshToken.Status.Active
import hr.algebra.domace.domain.security.jwt.Claims
import hr.algebra.domace.domain.security.jwt.Token

/**
 * Represents a Refresh Token.
 *
 * This is a sealed interface, meaning that all implementations of this interface are nested within this interface.
 */
sealed interface RefreshToken {

    /**
     * Represents a new Refresh Token.
     *
     * @property userId The ID of the user for whom the token is issued.
     * @property token The actual refresh token.
     * @property issuedAt The time at which the token was issued.
     * @property expiresAt The time at which the token expires.
     * @property status The status of the token, default is Active.
     */
    class New(
        val userId: User.Id,
        val token: Token.Refresh,
        val issuedAt: Claims.IssuedAt,
        val expiresAt: Claims.ExpiresAt,
        val status: Status = Active
    ) : RefreshToken {
        /**
         * Secondary constructor to create a new Refresh Token with a lasting duration.
         *
         * @param userId The ID of the user for whom the token is issued.
         * @param token The actual refresh token.
         * @param issuedAt The time at which the token was issued.
         * @param lasting The duration for which the token lasts.
         * @param status The status of the token, default is Active.
         */
        constructor(
            userId: User.Id,
            token: Token.Refresh,
            issuedAt: Claims.IssuedAt,
            lasting: Token.Lasting,
            status: Status = Active
        ) : this(userId, token, issuedAt, Claims.ExpiresAt(issuedAt.value + lasting.value), status)
    }

    /**
     * Represents a Refresh Token that is to be prolonged.
     *
     * @property id The ID of the token to be prolonged.
     * @property expiresAt The new expiry time of the token.
     */
    class Prolong(
        val id: Id,
        val expiresAt: Claims.ExpiresAt
    ) : RefreshToken

    /**
     * Represents an existing Refresh Token entity.
     *
     * @property id The ID of the token.
     * @property userId The ID of the user for whom the token is issued.
     * @property userRole The role of the user for whom the token is issued.
     * @property token The actual refresh token.
     * @property issuedAt The time at which the token was issued.
     * @property expiresAt The time at which the token expires.
     * @property status The status of the token.
     */
    data class Entity(
        val id: Id,
        val userId: User.Id,
        val userRole: User.Role,
        val token: Token.Refresh,
        val issuedAt: Claims.IssuedAt,
        val expiresAt: Claims.ExpiresAt,
        val status: Status
    ) : RefreshToken

    /**
     * Represents the ID of a Refresh Token.
     *
     * @property value The ID value.
     */
    @JvmInline value class Id(val value: Long)

    /**
     * Represents the status of a Refresh Token.
     */
    enum class Status { Active, Revoked }
}
