package hr.algebra.domace.infrastructure.persistence.exposed

import arrow.core.Either
import hr.algebra.domace.domain.DbError.EmailAlreadyExists
import hr.algebra.domace.domain.DbError.InvalidUsernameOrPassword
import hr.algebra.domace.domain.DbError.NothingWasChanged
import hr.algebra.domace.domain.DbError.UsernameAlreadyExists
import hr.algebra.domace.domain.DomainError
import hr.algebra.domace.domain.model.User
import hr.algebra.domace.domain.persistence.UserPersistence
import hr.algebra.domace.domain.validation.EmailValidation
import hr.algebra.domace.domain.validation.PasswordValidation
import hr.algebra.domace.domain.validation.UsernameValidation
import hr.algebra.domace.domain.with
import hr.algebra.domace.infrastructure.persistence.Database.test
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeNone
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.arrow.core.shouldBeSome
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.equals.shouldNotBeEqual
import io.kotest.matchers.should
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SchemaUtils.drop
import org.jetbrains.exposed.sql.transactions.transaction

object ExposedUserPersistenceTests : ShouldSpec({
    val persistence = ExposedUserPersistence(test)

    beforeEach {
        transaction(test) {
            create(UsersTable)
        }
    }

    afterEach {
        transaction(test) {
            drop(UsersTable)
        }
    }

    should("insert to database") {
        val username = VALID_USERNAME_1
        val email = VALID_EMAIL_1

        val inserted = insertUser(
            persistence,
            username = username,
            email = email
        ).shouldBeRight()

        inserted.should {
            it.username shouldBeEqual username
            it.email shouldBeEqual email
        }

        val selected = persistence.select(inserted.id).shouldBeSome()

        inserted shouldBeEqual selected
    }

    should("fail insert if username already exists") {
        val nonUniqueUsername = VALID_USERNAME_1

        insertUser(
            persistence,
            username = nonUniqueUsername,
            email = VALID_EMAIL_1
        ).shouldBeRight()

        insertUser(
            persistence,
            username = nonUniqueUsername,
            email = VALID_EMAIL_2
        ) shouldBeLeft UsernameAlreadyExists
    }

    should("fail insert if email already exists") {
        val nonUniqueEmail = VALID_EMAIL_1

        insertUser(
            persistence,
            username = VALID_USERNAME_1,
            email = nonUniqueEmail
        ).shouldBeRight()

        insertUser(
            persistence,
            username = VALID_USERNAME_2,
            email = nonUniqueEmail
        ) shouldBeLeft EmailAlreadyExists
    }

    should("return none if record with username not found") {
        persistence.select(VALID_USERNAME_1).shouldBeNone()
    }

    should("return user if record with username found") {
        val username = VALID_USERNAME_1

        val inserted = insertUser(
            persistence,
            username = username,
            email = VALID_EMAIL_1
        ).shouldBeRight()

        persistence.select(username) shouldBeSome inserted
    }

    should("return none if record with username and password not found") {
        persistence.select(VALID_USERNAME_1, VALID_PASSWORD_1).shouldBeNone()
    }

    should("return user if record with username and password found") {
        val username = VALID_USERNAME_1
        val password = VALID_PASSWORD_1

        val inserted = insertUser(
            persistence,
            username = username,
            email = VALID_EMAIL_1,
            password = password
        ).shouldBeRight()

        persistence.select(username, password) shouldBeSome inserted
    }

    should("return none if record with id not found") {
        persistence.select(User.Id(1)).shouldBeNone()
    }

    should("return user if record with id found") {
        val inserted = insertUser(
            persistence,
            username = VALID_USERNAME_1,
            email = VALID_EMAIL_1
        ).shouldBeRight()

        persistence.select(inserted.id) shouldBeSome inserted
    }

    should("update username or email") {
        val inserted = insertUser(
            persistence,
            username = VALID_USERNAME_1,
            email = VALID_EMAIL_1
        ).shouldBeRight()

        val newUsername = VALID_USERNAME_2

        val updated = updateUser(
            persistence,
            id = inserted.id,
            username = newUsername,
            email = inserted.email
        ).shouldBeRight()

        updated.should {
            it.username shouldBeEqual newUsername
            it.email shouldBeEqual inserted.email
            it.passwordHash shouldBeEqual inserted.passwordHash
            it.registrationDate shouldBeEqual inserted.registrationDate
        }
    }

    should("fail update if username already exists") {
        val username = VALID_USERNAME_1

        insertUser(
            persistence,
            username = username,
            email = VALID_EMAIL_1
        ).shouldBeRight()

        val inserted = insertUser(
            persistence,
            username = VALID_USERNAME_2,
            email = VALID_EMAIL_2
        ).shouldBeRight()

        updateUser(
            persistence,
            id = inserted.id,
            username = username,
            email = VALID_EMAIL_2
        ) shouldBeLeft UsernameAlreadyExists
    }

    should("fail update if email already exists") {
        val email = VALID_EMAIL_1

        insertUser(
            persistence,
            username = VALID_USERNAME_1,
            email = email
        ).shouldBeRight()

        val inserted = insertUser(
            persistence,
            username = VALID_USERNAME_2,
            email = VALID_EMAIL_2
        ).shouldBeRight()

        updateUser(
            persistence,
            id = inserted.id,
            username = VALID_USERNAME_2,
            email = email
        ) shouldBeLeft EmailAlreadyExists
    }

    should("fail update if record was not found by id") {
        updateUser(
            persistence,
            id = User.Id(1),
            username = VALID_USERNAME_1,
            email = VALID_EMAIL_1
        ) shouldBeLeft NothingWasChanged
    }

    should("update password") {
        val oldPassword = VALID_PASSWORD_1

        val inserted = insertUser(
            persistence,
            username = VALID_USERNAME_1,
            email = VALID_EMAIL_1,
            password = oldPassword
        ).shouldBeRight()

        val updated = updateUserPassword(
            persistence,
            username = inserted.username,
            oldPassword = oldPassword,
            newPassword = VALID_PASSWORD_2
        ).shouldBeRight()

        updated.should {
            it.username shouldBeEqual inserted.username
            it.email shouldBeEqual inserted.email
            it.passwordHash shouldNotBeEqual inserted.passwordHash
            it.registrationDate shouldBeEqual inserted.registrationDate
        }
    }

    should("fail update password if record was not found by username and old password") {
        updateUserPassword(
            persistence,
            username = VALID_USERNAME_1,
            oldPassword = VALID_PASSWORD_1,
            newPassword = VALID_PASSWORD_2
        ) shouldBeLeft InvalidUsernameOrPassword
    }

    should("delete user by id") {
        val inserted = insertUser(
            persistence,
            username = VALID_USERNAME_1,
            email = VALID_EMAIL_1
        ).shouldBeRight()

        persistence.select(inserted.id).shouldBeSome()

        persistence.delete(inserted.id) shouldBeRight inserted.id

        persistence.select(inserted.id).shouldBeNone()
    }

    should("fail delete when user is not found") {
        persistence.delete(User.Id(1)) shouldBeLeft NothingWasChanged
    }
})

