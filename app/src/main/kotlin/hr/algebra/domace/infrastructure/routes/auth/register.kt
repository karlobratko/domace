package hr.algebra.domace.infrastructure.routes.auth

import arrow.core.toEitherNel
import hr.algebra.domace.domain.DomainError
import hr.algebra.domace.domain.SecurityError.RegistrationError.UnknownRegistrationToken
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
import hr.algebra.domace.domain.security.Token
import hr.algebra.domace.domain.validation.EmailValidation
import hr.algebra.domace.domain.validation.PasswordValidation
import hr.algebra.domace.domain.validation.RegistrationTokenExpiredValidation
import hr.algebra.domace.domain.validation.RegistrationTokenNotExpiredValidation
import hr.algebra.domace.domain.validation.RegistrationTokenUnconfirmedValidation
import hr.algebra.domace.domain.validation.RoleValidation
import hr.algebra.domace.domain.validation.UsernameValidation
import hr.algebra.domace.domain.validation.validate
import hr.algebra.domace.domain.with
import hr.algebra.domace.infrastructure.ktor.post
import hr.algebra.domace.infrastructure.mailing.Senders
import hr.algebra.domace.infrastructure.mailing.templates.ConfirmRegistration
import hr.algebra.domace.infrastructure.routes.respond
import hr.algebra.domace.infrastructure.routes.toResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

fun Route.register() = route("/register") {
    val userPersistence by inject<UserPersistence>()
    val registrationTokenPersistence by inject<RegistrationTokenPersistence>()
    val senders by inject<Senders>()
    val mailingService by inject<MailingService>()

    post<Register> { request ->
        eitherNel<DomainError, Unit> {
            val user = userPersistence.insert(
                with(UsernameValidation, EmailValidation, PasswordValidation, RoleValidation) {
                    request.run {
                        User.New(Username(username), Email(email), Password(password), role).bind()
                    }
                }
            ).toEitherNel().bind()

            mailingService.send(
                ConfirmRegistration(
                    from = senders.auth,
                    email = user.email,
                    username = user.username,
                    confirmUrl = "${request.redirectUrl}?registrationToken=${user.registrationToken.value}"
                )
            ).toEitherNel().bind()
        }.toResponse(HttpStatusCode.Created).let { call.respond(it) }
    }

    post<ConfirmRegister>("/confirm") { request ->
        eitherNel<DomainError, Unit> {
            val registrationToken = registrationTokenPersistence.select(Token.Register(request.registrationToken))
                .getOrRaiseNel { UnknownRegistrationToken }
                .apply {
                    confirmationStatus.validate(RegistrationTokenUnconfirmedValidation).bind()
                    expiresAt.validate(RegistrationTokenNotExpiredValidation).bind()
                }

            registrationTokenPersistence.confirm(registrationToken.token).toEitherNel().bind()
        }.toResponse(HttpStatusCode.OK).let { call.respond(it) }
    }

    post<ResetRegister>("/reset") { request ->
        eitherNel<DomainError, Unit> {
            val registrationToken = registrationTokenPersistence.select(Token.Register(request.registrationToken))
                .getOrRaiseNel { UnknownRegistrationToken }
                .apply {
                    confirmationStatus.validate(RegistrationTokenUnconfirmedValidation).bind()
                    expiresAt.validate(RegistrationTokenExpiredValidation).bind()
                }

            registrationTokenPersistence.reset(registrationToken.token).toEitherNel().bind()
        }.toResponse(HttpStatusCode.OK).let { call.respond(it) }
    }
}

@Serializable
data class Register(
    val username: String,
    val email: String,
    val password: String,
    val role: User.Role,
    val redirectUrl: String
)

@Serializable data class ConfirmRegister(val registrationToken: String)

@Serializable data class ResetRegister(val registrationToken: String)
