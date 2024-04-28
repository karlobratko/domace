package hr.algebra.domace.infrastructure.security.auth.scope

import arrow.core.EitherNel
import hr.algebra.domace.domain.DomainError
import hr.algebra.domace.infrastructure.security.auth.AuthorizationContext
import io.ktor.server.request.ApplicationRequest

/**
 * Functional interface for defining an authorization scope.
 *
 * This interface defines a single function, authorize, which is a suspending function.
 * This means it can be used in a coroutine context.
 * The function takes an ApplicationRequest and returns an EitherNel object.
 * This object represents a value which is either a non-empty list of DomainError objects, or an AuthorizationContext.
 * This is used to handle errors in a functional way.
 */
fun interface AuthorizationScope {
    /**
     * Suspends function that authorizes an ApplicationRequest.
     *
     * @receiver ApplicationRequest The request to authorize.
     * @return Either a non-empty list of DomainError objects if the authorization failed, or an AuthorizationContext
     * if it succeeded.
     */
    suspend fun ApplicationRequest.authorize(): EitherNel<DomainError, AuthorizationContext>
}
