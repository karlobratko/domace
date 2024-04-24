package hr.algebra.domace.infrastructure.security.jwt

import arrow.core.raise.catch
import arrow.core.raise.either
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

typealias JwtToTokenClaimsConversionScope<T> = FailingConversionScope<ValidationNotPerformed, DecodedJWT<T>, Claims>

fun <T : JWSAlgorithm> JwtToTokenClaimsConversion(format: StringFormat) = JwtToTokenClaimsConversionScope<T> {
    either {
        val issuer = Claims.Issuer(issuer().getOrRaise { ValidationNotPerformed })
        val subject = Claims.Subject(subject().getOrRaise { ValidationNotPerformed })
        val issuedAt = Claims.IssuedAt(issuedAt().getOrRaise { ValidationNotPerformed }.toKotlinInstant())
        val expiresAt = Claims.ExpiresAt(expiresAt().getOrRaise { ValidationNotPerformed }.toKotlinInstant())

        val audience = catch({
            format.decodeFromString(
                ListSerializer(String.serializer()),
                audience().getOrRaise { ValidationNotPerformed }
            ).toNonEmptyListOrRaise { ValidationNotPerformed }.map { Claims.Audience(it) }
        }) { raise(ValidationNotPerformed) }

        val use = catch({
            Claims.Use.valueOf(use().getOrRaise { ValidationNotPerformed })
        }) { raise(ValidationNotPerformed) }

        when (use) {
            Claims.Use.Refresh -> Claims.Refresh(issuer, subject, audience, issuedAt, expiresAt)
            Claims.Use.Access -> Claims.Access(issuer, subject, audience, issuedAt, expiresAt)
        }
    }
}
