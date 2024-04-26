package hr.algebra.domace.infrastructure.mailing.jakarta

import arrow.core.Either.Companion.catchOrThrow
import hr.algebra.domace.domain.MailingError.CouldNotSendEmail
import hr.algebra.domace.domain.mailing.Email
import hr.algebra.domace.domain.mailing.MailingService
import jakarta.mail.MessagingException
import jakarta.mail.Session
import jakarta.mail.Transport.send
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun JakartaMailingService(session: Session) = MailingService { email ->
    catchOrThrow<MessagingException, Email> {
        email.also {
            val message = with(EmailToMimeMessageConversion(session)) {
                email.convert()
            }

            withContext(Dispatchers.IO) {
                send(message)
            }
        }
    }.mapLeft { CouldNotSendEmail }
}
