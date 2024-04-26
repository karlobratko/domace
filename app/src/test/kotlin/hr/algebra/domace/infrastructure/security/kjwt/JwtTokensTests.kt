package hr.algebra.domace.infrastructure.security.kjwt

import arrow.core.nonEmptyListOf
import hr.algebra.domace.domain.security.Claims
import hr.algebra.domace.domain.security.generateAccessToken
import hr.algebra.domace.domain.security.Secret
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import kotlin.time.Duration.Companion.minutes

object JwtTokensTests : ShouldSpec({
    val algebra = JwtTokens(Secret("6d792d73757065722d7365637572652d736563726574"))

    should("generate token based on claims and extract claims based on token") {
        val issuer = Claims.Issuer("issuer")
        val subject = Claims.Subject("subject")
        val audience = nonEmptyListOf(Claims.Audience("audience-1"))
        val issuedAt = Claims.IssuedAt()
        val expiresAt = Claims.ExpiresAt(issuedAt.value + 15.minutes)
        val claims = Claims.Access(issuer, subject, audience, issuedAt, expiresAt)

        val token = with(algebra) { claims.generateAccessToken() }.shouldBeRight()

        with(algebra) { token.extractClaims() } shouldBeRight claims
    }
})
