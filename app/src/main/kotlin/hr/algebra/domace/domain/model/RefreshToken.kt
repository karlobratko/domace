package hr.algebra.domace.domain.model

import hr.algebra.domace.domain.model.RefreshToken.Status.Active
import hr.algebra.domace.domain.security.LastingFor
import hr.algebra.domace.domain.security.Token
import hr.algebra.domace.domain.security.jwt.Claims

/**
 * Represents a Refresh Token.
 *
 * @property id The ID of the refresh token.
 * @property userId The ID of the user associated with the refresh token.
 * @property userRole The role of the user associated with the refresh token.
 * @property token The actual refresh token.
 * @property issuedAt The time at which the refresh token was issued.
 * @property expiresAt The time at which the refresh token expires.
 * @property status The status of the refresh token, either Active or Revoked.
 */
data class RefreshToken(
    val id: Id,
    val userId: User.Id,
    val userRole: User.Role,
    val token: Token.Refresh,
    val issuedAt: Claims.IssuedAt,
    val expiresAt: Claims.ExpiresAt,
    val status: Status
) {
    /**
     * Represents a new Refresh Token.
     *
     * @property userId The ID of the user associated with the new refresh token.
     * @property token The actual new refresh token.
     * @property issuedAt The time at which the new refresh token was issued.
     * @property expiresAt The time at which the new refresh token expires.
     * @property status The status of the new refresh token, default is Active.
     */
    class New(
        val userId: User.Id,
        val token: Token.Refresh,
        val issuedAt: Claims.IssuedAt,
        val expiresAt: Claims.ExpiresAt,
        val status: Status = Active
    ) {
        /**
         * Constructor for creating a new Refresh Token with a lasting duration.
         *
         * @param userId The ID of the user associated with the new refresh token.
         * @param token The actual new refresh token.
         * @param issuedAt The time at which the new refresh token was issued.
         * @param lasting The duration for which the new refresh token is valid.
         * @param status The status of the new refresh token, default is Active.
         */
        constructor(
            userId: User.Id,
            token: Token.Refresh,
            issuedAt: Claims.IssuedAt,
            lasting: LastingFor,
            status: Status = Active
        ) : this(userId, token, issuedAt, Claims.ExpiresAt(issuedAt.value + lasting.value), status)
    }

    /**
     * Represents a Refresh Token that is to be prolonged.
     *
     * @property id The ID of the refresh token to be prolonged.
     * @property expiresAt The new expiration time of the refresh token.
     */
    class Prolong(
        val id: Id,
        val expiresAt: Claims.ExpiresAt
    )

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
