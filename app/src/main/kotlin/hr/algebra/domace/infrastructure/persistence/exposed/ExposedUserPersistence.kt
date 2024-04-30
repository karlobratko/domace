package hr.algebra.domace.infrastructure.persistence.exposed

import arrow.core.Either
import arrow.core.Either.Companion.catchOrThrow
import arrow.core.Option
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.singleOrNone
import hr.algebra.domace.domain.DbError
import hr.algebra.domace.domain.DbError.EmailAlreadyExists
import hr.algebra.domace.domain.DbError.InvalidUsernameOrPassword
import hr.algebra.domace.domain.DbError.NothingWasChanged
import hr.algebra.domace.domain.DbError.UnhandledDbError
import hr.algebra.domace.domain.DbError.UsernameAlreadyExists
import hr.algebra.domace.domain.DbError.ValueAlreadyExists
import hr.algebra.domace.domain.DomainError
import hr.algebra.domace.domain.conversion.ConversionScope
import hr.algebra.domace.domain.getOrRaise
import hr.algebra.domace.domain.model.User
import hr.algebra.domace.domain.persistence.UserPersistence
import kotlinx.datetime.toKotlinInstant
import org.jetbrains.exposed.crypt.Encryptor
import org.jetbrains.exposed.crypt.encryptedVarchar
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestampWithTimeZone
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.postgresql.util.PSQLState.UNIQUE_VIOLATION as UniquenessViolation
import org.springframework.security.crypto.bcrypt.BCrypt.checkpw as checkPassword
import org.springframework.security.crypto.bcrypt.BCrypt.gensalt as generateSalt
import org.springframework.security.crypto.bcrypt.BCrypt.hashpw as hashPassword

private const val USERNAME_UNIQUE_INDEX = "users_username_unique_index"

private const val EMAIL_UNIQUE_INDEX = "users_email_unique_index"

object UsersTable : LongIdTable("users", "user_pk") {
    val username = varchar("username", 50).uniqueIndex(USERNAME_UNIQUE_INDEX)
    val email = varchar("email", 256).uniqueIndex(EMAIL_UNIQUE_INDEX)
    val passwordHash = encryptedVarchar(
        "password_hash",
        256,
        Encryptor({ hashPassword(it, generateSalt()) }, { it }, { it })
    )
    val registrationDate = timestampWithTimeZone("registration_date").defaultExpression(CurrentTimestamp())
    val role = enumeration<User.Role>("role")
}

fun ExposedUserPersistence(db: Database) = object : UserPersistence {
    init {
        transaction {
            SchemaUtils.create(UsersTable)
        }
    }

    override suspend fun insert(user: User.New): Either<DomainError, User.Entity> =
        ioTransaction(db) {
            either {
                val id =
                    catchOrThrow<ExposedSQLException, EntityID<Long>> {
                        UsersTable.insertAndGetId {
                            it[username] = user.username.value
                            it[email] = user.email.value
                            it[passwordHash] = user.password.value
                            it[role] = user.role
                        }
                    }.mapLeft { err ->
                        with(ExposedSQLExceptionToDbErrorConversion) {
                            err.convert()
                        }
                    }.bind()

                UsersTable
                    .selectAll()
                    .where { UsersTable.id eq id }
                    .single()
                    .toRecord()
            }
        }

    override suspend fun select(username: User.Username): Option<User.Entity> =
        ioTransaction(db) {
            UsersTable
                .selectAll()
                .where { UsersTable.username eq username.value }
                .singleOrNone()
                .map { it.toRecord() }
        }

    override suspend fun select(username: User.Username, password: User.Password): Option<User.Entity> =
        ioTransaction(db) {
            UsersTable
                .selectAll()
                .where { (UsersTable.username eq username.value) }
                .singleOrNone { checkPassword(password.value, it[UsersTable.passwordHash]) }
                .map { it.toRecord() }
        }

    override suspend fun select(id: User.Id): Option<User.Entity> =
        ioTransaction(db) {
            UsersTable
                .selectAll()
                .where { UsersTable.id eq id.value }
                .singleOrNone()
                .map { it.toRecord() }
        }

    override suspend fun update(data: User.Edit): Either<DomainError, User.Entity> =
        ioTransaction(db) {
            either {
                val updatedCount =
                    catchOrThrow<ExposedSQLException, Int> {
                        UsersTable.update({ UsersTable.id eq data.id.value }) {
                            it[username] = data.username.value
                            it[email] = data.email.value
                        }
                    }.mapLeft { err: ExposedSQLException ->
                        with(ExposedSQLExceptionToDbErrorConversion) {
                            err.convert()
                        }
                    }.bind()

                ensure(updatedCount > 0) { NothingWasChanged }

                UsersTable
                    .selectAll()
                    .where { UsersTable.id eq data.id.value }
                    .single()
                    .toRecord()
            }
        }

    override suspend fun update(data: User.ChangePassword): Either<DomainError, User.Entity> =
        ioTransaction(db) {
            either {
                val user = select(data.username, data.oldPassword).getOrRaise { InvalidUsernameOrPassword }

                val updatedCount =
                    UsersTable.update({ UsersTable.id eq user.id.value }) {
                        it[passwordHash] = data.newPassword.value
                    }

                ensure(updatedCount > 0) { NothingWasChanged }

                UsersTable
                    .selectAll()
                    .where { UsersTable.username eq data.username.value }
                    .single()
                    .toRecord()
            }
        }

    override suspend fun delete(id: User.Id): Either<DomainError, User.Id> =
        ioTransaction(db) {
            either {
                val deletedCount = UsersTable.deleteWhere { UsersTable.id eq id.value }

                ensure(deletedCount > 0) { NothingWasChanged }

                id
            }
        }

    private fun ResultRow.toRecord(): User.Entity =
        User.Entity(
            User.Id(this[UsersTable.id].value),
            User.Username(this[UsersTable.username]),
            User.Email(this[UsersTable.email]),
            User.PasswordHash(this[UsersTable.passwordHash]),
            User.RegistrationDate(this[UsersTable.registrationDate].toInstant().toKotlinInstant()),
            this[UsersTable.role]
        )
}

private typealias ExposedSQLExceptionToDomainErrorConversionScope = ConversionScope<ExposedSQLException, DbError>

private val ExposedSQLExceptionToDbErrorConversion = ExposedSQLExceptionToDomainErrorConversionScope {
    when (sqlState) {
        UniquenessViolation.state -> {
            val message = message?.lowercase() ?: ""
            when {
                message.contains(USERNAME_UNIQUE_INDEX) -> UsernameAlreadyExists
                message.contains(EMAIL_UNIQUE_INDEX) -> EmailAlreadyExists
                else -> ValueAlreadyExists
            }
        }

        else -> UnhandledDbError
    }
}
