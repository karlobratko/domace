package hr.algebra.domace.infrastructure.routes.auth

import arrow.core.toEitherNel
import hr.algebra.domace.domain.DomainError
import hr.algebra.domace.domain.eitherNel
import hr.algebra.domace.domain.model.User.Password
import hr.algebra.domace.domain.model.User.Username
import hr.algebra.domace.domain.persistence.UserPersistence
import hr.algebra.domace.domain.security.AuthContext
import hr.algebra.domace.domain.security.Token
import hr.algebra.domace.domain.security.jwt.JwtTokenService
import hr.algebra.domace.domain.validation.RegistrationTokenConfirmedValidation
import hr.algebra.domace.domain.validation.validate
import hr.algebra.domace.infrastructure.ktor.post
import hr.algebra.domace.infrastructure.routes.toResponse
import hr.algebra.domace.infrastructure.security.authentication.jwt
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

fun Route.access() = route("/access") {
    val userPersistence by inject<UserPersistence>()
    val jwtTokenService by inject<JwtTokenService>()

    post<AcquireAccess>("/acquire") { request ->
        eitherNel {
            val user = userPersistence.select(Username(request.username), Password(request.password))
                .toEitherNel().bind()
                .apply {
                    registrationToken.confirmationStatus.validate(RegistrationTokenConfirmedValidation).bind()
                }

            val (refreshToken, accessToken) = jwtTokenService.generate(AuthContext(user.id, user.role))
                .toEitherNel().bind()

            GrantAccess(accessToken.value, refreshToken.value)
        }.toResponse(HttpStatusCode.OK).let { call.respond(it) }
    }

    jwt {
        post<RefreshAccess>("/refresh") { (_, request) ->
            eitherNel {
                val (refreshToken, accessToken) = jwtTokenService.refresh(Token.Refresh(request.refreshToken)).bind()

                GrantAccess(accessToken.value, refreshToken.value)
            }.toResponse(HttpStatusCode.OK).let { call.respond(it) }
        }

        post<RevokeAccess>("/revoke") { (_, request) ->
            eitherNel<DomainError, Unit> {
                jwtTokenService.revoke(Token.Refresh(request.refreshToken)).bind()
            }.toResponse(HttpStatusCode.OK).let { call.respond(it) }
        }
    }
}

@Serializable data class AcquireAccess(val username: String, val password: String)

@Serializable data class GrantAccess(val accessToken: String, val refreshToken: String)

@Serializable data class RefreshAccess(val refreshToken: String)

@Serializable data class RevokeAccess(val refreshToken: String)
