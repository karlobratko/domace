package hr.algebra.domace.infrastructure.routes

import hr.algebra.domace.domain.model.User.Role.Agriculturist
import hr.algebra.domace.domain.model.User.Role.Agronomist
import hr.algebra.domace.infrastructure.ktor.get
import hr.algebra.domace.infrastructure.security.authentication.scope.jwt
import hr.algebra.domace.infrastructure.security.authorization.scope.role
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

@Serializable
data class Login(val username: String, val password: String)

fun Route.auth() = route("/auth") {
    get("/open/{id}") {
        println(call.parameters["id"])
        call.respond("Hello!")
    }

    post<Login>("login") {
        println(it)
        call.respond(HttpStatusCode.OK)
    }

    jwt {
        role(Agronomist, Agriculturist) {
            get("/secured") { (user) ->
                println(user)
                call.respond("Hello!")
            }
        }
    }
}
