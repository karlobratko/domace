package hr.algebra.domace.domain.validation

import arrow.core.raise.ensure
import hr.algebra.domace.domain.SecurityError
import hr.algebra.domace.domain.SecurityError.InvalidRefreshTokenStatus
import hr.algebra.domace.domain.model.RefreshToken
import hr.algebra.domace.domain.wrapEitherNel

typealias RefreshTokenValidationScope = ValidationScope<SecurityError, RefreshToken.Entity>

fun RefreshTokenValidation(status: RefreshToken.Status) =
    RefreshTokenValidationScope {
        validate(this) {
            with { token -> validate(token.expiresAt, ExpiresAtValidation) }
            with { token ->
                wrapEitherNel {
                    ensure(token.status == status) { InvalidRefreshTokenStatus }
                }
            }
        }
    }
