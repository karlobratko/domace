package hr.algebra.domace.infrastructure.security.auth.scope

import arrow.core.flatMap
import arrow.core.toOption
import hr.algebra.domace.domain.SecurityError
import hr.algebra.domace.domain.security.Token
import hr.algebra.domace.domain.security.TokenService
import hr.algebra.domace.domain.toEitherNel
import hr.algebra.domace.infrastructure.authHeaderBlob
import hr.algebra.domace.infrastructure.security.auth.AuthorizationContext
import io.ktor.server.routing.Route
import org.koin.core.qualifier.named
import org.koin.ktor.ext.get

/**
 * Function that creates an AuthorizationScope using a JWT token.
 *
 * This function takes a TokenService as a parameter and returns an AuthorizationScope.
 * The AuthorizationScope is created by a lambda function that:
 * - Extracts the authorization header from the request.
 * - Converts the header to an EitherNel object, which is either a MissingAuthorizationHeader error or the header
 * itself.
 * - Maps the header to an Access token.
 * - Verifies the token using the provided TokenService.
 * - Maps the verified token to an AuthorizationContext.
 *
 * @param tokenService The TokenService used to verify the token.
 * @return An AuthorizationScope that authorizes a request using a JWT token.
 */
fun JwtAuthorizationScope(tokenService: TokenService) = AuthorizationScope {
    authHeaderBlob.toOption()
        .toEitherNel { SecurityError.MissingAuthorizationHeader }
        .map { Token.Access(it) }
        .flatMap { tokenService.verify(it) }
        .map { AuthorizationContext(it) }
}

/**
 * Function that creates a new route using JWT-based authorization.
 *
 * This function takes a TokenService and a lambda function as parameters. The lambda function defines the route.
 * The function creates an AuthorizationScope using the provided TokenService and JWT token.
 * It then calls the lambda function with the AuthorizationScope as the receiver, creating the route.
 *
 * @param tokenService The TokenService used to verify the token.
 * @param body A lambda function that defines the route. This is an extension function for AuthorizationScope.
 * @return The created Route.
 */
fun jwt(tokenService: TokenService, body: AuthorizationScope.() -> Route): Route {
    return with(JwtAuthorizationScope(tokenService)) {
        body()
    }
}

/**
 * Extension function for Route that creates a new route using JWT-based authorization.
 *
 * This function takes a lambda function as a parameter, which defines the route.
 * The lambda function is an extension function for AuthorizationScope, which means it can use the functions and
 * properties of AuthorizationScope.
 * The function uses the Koin dependency injection framework to get an instance of AuthorizationScope with the name
 * "jwt".
 * It then calls the lambda function with the AuthorizationScope as the receiver, creating the route.
 *
 * @param body A lambda function that defines the route. This is an extension function for AuthorizationScope.
 * @return The created Route.
 */
fun Route.jwt(body: AuthorizationScope.() -> Route): Route {
    return with(get<AuthorizationScope>(named("jwt"))) {
        body()
    }
}
