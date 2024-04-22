package hr.algebra.domace.domain.validation

import arrow.core.nel
import arrow.core.nonEmptyListOf
import hr.algebra.domace.domain.SecurityError.MalformedSubject
import hr.algebra.domace.domain.SecurityError.TokenExpired
import hr.algebra.domace.domain.kotest.instant
import hr.algebra.domace.domain.security.Claims
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.az
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
            checkAll(Arb.long().map { it.toString() }) { string ->
                // given
                val subject = Claims.Subject(string)

                // when
                val actual = with(SubjectValidation) {
                    subject.validate()
                }

                // then
                actual shouldBeRight subject
            }
        }

        should("fail if subject can't be parsed to Long") {
            checkAll(Arb.string(codepoints = Codepoint.az())) { string ->
                // given
                val subject = Claims.Subject(string)

                // when
                val actual = with(SubjectValidation) {
                    subject.validate()
                }

                // then
                actual shouldBeLeft MalformedSubject.nel()
            }
        }
    }

    context("ExpiresAtValidation") {
        should("pass if not expired") {
            checkAll(Arb.instant(now().plus(1.seconds)..DistantFuture)) { instant ->
                // given
                val expiresAt = Claims.ExpiresAt(instant)

                // when
                val actual = with(ExpiresAtValidation) {
                    expiresAt.validate()
                }

                // then
                actual shouldBeRight expiresAt
            }
        }

        should("fail if expired") {
            checkAll(Arb.instant(DistantPast..now().minus(1.seconds))) { instant ->
                // given
                val expiresAt = Claims.ExpiresAt(instant)

                // when
                val actual = with(ExpiresAtValidation) {
                    expiresAt.validate()
                }

                // then
                actual shouldBeLeft TokenExpired.nel()
            }
        }
    }

    context("ClaimsValidation") {
        should(
            "pass if " +
                "subject can be parsed to long, " +
                "is not expired, " +
                "regardless of use"
        ) {
            checkAll(
                Arb.long().map { it.toString() },
                Arb.instant(now().plus(1.seconds)..DistantFuture)
            ) { a, b ->
                // given
                val issuer = Claims.Issuer("")
                val subject = Claims.Subject(a)
                val audience = nonEmptyListOf(Claims.Audience(""))
                val issuedAt = Claims.IssuedAt()
                val expiresAt = Claims.ExpiresAt(b)
                val claims = Claims.Access(issuer, subject, audience, issuedAt, expiresAt)

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
                Arb.string(codepoints = Codepoint.az()),
                Arb.instant(DistantPast..now().minus(1.seconds))
            ) { a, b ->
                // given
                val issuer = Claims.Issuer("")
                val subject = Claims.Subject(a)
                val audience = nonEmptyListOf(Claims.Audience(""))
                val issuedAt = Claims.IssuedAt()
                val expiresAt = Claims.ExpiresAt(b)
                val claims = Claims.Refresh(issuer, subject, audience, issuedAt, expiresAt)

                // when
                val actual = with(ClaimsValidation) {
                    claims.validate()
                }

                // then
                actual shouldBeLeft nonEmptyListOf(
                    MalformedSubject,
                    TokenExpired
                )
            }
        }
    }
})
