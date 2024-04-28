package hr.algebra.domace.infrastructure.security.auth

import hr.algebra.domace.domain.model.User

/**
 * Data class representing the context of an authorization.
 *
 * @property userId The ID of the user being authorized. This is an instance of `User.Id`.
 */
data class AuthorizationContext(val userId: User.Id)
