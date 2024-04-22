package hr.algebra.domace.infrastructure.security.jwt

import arrow.core.None
import arrow.core.getOrElse
import arrow.core.raise.catch
import arrow.core.raise.ensure
import arrow.core.some
import arrow.core.toNonEmptyListOrNone
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.MissingAudienceClaim
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.MissingExpiresAtClaim
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.MissingIssuedAtClaim
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.MissingIssuerClaim
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.MissingSubjectClaim
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.MissingUseClaim
import hr.algebra.domace.domain.SecurityError.ClaimsValidationError.UnsupportedUseClaim
import hr.algebra.domace.domain.security.Claims
import hr.algebra.domace.domain.toEitherNel
import hr.algebra.domace.domain.validation.ValidationScope
import hr.algebra.domace.domain.validation.validate
import hr.algebra.domace.domain.wrapEitherNel
import io.github.nefilim.kjwt.DecodedJWT
import io.github.nefilim.kjwt.JWSAlgorithm
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

typealias JwtValidationScope<T> = ValidationScope<ClaimsValidationError, DecodedJWT<T>>

fun <T : JWSAlgorithm> JwtValidation() =
    JwtValidationScope<T> {
        validate(this) {
            with { jwt -> jwt.issuer().toEitherNel { MissingIssuerClaim } }
            with { jwt -> jwt.subject().toEitherNel { MissingSubjectClaim } }
            with { jwt ->
                jwt.audience()
                    .flatMap {
                        catch({
                            Json.decodeFromString(ListSerializer(String.serializer()), it).some()
                        }) { None }
                    }
                    .map { it.toNonEmptyListOrNone() }
                    .toEitherNel { MissingAudienceClaim }
            }
            with { jwt -> jwt.use().toEitherNel { MissingUseClaim } }
            with { jwt ->
                wrapEitherNel {
                    val use = jwt.use().getOrElse { "" }
                    ensure(use == Claims.Use.Access.name || use == Claims.Use.Refresh.name) { UnsupportedUseClaim }
                }
            }
            with { jwt -> jwt.issuedAt().toEitherNel { MissingIssuedAtClaim } }
            with { jwt -> jwt.expiresAt().toEitherNel { MissingExpiresAtClaim } }
        }
    }
