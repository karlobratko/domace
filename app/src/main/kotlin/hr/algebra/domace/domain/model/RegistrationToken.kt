package hr.algebra.domace.domain.model

import hr.algebra.domace.domain.model.RegistrationToken.ConfirmationStatus.Unconfirmed
import hr.algebra.domace.domain.security.Token
import kotlinx.datetime.Instant

/**
 * Represents a Registration Token.
 *
 * This is a data class, meaning it has automatically generated equals(), hashCode(), toString(), copy() and
 * componentN() functions.
 * It contains several properties, each representing a different aspect of the Registration Token.
 *
 * @property token The Register Token.
 * @property createdAt The time at which the Registration Token was created.
 * @property expiresAt The time at which the Registration Token expires.
 * @property confirmationStatus The confirmation status of the Registration Token, which can be either Confirmed
 * or Unconfirmed.
 */
data class RegistrationToken(
    val token: Token.Register,
    val createdAt: CreatedAt,
    val expiresAt: ExpiresAt,
    val confirmationStatus: ConfirmationStatus = Unconfirmed
) {
    /**
     * Represents the time at which a Registration Token was created.
     *
     * This is a value class, meaning it only contains the value of the creation time.
     *
     * @property value The value of the creation time.
     */
    @JvmInline value class CreatedAt(val value: Instant)

    /**
     * Represents the time at which a Registration Token expires.
     *
     * This is a value class, meaning it only contains the value of the expiration time.
     *
     * @property value The value of the expiration time.
     */
    @JvmInline value class ExpiresAt(val value: Instant)

    /**
     * Represents the confirmation status of a Registration Token.
     *
     * This is a sealed interface, meaning it can only be implemented by classes in the same file.
     * It contains several subtypes, each representing a different confirmation status.
     */
    sealed interface ConfirmationStatus {
        /**
         * Represents a confirmed Registration Token.
         *
         * This is a value class, meaning it only contains the value of the confirmation time.
         *
         * @property value The value of the confirmation time.
         */
        @JvmInline value class Confirmed(val value: Instant) : ConfirmationStatus

        /**
         * Represents an unconfirmed Registration Token.
         *
         * This is a data object, meaning it has automatically generated equals(), hashCode() and toString() functions.
         */
        data object Unconfirmed : ConfirmationStatus
    }
}
