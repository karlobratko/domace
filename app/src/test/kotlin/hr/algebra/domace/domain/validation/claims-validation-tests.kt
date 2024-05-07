package hr.algebra.domace.domain.validation

import arrow.core.nel
import arrow.core.nonEmptyListOf
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.UnsupportedRoleClaim
import hr.algebra.domace.domain.SecurityError.MalformedSubject
import hr.algebra.domace.domain.SecurityError.TokenExpired
import hr.algebra.domace.domain.kotest.instant
import hr.algebra.domace.domain.model.User
import hr.algebra.domace.domain.security.jwt.Claims
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.az
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.filterNot
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.datetime.Clock.System.now
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.Instant.Companion.DISTANT_FUTURE as DistantFuture
import kotlinx.datetime.Instant.Companion.DISTANT_PAST as DistantPast

object ClaimsValidationTests : ShouldSpec({
    context("SubjectValidation") {
        should("pass if subject can be parsed to Long") {
            checkAll(Arb.long().map { Claims.Subject(it.toString()) }) { subject ->
                val actual = with(SubjectValidation) {
                    subject.validate()
                }

                actual shouldBeRight subject
            }
        }

        should("fail if subject can't be parsed to Long") {
            checkAll(Arb.string(codepoints = Codepoint.az()).map(Claims::Subject)) { subject ->
                val actual = with(SubjectValidation) {
                    subject.validate()
                }

                actual shouldBeLeft MalformedSubject.nel()
            }
        }
    }

    context("RoleClaimValidation") {
        should("pass if Claims.Role can be parsed to User.Role") {
            checkAll(Arb.enum<User.Role>().map { Claims.Role(it.toString()) }) { role ->
                val actual = with(RoleClaimValidation) {
                    role.validate()
                }

                actual shouldBeRight role
            }
        }

        should("fail if Claims.Role can't be parsed to User.Role") {
            checkAll(
                Arb.string(1..20, Codepoint.alphanumeric())
                    .filterNot { User.Role.entries.map(User.Role::name).contains(it) }
                    .map(Claims::Role)
            ) { role ->
                val actual = with(RoleClaimValidation) {
                    role.validate()
                }

                actual shouldBeLeft UnsupportedRoleClaim.nel()
            }
        }
    }

    context("ExpiresAtValidation") {
        should("pass if not expired") {
            checkAll(Arb.instant(now().plus(1.seconds)..DistantFuture).map(Claims::ExpiresAt)) { expiresAt ->
                val actual = with(ExpiresAtValidation) {
                    expiresAt.validate()
                }

                actual shouldBeRight expiresAt
            }
        }

        should("fail if expired") {
            checkAll(Arb.instant(DistantPast..now().minus(1.seconds)).map(Claims::ExpiresAt)) { expiresAt ->
                val actual = with(ExpiresAtValidation) {
                    expiresAt.validate()
                }

                actual shouldBeLeft TokenExpired.nel()
            }
        }
    }

    context("ClaimsValidation") {
        should(
            "pass if " +
                "subject can be parsed to long, " +
                "role is valid, " +
                "is not expired, " +
                "regardless of use"
        ) {
            checkAll(
                Arb.long().map { Claims.Subject(it.toString()) },
                Arb.enum<User.Role>().map { Claims.Role(it.toString()) },
                Arb.instant(now().plus(1.seconds)..DistantFuture).map(Claims::ExpiresAt)
            ) { subject, role, expiresAt ->
                // given
                val issuer = Claims.Issuer("")
                val audience = nonEmptyListOf(Claims.Audience(""))
                val issuedAt = Claims.IssuedAt()
                val claims = Claims.Access(issuer, subject, audience, issuedAt, expiresAt, role)

                // when
                val actual = with(ClaimsValidation) {
                    claims.validate()
                }

                // then
                actual shouldBeRight claims
            }
        }

        should("accumulate errors from all validation steps") {
            checkAll(
                Arb.string(codepoints = Codepoint.az()).map(Claims::Subject),
                Arb.string(1..20, Codepoint.alphanumeric())
                    .filterNot { User.Role.entries.map(User.Role::name).contains(it) }
                    .map(Claims::Role),
                Arb.instant(DistantPast..now().minus(1.seconds)).map(Claims::ExpiresAt)
            ) { subject, role, expiresAt ->
                // given
                val issuer = Claims.Issuer("")
                val audience = nonEmptyListOf(Claims.Audience(""))
                val issuedAt = Claims.IssuedAt()
                val claims = Claims.Refresh(issuer, subject, audience, issuedAt, expiresAt, role)

                // when
                val actual = with(ClaimsValidation) {
                    claims.validate()
                }

                // then
                actual.shouldBeLeft() shouldContainExactlyInAnyOrder nonEmptyListOf(
                    MalformedSubject,
                    UnsupportedRoleClaim,
                    TokenExpired
                )
            }
        }
    }
})
