package hr.algebra.domace.domain

sealed interface DomainError
sealed interface MailingError : DomainError {
    data object CouldNotSendEmail : MailingError
}

data object RequestBodyCouldNotBeParsed : DomainError

sealed interface DbError : DomainError {
    data object NothingWasChanged : DbError

    data object ValueAlreadyExists : DbError

    data object UsernameAlreadyExists : DbError

    data object EmailAlreadyExists : DbError

    data object InvalidUsernameOrPassword : DbError

    data object UnhandledDbError : DbError
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
    }

    data object ClaimsExtractionError : SecurityError

    data object MalformedSubject : SecurityError

    data object TokenExpired : SecurityError

    data object UnknownToken : SecurityError

    data object InvalidRefreshTokenStatus : SecurityError

    sealed interface AuthenticationError : SecurityError {
        data object MissingAuthorizationHeader : SecurityError
    }

    sealed interface AuthorizationError : SecurityError {
        data object UnknownSubjectResourceAccess : SecurityError
        data object UnauthorizedResourceAccess : SecurityError
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
    }
}
