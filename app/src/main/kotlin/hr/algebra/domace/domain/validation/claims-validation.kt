package hr.algebra.domace.domain.validation

import arrow.core.raise.ensure
import hr.algebra.domace.domain.SecurityError
import hr.algebra.domace.domain.SecurityError.MalformedSubject
import hr.algebra.domace.domain.SecurityError.TokenExpired
import hr.algebra.domace.domain.applyWrapEitherNel
import hr.algebra.domace.domain.config.RoundedInstantProvider
import hr.algebra.domace.domain.security.Claims
import hr.algebra.domace.domain.toLongOrLeftNel

typealias SubjectValidationScope = ValidationScope<SecurityError, Claims.Subject>

val SubjectValidation =
    SubjectValidationScope {
        value.toLongOrLeftNel { MalformedSubject }.map { this }
    }

typealias ExpiresAtValidationScope = ValidationScope<SecurityError, Claims.ExpiresAt>

val ExpiresAtValidation =
    ExpiresAtValidationScope {
        applyWrapEitherNel {
            ensure(RoundedInstantProvider.now() < value) { TokenExpired }
        }
    }

typealias ClaimsValidationScope = ValidationScope<SecurityError, Claims>

val ClaimsValidation =
    ClaimsValidationScope validationScope@{
        validate(this) {
            with { claims -> validate(claims.subject, SubjectValidation) }
            with { claims -> validate(claims.expiresAt, ExpiresAtValidation) }
        }
    }
