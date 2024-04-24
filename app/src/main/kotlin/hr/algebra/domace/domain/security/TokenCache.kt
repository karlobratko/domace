package hr.algebra.domace.domain.security

import arrow.core.Option

/**
 * An interface for a token cache.
 *
 * This interface defines methods for managing tokens in a cache.
 * The type of the subject associated with the tokens is generic and can be specified by the implementing class.
 *
 * The interface defines the following methods:
 * - `put`: Adds a token and its associated subject to the cache. Returns an `Option` of the old subject value.
 * - `get`: Retrieves the subject associated with a token from the cache. Returns an `Option` of the subject.
 * - `revoke`: Removes a token and its associated subject from the cache. Returns an `Option` of the old subject value.
 *
 * All methods are suspending, meaning they can be used in a coroutine context.
 *
 * @param Subject The type of the subject associated with the tokens.
 */
interface TokenCache<Subject> {
    /**
     * Adds a token and its associated subject to the cache.
     *
     * This method is suspending and can be used in a coroutine context.
     *
     * @param token The token to add to the cache.
     * @param claims The subject associated with the token.
     *
     * @return An `Option` of the subject. The `Option` should contain the old subject.Otherwise, it will be
     * `Option.None`.
     */
    suspend fun put(token: Token, claims: Subject): Option<Subject>

    /**
     * Retrieves the subject associated with a token from the cache.
     *
     * This method is suspending and can be used in a coroutine context.
     *
     * @param token The token to retrieve the subject for.
     *
     * @return An `Option` of the subject. If the token is in the cache, the `Option` will contain the subject.
     * Otherwise, it will be `Option.None`.
     */
    suspend fun get(token: Token): Option<Subject>

    /**
     * Removes a token and its associated subject from the cache.
     *
     * This method is suspending and can be used in a coroutine context.
     *
     * @param token The token to remove from the cache.
     *
     * @return An `Option` of the subject. If the token was in the cache, the `Option` will contain the subject.
     * Otherwise, it will be `Option.None`.
     */
    suspend fun revoke(token: Token): Option<Subject>
}
