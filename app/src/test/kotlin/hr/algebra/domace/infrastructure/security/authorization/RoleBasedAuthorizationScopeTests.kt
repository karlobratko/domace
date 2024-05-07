package hr.algebra.domace.infrastructure.security.authorization

import arrow.core.nel
import hr.algebra.domace.domain.SecurityError.AuthorizationError.UnauthorizedResourceAccess
import hr.algebra.domace.domain.model.User
import hr.algebra.domace.domain.model.User.Role.Admin
import hr.algebra.domace.domain.model.User.Role.Agriculturist
import hr.algebra.domace.domain.model.User.Role.Agronomist
import hr.algebra.domace.domain.model.User.Role.Customer
import hr.algebra.domace.domain.security.AuthContext
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec

object RoleBasedAuthorizationScopeTests : ShouldSpec({
    should("fail if role not in permitted roles") {
        val roleBasedAuthorizationScope = RoleBasedAuthorizationScope(listOf(Admin, Agriculturist, Agronomist))

        val context = AuthContext(User.Id(1), Customer)
        with(roleBasedAuthorizationScope) { context.authorize() } shouldBeLeft UnauthorizedResourceAccess.nel()
    }

    should("succeed if role in permitted roles") {
        val roleBasedAuthorizationScope = RoleBasedAuthorizationScope(listOf(Admin))

        val context = AuthContext(User.Id(1), Admin)
        with(roleBasedAuthorizationScope) { context.authorize() } shouldBeRight context
    }
})
