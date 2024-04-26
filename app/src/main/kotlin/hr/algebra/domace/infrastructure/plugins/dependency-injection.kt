package hr.algebra.domace.infrastructure.plugins

import hr.algebra.domace.infrastructure.mailing.MailingModule
import hr.algebra.domace.infrastructure.persistence.PersistenceModule
import hr.algebra.domace.infrastructure.security.SecurityModule
import io.ktor.server.application.Application
import org.koin.ktor.plugin.koin
import org.koin.logger.SLF4JLogger

fun Application.configureDI() {
    koin {
        logger(SLF4JLogger())
        modules(
            PersistenceModule,
            MailingModule,
            SecurityModule
        )
    }
}
