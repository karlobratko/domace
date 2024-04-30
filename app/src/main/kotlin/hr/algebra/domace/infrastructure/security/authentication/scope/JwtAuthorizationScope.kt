package hr.algebra.domace.infrastructure.security.authentication.scope

import arrow.core.flatMap
import arrow.core.leftNel
import arrow.core.right
import arrow.core.toOption
import hr.algebra.domace.domain.SecurityError.AuthenticationError.MissingAuthorizationHeader
import hr.algebra.domace.domain.SecurityError.AuthorizationError.UnknownSubjectResourceAccess
import hr.algebra.domace.domain.persistence.UserPersistence
import hr.algebra.domace.domain.security.Token
import hr.algebra.domace.domain.security.TokenService
import hr.algebra.domace.domain.toEitherNel
import hr.algebra.domace.infrastructure.ktor.authHeaderBlob
import hr.algebra.domace.infrastructure.security.authentication.AuthenticationContext
import io.ktor.server.routing.Route
import org.koin.core.qualifier.named
import org.koin.ktor.ext.get

/**
 * Creates an AuthenticationScope using JWT Authorization.
 *
 * This function takes a TokenService and a UserPersistence as parameters.
 * It uses the TokenService to verify the access token from the Authorization header.
 * If the token is valid, it uses the UserPersistence to select the user with the ID from the token.
 * If the user exists, it creates an AuthenticationContext with the user.
 * If the token is not valid or the user does not exist, it returns an error.
 *
 * @param tokenService The TokenService used to verify the access token.
 * @param userPersistence The UserPersistence used to select the user.
 * @return An AuthenticationScope that can be used to authenticate requests.
 */
fun JwtAuthorizationScope(tokenService: TokenService, userPersistence: UserPersistence) = AuthenticationScope {
    authHeaderBlob.toOption()
        .toEitherNel { MissingAuthorizationHeader }
        .map { Token.Access(it) }
        .flatMap { tokenService.verify(it) }
        .flatMap { id -> userPersistence.select(id).fold({ UnknownSubjectResourceAccess.leftNel() }, { it.right() }) }
        .map { AuthenticationContext(it) }
}

/**
 * Creates a Route using JWT Authorization.
 *
 * This function takes a TokenService, a UserPersistence, and a lambda function as parameters.
 * It creates an AuthenticationScope using the TokenService and the UserPersistence.
 * It then calls the lambda function with the AuthenticationScope as the receiver.
 * The lambda function can use the functions and properties of AuthenticationScope to create a Route.
 *
 * @param tokenService The TokenService used to verify the access token.
 * @param userPersistence The UserPersistence used to select the user.
 * @param body A lambda function that defines the Route. This is an extension function for AuthenticationScope.
 * @return The created Route.
 */
fun jwt(tokenService: TokenService, userPersistence: UserPersistence, body: AuthenticationScope.() -> Route): Route {
    return with(JwtAuthorizationScope(tokenService, userPersistence)) {
        body()
    }
}

/**
 * Creates a Route using JWT Authorization.
 *
 * This function takes a lambda function as a parameter.
 * It retrieves an AuthenticationScope named "jwt" from the Koin container.
 * It then calls the lambda function with the AuthenticationScope as the receiver.
 * The lambda function can use the functions and properties of AuthenticationScope to create a Route.
 *
 * @param body A lambda function that defines the Route. This is an extension function for AuthenticationScope.
 * @return The created Route.
 */
fun Route.jwt(body: AuthenticationScope.() -> Route): Route {
    return with(get<AuthenticationScope>(named("jwt"))) {
        body()
    }
}
