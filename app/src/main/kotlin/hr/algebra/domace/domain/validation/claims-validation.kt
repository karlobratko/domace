package hr.algebra.domace.domain.validation

import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import hr.algebra.domace.domain.SecurityError
import hr.algebra.domace.domain.SecurityError.MalformedSubject
import hr.algebra.domace.domain.SecurityError.TokenExpired
import hr.algebra.domace.domain.applyWrapEitherNel
import hr.algebra.domace.domain.security.Claims
import kotlinx.datetime.Clock.System.now

typealias SubjectValidationScope = ValidationScope<SecurityError, Claims.Subject>

val SubjectValidation =
    SubjectValidationScope {
        applyWrapEitherNel {
            ensureNotNull(value.toLongOrNull()) { MalformedSubject }
        }
    }

typealias ExpiresAtValidationScope = ValidationScope<SecurityError, Claims.ExpiresAt>

val ExpiresAtValidation =
    ExpiresAtValidationScope {
        applyWrapEitherNel {
            ensure(now() < value) { TokenExpired }
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
