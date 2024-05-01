package hr.algebra.domace.domain.validation

import arrow.core.raise.ensure
import hr.algebra.domace.domain.ValidationError.UserValidationError.EmailValidationError
import hr.algebra.domace.domain.ValidationError.UserValidationError.EmailValidationError.InvalidEmail
import hr.algebra.domace.domain.ValidationError.UserValidationError.EmailValidationError.TooLongEmail
import hr.algebra.domace.domain.ValidationError.UserValidationError.PasswordValidationError
import hr.algebra.domace.domain.ValidationError.UserValidationError.PasswordValidationError.NoDigitsInPassword
import hr.algebra.domace.domain.ValidationError.UserValidationError.PasswordValidationError.NoSpecialCharsInPassword
import hr.algebra.domace.domain.ValidationError.UserValidationError.PasswordValidationError.NoUppercaseCharsInPassword
import hr.algebra.domace.domain.ValidationError.UserValidationError.PasswordValidationError.TooShortPassword
import hr.algebra.domace.domain.ValidationError.UserValidationError.PasswordValidationError.WhitespaceInPassword
import hr.algebra.domace.domain.ValidationError.UserValidationError.RoleValidationError
import hr.algebra.domace.domain.ValidationError.UserValidationError.RoleValidationError.AdminCanNotBeCreated
import hr.algebra.domace.domain.ValidationError.UserValidationError.UsernameValidationError
import hr.algebra.domace.domain.ValidationError.UserValidationError.UsernameValidationError.NonAlphanumericCharacterInUsername
import hr.algebra.domace.domain.ValidationError.UserValidationError.UsernameValidationError.TooLongUsername
import hr.algebra.domace.domain.ValidationError.UserValidationError.UsernameValidationError.TooShortUsername
import hr.algebra.domace.domain.applyWrapEitherNel
import hr.algebra.domace.domain.model.User

typealias UsernameValidationScope = ValidationScope<UsernameValidationError, User.Username>

val UsernameValidation =
    UsernameValidationScope {
        validate(this, { it.value }) {
            with(StringAllSatisfiesPredicateValidation({ it.isLetterOrDigit() }) { NonAlphanumericCharacterInUsername })
            with(StringMinLengthValidation(5) { TooShortUsername })
            with(StringMaxLengthValidation(50) { TooLongUsername })
        }
    }

typealias EmailValidationScope = ValidationScope<EmailValidationError, User.Email>

const val EMAIL_PATTERN = "^[A-Za-z0-9+!#\$%&'*+-/=?^_`{|}~.]+@[A-Za-z0-9.-]+\$"

val EmailValidation =
    EmailValidationScope {
        validate(this, { it.value }) {
            with(StringMatchingPatternValidation(EMAIL_PATTERN.toRegex()) { InvalidEmail })
            with(StringMaxLengthValidation(256) { TooLongEmail })
        }
    }

typealias PasswordValidationScope = ValidationScope<PasswordValidationError, User.Password>

const val SPECIAL_CHARACTERS = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~"

private fun Char.isSpecialChar() = SPECIAL_CHARACTERS.contains(this)

val PasswordValidation =
    PasswordValidationScope {
        validate(this, { it.value }) {
            with(StringMinLengthValidation(5) { TooShortPassword })
            with(StringNoneSatisfiesPredicateValidation({ it.isWhitespace() }) { WhitespaceInPassword })
            with(StringAnySatisfiesPredicateValidation({ it.isDigit() }) { NoDigitsInPassword })
            with(StringAnySatisfiesPredicateValidation({ it.isUpperCase() }) { NoUppercaseCharsInPassword })
            with(StringAnySatisfiesPredicateValidation({ it.isSpecialChar() }) { NoSpecialCharsInPassword })
        }
    }

typealias RoleValidationScope = ValidationScope<RoleValidationError, User.Role>

val ALLOWED_CREATION_ROLES = listOf(User.Role.Agronomist, User.Role.Agriculturist, User.Role.Customer)

val RoleValidation = RoleValidationScope {
    applyWrapEitherNel {
        ensure(this@RoleValidationScope in ALLOWED_CREATION_ROLES) { AdminCanNotBeCreated }
    }
}
