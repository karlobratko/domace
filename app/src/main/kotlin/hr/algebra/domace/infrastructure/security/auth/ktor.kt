package hr.algebra.domace.infrastructure.security.auth

import hr.algebra.domace.infrastructure.routes.Response
import hr.algebra.domace.infrastructure.security.auth.scope.AuthorizationScope
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.util.KtorDsl
import io.ktor.util.pipeline.PipelineContext

/**
 * Extension function for Route that creates a new GET route with authorization.
 *
 * This function takes a path and a lambda function as parameters. The lambda function defines the behavior of the
 * route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use the
 * functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new GET route with the provided path.
 * The route uses the current AuthorizationScope to authorize the request.
 * If the authorization is successful, the lambda function is called with the PipelineContext<Unit, ApplicationCall>
 *     and the AuthorizationContext as parameters.
 *
 * @param path The path of the route.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
context(AuthorizationScope)
@KtorDsl
fun Route.get(
    path: String,
    body: suspend PipelineContext<Unit, ApplicationCall>.(AuthorizationContext) -> Unit
): Route = get(path, this@AuthorizationScope, body)

/**
 * Extension function for Route that creates a new GET route with a specified authorization scope.
 *
 * This function takes a path, an AuthorizationScope, and a lambda function as parameters. The lambda function defines
 * the behavior of the route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use the
 * functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new GET route with the provided path.
 * The route uses the provided AuthorizationScope to authorize the request.
 * If the authorization is successful, the lambda function is called with the PipelineContext<Unit, ApplicationCall>
 * and the AuthorizationContext as parameters.
 *
 * @param path The path of the route.
 * @param authorizationScope The AuthorizationScope used to authorize the request.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
@KtorDsl
fun Route.get(
    path: String,
    authorizationScope: AuthorizationScope,
    body: suspend PipelineContext<Unit, ApplicationCall>.(AuthorizationContext) -> Unit
): Route {
    return get(path) {
        authorize(authorizationScope) {
            body(this, it)
        }
    }
}

private suspend inline fun PipelineContext<Unit, ApplicationCall>.authorize(
    authorizationScope: AuthorizationScope,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(AuthorizationContext) -> Unit
) {
    with(authorizationScope) {
        authorize(body)
    }
}

context(PipelineContext<Unit, ApplicationCall>, AuthorizationScope)
private suspend inline fun PipelineContext<Unit, ApplicationCall>.authorize(
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(AuthorizationContext) -> Unit
) {
    call.request.authorize()
        .onRight { context -> body(this@authorize, context) }
        .onLeft { errors ->
            call.respond(
                HttpStatusCode.Unauthorized,
                Response.Failure(errors.map { it.toString() })
            )
        }
}
