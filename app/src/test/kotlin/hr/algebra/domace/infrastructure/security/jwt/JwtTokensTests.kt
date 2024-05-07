package hr.algebra.domace.infrastructure.security.jwt

import arrow.core.nonEmptyListOf
import hr.algebra.domace.domain.security.jwt.Claims
import hr.algebra.domace.domain.security.Secret
import hr.algebra.domace.domain.security.jwt.generateAccessToken
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import kotlin.time.Duration.Companion.minutes

object JwtTokensTests : ShouldSpec({
    val algebra = JwtTokens(Secret("6d792d73757065722d7365637572652d736563726574"))

    should("generate token based on claims and extract claims based on token") {
        val claims = Claims.Access(
            issuer = Claims.Issuer("issuer"),
            subject = Claims.Subject("subject"),
            audience = nonEmptyListOf(Claims.Audience("audience-1")),
            issuedAt = Claims.IssuedAt(),
            expiresAt = Claims.ExpiresAt(Claims.IssuedAt().value + 15.minutes),
            role = Claims.Role("Admin"),
        )

        val token = with(algebra) { claims.generateAccessToken() }.shouldBeRight()

        with(algebra) { token.extractClaims() } shouldBeRight claims
    }
})
