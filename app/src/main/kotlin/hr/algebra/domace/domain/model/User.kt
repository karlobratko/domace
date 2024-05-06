package hr.algebra.domace.domain.model

import arrow.core.EitherNel
import hr.algebra.domace.domain.ValidationError.UserValidationError
import hr.algebra.domace.domain.getOrAccumulateMerging
import hr.algebra.domace.domain.security.Token
import hr.algebra.domace.domain.validation.EmailValidationScope
import hr.algebra.domace.domain.validation.PasswordValidationScope
import hr.algebra.domace.domain.validation.RoleValidationScope
import hr.algebra.domace.domain.validation.UsernameValidationScope
import hr.algebra.domace.domain.zipOrAccumulateMerging

data class User(
    val id: Id,
    val username: Username,
    val email: Email,
    val passwordHash: PasswordHash,
    val registrationToken: Token.Register,
    val role: Role
) {

    class New private constructor(
        val username: Username,
        val email: Email,
        val password: Password,
        val role: Role
    ) {
        companion object {

            context(UsernameValidationScope, EmailValidationScope, PasswordValidationScope, RoleValidationScope)
            operator fun invoke(
                username: Username,
                email: Email,
                password: Password,
                role: Role
            ): EitherNel<UserValidationError, New> =
                zipOrAccumulateMerging(
                    { username.validate() },
                    { email.validate() },
                    { password.validate() },
                    { role.validate() },
                    User::New
                )
        }
    }

    class Edit private constructor(val id: Id, val username: Username, val email: Email) {
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
    ) {
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

    @JvmInline value class Id(val value: Long)

    @JvmInline value class Username(val value: String)

    @JvmInline value class Email(val value: String)

    @JvmInline value class Password(val value: String)

    @JvmInline value class PasswordHash(val value: String)

    enum class Role {
        Admin,
        Agronomist,
        Agriculturist,
        Customer
    }
}
