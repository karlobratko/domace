package hr.algebra.domace.infrastructure.security.authentication

import arrow.core.flatMap
import arrow.core.toOption
import hr.algebra.domace.domain.SecurityError.AuthenticationError.MissingAuthorizationHeader
import hr.algebra.domace.domain.security.Token
import hr.algebra.domace.domain.security.authentication.AuthenticationScope
import hr.algebra.domace.domain.security.jwt.JwtTokenService
import hr.algebra.domace.domain.toEitherNel
import hr.algebra.domace.infrastructure.ktor.authHeaderBlob
import io.ktor.server.routing.Route
import org.koin.core.qualifier.named
import org.koin.ktor.ext.get

/**
 * Creates an AuthenticationScope using a provided TokenService.
 * The scope is created by converting the authHeaderBlob to an Option, then to an EitherNel.
 * If the authHeaderBlob is missing, a MissingAuthorizationHeader error is returned.
 * If the authHeaderBlob is present, it is converted to a Token.Access and verified using the TokenService.
 * If the verification is successful, an AuthenticationContext is created with the verified token.
 *
 * @param jwtTokenService The TokenService to use for verifying the token.
 * @return An AuthenticationScope.
 */
fun JwtAuthenticationScope(jwtTokenService: JwtTokenService) = AuthenticationScope {
    authHeaderBlob.toOption()
        .toEitherNel { MissingAuthorizationHeader }
        .map { Token.Access(it) }
        .flatMap { jwtTokenService.verify(it) }
}

/**
 * Creates a Route using a provided TokenService and a body function.
 * The Route is created by creating a JwtAuthorizationScope with the TokenService and invoking the body function
 * within this scope.
 *
 * @param jwtTokenService The TokenService to use for creating the JwtAuthorizationScope.
 * @param body The function to invoke within the JwtAuthorizationScope.
 * @return A Route.
 */
fun jwt(jwtTokenService: JwtTokenService, body: AuthenticationScope.() -> Route): Route {
    return with(JwtAuthenticationScope(jwtTokenService)) {
        body()
    }
}

/**
 * Extension function on Route to create a new Route using a body function.
 * The new Route is created by retrieving an AuthenticationScope named "jwt" and invoking the body function
 * within this scope.
 *
 * @param body The function to invoke within the AuthenticationScope.
 * @return A Route.
 */
fun Route.jwt(body: AuthenticationScope.() -> Route): Route {
    return with(get<AuthenticationScope>(named("jwt"))) {
        body()
    }
}
