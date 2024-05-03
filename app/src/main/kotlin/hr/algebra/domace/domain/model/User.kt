package hr.algebra.domace.domain.model

import arrow.core.EitherNel
import hr.algebra.domace.domain.ValidationError.UserValidationError
import hr.algebra.domace.domain.getOrAccumulateMerging
import hr.algebra.domace.domain.validation.EmailValidationScope
import hr.algebra.domace.domain.validation.PasswordValidationScope
import hr.algebra.domace.domain.validation.RoleValidationScope
import hr.algebra.domace.domain.validation.UsernameValidationScope
import hr.algebra.domace.domain.zipOrAccumulateMerging
import kotlinx.datetime.Instant

/**
 * Represents a User.
 *
 * This is a sealed interface, meaning that all implementations of this interface are nested within this interface.
 */
sealed interface User {
    /**
     * Represents a new User.
     *
     * @property username The username of the new user.
     * @property email The email of the new user.
     * @property password The password of the new user.
     * @property role The role of the new user.
     */
    class New private constructor(
        val username: Username,
        val email: Email,
        val password: Password,
        val role: Role
    ) : User {
        companion object {
            /**
             * Creates a new User with the given parameters.
             *
             * @param username The username of the new user.
             * @param email The email of the new user.
             * @param password The password of the new user.
             * @param role The role of the new user.
             * @return Either a UserValidationError or the new User.
             */
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

    /**
     * Represents a User that is to be edited.
     *
     * @property id The ID of the user to be edited.
     * @property username The new username of the user.
     * @property email The new email of the user.
     */
    class Edit private constructor(val id: Id, val username: Username, val email: Email) : User {
        companion object {
            /**
             * Edits a User with the given parameters.
             *
             * @param id The ID of the user to be edited.
             * @param username The new username of the user.
             * @param email The new email of the user.
             * @return Either a UserValidationError or the edited User.
             */
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

    /**
     * Represents a User that is to change their password.
     *
     * @property username The username of the user.
     * @property oldPassword The old password of the user.
     * @property newPassword The new password of the user.
     */
    class ChangePassword private constructor(
        val username: Username,
        val oldPassword: Password,
        val newPassword: Password
    ) : User {
        companion object {
            /**
             * Changes the password of a User with the given parameters.
             *
             * @param username The username of the user.
             * @param oldPassword The old password of the user.
             * @param newPassword The new password of the user.
             * @return Either a UserValidationError or the User with the changed password.
             */
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

    /**
     * Represents an existing User entity.
     *
     * @property id The ID of the user.
     * @property username The username of the user.
     * @property email The email of the user.
     * @property passwordHash The hashed password of the user.
     * @property registrationDate The registration date of the user.
     * @property role The role of the user.
     */
    data class Entity(
        val id: Id,
        val username: Username,
        val email: Email,
        val passwordHash: PasswordHash,
        val registrationDate: RegistrationDate,
        val role: Role
    ) : User

    /**
     * Represents the ID of a User.
     *
     * @property value The ID value.
     */
    @JvmInline value class Id(val value: Long)

    /**
     * Represents the username of a User.
     *
     * @property value The username value.
     */
    @JvmInline value class Username(val value: String)

    /**
     * Represents the email of a User.
     *
     * @property value The email value.
     */
    @JvmInline value class Email(val value: String)

    /**
     * Represents the password of a User.
     *
     * @property value The password value.
     */
    @JvmInline value class Password(val value: String)

    /**
     * Represents the hashed password of a User.
     *
     * @property value The hashed password value.
     */
    @JvmInline value class PasswordHash(val value: String)

    /**
     * Represents the registration date of a User.
     *
     * @property value The registration date value.
     */
    @JvmInline value class RegistrationDate(val value: Instant)

    /**
     * Represents the role of a User.
     */
    enum class Role {
        Admin,
        Agronomist,
        Agriculturist,
        Customer
    }
}
