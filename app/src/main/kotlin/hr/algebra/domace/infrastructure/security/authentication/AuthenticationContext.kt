package hr.algebra.domace.infrastructure.security.authentication

import hr.algebra.domace.domain.model.User

/**
 * Data class representing the context of an authenticated user.
 *
 * This class holds the authenticated user's information. It is used in the authentication process
 * to pass the authenticated user's data to the routes that require authentication.
 *
 * @property user The authenticated user. This is an instance of User.Entity.
 */
data class AuthenticationContext(val user: User.Entity)
