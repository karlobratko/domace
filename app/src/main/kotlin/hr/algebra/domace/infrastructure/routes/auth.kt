package hr.algebra.domace.infrastructure.routes

import arrow.core.toEitherNel
import arrow.core.toOption
import hr.algebra.domace.domain.DomainError
import hr.algebra.domace.domain.RequestError.InvalidRequestPathParameter
import hr.algebra.domace.domain.RequestError.InvalidRequestQueryParameter
import hr.algebra.domace.domain.eitherNel
import hr.algebra.domace.domain.getOrRaiseNel
import hr.algebra.domace.domain.mailing.MailingService
import hr.algebra.domace.domain.mailing.send
import hr.algebra.domace.domain.model.User
import hr.algebra.domace.domain.model.User.Email
import hr.algebra.domace.domain.model.User.Password
import hr.algebra.domace.domain.model.User.Username
import hr.algebra.domace.domain.persistence.RegistrationTokenPersistence
import hr.algebra.domace.domain.persistence.UserPersistence
import hr.algebra.domace.domain.security.AuthContext
import hr.algebra.domace.domain.security.Token
import hr.algebra.domace.domain.security.jwt.JwtTokenService
import hr.algebra.domace.domain.validation.EmailValidation
import hr.algebra.domace.domain.validation.PasswordValidation
import hr.algebra.domace.domain.validation.RoleValidation
import hr.algebra.domace.domain.validation.UsernameValidation
import hr.algebra.domace.domain.with
import hr.algebra.domace.infrastructure.mailing.Senders
import hr.algebra.domace.infrastructure.mailing.templates.ConfirmRegistration
import hr.algebra.domace.infrastructure.routes.dto.LoginRequest
import hr.algebra.domace.infrastructure.routes.dto.LoginResponse
import hr.algebra.domace.infrastructure.routes.dto.LogoutRequest
import hr.algebra.domace.infrastructure.routes.dto.RefreshRequest
import hr.algebra.domace.infrastructure.routes.dto.RefreshResponse
import hr.algebra.domace.infrastructure.routes.dto.RegisterRequest
import hr.algebra.domace.infrastructure.routes.dto.respond
import hr.algebra.domace.infrastructure.routes.dto.toFailure
import hr.algebra.domace.infrastructure.routes.dto.toResponse
import hr.algebra.domace.infrastructure.security.authentication.jwt
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.server.application.call
import io.ktor.server.plugins.origin
import io.ktor.server.request.host
import io.ktor.server.request.port
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.util.url
import org.koin.ktor.ext.inject

fun Route.auth() = route("/auth") {
    val userPersistence by inject<UserPersistence>()
    val registrationTokenPersistence by inject<RegistrationTokenPersistence>()
    val senders by inject<Senders>()
    val mailingService by inject<MailingService>()
    val jwtTokenService by inject<JwtTokenService>()

    post<RegisterRequest>("/register") { request ->
        eitherNel<DomainError, Unit> {
            val user = userPersistence.insert(
                with(UsernameValidation, EmailValidation, PasswordValidation, RoleValidation) {
                    request.run {
                        User.New(
                            Username(username),
                            Email(email),
                            Password(password),
                            role
                        ).bind()
                    }
                }
            ).toEitherNel().bind()

            mailingService.send(
                ConfirmRegistration(
                    from = senders.auth,
                    email = user.email,
                    username = user.username,
                    confirmUrl = url {
                        protocol = URLProtocol.createOrDefault(call.request.origin.scheme)
                        host = call.request.host()
                        port = call.request.port()
                        path("api", "v1", "auth", "confirm-registration", user.registrationToken.value)
                        parameters.append("redirectUrl", request.redirectUrl)
                    }
                )
            ).toEitherNel().bind()
        }.toResponse(HttpStatusCode.Created).respond()
    }

    get("/confirm-registration/{registrationToken}") {
        eitherNel {
            val registrationToken = call.parameters["registrationToken"].toOption()
                .getOrRaiseNel { InvalidRequestPathParameter }
            val redirectUrl = call.request.queryParameters["redirectUrl"].toOption()
                .getOrRaiseNel { InvalidRequestQueryParameter }

            registrationTokenPersistence.confirm(Token.Register(registrationToken)).toEitherNel().bind()

            redirectUrl
        }.fold(
            ifLeft = {
                it.toFailure().respond()
            },
            ifRight = {
                call.respondRedirect(it)
            }
        )
    }

    post<LoginRequest>("/login") { request ->
        eitherNel {
            val user = userPersistence.select(
                Username(request.username),
                Password(request.password)
            ).toEitherNel().bind()

            val (refreshToken, accessToken) = jwtTokenService.generate(AuthContext(user)).toEitherNel().bind()

            LoginResponse(accessToken.value, refreshToken.value)
        }.toResponse(HttpStatusCode.OK).respond()
    }

    jwt {
        post<RefreshRequest>("/refresh") { request ->
            eitherNel {
                val (refreshToken, accessToken) = jwtTokenService.refresh(Token.Refresh(request.refreshToken)).bind()

                RefreshResponse(accessToken.value, refreshToken.value)
            }.toResponse(HttpStatusCode.OK).respond()
        }

        post<LogoutRequest>("/logout") { request ->
            eitherNel<DomainError, Unit> {
                jwtTokenService.revoke(Token.Refresh(request.refreshToken)).bind()
            }.toResponse(HttpStatusCode.OK).respond()
        }
    }
}
