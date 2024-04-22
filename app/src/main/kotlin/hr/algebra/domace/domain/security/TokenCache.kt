package hr.algebra.domace.domain.security

import arrow.core.Option
import hr.algebra.domace.domain.model.User

interface TokenCache {
    suspend fun put(token: Token, claims: User.Id)

    suspend fun get(token: Token): Option<User.Id>

    suspend fun revoke(token: Token)
}