suspend fun insertUser(
    persistence: UserPersistence,
    username: User.Username = VALID_USERNAME_1,
    email: User.Email = VALID_EMAIL_1,
    password: User.Password = VALID_PASSWORD_1
): Either<DomainError, User.Entity> {
    val user = with(UsernameValidation, EmailValidation, PasswordValidation) {
        User.New(username, email, password, User.Role.Admin)
    }.shouldBeRight()

    return persistence.insert(user)
}

private suspend fun updateUser(
    persistence: UserPersistence,
    id: User.Id,
    username: User.Username,
    email: User.Email
): Either<DomainError, User.Entity> {
    val user = with(UsernameValidation, EmailValidation) {
        User.Edit(id, username, email)
    }.shouldBeRight()

    return persistence.update(user)
}

private suspend fun updateUserPassword(
    persistence: UserPersistence,
    username: User.Username,
    oldPassword: User.Password,
    newPassword: User.Password
): Either<DomainError, User.Entity> {
    val user = with(UsernameValidation, PasswordValidation) {
        User.ChangePassword(username, oldPassword, newPassword)
    }.shouldBeRight()

    return persistence.update(user)
}

private val VALID_USERNAME_1 = User.Username("kbratko")
private val VALID_EMAIL_1 = User.Email("kbratko@algebra.hr")
private val VALID_USERNAME_2 = User.Username("karlobratko")
private val VALID_EMAIL_2 = User.Email("karlobratko@algebra.hr")
private val VALID_PASSWORD_1 = User.Password("Pa\$\$w0rd")
private val VALID_PASSWORD_2 = User.Password("L0z!nk4")
