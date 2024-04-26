package hr.algebra.domace.infrastructure.security.kjwt

import arrow.core.nel
import hr.algebra.domace.domain.SecurityError.InvalidRefreshTokenStatus
import hr.algebra.domace.domain.model.RefreshToken
import hr.algebra.domace.domain.model.User
import hr.algebra.domace.domain.security.Claims
import hr.algebra.domace.domain.security.Token
import hr.algebra.domace.domain.security.TokenCache
import hr.algebra.domace.domain.security.TokenService
import hr.algebra.domace.infrastructure.persistence.Database.test
import hr.algebra.domace.infrastructure.persistence.exposed.ExposedRefreshTokenPersistence
import hr.algebra.domace.infrastructure.persistence.exposed.ExposedUserPersistence
import hr.algebra.domace.infrastructure.persistence.exposed.RefreshTokensTable
import hr.algebra.domace.infrastructure.persistence.exposed.UsersTable
import hr.algebra.domace.infrastructure.persistence.exposed.insertUser
import hr.algebra.domace.infrastructure.security.InMemoryTokenCache
import hr.algebra.domace.domain.security.Secret
import hr.algebra.domace.domain.security.Security
import hr.algebra.domace.domain.security.jwt.JwtTokenService
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeNone
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.arrow.core.shouldBeSome
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.equals.shouldNotBeEqual
import io.kotest.matchers.should
import kotlinx.coroutines.delay
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SchemaUtils.drop
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object JwtTokenServiceTests : ShouldSpec({
    val refreshTokenLasting = Token.Lasting(1.days)
    val accessTokenLasting = Token.Lasting(15.minutes)

    val security = Security(
        Claims.Issuer("domace"),
        refreshTokenLasting,
        accessTokenLasting
    )

    val algebra = JwtTokens(Secret("6d792d73757065722d7365637572652d736563726574"))

    val userPersistence = ExposedUserPersistence(test)

    val refreshTokenPersistence = ExposedRefreshTokenPersistence(test)

    lateinit var tokenCache: TokenCache<User.Id>

    lateinit var tokenService: TokenService

    beforeContainer {
        transaction(test) {
            create(UsersTable, RefreshTokensTable)
        }

        tokenCache = InMemoryTokenCache(accessTokenLasting)

        tokenService = JwtTokenService(
            security,
            algebra,
            refreshTokenPersistence,
            tokenCache
        )
    }

    afterContainer {
        transaction(test) {
            drop(UsersTable, RefreshTokensTable)
        }
    }

    context("token pair generation") {
        val userId = insertUser(userPersistence).shouldBeRight().id

        should("user should already be persisted") {
            userPersistence.select(userId).shouldBeSome()
        }

        val (refresh, access) = tokenService.generate(userId).shouldBeRight()

        should("valid expireAt claims should be set based on token lasting configuration") {
            with(algebra) {
                access.extractClaims()
                    .shouldBeRight()
                    .should {
                        (it.expiresAt.value - it.issuedAt.value) shouldBeEqual accessTokenLasting.value
                    }

                refresh.extractClaims()
                    .shouldBeRight()
                    .should {
                        (it.expiresAt.value - it.issuedAt.value) shouldBeEqual refreshTokenLasting.value
                    }
            }
        }

        should("refresh token should be persisted to database") {
            refreshTokenPersistence.select(refresh)
                .shouldBeSome()
                .should {
                    it.userId shouldBeEqual userId
                    it.status shouldBeEqual RefreshToken.Status.Active
                    (it.expiresAt.value - it.issuedAt.value) shouldBeEqual refreshTokenLasting.value
                }
        }

        should("access token should be cached") {
            tokenCache.get(access) shouldBeSome userId
        }
    }

    context("cached access token verification") {
        val userId = insertUser(userPersistence).shouldBeRight().id

        should("user should already be persisted") {
            userPersistence.select(userId).shouldBeSome()
        }

        val (_, access) = tokenService.generate(userId).shouldBeRight()

        should("access token should be cached") {
            tokenCache.get(access) shouldBeSome userId
        }

        should("userId should be retrieved from cache") {
            tokenService.verify(access) shouldBeRight userId
        }
    }

    context("uncached access token verification") {
        val userId = insertUser(userPersistence).shouldBeRight().id

        should("user should already be persisted") {
            userPersistence.select(userId).shouldBeSome()
        }

        val (_, access) = tokenService.generate(userId).shouldBeRight()

        tokenCache.revoke(access)

        should("access token should not be cached") {
            tokenCache.get(access).shouldBeNone()
        }

        should("userId should be extracted from access token") {
            tokenService.verify(access) shouldBeRight userId
        }

        should("userId should be saved to token cache") {
            tokenCache.get(access) shouldBeSome userId
        }
    }

    context("successful token refresh") {
        val userId = insertUser(userPersistence).shouldBeRight().id

        should("user should already be persisted") {
            userPersistence.select(userId).shouldBeSome()
        }

        val (refresh, access) = tokenService.generate(userId).shouldBeRight()

        should("refresh token should be persisted") {
            refreshTokenPersistence.select(refresh).shouldBeSome()
        }

        // we should wait 1 second since only difference in tokens is time
        delay(1.seconds)

        val (newRefresh, newAccess) = tokenService.refresh(refresh).shouldBeRight()

        should("refresh token should be rotated and access regenerated") {
            newRefresh shouldNotBeEqual refresh
            newAccess shouldNotBeEqual access
        }

        should("revoke old refresh token and insert new refresh token") {
            refreshTokenPersistence.select(newRefresh).shouldBeSome()
            refreshTokenPersistence.select(refresh)
                .shouldBeSome()
                .should {
                    it.status shouldBeEqual RefreshToken.Status.Revoked
                }
        }
    }

    context("revoked token refresh") {
        val userId = insertUser(userPersistence).shouldBeRight().id

        should("user should already be persisted") {
            userPersistence.select(userId).shouldBeSome()
        }

        val (refresh, _) = tokenService.generate(userId).shouldBeRight()

        should("refresh token should be persisted and revoked") {
            val entity = refreshTokenPersistence.select(refresh).shouldBeSome()

            refreshTokenPersistence.revoke(entity.id).shouldBeRight()
            refreshTokenPersistence.select(refresh)
                .shouldBeSome()
                .should {
                    it.status shouldBeEqual RefreshToken.Status.Revoked
                }
        }

        // we should wait 1 second since only difference in tokens is time
        delay(1.seconds)

        should("refreshing should result in security error") {
            tokenService.refresh(refresh) shouldBeLeft InvalidRefreshTokenStatus.nel()
        }
    }

    context("successful token revoke") {
        val userId = insertUser(userPersistence).shouldBeRight().id

        should("user should already be persisted") {
            userPersistence.select(userId).shouldBeSome()
        }

        val (refresh, _) = tokenService.generate(userId).shouldBeRight()

        should("refresh token should be persisted") {
            refreshTokenPersistence.select(refresh).shouldBeSome()
        }

        val revokedRefresh = tokenService.revoke(refresh).shouldBeRight()

        should("after revoking token should be marked as revoked") {
            refreshTokenPersistence.select(revokedRefresh)
                .shouldBeSome()
                .should {
                    it.status shouldBeEqual RefreshToken.Status.Revoked
                }
        }
    }

    context("revoking already revoked token") {
        val userId = insertUser(userPersistence).shouldBeRight().id

        should("user should already be persisted") {
            userPersistence.select(userId).shouldBeSome()
        }

        val (refresh, _) = tokenService.generate(userId).shouldBeRight()

        should("refresh token should be persisted and revoked") {
            val entity = refreshTokenPersistence.select(refresh).shouldBeSome()

            refreshTokenPersistence.revoke(entity.id).shouldBeRight()
            refreshTokenPersistence.select(refresh)
                .shouldBeSome()
                .should {
                    it.status shouldBeEqual RefreshToken.Status.Revoked
                }
        }

        should("revoking should result in security error") {
            tokenService.revoke(refresh) shouldBeLeft InvalidRefreshTokenStatus.nel()
        }
    }
})
