package hr.algebra.domace.infrastructure.security.jwt

import arrow.core.Option
import arrow.core.nel
import arrow.core.nonEmptyListOf
import arrow.core.none
import arrow.core.some
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.EmptyAudienceClaim
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.InvalidAudienceClaim
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.MissingAudienceClaim
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.MissingExpiresAtClaim
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.MissingIssuedAtClaim
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.MissingIssuerClaim
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.MissingRoleClaim
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.MissingSubjectClaim
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.MissingUseClaim
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.UnsupportedUseClaim
import hr.algebra.domace.domain.security.jwt.Claims
import io.github.nefilim.kjwt.DecodedJWT
import io.github.nefilim.kjwt.JWSAlgorithm
import io.github.nefilim.kjwt.JWSHMAC512Algorithm
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

object JwtValidationTests : ShouldSpec({
    should(
        "pass if " +
            "issuer is present, " +
            "subject is present, " +
            "audience are present, in valid format, and non-empty, " +
            "use is present and of supported type, " +
            "issuedAt is present and " +
            "expiresAt is present"
    ) {
        val decoded = decodedJWT<JWSHMAC512Algorithm>()

        val actual = with(JwtValidation<JWSHMAC512Algorithm>(TestJwtStringFormat)) {
            decoded.validate()
        }

        actual shouldBeRight decoded
    }

    should("accumulate errors from all validation steps") {
        val decoded = decodedJWT<JWSHMAC512Algorithm>(
            issuerAnswer = ::none,
            subjectAnswer = ::none,
            audienceAnswer = ::none,
            useAnswer = ::none,
            issuedAtAnswer = ::none,
            expiresAtAnswer = ::none,
            roleAnswer = ::none
        )

        val actual = with(JwtValidation<JWSHMAC512Algorithm>(TestJwtStringFormat)) {
            decoded.validate()
        }

        actual.shouldBeLeft() shouldContainExactlyInAnyOrder nonEmptyListOf(
            MissingIssuerClaim,
            MissingSubjectClaim,
            MissingAudienceClaim,
            MissingUseClaim,
            MissingIssuedAtClaim,
            MissingExpiresAtClaim,
            MissingRoleClaim
        )
    }

    context("invalid audience claim") {
        should("fail on missing audience claim") {
            val decoded = decodedJWT<JWSHMAC512Algorithm>(
                audienceAnswer = ::none
            )

            val actual = with(JwtValidation<JWSHMAC512Algorithm>(TestJwtStringFormat)) {
                decoded.validate()
            }

            actual shouldBeLeft MissingAudienceClaim.nel()
        }

        should("fail on unsupported audience string format") {
            val decoded = decodedJWT<JWSHMAC512Algorithm>(
                audienceAnswer = { "{}".some() }
            )

            val actual = with(JwtValidation<JWSHMAC512Algorithm>(TestJwtStringFormat)) {
                decoded.validate()
            }

            actual shouldBeLeft InvalidAudienceClaim.nel()
        }

        should("fail on empty audience list") {
            val decoded = decodedJWT<JWSHMAC512Algorithm>(
                audienceAnswer = { "[]".some() }
            )

            val actual = with(JwtValidation<JWSHMAC512Algorithm>(TestJwtStringFormat)) {
                decoded.validate()
            }

            actual shouldBeLeft EmptyAudienceClaim.nel()
        }
    }

    context("invalid use claim") {
        should("fail on missing use claim") {
            val decoded = decodedJWT<JWSHMAC512Algorithm>(
                useAnswer = ::none
            )

            val actual = with(JwtValidation<JWSHMAC512Algorithm>(TestJwtStringFormat)) {
                decoded.validate()
            }

            actual shouldBeLeft MissingUseClaim.nel()
        }

        should("fail on unsupported audience string format") {
            val decoded = decodedJWT<JWSHMAC512Algorithm>(
                useAnswer = { "unsupported".some() }
            )

            val actual = with(JwtValidation<JWSHMAC512Algorithm>(TestJwtStringFormat)) {
                decoded.validate()
            }

            actual shouldBeLeft UnsupportedUseClaim.nel()
        }
    }
})

val TestJwtStringFormat = Json

fun <T : JWSAlgorithm> decodedJWT(
    issuerAnswer: () -> Option<String> = { "issuer".some() },
    subjectAnswer: () -> Option<String> = { "subject".some() },
    roleAnswer: () -> Option<String> = { "Admin".some() },
    audienceAnswer: () -> Option<String> = {
        TestJwtStringFormat.encodeToString(
            ListSerializer(String.serializer()),
            nonEmptyListOf("audience-1", "audience-2")
        ).some()
    },
    useAnswer: () -> Option<String> = { Claims.Use.Access.name.some() },
    issuedAtAnswer: () -> Option<Instant> = { Clock.System.now().some() },
    expiresAtAnswer: () -> Option<Instant> = { Clock.System.now().some() }
): DecodedJWT<T> = mockk<DecodedJWT<T>> {
    every { issuer() } answers { issuerAnswer() }
    every { subject() } answers { subjectAnswer() }
    every { audience() } answers { audienceAnswer() }
    every { use() } answers { useAnswer() }
    every { issuedAt() } answers { issuedAtAnswer().map { it.toJavaInstant() } }
    every { expiresAt() } answers { expiresAtAnswer().map { it.toJavaInstant() } }
    every { role() } answers { roleAnswer() }
}
