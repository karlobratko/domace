package hr.algebra.domace.domain.security.jwt

import arrow.core.Nel
import hr.algebra.domace.domain.config.RoundedInstantProvider
import hr.algebra.domace.domain.security.LastingFor
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents the claims made in the JWT token.
 *
 * @property issuer The issuer of the JWT token.
 * @property subject The subject of the JWT token.
 * @property audience The audience of the JWT token.
 * @property use The use of the JWT token, either refresh or access.
 * @property issuedAt The time at which the JWT token was issued.
 * @property expiresAt The time at which the JWT token expires.
 * @property role The role of the user for whom the JWT token is issued.
 */
data class Claims(
    val issuer: Issuer,
    val subject: Subject,
    val audience: Nel<Audience>,
    val use: Use,
    val issuedAt: IssuedAt,
    val expiresAt: ExpiresAt,
    val role: Role
) {
    companion object {
        /**
         * Creates a refresh JWT token with the given parameters.
         *
         * @param issuer The issuer of the JWT token.
         * @param subject The subject of the JWT token.
         * @param audience The audience of the JWT token.
         * @param issuedAt The time at which the JWT token was issued.
         * @param expiresAt The time at which the JWT token expires.
         * @param role The role of the user for whom the JWT token is issued.
         * @return A Claims object representing the refresh JWT token.
         */
        fun Refresh(
            issuer: Issuer,
            subject: Subject,
            audience: Nel<Audience>,
            issuedAt: IssuedAt,
            expiresAt: ExpiresAt,
            role: Role
        ) = Claims(issuer, subject, audience, Use.Refresh, issuedAt, expiresAt, role)

        /**
         * Creates a refresh JWT token with the given parameters and a lasting duration.
         *
         * @param issuer The issuer of the JWT token.
         * @param subject The subject of the JWT token.
         * @param audience The audience of the JWT token.
         * @param issuedAt The time at which the JWT token was issued.
         * @param lasting The duration for which the JWT token lasts.
         * @param role The role of the user for whom the JWT token is issued.
         * @return A Claims object representing the refresh JWT token.
         */
        fun Refresh(
            issuer: Issuer,
            subject: Subject,
            audience: Nel<Audience>,
            issuedAt: IssuedAt,
            lasting: LastingFor,
            role: Role
        ) = Claims(issuer, subject, audience, Use.Refresh, issuedAt, ExpiresAt(issuedAt.value + lasting.value), role)

        /**
         * Creates an access JWT token with the given parameters.
         *
         * @param issuer The issuer of the JWT token.
         * @param subject The subject of the JWT token.
         * @param audience The audience of the JWT token.
         * @param issuedAt The time at which the JWT token was issued.
         * @param expiresAt The time at which the JWT token expires.
         * @param role The role of the user for whom the JWT token is issued.
         * @return A Claims object representing the access JWT token.
         */
        fun Access(
            issuer: Issuer,
            subject: Subject,
            audience: Nel<Audience>,
            issuedAt: IssuedAt,
            expiresAt: ExpiresAt,
            role: Role
        ) = Claims(issuer, subject, audience, Use.Access, issuedAt, expiresAt, role)

        /**
         * Creates an access JWT token with the given parameters and a lasting duration.
         *
         * @param issuer The issuer of the JWT token.
         * @param subject The subject of the JWT token.
         * @param audience The audience of the JWT token.
         * @param issuedAt The time at which the JWT token was issued.
         * @param lasting The duration for which the JWT token lasts.
         * @param role The role of the user for whom the JWT token is issued.
         * @return A Claims object representing the access JWT token.
         */
        fun Access(
            issuer: Issuer,
            subject: Subject,
            audience: Nel<Audience>,
            issuedAt: IssuedAt,
            lasting: LastingFor,
            role: Role
        ) = Claims(issuer, subject, audience, Use.Access, issuedAt, ExpiresAt(issuedAt.value + lasting.value), role)
    }

    /**
     * Represents the issuer of the JWT token.
     *
     * @property value The issuer value.
     */
    @JvmInline value class Issuer(val value: String)

    /**
     * Represents the subject of the JWT token.
     *
     * @property value The subject value.
     */
    @JvmInline value class Subject(val value: String)

    /**
     * Represents the audience of the JWT token.
     *
     * @property value The audience value.
     */
    @Serializable
    @JvmInline value class Audience(val value: String)

    /**
     * Represents the use of the JWT token, either refresh or access.
     */
    enum class Use { Refresh, Access }

    /**
     * Represents the time at which the JWT token was issued.
     *
     * @property value The issued at time.
     */
    @JvmInline value class IssuedAt(val value: Instant = RoundedInstantProvider.now())

    /**
     * Represents the time at which the JWT token expires.
     *
     * @property value The expires at time.
     */
    @JvmInline value class ExpiresAt(val value: Instant)

    /**
     * Represents the role of the user for whom the JWT token is issued.
     *
     * @property value The role value.
     */
    @JvmInline value class Role(val value: String)
}
