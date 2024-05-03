package hr.algebra.domace.domain.persistence

import arrow.core.Either
import arrow.core.Option
import hr.algebra.domace.domain.DomainError
import hr.algebra.domace.domain.model.RefreshToken
import hr.algebra.domace.domain.security.jwt.Token
import kotlin.time.Duration

/**
 * Interface for the persistence operations related to Refresh Tokens.
 */
interface RefreshTokenPersistence {
    /**
     * Inserts a new refresh token into the persistence layer.
     *
     * @param refreshToken The new refresh token to be inserted.
     * @return Either a DomainError or the inserted RefreshToken Entity.
     */
    suspend fun insert(refreshToken: RefreshToken.New): Either<DomainError, RefreshToken.Entity>

    /**
     * Selects a refresh token from the persistence layer based on its ID.
     *
     * @param id The ID of the refresh token to be selected.
     * @return An Option of RefreshToken Entity.
     */
    suspend fun select(id: RefreshToken.Id): Option<RefreshToken.Entity>

    /**
     * Selects a refresh token from the persistence layer based on its token.
     *
     * @param token The token of the refresh token to be selected.
     * @return An Option of RefreshToken Entity.
     */
    suspend fun select(token: Token.Refresh): Option<RefreshToken.Entity>

    /**
     * Revokes a refresh token in the persistence layer based on its ID.
     *
     * @param tokenId The ID of the refresh token to be revoked.
     * @return Either a DomainError or the revoked RefreshToken Entity.
     */
    suspend fun revoke(tokenId: RefreshToken.Id): Either<DomainError, RefreshToken.Entity>

    /**
     * Prolongs the validity of a refresh token in the persistence layer.
     *
     * @param prolong The refresh token to be prolonged.
     * @return Either a DomainError or the prolonged RefreshToken Entity.
     */
    suspend fun prolong(prolong: RefreshToken.Prolong): Either<DomainError, RefreshToken.Entity>

    /**
     * Revokes all expired refresh tokens in the persistence layer.
     *
     * @return A Set of revoked RefreshToken Entities.
     */
    suspend fun revokeExpired(): Set<RefreshToken.Entity>

    /**
     * Deletes a refresh token from the persistence layer based on its ID.
     *
     * @param id The ID of the refresh token to be deleted.
     * @return Either a DomainError or the ID of the deleted refresh token.
     */
    suspend fun delete(id: RefreshToken.Id): Either<DomainError, RefreshToken.Id>

    /**
     * Deletes all expired refresh tokens for a certain duration from the persistence layer.
     *
     * @param duration The duration for which the refresh tokens are expired.
     * @return A Set of deleted RefreshToken Entities.
     */
    suspend fun deleteExpiredFor(duration: Duration): Set<RefreshToken.Entity>
}
