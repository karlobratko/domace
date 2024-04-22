package hr.algebra.domace.infrastructure.security.jwt

import arrow.core.Either
import arrow.core.EitherNel
import arrow.core.flatMap
import arrow.core.nonEmptyListOf
import arrow.core.raise.either
import arrow.core.right
import arrow.core.toEitherNel
import hr.algebra.domace.domain.DomainError
import hr.algebra.domace.domain.SecurityError.UnknownToken
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
import hr.algebra.domace.infrastructure.remoteHost
import hr.algebra.domace.infrastructure.security.SubjectToUserIdConversion
import hr.algebra.domace.infrastructure.security.UserIdToSubjectConversion
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.util.pipeline.PipelineContext

data class JwtConfig(
    val issuer: Claims.Issuer,
    val refreshLasting: Token.Lasting,
    val accessLasting: Token.Lasting
)

context(PipelineContext<Unit, ApplicationCall>)
fun JwtTokenService(
    config: JwtConfig,
    algebra: Tokens,
    refreshTokenPersistence: RefreshTokenPersistence,
    accessTokenCache: TokenCache
) = object : TokenService {
    override suspend fun generate(userId: User.Id): Either<DomainError, Token.Pair> =
        either {
            val subject = with(UserIdToSubjectConversion) { userId.convert() }
            val audience = nonEmptyListOf(Claims.Audience(call.request.remoteHost))
            val issuedAt = Claims.IssuedAt()

            with(algebra) {
                val refreshToken =
                    Claims.Refresh(config.issuer, subject, audience, issuedAt, config.refreshLasting)
                        .generateRefreshToken().bind()

                val accessToken =
                    Claims.Access(config.issuer, subject, audience, issuedAt, config.accessLasting)
                        .generateAccessToken().bind()

                refreshTokenPersistence.insert(RefreshToken.New(userId, refreshToken, issuedAt, config.refreshLasting))
                    .bind()
                accessTokenCache.put(accessToken, userId)

                Token.Pair(refresh = refreshToken, access = accessToken)
            }
        }

    override suspend fun verify(token: Token.Access): EitherNel<DomainError, User.Id> =
        accessTokenCache.get(token)
            .fold(
                ifEmpty = {
                    extractAndValidateClaims(token)
                        .map {
                            with(SubjectToUserIdConversion) {
                                it.subject.convert()
                            }
                        }
                },
                ifSome = { it.right() }
            )

    override suspend fun refresh(token: Token.Refresh): EitherNel<DomainError, Token.Pair> =
        validateClaims(token)
            .flatMap { selectAndRevokeToken(token) }
            .flatMap { generate(it.userId).toEitherNel() }

    override suspend fun revoke(token: Token.Refresh): EitherNel<DomainError, Token.Refresh> =
        validateClaims(token)
            .flatMap { selectAndRevokeToken(token) }
            .map { it.token }

    private suspend fun extractAndValidateClaims(token: Token): EitherNel<DomainError, Claims> =
        with(algebra) {
            token.extractClaims().flatMap {
                with(ClaimsValidation) {
                    it.validate()
                }
            }
        }

    private suspend fun validateClaims(token: Token): EitherNel<DomainError, Unit> =
        extractAndValidateClaims(token).map { }

    private suspend fun selectAndRevokeToken(token: Token.Refresh): EitherNel<DomainError, RefreshToken.Entity> =
        refreshTokenPersistence.select(token).toEitherNel { UnknownToken }
            .flatMap {
                with(RefreshTokenValidation(RefreshToken.Status.Active)) {
                    it.validate()
                }
            }
            .flatMap { refreshTokenPersistence.revoke(it.id).toEitherNel() }
}
