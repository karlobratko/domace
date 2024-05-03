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
import hr.algebra.domace.domain.conversion.RoleClaimToRoleConversion
import hr.algebra.domace.domain.conversion.RoleToRoleClaimConversion
import hr.algebra.domace.domain.conversion.SubjectToUserIdConversion
import hr.algebra.domace.domain.conversion.UserIdToSubjectConversion
import hr.algebra.domace.domain.model.RefreshToken
import hr.algebra.domace.domain.persistence.RefreshTokenPersistence
import hr.algebra.domace.domain.security.AuthContext
import hr.algebra.domace.domain.security.Security
import hr.algebra.domace.domain.toEitherNel
import hr.algebra.domace.domain.validation.ClaimsValidation
import hr.algebra.domace.domain.validation.RefreshTokenValidation
import hr.algebra.domace.domain.with

fun JwtTokenService(
    security: Security,
    algebra: Tokens,
    refreshTokenPersistence: RefreshTokenPersistence,
    accessTokenCache: TokenCache<AuthContext>
) = object : TokenService {
    override suspend fun generate(authContext: AuthContext): Either<DomainError, Token.Pair> =
        either {
            val subject = with(UserIdToSubjectConversion) { authContext.userId.convert() }
            val role = with(RoleToRoleClaimConversion) { authContext.role.convert() }
            val audience = Claims.Audience(authContext.userId.value.toString()).nel()
            val issuedAt = Claims.IssuedAt()

            with(algebra) {
                val refreshToken =
                    Claims.Refresh(security.issuer, subject, audience, issuedAt, security.refreshLasting, role)
                        .generateRefreshToken().bind()

                val accessToken =
                    Claims.Access(security.issuer, subject, audience, issuedAt, security.accessLasting, role)
                        .generateAccessToken().bind()

                refreshTokenPersistence.insert(
                    RefreshToken.New(
                        authContext.userId,
                        refreshToken,
                        issuedAt,
                        security.refreshLasting
                    )
                ).bind()
                accessTokenCache.put(accessToken, authContext)

                Token.Pair(refresh = refreshToken, access = accessToken)
            }
        }

    override suspend fun verify(token: Token.Access): EitherNel<DomainError, AuthContext> =
        accessTokenCache.get(token)
            .fold(
                ifEmpty = {
                    extractAndValidateClaims(token)
                        .flatMap { claims ->
                            either {
                                with(SubjectToUserIdConversion, RoleClaimToRoleConversion) {
                                    AuthContext(
                                        claims.subject.convert().toEitherNel().bind(),
                                        claims.role.convert().toEitherNel().bind()

                                    )
                                }
                            }
                        }
                        .onRight { authParams -> accessTokenCache.put(token, authParams) }
                },
                ifSome = { userId -> userId.right() }
            )

    override suspend fun refresh(token: Token.Refresh): EitherNel<DomainError, Token.Pair> =
        revokeTokenEntity(token)
            .flatMap { entity -> generate(AuthContext(entity)).toEitherNel() }

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
        refreshTokenPersistence.select(token).toEitherNel { UnknownToken }
            .flatMap { entity ->
                with(RefreshTokenValidation(RefreshToken.Status.Active)) {
                    entity.validate()
                }
            }
            .flatMap { entity -> refreshTokenPersistence.revoke(entity.id).toEitherNel() }
}
