package hr.algebra.domace.domain.persistence

import arrow.core.Either
import arrow.core.Option
import hr.algebra.domace.domain.DomainError
import hr.algebra.domace.domain.model.RefreshToken
import hr.algebra.domace.domain.security.Token
import kotlin.time.Duration

interface RefreshTokenPersistence {
    suspend fun insert(refreshToken: RefreshToken.New): Either<DomainError, RefreshToken.Entity>

    suspend fun select(id: RefreshToken.Id): Option<RefreshToken.Entity>

    suspend fun select(token: Token.Refresh): Option<RefreshToken.Entity>

    suspend fun revoke(tokenId: RefreshToken.Id): Either<DomainError, RefreshToken.Entity>

    suspend fun prolong(prolong: RefreshToken.Prolong): Either<DomainError, RefreshToken.Entity>

    suspend fun revokeExpired(): Set<RefreshToken.Entity>

    suspend fun delete(id: RefreshToken.Id): Either<DomainError, RefreshToken.Id>

    suspend fun deleteExpiredFor(duration: Duration): Set<RefreshToken.Entity>
}
