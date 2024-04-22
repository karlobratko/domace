package hr.algebra.domace.domain.model

import arrow.core.EitherNel
import hr.algebra.domace.domain.ValidationError.UserValidationError
import hr.algebra.domace.domain.getOrAccumulateMerging
import hr.algebra.domace.domain.validation.EmailValidationScope
import hr.algebra.domace.domain.validation.PasswordValidationScope
import hr.algebra.domace.domain.validation.UsernameValidationScope
import hr.algebra.domace.domain.zipOrAccumulateMerging
import kotlinx.datetime.Instant

sealed interface User {
    class New private constructor(val username: Username, val email: Email, val password: Password) : User {
        companion object {
            context(UsernameValidationScope, EmailValidationScope, PasswordValidationScope)
            operator fun invoke(
                username: Username,
                email: Email,
                password: Password
            ): EitherNel<UserValidationError, New> =
                zipOrAccumulateMerging(
                    { username.validate() },
                    { email.validate() },
                    { password.validate() },
                    User::New
                )
        }
    }

    class Edit private constructor(val id: Id, val username: Username, val email: Email) : User {
        companion object {
            context(UsernameValidationScope, EmailValidationScope)
            operator fun invoke(
                id: Id,
                username: Username,
                email: Email
            ): EitherNel<UserValidationError, Edit> =
                getOrAccumulateMerging(
                    { username.validate() },
                    { email.validate() }
                ) { Edit(id, username, email) }
        }
    }

    class ChangePassword private constructor(
        val username: Username,
        val oldPassword: Password,
        val newPassword: Password
    ) : User {
        companion object {
            context(UsernameValidationScope, PasswordValidationScope)
            operator fun invoke(
                username: Username,
                oldPassword: Password,
                newPassword: Password
            ): EitherNel<UserValidationError, ChangePassword> =
                zipOrAccumulateMerging(
                    { username.validate() },
                    { oldPassword.validate() },
                    { newPassword.validate() },
                    User::ChangePassword
                )
        }
    }

    data class Entity(
        val id: Id,
        val username: Username,
        val email: Email,
        val passwordHash: PasswordHash,
        val registrationDate: RegistrationDate
    ) : User

    @JvmInline value class Id(val value: Long)

    @JvmInline value class Username(val value: String)

    @JvmInline value class Email(val value: String)

    @JvmInline value class Password(val value: String)

    @JvmInline value class PasswordHash(val value: String)

    @JvmInline value class RegistrationDate(val value: Instant)
}
