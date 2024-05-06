package hr.algebra.domace.infrastructure.persistence.exposed

import arrow.core.Either
import arrow.core.Either.Companion.catchOrThrow
import arrow.core.Option
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.singleOrNone
import hr.algebra.domace.domain.DbError.NothingWasChanged
import hr.algebra.domace.domain.DbError.TokenAlreadyExists
import hr.algebra.domace.domain.DbError.UnhandledDbError
import hr.algebra.domace.domain.DbError.ValueAlreadyExists
import hr.algebra.domace.domain.DomainError
import hr.algebra.domace.domain.config.RoundedInstantProvider
import hr.algebra.domace.domain.conversion.ConversionScope
import hr.algebra.domace.domain.conversion.convert
import hr.algebra.domace.domain.model.RefreshToken
import hr.algebra.domace.domain.model.User
import hr.algebra.domace.domain.persistence.RefreshTokenPersistence
import hr.algebra.domace.domain.security.Token
import hr.algebra.domace.domain.security.jwt.Claims
import java.time.ZoneOffset.UTC
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.kotlin.datetime.timestampWithTimeZone
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.postgresql.util.PSQLState.UNIQUE_VIOLATION
import kotlin.time.Duration
import org.jetbrains.exposed.sql.ReferenceOption.CASCADE as Cascade
import org.jetbrains.exposed.sql.SchemaUtils.create as createIfNotExists

private const val TOKEN_UNIQUE_INDEX = "refresh_tokens_token_unique_index"

object RefreshTokensTable : LongIdTable("refresh_tokens", "refresh_token_pk") {
    val userId = reference("user_fk", UsersTable, onDelete = Cascade)
    val token = varchar("token", 1024).uniqueIndex(TOKEN_UNIQUE_INDEX)
    val issuedAt = timestampWithTimeZone("issued_at")
    val expiresAt = timestampWithTimeZone("expires_at")
    val status = enumeration<RefreshToken.Status>("status")
}

fun ExposedRefreshTokenPersistence(db: Database) = object : RefreshTokenPersistence {
    init {
        transaction {
            createIfNotExists(RefreshTokensTable)
        }
    }

    override suspend fun insert(refreshToken: RefreshToken.New): Either<DomainError, RefreshToken> = either {
        ioTransaction(db = db) {
            val id = catchOrThrow<ExposedSQLException, EntityID<Long>> {
                RefreshTokensTable.insertAndGetId {
                    it[userId] = refreshToken.userId.value
                    it[token] = refreshToken.token.value
                    it[issuedAt] = refreshToken.issuedAt.value.toJavaInstant().atOffset(UTC)
                    it[expiresAt] = refreshToken.expiresAt.value.toJavaInstant().atOffset(UTC)
                    it[status] = refreshToken.status
                }
            }.mapLeft { it.convert(ExposedSQLExceptionToDbErrorConversion) }.bind()

            selectAllWithUserRole()
                .where { RefreshTokensTable.id eq id }
                .single()
                .convert(ResultRowToRefreshTokenConversion)
        }
    }

    override suspend fun select(token: Token.Refresh): Option<RefreshToken> = ioTransaction(db = db) {
        selectAllWithUserRole()
            .where { RefreshTokensTable.token eq token.value }
            .singleOrNone()
            .map { it.convert(ResultRowToRefreshTokenConversion) }
    }

    override suspend fun revoke(tokenId: RefreshToken.Id): Either<DomainError, RefreshToken> = either {
        ioTransaction(db = db) {
            val updatedCount = RefreshTokensTable.update({ RefreshTokensTable.id eq tokenId.value }) {
                it[status] = RefreshToken.Status.Revoked
            }
            ensure(updatedCount > 0) { NothingWasChanged }

            selectAllWithUserRole()
                .where { RefreshTokensTable.id eq tokenId.value }
                .single()
                .convert(ResultRowToRefreshTokenConversion)
        }
    }

    override suspend fun prolong(prolong: RefreshToken.Prolong): Either<DomainError, RefreshToken> = either {
        ioTransaction(db = db) {
            val updatedCount =
                RefreshTokensTable.update({ RefreshTokensTable.id eq prolong.id.value }) {
                    it[expiresAt] = prolong.expiresAt.value.toJavaInstant().atOffset(UTC)
                }
            ensure(updatedCount > 0) { NothingWasChanged }

            selectAllWithUserRole()
                .where { RefreshTokensTable.id eq prolong.id.value }
                .single()
                .convert(ResultRowToRefreshTokenConversion)
        }
    }

    override suspend fun revokeExpired(): Set<RefreshToken> = ioTransaction(db = db) {
        val now = RoundedInstantProvider.now().toJavaInstant().atOffset(UTC)
        val tokensToRevoke = selectAllWithUserRole()
            .where { RefreshTokensTable.expiresAt lessEq now }
            .map { it.convert(ResultRowToRefreshTokenConversion) }
            .toSet()

        RefreshTokensTable.update({ RefreshTokensTable.expiresAt lessEq now }) {
            it[status] = RefreshToken.Status.Revoked
        }

        tokensToRevoke
    }

    override suspend fun deleteExpiredFor(duration: Duration): Set<RefreshToken> = ioTransaction(db = db) {
        val boundary = (RoundedInstantProvider.now() - duration).toJavaInstant().atOffset(UTC)
        val expiredTokens = selectAllWithUserRole()
            .where { RefreshTokensTable.expiresAt lessEq boundary }
            .map { it.convert(ResultRowToRefreshTokenConversion) }
            .toSet()

        RefreshTokensTable.deleteWhere { expiresAt lessEq boundary }

        expiredTokens
    }

    private fun selectAllWithUserRole() =
        (RefreshTokensTable innerJoin UsersTable)
            .select(
                RefreshTokensTable.id,
                RefreshTokensTable.userId,
                UsersTable.role,
                RefreshTokensTable.token,
                RefreshTokensTable.issuedAt,
                RefreshTokensTable.expiresAt,
                RefreshTokensTable.status
            )
}

private typealias ResultRowToRefreshTokenConversionScope = ConversionScope<ResultRow, RefreshToken>

private val ResultRowToRefreshTokenConversion = ResultRowToRefreshTokenConversionScope {
    RefreshToken(
        RefreshToken.Id(this[RefreshTokensTable.id].value),
        User.Id(this[RefreshTokensTable.userId].value),
        this[UsersTable.role],
        Token.Refresh(this[RefreshTokensTable.token]),
        Claims.IssuedAt(this[RefreshTokensTable.issuedAt].toInstant().toKotlinInstant()),
        Claims.ExpiresAt(this[RefreshTokensTable.expiresAt].toInstant().toKotlinInstant()),
        this[RefreshTokensTable.status]
    )
}

private val ExposedSQLExceptionToDbErrorConversion = ExposedSQLExceptionToDomainErrorConversionScope {
    when (sqlState) {
        UNIQUE_VIOLATION.state -> {
            val message = message?.lowercase() ?: ""
            when {
                message.contains(TOKEN_UNIQUE_INDEX) -> TokenAlreadyExists
                else -> ValueAlreadyExists
            }
        }

        else -> UnhandledDbError
    }
}
