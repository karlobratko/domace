package hr.algebra.domace.domain.security.jwt

import arrow.core.Either
import arrow.core.EitherNel
import arrow.core.flatMap
import arrow.core.nel
import arrow.core.raise.either
import arrow.core.right
import arrow.core.toEitherNel
import hr.algebra.domace.domain.DomainError
import hr.algebra.domace.domain.SecurityError.UnknownToken
import hr.algebra.domace.domain.conversion.SubjectToUserIdConversion
import hr.algebra.domace.domain.conversion.UserIdToSubjectConversion
import hr.algebra.domace.domain.model.RefreshToken
import hr.algebra.domace.domain.model.User
import hr.algebra.domace.domain.persistence.RefreshTokenPersistence
import hr.algebra.domace.domain.security.Claims
import hr.algebra.domace.domain.security.Token
import hr.algebra.domace.domain.security.TokenCache
import hr.algebra.domace.domain.security.TokenService
import hr.algebra.domace.domain.security.Tokens
import hr.algebra.domace.domain.security.generateAccessToken
import hr.algebra.domace.domain.security.generateRefreshToken
import hr.algebra.domace.domain.toEitherNel
import hr.algebra.domace.domain.validation.ClaimsValidation
import hr.algebra.domace.domain.validation.RefreshTokenValidation
import hr.algebra.domace.domain.security.Security

fun JwtTokenService(
    security: Security,
    algebra: Tokens,
    refreshTokenPersistence: RefreshTokenPersistence,
    accessTokenCache: TokenCache<User.Id>
) = object : TokenService {
    override suspend fun generate(userId: User.Id): Either<DomainError, Token.Pair> =
        either {
            val subject = with(UserIdToSubjectConversion) { userId.convert() }
            val audience = Claims.Audience(userId.value.toString()).nel()
            val issuedAt = Claims.IssuedAt()

            with(algebra) {
                val refreshToken =
                    Claims.Refresh(security.issuer, subject, audience, issuedAt, security.refreshLasting)
                        .generateRefreshToken().bind()

                val accessToken =
                    Claims.Access(security.issuer, subject, audience, issuedAt, security.accessLasting)
                        .generateAccessToken().bind()

                refreshTokenPersistence.insert(
                    RefreshToken.New(
                        userId,
                        refreshToken,
                        issuedAt,
                        security.refreshLasting
                    )
                ).bind()
                accessTokenCache.put(accessToken, userId)

                Token.Pair(refresh = refreshToken, access = accessToken)
            }
        }

    override suspend fun verify(token: Token.Access): EitherNel<DomainError, User.Id> =
        accessTokenCache.get(token)
            .fold(
                ifEmpty = {
                    extractAndValidateClaims(token)
                        .flatMap { claims ->
                            with(SubjectToUserIdConversion) {
                                claims.subject.convert().toEitherNel()
                            }
                        }
                        .onRight { userId ->
                            accessTokenCache.put(token, userId)
                        }
                },
                ifSome = { userId -> userId.right() }
            )

    override suspend fun refresh(token: Token.Refresh): EitherNel<DomainError, Token.Pair> =
        revokeTokenEntity(token).flatMap { entity -> generate(entity.userId).toEitherNel() }

    override suspend fun revoke(token: Token.Refresh): EitherNel<DomainError, Token.Refresh> =
        revokeTokenEntity(token).map { entity -> entity.token }

    private suspend fun extractAndValidateClaims(token: Token): EitherNel<DomainError, Claims> =
        with(algebra) {
            token.extractClaims().flatMap { claims ->
                with(ClaimsValidation) {
                    claims.validate()
                }
            }
        }

    private suspend fun revokeTokenEntity(token: Token.Refresh): EitherNel<DomainError, RefreshToken.Entity> =
        extractAndValidateClaims(token)
            .flatMap { refreshTokenPersistence.select(token).toEitherNel { UnknownToken } }
            .flatMap { entity ->
                with(RefreshTokenValidation(RefreshToken.Status.Active)) {
                    entity.validate()
                }
            }
            .flatMap { entity -> refreshTokenPersistence.revoke(entity.id).toEitherNel() }
}
