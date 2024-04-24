package hr.algebra.domace.domain.conversion

import hr.algebra.domace.domain.ConversionError.ValidationNotPerformed
import hr.algebra.domace.domain.model.User
import hr.algebra.domace.domain.security.Claims
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.filterNot
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

object ClaimsConversionTests : ShouldSpec({
    should("convert from User.Id to Subject") {
        checkAll(Arb.long().map(User::Id)) { userId ->
            val subject = with(UserIdToSubjectConversion) {
                userId.convert()
            }

            subject.value shouldBeEqual userId.value.toString()
        }
    }

    should("convert from Subject to User.Id if numeric subject is provided") {
        checkAll(Arb.long().map(Long::toString).map(Claims::Subject)) { subject ->
            val userId = with(SubjectToUserIdConversion) {
                subject.convert()
            }

            userId shouldBeRight User.Id(subject.value.toLong())
        }
    }

    should("fail convert from Subject to User.Id if alphanumeric subject is provided") {
        checkAll(
            Arb.string(1..20, Codepoint.alphanumeric()).filterNot { it.all(Char::isDigit) }.map(Claims::Subject)
        ) { subject ->
            val userId = with(SubjectToUserIdConversion) {
                subject.convert()
            }

            userId shouldBeLeft ValidationNotPerformed
        }
    }
})
