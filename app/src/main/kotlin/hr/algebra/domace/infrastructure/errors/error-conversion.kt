package hr.algebra.domace.infrastructure.errors

import arrow.core.Nel
import hr.algebra.domace.domain.ConversionError
import hr.algebra.domace.domain.DbError
import hr.algebra.domace.domain.DomainError
import hr.algebra.domace.domain.MailingError
import hr.algebra.domace.domain.RequestBodyCouldNotBeParsed
import hr.algebra.domace.domain.SecurityError
import hr.algebra.domace.domain.ValidationError
import hr.algebra.domace.domain.conversion.ConversionScope
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.Unauthorized

typealias NelErrorToHttpStatusCodeConversionScope = ConversionScope<Nel<DomainError>, HttpStatusCode>

/**
 * A conversion scope that maps a NonEmptyList of DomainError to an HttpStatusCode.
 * It first maps each DomainError to an HttpStatusCode using the ErrorToHttpStatusCodeConversion.
 * Then it reduces the list of HttpStatusCodes to a single HttpStatusCode.
 * The reduction prioritizes InternalServerError, then Unauthorized, then Forbidden.
 * If none of these status codes are present, it selects the HttpStatusCode with the highest value.
 */
val NelErrorToHttpStatusCodeConversion = NelErrorToHttpStatusCodeConversionScope {
    map {
        with(ErrorToHttpStatusCodeConversion) {
            it.convert()
        }
    }.reduce { acc, cur ->
        when (cur) {
            InternalServerError -> cur
            Unauthorized -> if (acc != InternalServerError) cur else acc
            Forbidden -> if (acc != InternalServerError && acc != Unauthorized) cur else acc
            else -> if (cur.value > acc.value) cur else acc
        }
    }
}

typealias ErrorToHttpStatusCodeConversionScope = ConversionScope<DomainError, HttpStatusCode>

/**
 * A conversion scope that maps a DomainError to an HttpStatusCode.
 * The mapping is based on the type of the DomainError.
 * For example, a DbError.UsernameAlreadyExists is mapped to a BadRequest.
 * The mapping covers all known types of DomainError.
 */
val ErrorToHttpStatusCodeConversion = ErrorToHttpStatusCodeConversionScope {
    when (this) {
        DbError.UsernameAlreadyExists -> BadRequest
        DbError.EmailAlreadyExists -> BadRequest
        DbError.InvalidUsernameOrPassword -> BadRequest
        DbError.ValueAlreadyExists -> BadRequest
        SecurityError.InvalidRefreshTokenStatus -> BadRequest
        SecurityError.UnknownToken -> BadRequest
        RequestBodyCouldNotBeParsed -> BadRequest
        is ValidationError -> BadRequest
        SecurityError.ClaimsExtractionError -> Unauthorized
        is SecurityError.ClaimsValidationError -> Unauthorized
        SecurityError.MalformedSubject -> Unauthorized
        is SecurityError.AuthenticationError -> Unauthorized
        SecurityError.TokenExpired -> Unauthorized
        DbError.NothingWasChanged -> NotFound
        is SecurityError.AuthorizationError -> Forbidden
        is ConversionError -> InternalServerError
        DbError.UnhandledDbError -> InternalServerError
        is MailingError -> InternalServerError
        SecurityError.TokenGenerationError -> InternalServerError
        SecurityError.TokenVerificationError -> InternalServerError
    }
}
