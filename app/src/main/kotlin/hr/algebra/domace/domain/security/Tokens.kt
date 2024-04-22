package hr.algebra.domace.domain.security

import arrow.core.Either
import arrow.core.EitherNel
import hr.algebra.domace.domain.SecurityError

interface Tokens {
    suspend fun Claims.generateToken(): Either<SecurityError, Token>

    suspend fun Token.extractClaims(): EitherNel<SecurityError, Claims>
}

context(Tokens)
suspend fun Claims.Refresh.generateRefreshToken(): Either<SecurityError, Token.Refresh> =
    generateToken().map { it as Token.Refresh }

context(Tokens)
suspend fun Claims.Access.generateAccessToken(): Either<SecurityError, Token.Access> =
    generateToken().map { it as Token.Access }
