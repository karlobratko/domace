package hr.algebra.domace.domain.persistence

import arrow.core.Either
import arrow.core.Option
import hr.algebra.domace.domain.DomainError
import hr.algebra.domace.domain.model.RegistrationToken
import hr.algebra.domace.domain.security.Token

/**
 * Interface for the persistence operations related to Registration Tokens.
 *
 * This interface defines the methods that must be implemented for the persistence of Registration Tokens.
 * The methods are suspending functions, meaning they are designed to be used with Kotlin's coroutines.
 */
interface RegistrationTokenPersistence {
    /**
     * Inserts a new Registration Token into the persistence layer and returns its ID.
     *
     * @return The ID of the inserted Registration Token.
     */
    suspend fun insertAndGetToken(): Token.Register

    /**
     * Selects a Registration Token from the persistence layer based on its ID.
     *
     * @param id The ID of the Registration Token to be selected.
     *
     * @return An Option containing the selected Registration Token if it exists, or None if it does not.
     */
    suspend fun select(id: Token.Register): Option<RegistrationToken>

    /**
     * Confirms a Registration Token in the persistence layer based on its ID.
     *
     * @param id The ID of the Registration Token to be confirmed.
     *
     * @return An Either containing the ID of the confirmed Registration Token if the operation was successful,
     * or a DomainError if it was not.
     */
    suspend fun confirm(id: Token.Register): Either<DomainError, Token.Register>
}
