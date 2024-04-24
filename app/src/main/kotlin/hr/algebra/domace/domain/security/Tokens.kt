package hr.algebra.domace.domain.security

import arrow.core.Either
import arrow.core.EitherNel
import hr.algebra.domace.domain.SecurityError

/**
 * An interface for managing tokens.
 *
 * This interface defines methods for generating a token from claims and extracting claims from a token.
 * The type of the claims and the token are defined by the implementing class.
 *
 * The interface defines the following methods:
 * - `generateToken`: Generates a token from the given claims. Returns an `Either` of `SecurityError` and `Token`.
 * - `extractClaims`: Extracts claims from the given token. Returns an `EitherNel` of `SecurityError` and `Claims`.
 *
 * All methods are suspending, meaning they can be used in a coroutine context.
 */
interface Tokens {
    /**
     * Generates a token from the given claims.
     *
     * This method is suspending and can be used in a coroutine context.
     *
     * @return An `Either` of `SecurityError` and `Token`. If the operation was successful, the `Either` will be
     * `Either.Right` with the generated `Token`. Otherwise, it will be `Either.Left` with the `SecurityError`.
     */
    suspend fun Claims.generateToken(): Either<SecurityError, Token>

    /**
     * Extracts claims from the given token.
     *
     * This method is suspending and can be used in a coroutine context.
     *
     * @return An `EitherNel` of `SecurityError` and `Claims`. If the operation was successful, the `EitherNel` will be
     * `EitherNel.Right` with the extracted `Claims`. Otherwise, it will be `EitherNel.Left` with the `SecurityError`.
     */
    suspend fun Token.extractClaims(): EitherNel<SecurityError, Claims>
}

context(Tokens)
suspend fun Claims.Refresh.generateRefreshToken(): Either<SecurityError, Token.Refresh> =
    generateToken().map { it as Token.Refresh }

context(Tokens)
suspend fun Claims.Access.generateAccessToken(): Either<SecurityError, Token.Access> =
    generateToken().map { it as Token.Access }
