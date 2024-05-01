package hr.algebra.domace.infrastructure.ktor

import hr.algebra.domace.infrastructure.routes.Response
import hr.algebra.domace.infrastructure.routes.respond
import hr.algebra.domace.infrastructure.security.authentication.AuthenticationContext
import hr.algebra.domace.infrastructure.security.authentication.scope.AuthenticationScope
import hr.algebra.domace.infrastructure.security.authorization.scope.AuthorizationScope
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.util.KtorDsl
import io.ktor.util.pipeline.PipelineContext

/**
 * Extension function for Route that creates a new GET route with authentication and authorization.
 *
 * This function takes a path and a lambda function as parameters. The lambda function defines the behavior of the
 * route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use the
 * functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new GET route with the provided path.
 * The route uses the current AuthenticationScope and AuthorizationScope to authenticate and authorize the request.
 * If the authentication and authorization are successful, the lambda function is called with the
 * PipelineContext<Unit, ApplicationCall> and the AuthenticationContext as parameters.
 *
 * @param path The path of the route.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
context(AuthenticationScope, AuthorizationScope)
@KtorDsl
fun Route.get(
    path: String,
    body: suspend PipelineContext<Unit, ApplicationCall>.(AuthenticationContext) -> Unit
): Route = get(path, this@AuthenticationScope, this@AuthorizationScope, body)

/**
 * Extension function for Route that creates a new GET route with authentication, authorization, and resource reception.
 *
 * This function takes a path and a lambda function as parameters. The lambda function defines the behavior of the
 * route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use
 * the functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new GET route with the provided path.
 * The route uses the current AuthenticationScope and AuthorizationScope to authenticate and authorize the request.
 * If the authentication and authorization are successful, the route receives an object of the specified type from
 * the request and calls the lambda function with the PipelineContext<Unit, ApplicationCall> and a pair of the
 * AuthenticationContext and the received object as parameters.
 *
 * @param path The path of the route.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
context(AuthenticationScope, AuthorizationScope)
@KtorDsl
@JvmName("getResource")
inline fun <reified T> Route.get(
    path: String,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(Pair<AuthenticationContext, T>) -> Unit
): Route = get(path, this@AuthenticationScope, this@AuthorizationScope, body)

/**
 * Extension function for Route that creates a new GET route with authentication and authorization.
 *
 * This function takes a path, an AuthenticationScope, an AuthorizationScope, and a lambda function as parameters.
 * The lambda function defines the behavior of the route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use the
 * functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new GET route with the provided path.
 * The route uses the provided AuthenticationScope and AuthorizationScope to authenticate and authorize the request.
 * If the authentication and authorization are successful, the lambda function is called with the
 * PipelineContext<Unit, ApplicationCall> and the AuthenticationContext as parameters.
 *
 * @param path The path of the route.
 * @param authenticationScope The AuthenticationScope used to authenticate the request.
 * @param authorizationScope The AuthorizationScope used to authorize the request.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
@KtorDsl
fun Route.get(
    path: String,
    authenticationScope: AuthenticationScope,
    authorizationScope: AuthorizationScope,
    body: suspend PipelineContext<Unit, ApplicationCall>.(AuthenticationContext) -> Unit
): Route =
    get(path) {
        call.request.authenticate(authenticationScope) { ctx ->
            ctx.authorize(authorizationScope) {
                body(this@authorize, it)
            }
        }
    }

/**
 * Extension function for Route that creates a new GET route with authentication, authorization, and resource reception.
 *
 * This function takes a path, an AuthenticationScope, an AuthorizationScope, and a lambda function as parameters.
 * The lambda function defines the behavior of the route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use
 * the functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new GET route with the provided path.
 * The route uses the provided AuthenticationScope and AuthorizationScope to authenticate and authorize the request.
 * If the authentication and authorization are successful, the route receives an object of the specified type from
 * the request and calls the lambda function with the PipelineContext<Unit, ApplicationCall> and a pair of the
 * AuthenticationContext and the received object as parameters.
 *
 * @param path The path of the route.
 * @param authenticationScope The AuthenticationScope used to authenticate the request.
 * @param authorizationScope The AuthorizationScope used to authorize the request.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
@KtorDsl
@JvmName("getResource")
inline fun <reified T> Route.get(
    path: String,
    authenticationScope: AuthenticationScope,
    authorizationScope: AuthorizationScope,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(Pair<AuthenticationContext, T>) -> Unit
): Route =
    get(path) {
        call.request.authenticate(authenticationScope) { context1 ->
            context1.authorize(authorizationScope) { context2 ->
                call.receive<T> { resource ->
                    body(this@receive, context2 to resource)
                }
            }
        }
    }

/**
 * Extension function for Route that creates a new POST route with authentication and authorization.
 *
 * This function takes a path and a lambda function as parameters. The lambda function defines the behavior of the
 * route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use the
 * functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new POST route with the provided path.
 * The route uses the current AuthenticationScope and AuthorizationScope to authenticate and authorize the request.
 * If the authentication and authorization are successful, the lambda function is called with the
 * PipelineContext<Unit, ApplicationCall> and the AuthenticationContext as parameters.
 *
 * @param path The path of the route.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
context(AuthenticationScope, AuthorizationScope)
@KtorDsl
fun Route.post(
    path: String,
    body: suspend PipelineContext<Unit, ApplicationCall>.(AuthenticationContext) -> Unit
): Route = post(path, this@AuthenticationScope, this@AuthorizationScope, body)

/**
 * Extension function for Route that creates a new POST route with authentication, authorization, and resource
 * reception.
 *
 * This function takes a path and a lambda function as parameters. The lambda function defines the behavior of the
 * route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use
 * the functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new POST route with the provided path.
 * The route uses the current AuthenticationScope and AuthorizationScope to authenticate and authorize the request.
 * If the authentication and authorization are successful, the route receives an object of the specified type from
 * the request and calls the lambda function with the PipelineContext<Unit, ApplicationCall> and a pair of the
 * AuthenticationContext and the received object as parameters.
 *
 * @param path The path of the route.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
context(AuthenticationScope, AuthorizationScope)
@KtorDsl
@JvmName("postResource")
inline fun <reified T> Route.post(
    path: String,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(Pair<AuthenticationContext, T>) -> Unit
): Route = post(path, this@AuthenticationScope, this@AuthorizationScope, body)

/**
 * Extension function for Route that creates a new POST route with authentication and authorization.
 *
 * This function takes a path, an AuthenticationScope, an AuthorizationScope, and a lambda function as parameters.
 * The lambda function defines the behavior of the route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use the
 * functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new POST route with the provided path.
 * The route uses the provided AuthenticationScope and AuthorizationScope to authenticate and authorize the request.
 * If the authentication and authorization are successful, the lambda function is called with the
 * PipelineContext<Unit, ApplicationCall> and the AuthenticationContext as parameters.
 *
 * @param path The path of the route.
 * @param authenticationScope The AuthenticationScope used to authenticate the request.
 * @param authorizationScope The AuthorizationScope used to authorize the request.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
@KtorDsl
fun Route.post(
    path: String,
    authenticationScope: AuthenticationScope,
    authorizationScope: AuthorizationScope,
    body: suspend PipelineContext<Unit, ApplicationCall>.(AuthenticationContext) -> Unit
): Route =
    post(path) {
        call.request.authenticate(authenticationScope) { ctx ->
            ctx.authorize(authorizationScope, body)
        }
    }

/**
 * Extension function for Route that creates a new POST route with authentication, authorization, and resource
 * reception.
 *
 * This function takes a path, an AuthenticationScope, an AuthorizationScope, and a lambda function as parameters.
 * The lambda function defines the behavior of the route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use
 * the functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new POST route with the provided path.
 * The route uses the provided AuthenticationScope and AuthorizationScope to authenticate and authorize the request.
 * If the authentication and authorization are successful, the route receives an object of the specified type from
 * the request and calls the lambda function with the PipelineContext<Unit, ApplicationCall> and a pair of the
 * AuthenticationContext and the received object as parameters.
 *
 * @param path The path of the route.
 * @param authenticationScope The AuthenticationScope used to authenticate the request.
 * @param authorizationScope The AuthorizationScope used to authorize the request.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
@KtorDsl
@JvmName("postResource")
inline fun <reified T> Route.post(
    path: String,
    authenticationScope: AuthenticationScope,
    authorizationScope: AuthorizationScope,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(Pair<AuthenticationContext, T>) -> Unit
): Route =
    post(path) {
        call.request.authenticate(authenticationScope) { context1 ->
            context1.authorize(authorizationScope) { context2 ->
                call.receive<T> { resource ->
                    body(this@receive, context2 to resource)
                }
            }
        }
    }

/**
 * Extension function for Route that creates a new PUT route with authentication and authorization.
 *
 * This function takes a path and a lambda function as parameters. The lambda function defines the behavior of the
 * route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use the
 * functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new PUT route with the provided path.
 * The route uses the current AuthenticationScope and AuthorizationScope to authenticate and authorize the request.
 * If the authentication and authorization are successful, the lambda function is called with the
 * PipelineContext<Unit, ApplicationCall> and the AuthenticationContext as parameters.
 *
 * @param path The path of the route.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
context(AuthenticationScope, AuthorizationScope)
@KtorDsl
fun Route.put(
    path: String,
    body: suspend PipelineContext<Unit, ApplicationCall>.(AuthenticationContext) -> Unit
): Route = put(path, this@AuthenticationScope, this@AuthorizationScope, body)

/**
 * Extension function for Route that creates a new PUT route with authentication, authorization, and resource reception.
 *
 * This function takes a path and a lambda function as parameters. The lambda function defines the behavior of the
 * route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use
 * the functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new PUT route with the provided path.
 * The route uses the current AuthenticationScope and AuthorizationScope to authenticate and authorize the request.
 * If the authentication and authorization are successful, the route receives an object of the specified type from
 * the request and calls the lambda function with the PipelineContext<Unit, ApplicationCall> and a pair of the
 * AuthenticationContext and the received object as parameters.
 *
 * @param path The path of the route.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
context(AuthenticationScope, AuthorizationScope)
@KtorDsl
@JvmName("putResource")
inline fun <reified T> Route.put(
    path: String,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(Pair<AuthenticationContext, T>) -> Unit
): Route = put(path, this@AuthenticationScope, this@AuthorizationScope, body)

/**
 * Extension function for Route that creates a new PUT route with authentication and authorization.
 *
 * This function takes a path, an AuthenticationScope, an AuthorizationScope, and a lambda function as parameters.
 * The lambda function defines the behavior of the route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use the
 * functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new PUT route with the provided path.
 * The route uses the provided AuthenticationScope and AuthorizationScope to authenticate and authorize the request.
 * If the authentication and authorization are successful, the lambda function is called with the
 * PipelineContext<Unit, ApplicationCall> and the AuthenticationContext as parameters.
 *
 * @param path The path of the route.
 * @param authenticationScope The AuthenticationScope used to authenticate the request.
 * @param authorizationScope The AuthorizationScope used to authorize the request.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
@KtorDsl
fun Route.put(
    path: String,
    authenticationScope: AuthenticationScope,
    authorizationScope: AuthorizationScope,
    body: suspend PipelineContext<Unit, ApplicationCall>.(AuthenticationContext) -> Unit
): Route =
    put(path) {
        call.request.authenticate(authenticationScope) { ctx ->
            ctx.authorize(authorizationScope, body)
        }
    }

/**
 * Extension function for Route that creates a new PUT route with authentication, authorization, and resource reception.
 *
 * This function takes a path, an AuthenticationScope, an AuthorizationScope, and a lambda function as parameters.
 * The lambda function defines the behavior of the route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use
 * the functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new PUT route with the provided path.
 * The route uses the provided AuthenticationScope and AuthorizationScope to authenticate and authorize the request.
 * If the authentication and authorization are successful, the route receives an object of the specified type from
 * the request and calls the lambda function with the PipelineContext<Unit, ApplicationCall> and a pair of the
 * AuthenticationContext and the received object as parameters.
 *
 * @param path The path of the route.
 * @param authenticationScope The AuthenticationScope used to authenticate the request.
 * @param authorizationScope The AuthorizationScope used to authorize the request.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
@KtorDsl
@JvmName("putResource")
inline fun <reified T> Route.put(
    path: String,
    authenticationScope: AuthenticationScope,
    authorizationScope: AuthorizationScope,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(Pair<AuthenticationContext, T>) -> Unit
): Route =
    put(path) {
        call.request.authenticate(authenticationScope) { context1 ->
            context1.authorize(authorizationScope) { context2 ->
                call.receive<T> { resource ->
                    body(this@receive, context2 to resource)
                }
            }
        }
    }

/**
 * Extension function for Route that creates a new DELETE route with authentication and authorization.
 *
 * This function takes a path and a lambda function as parameters. The lambda function defines the behavior of the
 * route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use the
 * functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new DELETE route with the provided path.
 * The route uses the current AuthenticationScope and AuthorizationScope to authenticate and authorize the request.
 * If the authentication and authorization are successful, the lambda function is called with the
 * PipelineContext<Unit, ApplicationCall> and the AuthenticationContext as parameters.
 *
 * @param path The path of the route.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
context(AuthenticationScope, AuthorizationScope)
@KtorDsl
fun Route.delete(
    path: String,
    body: suspend PipelineContext<Unit, ApplicationCall>.(AuthenticationContext) -> Unit
): Route = delete(path, this@AuthenticationScope, this@AuthorizationScope, body)

/**
 * Extension function for Route that creates a new DELETE route with authentication, authorization, and resource
 * reception.
 *
 * This function takes a path and a lambda function as parameters. The lambda function defines the behavior of the
 * route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use
 * the functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new DELETE route with the provided path.
 * The route uses the current AuthenticationScope and AuthorizationScope to authenticate and authorize the request.
 * If the authentication and authorization are successful, the route receives an object of the specified type from
 * the request and calls the lambda function with the PipelineContext<Unit, ApplicationCall> and a pair of the
 * AuthenticationContext and the received object as parameters.
 *
 * @param path The path of the route.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
context(AuthenticationScope, AuthorizationScope)
@KtorDsl
@JvmName("deleteResource")
inline fun <reified T> Route.delete(
    path: String,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(Pair<AuthenticationContext, T>) -> Unit
): Route = delete(path, this@AuthenticationScope, this@AuthorizationScope, body)

/**
 * Extension function for Route that creates a new DELETE route with authentication and authorization.
 *
 * This function takes a path, an AuthenticationScope, an AuthorizationScope, and a lambda function as parameters.
 * The lambda function defines the behavior of the route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use the
 * functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new DELETE route with the provided path.
 * The route uses the provided AuthenticationScope and AuthorizationScope to authenticate and authorize the request.
 * If the authentication and authorization are successful, the lambda function is called with the
 * PipelineContext<Unit, ApplicationCall> and the AuthenticationContext as parameters.
 *
 * @param path The path of the route.
 * @param authenticationScope The AuthenticationScope used to authenticate the request.
 * @param authorizationScope The AuthorizationScope used to authorize the request.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
@KtorDsl
fun Route.delete(
    path: String,
    authenticationScope: AuthenticationScope,
    authorizationScope: AuthorizationScope,
    body: suspend PipelineContext<Unit, ApplicationCall>.(AuthenticationContext) -> Unit
): Route =
    delete(path) {
        call.request.authenticate(authenticationScope) { ctx ->
            ctx.authorize(authorizationScope, body)
        }
    }

/**
 * Extension function for Route that creates a new DELETE route with authentication, authorization, and resource
 * reception.
 *
 * This function takes a path, an AuthenticationScope, an AuthorizationScope, and a lambda function as parameters.
 * The lambda function defines the behavior of the route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use
 * the functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new DELETE route with the provided path.
 * The route uses the provided AuthenticationScope and AuthorizationScope to authenticate and authorize the request.
 * If the authentication and authorization are successful, the route receives an object of the specified type from
 * the request and calls the lambda function with the PipelineContext<Unit, ApplicationCall> and a pair of the
 * AuthenticationContext and the received object as parameters.
 *
 * @param path The path of the route.
 * @param authenticationScope The AuthenticationScope used to authenticate the request.
 * @param authorizationScope The AuthorizationScope used to authorize the request.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
@KtorDsl
@JvmName("deleteResource")
inline fun <reified T> Route.delete(
    path: String,
    authenticationScope: AuthenticationScope,
    authorizationScope: AuthorizationScope,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(Pair<AuthenticationContext, T>) -> Unit
): Route =
    delete(path) {
        call.request.authenticate(authenticationScope) { context1 ->
            context1.authorize(authorizationScope) { context2 ->
                call.receive<T> { resource ->
                    body(this@receive, context2 to resource)
                }
            }
        }
    }

/**
 * Extension function for AuthenticationContext that authorizes the request.
 *
 * This function takes an AuthorizationScope and a lambda function as parameters.
 * The lambda function defines the behavior of the route after the authorization.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use the
 * functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the provided AuthorizationScope to authorize the request.
 * If the authorization is successful, the lambda function is called with the PipelineContext<Unit, ApplicationCall> and
 * the AuthenticationContext as parameters.
 * If the authorization fails, the function responds with a HTTP 403 Forbidden status and a list of error messages.
 *
 * @param authorizationScope The AuthorizationScope used to authorize the request.
 * @param body A lambda function that defines the behavior of the route after the authorization. This is an extension
 * function for PipelineContext<Unit, ApplicationCall>.
 */
context(PipelineContext<Unit, ApplicationCall>)
suspend fun AuthenticationContext.authorize(
    authorizationScope: AuthorizationScope,
    body: suspend PipelineContext<Unit, ApplicationCall>.(AuthenticationContext) -> Unit
) {
    with(authorizationScope) {
        this@authorize.authorize()
            .onRight { context -> body(this@PipelineContext, context) }
            .onLeft { errors ->
                Response.Failure(errors.map { it.toString() }, HttpStatusCode.Forbidden).respond()
            }
    }
}
