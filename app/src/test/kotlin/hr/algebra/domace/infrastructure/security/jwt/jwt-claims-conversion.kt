package hr.algebra.domace.infrastructure.security.jwt

import arrow.core.nonEmptyListOf
import arrow.core.none
import arrow.core.some
import hr.algebra.domace.domain.ConversionError.ValidationNotPerformed
import hr.algebra.domace.domain.security.jwt.Claims
import io.github.nefilim.kjwt.JWSHMAC512Algorithm
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.time.Duration.Companion.minutes

object JwtClaimsConversion : ShouldSpec({
    context("JwtToTokenClaimsConversion") {
        should("convert from string claims to Claims based on use type") {
            val issuer = Claims.Issuer("issuer")
            val subject = Claims.Subject("subject")
            val role = Claims.Role("Admin")
            val audience = nonEmptyListOf(Claims.Audience("audience-1"))
            val use = Claims.Use.Access
            val issuedAt = Claims.IssuedAt()
            val expiresAt = Claims.ExpiresAt(issuedAt.value + 15.minutes)

            val decoded = decodedJWT<JWSHMAC512Algorithm>(
                issuerAnswer = { issuer.value.some() },
                subjectAnswer = { subject.value.some() },
                audienceAnswer = {
                    TestJwtStringFormat.encodeToString(
                        ListSerializer(String.serializer()),
                        audience.map { it.value }
                    ).some()
                },
                useAnswer = { use.name.some() },
                issuedAtAnswer = { issuedAt.value.some() },
                expiresAtAnswer = { expiresAt.value.some() },
            )

            val actual = with(JwtToTokenClaimsConversion<JWSHMAC512Algorithm>(TestJwtStringFormat)) {
                decoded.convert()
            }

            actual shouldBeRight Claims.Access(
                issuer = issuer,
                subject = subject,
                audience = audience,
                issuedAt = issuedAt,
                expiresAt = expiresAt,
                role = role
            )
        }

        should("fail on first error and not accumulate errors from validation steps") {
            val decoded = decodedJWT<JWSHMAC512Algorithm>(
                issuerAnswer = ::none,
                subjectAnswer = ::none,
                audienceAnswer = ::none,
                useAnswer = ::none,
                issuedAtAnswer = ::none,
                expiresAtAnswer = ::none,
                roleAnswer = ::none
            )

            val actual = with(JwtToTokenClaimsConversion<JWSHMAC512Algorithm>(TestJwtStringFormat)) {
                decoded.convert()
            }

            actual shouldBeLeft ValidationNotPerformed
        }

        context("invalid audience claim") {
            should("fail on missing audience claim") {
                val decoded = decodedJWT<JWSHMAC512Algorithm>(
                    audienceAnswer = ::none
                )

                val actual = with(JwtToTokenClaimsConversion<JWSHMAC512Algorithm>(TestJwtStringFormat)) {
                    decoded.convert()
                }

                actual shouldBeLeft ValidationNotPerformed
            }

            should("fail on unsupported audience string format") {
                val decoded = decodedJWT<JWSHMAC512Algorithm>(
                    audienceAnswer = { "{}".some() }
                )

                val actual = with(JwtToTokenClaimsConversion<JWSHMAC512Algorithm>(TestJwtStringFormat)) {
                    decoded.convert()
                }

                actual shouldBeLeft ValidationNotPerformed
            }

            should("fail on empty audience list") {
                val decoded = decodedJWT<JWSHMAC512Algorithm>(
                    audienceAnswer = { "[]".some() }
                )

                val actual = with(JwtToTokenClaimsConversion<JWSHMAC512Algorithm>(TestJwtStringFormat)) {
                    decoded.convert()
                }

                actual shouldBeLeft ValidationNotPerformed
            }
        }

        context("invalid use claim") {
            should("fail on missing use claim") {
                val decoded = decodedJWT<JWSHMAC512Algorithm>(
                    useAnswer = ::none
                )

                val actual = with(JwtToTokenClaimsConversion<JWSHMAC512Algorithm>(TestJwtStringFormat)) {
                    decoded.convert()
                }

                actual shouldBeLeft ValidationNotPerformed
            }

            should("fail on unsupported audience string format") {
                val decoded = decodedJWT<JWSHMAC512Algorithm>(
                    useAnswer = { "unsupported".some() }
                )

                val actual = with(JwtToTokenClaimsConversion<JWSHMAC512Algorithm>(TestJwtStringFormat)) {
                    decoded.convert()
                }

                actual shouldBeLeft ValidationNotPerformed
            }
        }
    }
})
