package hr.algebra.domace.infrastructure.routes

import arrow.core.EitherNel
import arrow.core.toEitherNel
import hr.algebra.domace.domain.ValidationError.UserValidationError
import hr.algebra.domace.domain.eitherNel
import hr.algebra.domace.domain.model.User
import hr.algebra.domace.domain.model.User.Role.Agriculturist
import hr.algebra.domace.domain.model.User.Role.Agronomist
import hr.algebra.domace.domain.persistence.UserPersistence
import hr.algebra.domace.domain.validation.EmailValidation
import hr.algebra.domace.domain.validation.PasswordValidation
import hr.algebra.domace.domain.validation.RoleValidation
import hr.algebra.domace.domain.validation.UsernameValidation
import hr.algebra.domace.domain.with
import hr.algebra.domace.infrastructure.ktor.get
import hr.algebra.domace.infrastructure.security.authentication.jwt
import hr.algebra.domace.infrastructure.security.authorization.role
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class Register(
    val username: String,
    val email: String,
    val password: String,
    val role: User.Role
)

fun Register.toUser(): EitherNel<UserValidationError, User.New> =
    with(UsernameValidation, EmailValidation, PasswordValidation, RoleValidation) {
        User.New(
            User.Username(username),
            User.Email(email),
            User.Password(password),
            role
        )
    }

@Serializable
data class Login(val username: String, val password: String)

fun Route.auth() = route("/auth") {
    val userPersistence by inject<UserPersistence>()

    post<Register>("/register") { request ->
        eitherNel {
            val user = request.toUser().bind()
            userPersistence.insert(user).toEitherNel().bind()
        }.toResponse(HttpStatusCode.Created).respond()
    }

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
