package hr.algebra.domace.infrastructure.routes

import hr.algebra.domace.infrastructure.security.auth.get
import hr.algebra.domace.infrastructure.security.auth.scope.jwt
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.server.routing.get

fun Route.auth() = route("/auth") {
    jwt {
        get("/secured") { auth ->
            println(auth.userId)
            call.respond("Hello!")
        }
    }

    get("/open") {
        call.respond("Hello!")
    }
}
