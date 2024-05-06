package hr.algebra.domace.infrastructure.security.authentication

import arrow.core.nel
import arrow.core.right
import hr.algebra.domace.domain.SecurityError.AuthenticationError.MissingAuthorizationHeader
import hr.algebra.domace.domain.model.User
import hr.algebra.domace.domain.security.AuthContext
import hr.algebra.domace.domain.security.jwt.JwtTokenService
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.authorization
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk

object JwtAuthenticationScopeTests : ShouldSpec({
    val jwtTokenService = mockk<JwtTokenService>()

    val authorizationScope = JwtAuthenticationScope(jwtTokenService)

    should("fail if authorization header is missing") {
        val request = mockk<ApplicationRequest>()
        every { request.authorization() } answers { null }

        with(authorizationScope) { request.authenticate() } shouldBeLeft MissingAuthorizationHeader.nel()
    }

    should("succeed if authorization is present and token verification is successful") {
        val request = mockk<ApplicationRequest>()
        every { request.authorization() } answers { "token" }

        val context = AuthContext(User.Id(1), User.Role.Admin)

        coEvery { jwtTokenService.verify(any()) } coAnswers { context.right() }

        with(authorizationScope) { request.authenticate() } shouldBeRight context
    }
})
