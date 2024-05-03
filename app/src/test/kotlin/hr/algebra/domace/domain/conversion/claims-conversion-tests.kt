package hr.algebra.domace.domain.conversion

import hr.algebra.domace.domain.ConversionError.ValidationNotPerformed
import hr.algebra.domace.domain.model.User
import hr.algebra.domace.domain.security.jwt.Claims
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.filterNot
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

object ClaimsConversionTests : ShouldSpec({
    should("convert from User.Id to Claims.Subject") {
        checkAll(Arb.long().map(User::Id)) { userId ->
            val subject = with(UserIdToSubjectConversion) {
                userId.convert()
            }

            subject.value shouldBeEqual userId.value.toString()
        }
    }

    should("convert from Claims.Subject to User.Id if numeric subject is provided") {
        checkAll(Arb.long().map(Long::toString).map(Claims::Subject)) { subject ->
            val userId = with(SubjectToUserIdConversion) {
                subject.convert()
            }

            userId shouldBeRight User.Id(subject.value.toLong())
        }
    }

    should("fail convert from Claims.Subject to User.Id if alphanumeric subject is provided") {
        checkAll(
            Arb.string(1..20, Codepoint.alphanumeric()).filterNot { it.all(Char::isDigit) }.map(Claims::Subject)
        ) { subject ->
            val userId = with(SubjectToUserIdConversion) {
                subject.convert()
            }

            userId shouldBeLeft ValidationNotPerformed
        }
    }

    should("convert from User.Role to Claims.Role") {
        checkAll(
            Arb.enum<User.Role>()
        ) { userRole ->
            val roleClaim = with(RoleToRoleClaimConversion) {
                userRole.convert()
            }

            roleClaim.value shouldBeEqual userRole.name
        }
    }

    should("convert from Claims.Role to User.Role if valid string is provided") {
        checkAll(
            Arb.enum<User.Role>().map { Claims.Role(it.name) }
        ) { roleClaim ->
            val userRole = with(RoleClaimToRoleConversion) {
                roleClaim.convert()
            }

            userRole shouldBeRight User.Role.valueOf(roleClaim.value)
        }
    }

    should("fail convert from Claims.Role to User.Role if not valid string is provided") {
        checkAll(
            Arb.string(1..20, Codepoint.alphanumeric())
                .filterNot { User.Role.entries.map(User.Role::name).contains(it) }
                .map(Claims::Role)
        ) { roleClaim ->
            val userRole = with(RoleClaimToRoleConversion) {
                roleClaim.convert()
            }

            userRole shouldBeLeft ValidationNotPerformed
        }
    }
})
