package hr.algebra.domace.infrastructure.persistence.exposed

import arrow.core.Either
import hr.algebra.domace.domain.DbError.NothingWasChanged
import hr.algebra.domace.domain.DbError.TokenAlreadyExists
import hr.algebra.domace.domain.DomainError
import hr.algebra.domace.domain.config.RoundedInstantProvider
import hr.algebra.domace.domain.model.RefreshToken
import hr.algebra.domace.domain.model.User
import hr.algebra.domace.domain.persistence.RefreshTokenPersistence
import hr.algebra.domace.domain.security.LastingFor
import hr.algebra.domace.domain.security.Token
import hr.algebra.domace.domain.security.jwt.Claims
import hr.algebra.domace.infrastructure.persistence.Database.test
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeNone
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.arrow.core.shouldBeSome
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.should
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SchemaUtils.drop
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration.Companion.minutes

object ExposedRefreshTokenPersistenceTests : ShouldSpec({
    val registrationTokenPersistence = ExposedRegistrationTokenPersistence(
        test,
        RegistrationConfig(LastingFor(15.minutes))
    )
    val userPersistence = ExposedUserPersistence(test, registrationTokenPersistence)
    val persistence = ExposedRefreshTokenPersistence(test)

    beforeEach {
        transaction(test) {
            create(RegistrationTokensTable, UsersTable, RefreshTokensTable)
        }
    }

    afterEach {
        transaction(test) {
            drop(RegistrationTokensTable, UsersTable, RefreshTokensTable)
        }
    }

    should("insert to database") {
        val user = insertUser(userPersistence).shouldBeRight()
        val token = Token.Refresh("token")
        val issuedAt = Claims.IssuedAt()
        val lasting = LastingFor(15.minutes)
        val expiresAt = Claims.ExpiresAt(issuedAt.value + lasting.value)

        val actual = insertRefreshToken(
            persistence,
            userId = user.id,
            token = token,
            issuedAt = issuedAt,
            lasting = lasting
        ).shouldBeRight()

        actual.should {
            it.userId shouldBeEqual user.id
            it.token shouldBeEqual token
            it.issuedAt shouldBeEqual issuedAt
            it.expiresAt shouldBeEqual expiresAt
            it.status shouldBeEqual RefreshToken.Status.Active
            it.userRole shouldBeEqual user.role
        }
    }

    should("fail if same token is inserted") {
        val userId = insertUser(userPersistence).shouldBeRight().id

        val inserted1 = insertRefreshToken(
            persistence,
            userId = userId,
            token = Token.Refresh("token"),
            issuedAt = Claims.IssuedAt(),
            lasting = LastingFor(15.minutes),
        ).shouldBeRight()

        persistence.select(inserted1.token).shouldBeSome()

        insertRefreshToken(
            persistence,
            userId = userId,
            token = Token.Refresh("token"),
            issuedAt = Claims.IssuedAt(),
            lasting = LastingFor(15.minutes),
        ) shouldBeLeft TokenAlreadyExists
    }

    should("return none if record with token not found") {
        persistence.select(Token.Refresh("token")).shouldBeNone()
    }

    should("return user if record with token found") {
        val inserted = insertRefreshToken(
            persistence,
            userId = insertUser(userPersistence).shouldBeRight().id,
            token = Token.Refresh("token"),
            issuedAt = Claims.IssuedAt(),
            lasting = LastingFor(15.minutes),
        ).shouldBeRight()

        persistence.select(inserted.token) shouldBeSome inserted
    }

    should("revoke token by id") {
        val inserted = insertRefreshToken(
            persistence,
            userId = insertUser(userPersistence).shouldBeRight().id,
            token = Token.Refresh("token"),
            issuedAt = Claims.IssuedAt(),
            lasting = LastingFor(15.minutes),
        ).shouldBeRight()

        inserted.status shouldBeEqual RefreshToken.Status.Active

        val revoked = persistence.revoke(inserted.id).shouldBeRight()

        revoked.status shouldBeEqual RefreshToken.Status.Revoked
    }

    should("fail revoke if record was not found by id") {
        persistence.revoke(RefreshToken.Id(1)) shouldBeLeft NothingWasChanged
    }

    should("prolong token by id") {
        val issuedAt = Claims.IssuedAt()
        val lasting = LastingFor(15.minutes)

        val inserted = insertRefreshToken(
            persistence,
            userId = insertUser(userPersistence).shouldBeRight().id,
            token = Token.Refresh("token"),
            issuedAt = issuedAt,
            lasting = lasting,
        ).shouldBeRight()

        inserted.expiresAt shouldBeEqual Claims.ExpiresAt(issuedAt.value + lasting.value)

        val newExpiresAt = Claims.ExpiresAt(issuedAt.value + 20.minutes)

        val prolonged = persistence.prolong(
            RefreshToken.Prolong(
                inserted.id,
                newExpiresAt
            )
        ).shouldBeRight()

        prolonged.expiresAt shouldBeEqual newExpiresAt
    }

    should("fail prolong if record was not found by id") {
        persistence.prolong(
            RefreshToken.Prolong(
                RefreshToken.Id(1),
                Claims.ExpiresAt(RoundedInstantProvider.now() + 15.minutes)
            )
        ) shouldBeLeft NothingWasChanged
    }

    should("revoke expired") {
        val userId = insertUser(userPersistence).shouldBeRight().id

        val inserted1 = insertRefreshToken(
            persistence,
            userId = userId,
            token = Token.Refresh("token1"),
            issuedAt = Claims.IssuedAt(),
            lasting = LastingFor(15.minutes),
        ).shouldBeRight()

        persistence.select(inserted1.token).shouldBeSome()

        val inserted2 = insertRefreshToken(
            persistence,
            userId = userId,
            token = Token.Refresh("token2"),
            issuedAt = Claims.IssuedAt(RoundedInstantProvider.now() - 30.minutes),
            expiresAt = Claims.ExpiresAt(RoundedInstantProvider.now() - 15.minutes),
        ).shouldBeRight()

        persistence.select(inserted2.token).shouldBeSome()

        persistence.revokeExpired() shouldHaveSize 1

        persistence.select(inserted1.token).shouldBeSome()

        val revoked = persistence.select(inserted2.token).shouldBeSome()
        revoked.status shouldBeEqual RefreshToken.Status.Revoked
    }

    should("delete expired for specific period") {
        val userId = insertUser(userPersistence).shouldBeRight().id

        val inserted1 = insertRefreshToken(
            persistence,
            userId = userId,
            token = Token.Refresh("token1"),
            issuedAt = Claims.IssuedAt(),
            lasting = LastingFor(15.minutes),
        ).shouldBeRight()

        persistence.select(inserted1.token).shouldBeSome()

        val inserted2 = insertRefreshToken(
            persistence,
            userId = userId,
            token = Token.Refresh("token2"),
            issuedAt = Claims.IssuedAt(RoundedInstantProvider.now() - 30.minutes),
            expiresAt = Claims.ExpiresAt(RoundedInstantProvider.now() - 15.minutes),
        ).shouldBeRight()

        persistence.select(inserted2.token).shouldBeSome()

        val deleted = persistence.deleteExpiredFor(15.minutes) shouldHaveSize 1
        deleted.first() shouldBeEqual inserted2

        persistence.select(inserted1.token).shouldBeSome()

        persistence.select(inserted2.token).shouldBeNone()
    }
})

private suspend fun insertRefreshToken(
    persistence: RefreshTokenPersistence,
    userId: User.Id,
    token: Token.Refresh,
    issuedAt: Claims.IssuedAt,
    lasting: LastingFor
): Either<DomainError, RefreshToken> {
    return persistence.insert(
        RefreshToken.New(
            userId = userId,
            token = token,
            issuedAt = issuedAt,
            lasting = lasting
        )
    )
}

private suspend fun insertRefreshToken(
    persistence: RefreshTokenPersistence,
    userId: User.Id,
    token: Token.Refresh,
    issuedAt: Claims.IssuedAt,
    expiresAt: Claims.ExpiresAt
): Either<DomainError, RefreshToken> {
    return persistence.insert(
        RefreshToken.New(
            userId = userId,
            token = token,
            issuedAt = issuedAt,
            expiresAt = expiresAt
        )
    )
}
