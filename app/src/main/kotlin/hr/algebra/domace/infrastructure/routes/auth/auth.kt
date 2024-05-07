package hr.algebra.domace.infrastructure.routes.auth

import io.ktor.server.routing.Route
import io.ktor.server.routing.route

fun Route.auth() = route("/auth") {
    register()
    access()
}
