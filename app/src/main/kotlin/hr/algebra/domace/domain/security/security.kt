package hr.algebra.domace.domain.security

import hr.algebra.domace.domain.model.RefreshToken
import hr.algebra.domace.domain.model.User
import hr.algebra.domace.domain.security.jwt.Claims
import hr.algebra.domace.domain.security.jwt.Token

/**
 * Represents the security configuration for JWT tokens.
 *
 * @property issuer The issuer of the JWT tokens.
 * @property refreshLasting The duration for which the refresh token lasts.
 * @property accessLasting The duration for which the access token lasts.
 */
data class Security(
    val issuer: Claims.Issuer,
    val refreshLasting: Token.Lasting,
    val accessLasting: Token.Lasting
)

/**
 * Represents a secret value.
 *
 * @property value The secret value.
 */
@JvmInline value class Secret(val value: String)

/**
 * Represents the authentication context for a user.
 *
 * @property userId The ID of the user.
 * @property role The role of the user.
 *
 * @constructor Creates an instance of AuthContext from a User entity.
 * @param user The User entity.
 *
 * @constructor Creates an instance of AuthContext from a RefreshToken entity.
 * @param refreshToken The RefreshToken entity.
 */
data class AuthContext(val userId: User.Id, val role: User.Role) {
    constructor(user: User.Entity) : this(user.id, user.role)

    constructor(refreshToken: RefreshToken.Entity) : this(refreshToken.userId, refreshToken.userRole)
}
