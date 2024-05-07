package hr.algebra.domace.infrastructure.persistence.exposed

import arrow.core.Either
import arrow.core.Option
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.singleOrNone
import hr.algebra.domace.domain.DbError.NothingWasChanged
import hr.algebra.domace.domain.DomainError
import hr.algebra.domace.domain.config.DefaultInstantProvider
import hr.algebra.domace.domain.conversion.ConversionScope
import hr.algebra.domace.domain.conversion.convert
import hr.algebra.domace.domain.model.RegistrationToken
import hr.algebra.domace.domain.model.RegistrationToken.ConfirmationStatus.Confirmed
import hr.algebra.domace.domain.model.RegistrationToken.ConfirmationStatus.Unconfirmed
import hr.algebra.domace.domain.model.RegistrationToken.CreatedAt
import hr.algebra.domace.domain.model.RegistrationToken.ExpiresAt
import hr.algebra.domace.domain.persistence.RegistrationTokenPersistence
import hr.algebra.domace.domain.security.LastingFor
import hr.algebra.domace.domain.security.Token
import java.time.ZoneOffset.UTC
import java.util.UUID
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.kotlin.datetime.timestampWithTimeZone
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.SchemaUtils.create as createIfNotExists

object RegistrationTokensTable : UUIDTable("registration_tokens", "registration_token_pk") {
    val createdAt = timestampWithTimeZone("created_at")
    val expiresAt = timestampWithTimeZone("expires_at")
    val confirmedAt = timestampWithTimeZone("confirmed_at").nullable()
}

data class RegistrationConfig(val expiresAfter: LastingFor)

fun ExposedRegistrationTokenPersistence(db: Database, config: RegistrationConfig) =
    object : RegistrationTokenPersistence {
        init {
            transaction {
                createIfNotExists(RegistrationTokensTable)
            }
        }

        override suspend fun insertAndGetToken(): Token.Register = ioTransaction(db = db) {
            val id = RegistrationTokensTable.insertAndGetId {
                val creationInstant = DefaultInstantProvider.now()
                it[createdAt] = creationInstant.toJavaInstant().atOffset(UTC)
                it[expiresAt] = (creationInstant + config.expiresAfter.value).toJavaInstant().atOffset(UTC)
            }

            Token.Register(id.value.toString())
        }

        override suspend fun select(id: Token.Register): Option<RegistrationToken> = ioTransaction(db = db) {
            RegistrationTokensTable
                .selectAll()
                .where { RegistrationTokensTable.id eq UUID.fromString(id.value) }
                .singleOrNone()
                .map { it.convert(ResultRowToRegistrationTokenConversion) }
        }

        override suspend fun confirm(id: Token.Register): Either<DomainError, Token.Register> = either {
            ioTransaction(db = db) {
                val updatedCount = RegistrationTokensTable.update({
                    RegistrationTokensTable.id eq UUID.fromString(id.value)
                }) {
                    it[confirmedAt] = DefaultInstantProvider.now().toJavaInstant().atOffset(UTC)
                }
                ensure(updatedCount > 0) { NothingWasChanged }

                id
            }
        }

        override suspend fun reset(id: Token.Register): Either<DomainError, Token.Register> = either {
            ioTransaction(db = db) {
                val updatedCount = RegistrationTokensTable.update({
                    RegistrationTokensTable.id eq UUID.fromString(id.value)
                }) {
                    it[expiresAt] = (DefaultInstantProvider.now() + config.expiresAfter.value)
                        .toJavaInstant().atOffset(UTC)
                }
                ensure(updatedCount > 0) { NothingWasChanged }

                id
            }
        }
    }

typealias ResultRowToRegistrationTokenConversionScope = ConversionScope<ResultRow, RegistrationToken>

val ResultRowToRegistrationTokenConversion = ResultRowToRegistrationTokenConversionScope {
    RegistrationToken(
        Token.Register(this[RegistrationTokensTable.id].value.toString()),
        CreatedAt(this[RegistrationTokensTable.createdAt].toInstant().toKotlinInstant()),
        ExpiresAt(this[RegistrationTokensTable.expiresAt].toInstant().toKotlinInstant()),
        if (this[RegistrationTokensTable.confirmedAt] == null) {
            Unconfirmed
        } else {
            Confirmed(this[RegistrationTokensTable.createdAt].toInstant().toKotlinInstant())
        }
    )
}
