package hr.algebra.domace.infrastructure.security.authorization

import arrow.core.leftNel
import arrow.core.right
import hr.algebra.domace.domain.SecurityError.AuthorizationError.UnauthorizedResourceAccess
import hr.algebra.domace.domain.model.User
import hr.algebra.domace.domain.security.authorization.AuthorizationScope
import io.ktor.server.routing.Route

/**
 * Creates an AuthorizationScope based on user roles.
 *
 * This function takes a list of User.Role as a parameter.
 * It creates an AuthorizationScope that authorizes a user if the user's role is in the provided list of roles.
 * If the user's role is in the list, the function returns a Right of AuthenticationContext.
 * If the user's role is not in the list, the function returns a Left of UnauthorizedResourceAccess.
 *
 * @param roles The list of roles that are authorized.
 * @return An AuthorizationScope that authorizes a user based on their role.
 */
fun RoleBasedAuthorizationScope(roles: List<User.Role>) = AuthorizationScope {
    if (role in roles) right() else UnauthorizedResourceAccess.leftNel()
}

/**
 * Creates a Route with role-based authorization.
 *
 * This function takes a vararg of User.Role and a lambda function as parameters.
 * It creates a RoleBasedAuthorizationScope with the provided roles.
 * It then calls the lambda function with the AuthorizationScope as the receiver.
 * The lambda function can use the functions and properties of AuthorizationScope to create a Route.
 *
 * @param roles The roles that are authorized.
 * @param body A lambda function that defines the Route. This is an extension function for AuthorizationScope.
 * @return The created Route.
 */
fun role(vararg roles: User.Role, body: AuthorizationScope.() -> Route): Route {
    return with(RoleBasedAuthorizationScope(roles.toList())) {
        body()
    }
}
