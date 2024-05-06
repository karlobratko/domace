package hr.algebra.domace.infrastructure.plugins

import arrow.core.nel
import hr.algebra.domace.domain.UnhandledError
import hr.algebra.domace.infrastructure.routes.dto.toFailure
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            this@configureStatusPages.log.error(cause.message)
            call.respond(InternalServerError, UnhandledError.nel().toFailure(InternalServerError))
        }
    }
}
