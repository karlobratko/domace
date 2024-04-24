package hr.algebra.domace.infrastructure.security.jwt

import arrow.core.nonEmptyListOf
import hr.algebra.domace.domain.model.RefreshToken
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
import hr.algebra.domace.infrastructure.remoteHost
import hr.algebra.domace.infrastructure.security.InMemoryTokenCache
import hr.algebra.domace.infrastructure.security.Secret
import io.kotest.assertions.arrow.core.shouldBeNone
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.arrow.core.shouldBeSome
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.should
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.util.pipeline.PipelineContext
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SchemaUtils.drop
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

private const val REQUEST_REMOTE_HOST = "remote"

object JwtTokenServiceTests : ShouldSpec({
    val pipelineContext = mockk<PipelineContext<Unit, ApplicationCall>> {
        every { call.request.remoteHost } answers { REQUEST_REMOTE_HOST }
    }

    val refreshTokenLasting = Token.Lasting(1.days)
    val accessTokenLasting = Token.Lasting(15.minutes)

    val config = JwtConfig(
        Claims.Issuer("domace"),
        refreshTokenLasting,
        accessTokenLasting
    )

    val algebra = JwtTokens(Secret("6d792d73757065722d7365637572652d736563726574"))

    val userPersistence = ExposedUserPersistence(test)

    val refreshTokenPersistence = ExposedRefreshTokenPersistence(test)

    lateinit var tokenCache: TokenCache

    lateinit var tokenService: TokenService

    beforeContainer {
        transaction(test) {
            create(UsersTable, RefreshTokensTable)
        }

        tokenCache = InMemoryTokenCache(accessTokenLasting)

        with(pipelineContext) {
            tokenService = JwtTokenService(
                config,
                algebra,
                refreshTokenPersistence,
                tokenCache
            )
        }
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

        should("remote host should be extracted from request and saved to audience claim of tokens") {
            val expectedAudience = nonEmptyListOf(Claims.Audience(REQUEST_REMOTE_HOST))

            verify(exactly = 1) { pipelineContext.call.request.remoteHost }

            with(algebra) {
                access.extractClaims()
                    .shouldBeRight()
                    .should {
                        it.audience shouldBeEqual expectedAudience
                    }

                refresh.extractClaims()
                    .shouldBeRight()
                    .should {
                        it.audience shouldBeEqual expectedAudience
                    }
            }
        }

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
})
