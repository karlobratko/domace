package hr.algebra.domace.infrastructure.security.authorization.scope

import arrow.core.EitherNel
import hr.algebra.domace.domain.DomainError
import hr.algebra.domace.infrastructure.security.authentication.AuthenticationContext

/**
 * Functional interface representing an authorization scope.
 *
 * This interface defines a single function, authorize, which is used to authorize an authenticated user.
 * The function takes an AuthenticationContext as a receiver, which means it can use the functions and properties of
 * AuthenticationContext.
 * The function is a suspending function, which means it can be used in a coroutine context.
 * The function returns an EitherNel of DomainError and AuthenticationContext. If the authorization is successful, it
 * returns a Right of AuthenticationContext.
 * If the authorization fails, it returns a Left of NonEmptyList of DomainError.
 */
fun interface AuthorizationScope {
    /**
     * Authorizes an authenticated user.
     *
     * This function is an extension function for AuthenticationContext, which means it can use the functions and
     * properties of AuthenticationContext.
     * The function is a suspending function, which means it can be used in a coroutine context.
     * The function returns an EitherNel of DomainError and AuthenticationContext. If the authorization is successful,
     * it returns a Right of AuthenticationContext.
     * If the authorization fails, it returns a Left of NonEmptyList of DomainError.
     *
     * @return An EitherNel of DomainError and AuthenticationContext.
     */
    suspend fun AuthenticationContext.authorize(): EitherNel<DomainError, AuthenticationContext>
}
