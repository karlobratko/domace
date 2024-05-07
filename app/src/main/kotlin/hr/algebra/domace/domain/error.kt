package hr.algebra.domace.domain

sealed interface DomainError

data object UnhandledError : DomainError

sealed interface MailingError : DomainError {
    data object CouldNotSendEmail : MailingError
}

sealed interface RequestError : DomainError {
    data object RequestBodyCouldNotBeParsed : RequestError

    data object InvalidRequestPathParameter : RequestError

    data object InvalidRequestQueryParameter : RequestError
}

sealed interface DbError : DomainError {
    data object NothingWasChanged : DbError

    data object ValueAlreadyExists : DbError

    data object UsernameAlreadyExists : DbError

    data object EmailAlreadyExists : DbError

    data object InvalidUsernameOrPassword : DbError

    data object UnhandledDbError : DbError

    data object TokenAlreadyExists : DbError
}

sealed interface SecurityError : DomainError {
    data object TokenGenerationError : SecurityError

    data object TokenVerificationError : SecurityError

    interface ClaimsValidationError : SecurityError {
        data object MissingIssuerClaim : ClaimsValidationError

        data object MissingSubjectClaim : ClaimsValidationError

        data object MissingAudienceClaim : ClaimsValidationError

        data object InvalidAudienceClaim : ClaimsValidationError

        data object EmptyAudienceClaim : ClaimsValidationError

        data object MissingUseClaim : ClaimsValidationError

        data object UnsupportedUseClaim : ClaimsValidationError

        data object MissingIssuedAtClaim : ClaimsValidationError

        data object MissingExpiresAtClaim : ClaimsValidationError

        data object MissingRoleClaim : ClaimsValidationError

        data object UnsupportedRoleClaim : ClaimsValidationError
    }

    data object ClaimsExtractionError : SecurityError

    data object MalformedSubject : SecurityError

    data object TokenExpired : SecurityError

    data object UnknownToken : SecurityError

    data object InvalidRefreshTokenStatus : SecurityError

    sealed interface AuthenticationError : SecurityError {
        data object MissingAuthorizationHeader : AuthenticationError
    }

    sealed interface AuthorizationError : SecurityError {
        data object UnauthorizedResourceAccess : AuthorizationError
    }

    sealed interface RegistrationError : SecurityError {
        data object UnknownRegistrationToken : RegistrationError

        data object RegistrationTokenExpired : RegistrationError

        data object RegistrationTokenStillValid : RegistrationError

        data object RegistrationTokenAlreadyConfirmed : RegistrationError

        data object RegistrationTokenNotConfirmed : RegistrationError
    }
}

sealed interface ConversionError : DomainError {
    data object ValidationNotPerformed : ConversionError
}

sealed interface ValidationError : DomainError {
    sealed interface UserValidationError : ValidationError {
        sealed interface UsernameValidationError : UserValidationError {
            data object NonAlphanumericCharacterInUsername : UsernameValidationError

            data object TooShortUsername : UsernameValidationError

            data object TooLongUsername : UsernameValidationError
        }

        sealed interface EmailValidationError : UserValidationError {
            data object InvalidEmail : EmailValidationError

            data object TooLongEmail : EmailValidationError
        }

        sealed interface PasswordValidationError : UserValidationError {
            data object TooShortPassword : PasswordValidationError

            data object NoDigitsInPassword : PasswordValidationError

            data object WhitespaceInPassword : PasswordValidationError

            data object NoUppercaseCharsInPassword : PasswordValidationError

            data object NoSpecialCharsInPassword : PasswordValidationError
        }

        sealed interface RoleValidationError : UserValidationError {
            data object AdminCanNotBeCreated : RoleValidationError
        }
    }
}
