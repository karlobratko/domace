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
import hr.algebra.domace.domain.security.Token
import hr.algebra.domace.domain.security.Token.Access
import hr.algebra.domace.domain.security.Token.Refresh
import hr.algebra.domace.domain.security.TokenCache
import hr.algebra.domace.domain.toEitherNel
import hr.algebra.domace.domain.validation.ClaimsValidation
import hr.algebra.domace.domain.validation.RefreshTokenValidation
import hr.algebra.domace.domain.with
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Interface for JWT Token Service.
 *
 * This interface defines the contract for a service that handles operations related to JWT (JSON Web Tokens).
 * It includes methods for generating, verifying, refreshing, and revoking tokens.
 */
interface JwtTokenService {

    /**
     * Generates a pair of refresh and access tokens for a given authentication context.
     *
     * @param authContext The authentication context for which to generate the tokens.
     * @return Either a DomainError or a Pair of Refresh and Access tokens.
     */
    suspend fun generate(authContext: AuthContext): Either<DomainError, Pair<Refresh, Access>>

    /**
     * Verifies an access token.
     *
     * @param token The access token to verify.
     * @return Either a non-empty list of DomainErrors or an AuthContext.
     */
    suspend fun verify(token: Access): EitherNel<DomainError, AuthContext>

    /**
     * Refreshes a refresh token.
     *
     * @param token The refresh token to refresh.
     * @return Either a non-empty list of DomainErrors or a Pair of Refresh and Access tokens.
     */
    suspend fun refresh(token: Refresh): EitherNel<DomainError, Pair<Refresh, Access>>

    /**
     * Revokes a refresh token.
     *
     * @param token The refresh token to revoke.
     * @return Either a non-empty list of DomainErrors or a Refresh token.
     */
    suspend fun revoke(token: Refresh): EitherNel<DomainError, Refresh>
}

fun JwtTokenService(
    security: Security,
    algebra: Tokens,
    refreshTokenPersistence: RefreshTokenPersistence,
    accessTokenCache: TokenCache<AuthContext>
) = object : JwtTokenService {
    override suspend fun generate(authContext: AuthContext): Either<DomainError, Pair<Refresh, Access>> =
        either {
            val subject = with(UserIdToSubjectConversion) { authContext.userId.convert() }
            val role = with(RoleToRoleClaimConversion) { authContext.role.convert() }
            val audience = Claims.Audience(authContext.userId.value.toString()).nel()
            val issuedAt = Claims.IssuedAt()

            coroutineScope {
                with(algebra) {
                    val refreshTokenJob = async {
                        Claims.Refresh(security.issuer, subject, audience, issuedAt, security.refreshLasting, role)
                            .generateRefreshToken().bind()
                    }

                    val accessToken =
                        Claims.Access(security.issuer, subject, audience, issuedAt, security.accessLasting, role)
                            .generateAccessToken().bind()

                    val insertedRefreshToken = async {
                        refreshTokenPersistence.insert(
                            RefreshToken.New(
                                authContext.userId,
                                refreshTokenJob.await(),
                                issuedAt,
                                security.refreshLasting
                            )
                        ).bind()
                    }
                    accessTokenCache.put(accessToken, authContext)

                    insertedRefreshToken.await().token to accessToken
                }
            }
        }

    override suspend fun verify(token: Access): EitherNel<DomainError, AuthContext> =
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

    override suspend fun refresh(token: Refresh): EitherNel<DomainError, Pair<Refresh, Access>> =
        revokeTokenEntity(token)
            .flatMap { entity -> generate(AuthContext(entity)).toEitherNel() }

    override suspend fun revoke(token: Refresh): EitherNel<DomainError, Refresh> =
        revokeTokenEntity(token).map { entity -> entity.token }

    private suspend fun extractAndValidateClaims(token: Token): EitherNel<DomainError, Claims> =
        with(algebra) {
            token.extractClaims().flatMap { claims ->
                with(ClaimsValidation) {
                    claims.validate()
                }
            }
        }

    private suspend fun revokeTokenEntity(token: Refresh): EitherNel<DomainError, RefreshToken> =
        refreshTokenPersistence.select(token).toEitherNel { UnknownToken }
            .flatMap { entity ->
                with(RefreshTokenValidation(RefreshToken.Status.Active)) {
                    entity.validate()
                }
            }
            .flatMap { entity -> refreshTokenPersistence.revoke(entity.id).toEitherNel() }
}
