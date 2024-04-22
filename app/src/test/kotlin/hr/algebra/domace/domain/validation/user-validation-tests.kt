package hr.algebra.domace.domain.validation

import arrow.core.nel
import arrow.core.nonEmptyListOf
import hr.algebra.domace.domain.ValidationError.UserValidationError.PasswordValidationError.NoDigitsInPassword
import hr.algebra.domace.domain.ValidationError.UserValidationError.PasswordValidationError.NoSpecialCharsInPassword
import hr.algebra.domace.domain.ValidationError.UserValidationError.PasswordValidationError.NoUppercaseCharsInPassword
import hr.algebra.domace.domain.ValidationError.UserValidationError.PasswordValidationError.TooShortPassword
import hr.algebra.domace.domain.ValidationError.UserValidationError.PasswordValidationError.WhitespaceInPassword
import hr.algebra.domace.domain.ValidationError.UserValidationError.UsernameValidationError.NonAlphanumericCharacterInUsername
import hr.algebra.domace.domain.ValidationError.UserValidationError.UsernameValidationError.TooLongUsername
import hr.algebra.domace.domain.ValidationError.UserValidationError.UsernameValidationError.TooShortUsername
import hr.algebra.domace.domain.model.User
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.char
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.email
import io.kotest.property.arbitrary.filterNot
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.printableAscii
import io.kotest.property.arbitrary.single
import io.kotest.property.arbitrary.string
import io.kotest.property.assume
import io.kotest.property.checkAll

object UserValidationTests : ShouldSpec({
    context("UsernameValidation") {
        should("pass if username is of length between 5 and 50 and alphanumeric") {
            checkAll(Arb.string(5..50, Codepoint.alphanumeric())) { string ->
                // given
                val username = User.Username(string)

                // when
                val actual = with(UsernameValidation) {
                    username.validate()
                }

                // then
                actual shouldBeRight username
            }
        }

        should("fail if username is not alphanumeric") {
            checkAll(Arb.string(5..50, Codepoint.printableAscii())) { string ->
                assume(!string.isAlphanumeric())

                // given
                val username = User.Username(string)

                // when
                val actual = with(UsernameValidation) {
                    username.validate()
                }

                // then
                actual shouldBeLeft NonAlphanumericCharacterInUsername.nel()
            }
        }

        should("fail if username is shorter then 5 characters") {
            checkAll(Arb.string(0..4, Codepoint.alphanumeric())) { string ->
                // given
                val username = User.Username(string)

                // when
                val actual = with(UsernameValidation) {
                    username.validate()
                }

                // then
                actual shouldBeLeft TooShortUsername.nel()
            }
        }

        should("fail if username is longer than 50 characters") {
            checkAll(Arb.string(51..100, Codepoint.alphanumeric())) { string ->
                // given
                val username = User.Username(string)

                // when
                val actual = with(UsernameValidation) {
                    username.validate()
                }

                // then
                actual shouldBeLeft TooLongUsername.nel()
            }
        }

        should("accumulate errors from all validation steps") {
            checkAll(Arb.string(51..100, Codepoint.printableAscii()).filterNot(String::isAlphanumeric)) { string ->
                // given
                val username = User.Username(string)

                // when
                val actual = with(UsernameValidation) {
                    username.validate()
                }

                // then
                actual shouldBeLeft nonEmptyListOf(NonAlphanumericCharacterInUsername, TooLongUsername)
            }
        }
    }

    context("EmailValidation") {
        should("pass if email is of length at max 256 and satisfies pattern") {
            checkAll(Arb.email()) { string ->
                // given
                val email = User.Email(string)

                // when
                val actual = with(EmailValidation) {
                    email.validate()
                }

                // then
                actual shouldBeRight email
            }
        }
    }

    context("PasswordValidation") {
        should(
            "pass if password is " +
                "at least 5 characters long, " +
                "has no whitespaces, and " +
                "has at least one digit, uppercase character and special character"
        ) {
            checkAll(Arb.password(5..50)) { string ->
                // given
                val password = User.Password(string)

                // when
                val actual = with(PasswordValidation) {
                    password.validate()
                }

                // then
                actual shouldBeRight password
            }
        }

        should("accumulate errors from all validation steps") {
            // given
            val password = User.Password("pas ")

            // when
            val actual = with(PasswordValidation) {
                password.validate()
            }

            // then
            actual shouldBeLeft nonEmptyListOf(
                TooShortPassword,
                WhitespaceInPassword,
                NoDigitsInPassword,
                NoUppercaseCharsInPassword,
                NoSpecialCharsInPassword
            )
        }
    }
})

private fun String.isAlphanumeric() = all(Char::isLetterOrDigit)

private fun Arb.Companion.password(range: IntRange) = arbitrary {
    val length = Arb.int(range).single()
    val password = StringBuilder(length)

    password.append(Arb.int(0..9).single().toString())
    password.append(Arb.char('A'..'Z').single())
    password.append(Arb.element(SPECIAL_CHARACTERS).single())

    while (password.length < length) {
        password.append(Arb.char('A'..'Z', 'a'..'z', '0'..'9').single())
    }

    password.toString().toList().shuffled().joinToString("")
}
