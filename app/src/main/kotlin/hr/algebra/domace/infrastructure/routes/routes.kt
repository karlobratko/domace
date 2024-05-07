package hr.algebra.domace.infrastructure.routes

import hr.algebra.domace.infrastructure.routes.auth.auth
import io.ktor.server.application.Application
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.routes() {
    routing {
        route("/api/v1") {
            auth()
        }
    }
}
