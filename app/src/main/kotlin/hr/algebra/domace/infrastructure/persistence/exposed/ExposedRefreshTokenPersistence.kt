package hr.algebra.domace.infrastructure.persistence.exposed

import arrow.core.Either
import arrow.core.Option
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.right
import arrow.core.singleOrNone
import hr.algebra.domace.domain.DbError.NothingWasChanged
import hr.algebra.domace.domain.DomainError
import hr.algebra.domace.domain.model.RefreshToken
import hr.algebra.domace.domain.model.User
import hr.algebra.domace.domain.persistence.RefreshTokenPersistence
import hr.algebra.domace.domain.security.Claims
import hr.algebra.domace.domain.security.Token
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ReferenceOption.CASCADE
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.kotlin.datetime.timestampWithTimeZone
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.ZoneOffset.UTC
import kotlin.time.Duration

object RefreshTokensTable : LongIdTable("refresh_tokens", "refresh_token_pk") {
    val userId = reference("user_fk", UsersTable, onDelete = CASCADE)
    val token = varchar("token", 1024)
    val issuedAt = timestampWithTimeZone("issued_at")
    val expiresAt = timestampWithTimeZone("expires_at")
    val status = enumeration<RefreshToken.Status>("status")
}

fun ExposedRefreshTokenPersistence(db: Database) =
    object : RefreshTokenPersistence {
        override suspend fun insert(refreshToken: RefreshToken.New): Either<DomainError, RefreshToken.Entity> =
            ioTransaction(db) {
                val id = RefreshTokensTable.insertAndGetId {
                    it[userId] = refreshToken.userId.value
                    it[token] = refreshToken.token.value
                    it[issuedAt] = refreshToken.issuedAt.value.toJavaInstant().atOffset(UTC)
                    it[expiresAt] = refreshToken.expiresAt.value.toJavaInstant().atOffset(UTC)
                    it[status] = refreshToken.status
                }

                RefreshTokensTable
                    .selectAll()
                    .where { RefreshTokensTable.id eq id }
                    .single()
                    .toRecord()
                    .right()
            }

        override suspend fun select(id: RefreshToken.Id): Option<RefreshToken.Entity> =
            ioTransaction(db) {
                RefreshTokensTable
                    .selectAll()
                    .where { RefreshTokensTable.id eq id.value }
                    .singleOrNone()
                    .map { it.toRecord() }
            }

        override suspend fun select(token: Token.Refresh): Option<RefreshToken.Entity> =
            ioTransaction(db) {
                RefreshTokensTable
                    .selectAll()
                    .where { RefreshTokensTable.token eq token.value }
                    .singleOrNone()
                    .map { it.toRecord() }
            }

        override suspend fun revoke(tokenId: RefreshToken.Id): Either<DomainError, RefreshToken.Entity> =
            ioTransaction(db) {
                either {
                    val updatedCount =
                        RefreshTokensTable.update({ RefreshTokensTable.id eq tokenId.value }) {
                            it[status] = RefreshToken.Status.Revoked
                        }
                    ensure(updatedCount > 0) { NothingWasChanged }

                    RefreshTokensTable
                        .selectAll()
                        .where { RefreshTokensTable.id eq tokenId.value }
                        .single()
                        .toRecord()
                }
            }

        override suspend fun prolong(prolong: RefreshToken.Prolong): Either<DomainError, RefreshToken.Entity> =
            ioTransaction(db) {
                either {
                    val updatedCount =
                        RefreshTokensTable.update({ RefreshTokensTable.id eq prolong.id.value }) {
                            it[expiresAt] = prolong.expiresAt.value.toJavaInstant().atOffset(UTC)
                        }
                    ensure(updatedCount > 0) { NothingWasChanged }

                    RefreshTokensTable
                        .selectAll()
                        .where { RefreshTokensTable.id eq prolong.id.value }
                        .single()
                        .toRecord()
                }
            }

        override suspend fun revokeExpired(): Set<RefreshToken.Entity> =
            ioTransaction(db) {
                val now = Clock.System.now().toJavaInstant().atOffset(UTC)
                val tokensToRevoke =
                    RefreshTokensTable
                        .selectAll()
                        .where { RefreshTokensTable.expiresAt lessEq now }
                        .map { it.toRecord() }
                        .toSet()

                RefreshTokensTable.update({ RefreshTokensTable.expiresAt lessEq now }) {
                    it[status] = RefreshToken.Status.Revoked
                }

                tokensToRevoke
            }

        override suspend fun delete(id: RefreshToken.Id): Either<DomainError, RefreshToken.Id> =
            ioTransaction(db) {
                either {
                    val deletedCount = RefreshTokensTable.deleteWhere { RefreshTokensTable.id eq id.value }
                    ensure(deletedCount > 0) { NothingWasChanged }
                    id
                }
            }

        override suspend fun deleteExpiredFor(duration: Duration): Set<RefreshToken.Entity> =
            ioTransaction(db) {
                val boundary = (Clock.System.now() - duration).toJavaInstant().atOffset(UTC)
                val expiredTokens =
                    RefreshTokensTable
                        .selectAll()
                        .where { RefreshTokensTable.expiresAt lessEq boundary }
                        .map { it.toRecord() }
                        .toSet()

                RefreshTokensTable.deleteWhere { expiresAt lessEq boundary }

                expiredTokens
            }

        private fun ResultRow.toRecord(): RefreshToken.Entity {
            return RefreshToken.Entity(
                RefreshToken.Id(this[RefreshTokensTable.id].value),
                User.Id(this[RefreshTokensTable.userId].value),
                Token.Refresh(this[RefreshTokensTable.token]),
                Claims.IssuedAt(this[RefreshTokensTable.issuedAt].toInstant().toKotlinInstant()),
                Claims.ExpiresAt(this[RefreshTokensTable.expiresAt].toInstant().toKotlinInstant()),
                this[RefreshTokensTable.status]
            )
        }
    }
