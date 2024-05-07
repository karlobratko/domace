package hr.algebra.domace.domain.mailing

import arrow.core.Either
import hr.algebra.domace.domain.DomainError

/**
 * A functional interface for a mailing service.
 *
 * This interface defines a single function, `send`, which is used to send an email.
 * The function is a suspending function, meaning it is designed to be used with Kotlin's coroutines.
 *
 * @property send A suspending function that takes an `Email` object and returns an `Either` object.
 * The `Either` object will contain a `DomainError` object if the email could not be sent, or the `Email` object if
 * the email was sent successfully.
 */
fun interface MailingService {
    /**
     * Sends an email using the provided `MailingService`.
     *
     * This function takes an `Email` object and sends it using the `MailingService`. The result of the operation
     * is returned as an `Either` object. If the email was sent successfully, the `Either` object will contain the
     * `Email` object.
     * If the email could not be sent, the `Either` object will contain a `DomainError` object.
     *
     * This function is a suspending function, meaning it is designed to be used with Kotlin's coroutines.
     *
     * @param email The `Email` object to be sent.
     *
     * @return An `Either` object containing a `DomainError` object if the email could not be sent, or the `Email`
     * object if the email was sent successfully.
     */
    suspend fun send(email: Email): Either<DomainError, Email>
}

/**
 * Sends an email using the provided `MailingService` and an `EmailTemplate`.
 *
 * This function takes an `EmailTemplate` object, converts it to an `Email` object using the `email` function,
 * and sends it using the `MailingService`. The result of the operation is returned as an `Either` object.
 * If the email was sent successfully, the `Either` object will contain the `Email` object.
 * If the email could not be sent, the `Either` object will contain a `DomainError` object.
 *
 * This function is a suspending function, meaning it is designed to be used with Kotlin's coroutines.
 *
 * @param template The `EmailTemplate` object to be converted to an `Email` object and sent.
 *
 * @return An `Either` object containing a `DomainError` object if the email could not be sent, or the `Email`
 * object if the email was sent successfully.
 */
suspend fun MailingService.send(template: EmailTemplate): Either<DomainError, Email> = send(template.email())
