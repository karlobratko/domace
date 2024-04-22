package hr.algebra.domace.infrastructure.security.jwt

import arrow.core.raise.either
import hr.algebra.domace.domain.ConversionError
import hr.algebra.domace.domain.ConversionError.ValidationNotPerformed
import hr.algebra.domace.domain.conversion.FailingConversionScope
import hr.algebra.domace.domain.getOrRaise
import hr.algebra.domace.domain.security.Claims
import hr.algebra.domace.domain.toNonEmptyListOrRaise
import io.github.nefilim.kjwt.DecodedJWT
import io.github.nefilim.kjwt.JWSAlgorithm
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.StringFormat
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

typealias JwtToTokenClaimsConversionScope<T> = FailingConversionScope<ConversionError, DecodedJWT<T>, Claims>

fun <T : JWSAlgorithm> JwtToTokenClaimsConversion(format: StringFormat = Json) =
    JwtToTokenClaimsConversionScope<T> {
        either {
            val issuer = Claims.Issuer(issuer().getOrRaise { ValidationNotPerformed })
            val subject = Claims.Subject(subject().getOrRaise { ValidationNotPerformed })
            val audience =
                format.decodeFromString(
                    ListSerializer(String.serializer()),
                    audience().getOrRaise { ValidationNotPerformed }
                ).map { Claims.Audience(it) }.toNonEmptyListOrRaise { ValidationNotPerformed }
            val use = Claims.Use.valueOf(use().getOrRaise { ValidationNotPerformed })
            val issuedAt = Claims.IssuedAt(issuedAt().getOrRaise { ValidationNotPerformed }.toKotlinInstant())
            val expiresAt = Claims.ExpiresAt(expiresAt().getOrRaise { ValidationNotPerformed }.toKotlinInstant())

            when (use) {
                Claims.Use.Refresh -> Claims.Refresh(issuer, subject, audience, issuedAt, expiresAt)
                Claims.Use.Access -> Claims.Access(issuer, subject, audience, issuedAt, expiresAt)
            }
        }
    }
