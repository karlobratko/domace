package hr.algebra.domace.infrastructure.routes

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
