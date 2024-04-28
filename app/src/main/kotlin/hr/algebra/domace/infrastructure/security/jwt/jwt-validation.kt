package hr.algebra.domace.infrastructure.security.jwt

import arrow.core.Either.Companion.catch
import arrow.core.flatMap
import arrow.core.nel
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.EmptyAudienceClaim
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.InvalidAudienceClaim
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.MissingAudienceClaim
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.MissingExpiresAtClaim
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.MissingIssuedAtClaim
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.MissingIssuerClaim
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.MissingSubjectClaim
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.MissingUseClaim
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.UnsupportedUseClaim
import hr.algebra.domace.domain.security.Claims
import hr.algebra.domace.domain.toEitherNel
import hr.algebra.domace.domain.toNonEmptyListOrLeftNel
import hr.algebra.domace.domain.validation.ValidationScope
import hr.algebra.domace.domain.validation.validate
import io.github.nefilim.kjwt.DecodedJWT
import io.github.nefilim.kjwt.JWSAlgorithm
import kotlinx.serialization.StringFormat
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

typealias JwtValidationScope<T> = ValidationScope<ClaimsValidationError, DecodedJWT<T>>

fun <T : JWSAlgorithm> JwtValidation(format: StringFormat) = JwtValidationScope<T> {
    validate(this) {
        with { jwt -> jwt.issuer().toEitherNel { MissingIssuerClaim } }
        with { jwt -> jwt.subject().toEitherNel { MissingSubjectClaim } }
        with { jwt ->
            jwt.audience().toEitherNel { MissingAudienceClaim }
                .flatMap {
                    catch {
                        format.decodeFromString(ListSerializer(String.serializer()), it)
                    }.mapLeft { InvalidAudienceClaim.nel() }
                }
                .flatMap { it.toNonEmptyListOrLeftNel { EmptyAudienceClaim } }
        }
        with { jwt ->
            jwt.use().toEitherNel { MissingUseClaim }
                .flatMap {
                    catch {
                        Claims.Use.valueOf(it)
                    }.mapLeft { UnsupportedUseClaim.nel() }
                }
        }
        with { jwt -> jwt.issuedAt().toEitherNel { MissingIssuedAtClaim } }
        with { jwt -> jwt.expiresAt().toEitherNel { MissingExpiresAtClaim } }
    }
}
