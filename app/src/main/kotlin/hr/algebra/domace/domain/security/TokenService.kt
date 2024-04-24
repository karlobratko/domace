package hr.algebra.domace.domain.security

import arrow.core.Either
import arrow.core.EitherNel
import hr.algebra.domace.domain.DomainError
import hr.algebra.domace.domain.model.User

interface TokenService {
    suspend fun generate(userId: User.Id): Either<DomainError, Token.Pair>

    suspend fun verify(token: Token.Access): EitherNel<DomainError, User.Id>

    suspend fun refresh(token: Token.Refresh): EitherNel<DomainError, Token.Pair>

    suspend fun revoke(token: Token.Refresh): EitherNel<DomainError, Token.Refresh>
}
