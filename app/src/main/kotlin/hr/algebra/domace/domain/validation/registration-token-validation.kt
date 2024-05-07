package hr.algebra.domace.domain.validation

import arrow.core.raise.ensure
import hr.algebra.domace.domain.SecurityError.RegistrationError
import hr.algebra.domace.domain.SecurityError.RegistrationError.RegistrationTokenAlreadyConfirmed
import hr.algebra.domace.domain.SecurityError.RegistrationError.RegistrationTokenExpired
import hr.algebra.domace.domain.SecurityError.RegistrationError.RegistrationTokenNotConfirmed
import hr.algebra.domace.domain.SecurityError.RegistrationError.RegistrationTokenStillValid
import hr.algebra.domace.domain.applyWrapEitherNel
import hr.algebra.domace.domain.config.DefaultInstantProvider
import hr.algebra.domace.domain.model.RegistrationToken
import hr.algebra.domace.domain.model.RegistrationToken.ConfirmationStatus
import hr.algebra.domace.domain.model.RegistrationToken.ConfirmationStatus.Confirmed
import hr.algebra.domace.domain.model.RegistrationToken.ConfirmationStatus.Unconfirmed

typealias RegistrationTokenExpiresAtValidationScope = ValidationScope<RegistrationError, RegistrationToken.ExpiresAt>

val RegistrationTokenExpiredValidation = RegistrationTokenExpiresAtValidationScope {
    applyWrapEitherNel {
        ensure(value < DefaultInstantProvider.now()) { RegistrationTokenStillValid }
    }
}

val RegistrationTokenNotExpiredValidation = RegistrationTokenExpiresAtValidationScope {
    applyWrapEitherNel {
        ensure(value >= DefaultInstantProvider.now()) { RegistrationTokenExpired }
    }
}

typealias ConfirmationStatusValidationScope = ValidationScope<RegistrationError, ConfirmationStatus>

val RegistrationTokenUnconfirmedValidation = ConfirmationStatusValidationScope {
    applyWrapEitherNel {
        ensure(this@ConfirmationStatusValidationScope is Unconfirmed) { RegistrationTokenAlreadyConfirmed }
    }
}

val RegistrationTokenConfirmedValidation = ConfirmationStatusValidationScope {
    applyWrapEitherNel {
        ensure(this@ConfirmationStatusValidationScope is Confirmed) { RegistrationTokenNotConfirmed }
    }
}
