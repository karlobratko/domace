package hr.algebra.domace.domain.security

import arrow.core.Either
import arrow.core.EitherNel
import hr.algebra.domace.domain.DomainError
import hr.algebra.domace.domain.model.User

/**
 * An interface for managing tokens in a security context.
 *
 * This interface defines methods for generating, verifying, refreshing, and revoking tokens.
 * The type of the user ID and the token are defined by the implementing class.
 *
 * The interface defines the following methods:
 * - `generate`: Generates a token pair for the given user ID. Returns an `Either` of `DomainError` and `Token.Pair`.
 * - `verify`: Verifies an access token. Returns an `EitherNel` of `DomainError` and `User.Id`.
 * - `refresh`: Refreshes a refresh token. Returns an `EitherNel` of `DomainError` and `Token.Pair`.
 * - `revoke`: Revokes a refresh token. Returns an `EitherNel` of `DomainError` and `Token.Refresh`.
 *
 * All methods are suspending, meaning they can be used in a coroutine context.
 */
interface TokenService {
    /**
     * Generates a token pair for the given user ID.
     *
     * This method is suspending and can be used in a coroutine context.
     *
     * @param userId The user ID to generate the token pair for.
     *
     * @return An `Either` of `DomainError` and `Token.Pair`. If the operation was successful, the `Either` will be
     * `Either.Right` with the generated `Token.Pair`. Otherwise, it will be `Either.Left` with the `DomainError`.
     */
    suspend fun generate(userId: User.Id): Either<DomainError, Token.Pair>

    /**
     * Verifies an access token.
     *
     * This method is suspending and can be used in a coroutine context.
     *
     * @param token The access token to verify.
     *
     * @return An `EitherNel` of `DomainError` and `User.Id`. If the operation was successful, the `EitherNel` will be
     * `EitherNel.Right` with the `User.Id`. Otherwise, it will be `EitherNel.Left` with the `DomainError`.
     */
    suspend fun verify(token: Token.Access): EitherNel<DomainError, User.Id>

    /**
     * Refreshes a refresh token.
     *
     * This method is suspending and can be used in a coroutine context.
     *
     * @param token The refresh token to refresh.
     *
     * @return An `EitherNel` of `DomainError` and `Token.Pair`. If the operation was successful, the `EitherNel` will
     * be `EitherNel.Right` with the refreshed `Token.Pair`. Otherwise, it will be `EitherNel.Left` with
     * the `DomainError`.
     */
    suspend fun refresh(token: Token.Refresh): EitherNel<DomainError, Token.Pair>

    /**
     * Revokes a refresh token.
     *
     * This method is suspending and can be used in a coroutine context.
     *
     * @param token The refresh token to revoke.
     *
     * @return An `EitherNel` of `DomainError` and `Token.Refresh`. If the operation was successful, the `EitherNel`
     * will be `EitherNel.Right` with the revoked `Token.Refresh`. Otherwise, it will be `EitherNel.Left` with
     * the `DomainError`.
     */
    suspend fun revoke(token: Token.Refresh): EitherNel<DomainError, Token.Refresh>
}
