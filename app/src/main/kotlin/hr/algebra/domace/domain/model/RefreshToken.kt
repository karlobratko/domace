package hr.algebra.domace.domain.model

import hr.algebra.domace.domain.model.RefreshToken.Status.Active
import hr.algebra.domace.domain.security.Claims
import hr.algebra.domace.domain.security.Token

sealed interface RefreshToken {
    class New(
        val userId: User.Id,
        val token: Token.Refresh,
        val issuedAt: Claims.IssuedAt,
        val expiresAt: Claims.ExpiresAt,
        val status: Status = Active
    ) : RefreshToken {
        constructor(
            userId: User.Id,
            token: Token.Refresh,
            issuedAt: Claims.IssuedAt,
            lasting: Token.Lasting,
            status: Status = Active
        ) : this(userId, token, issuedAt, Claims.ExpiresAt(issuedAt.value + lasting.value), status)
    }

    class Prolong(
        val id: Id,
        val expiresAt: Claims.ExpiresAt
    ) : RefreshToken

    data class Entity(
        val id: Id,
        val userId: User.Id,
        val token: Token.Refresh,
        val issuedAt: Claims.IssuedAt,
        val expiresAt: Claims.ExpiresAt,
        val status: Status
    ) : RefreshToken

    @JvmInline value class Id(val value: Long)

    enum class Status { Active, Revoked }
}
