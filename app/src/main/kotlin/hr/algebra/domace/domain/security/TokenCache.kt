package hr.algebra.domace.domain.security

import arrow.core.Option

/**
 * Interface for a cache that stores tokens.
 *
 * @param T The type of the value associated with each token in the cache.
 */
interface TokenCache<T> {

    /**
     * Inserts a token-value pair into the cache.
     *
     * This method is suspending and can be used in a coroutine context.
     *
     * @param token The token to insert into the cache.
     * @param value The value to associate with the token.
     *
     * @return An `Option` of `T`. If the operation was successful, the `Option` will be `Option.Some` with the
     * value `T`.
     * Otherwise, it will be `Option.None`.
     */
    suspend fun put(token: Token, value: T): Option<T>

    /**
     * Retrieves the value associated with a token from the cache.
     *
     * This method is suspending and can be used in a coroutine context.
     *
     * @param token The token to retrieve the value for.
     *
     * @return An `Option` of `T`. If the token is found in the cache, the `Option` will be `Option.Some` with the
     * value `T`.
     * Otherwise, it will be `Option.None`.
     */
    suspend fun get(token: Token): Option<T>

    /**
     * Revokes a token from the cache.
     *
     * This method is suspending and can be used in a coroutine context.
     *
     * @param token The token to revoke.
     *
     * @return An `Option` of `T`. If the token was found and revoked, the `Option` will be `Option.Some` with the
     * value `T`.
     * Otherwise, it will be `Option.None`.
     */
    suspend fun revoke(token: Token): Option<T>
}
