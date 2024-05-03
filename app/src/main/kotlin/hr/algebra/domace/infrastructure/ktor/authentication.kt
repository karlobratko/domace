package hr.algebra.domace.infrastructure.ktor

import hr.algebra.domace.domain.security.AuthContext
import hr.algebra.domace.domain.security.authentication.AuthenticationScope
import hr.algebra.domace.infrastructure.routes.respond
import hr.algebra.domace.infrastructure.routes.toFailure
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.util.KtorDsl
import io.ktor.util.pipeline.PipelineContext

/**
 * Extension function for Route that creates a new GET route with authentication.
 *
 * This function takes a path and a lambda function as parameters. The lambda function defines the behavior of the
 * route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use the
 * functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new GET route with the provided path.
 * The route uses the current AuthenticationScope to authenticate the request.
 * If the authentication is successful, the lambda function is called with the
 * PipelineContext<Unit, ApplicationCall> and the AuthenticationContext as parameters.
 *
 * @param path The path of the route.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
context(AuthenticationScope)
@KtorDsl
fun Route.get(
    path: String,
    body: suspend PipelineContext<Unit, ApplicationCall>.(AuthContext) -> Unit
): Route = get(path, this@AuthenticationScope, body)

/**
 * Extension function for Route that creates a new GET route with authentication and resource reception.
 *
 * This function takes a path and a lambda function as parameters. The lambda function defines the behavior of the
 * route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use
 * the functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new GET route with the provided path.
 * The route uses the current AuthenticationScope to authenticate the request.
 * If the authentication is successful, the route receives an object of the specified type from
 * the request and calls the lambda function with the PipelineContext<Unit, ApplicationCall> and a pair of the
 * AuthenticationContext and the received object as parameters.
 *
 * @param path The path of the route.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
context(AuthenticationScope)
@KtorDsl
@JvmName("getResource")
inline fun <reified T> Route.get(
    path: String,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(Pair<AuthContext, T>) -> Unit
): Route = get(path, this@AuthenticationScope, body)

/**
 * Extension function for Route that creates a new GET route with authentication.
 *
 * This function takes a path, an AuthenticationScope, and a lambda function as parameters.
 * The lambda function defines the behavior of the route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use the
 * functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new GET route with the provided path.
 * The route uses the provided AuthenticationScope to authenticate the request.
 * If the authentication is successful, the lambda function is called with the
 * PipelineContext<Unit, ApplicationCall> and the AuthenticationContext as parameters.
 *
 * @param path The path of the route.
 * @param authenticationScope The AuthenticationScope used to authenticate the request.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
@KtorDsl
fun Route.get(
    path: String,
    authenticationScope: AuthenticationScope,
    body: suspend PipelineContext<Unit, ApplicationCall>.(AuthContext) -> Unit
): Route =
    get(path) {
        call.request.authenticate(authenticationScope, body)
    }

/**
 * Extension function for Route that creates a new GET route with authentication and resource reception.
 *
 * This function takes a path, an AuthenticationScope, and a lambda function as parameters.
 * The lambda function defines the behavior of the route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use
 * the functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new GET route with the provided path.
 * The route uses the provided AuthenticationScope to authenticate the request.
 * If the authentication is successful, the route receives an object of the specified type from
 * the request and calls the lambda function with the PipelineContext<Unit, ApplicationCall> and a pair of the
 * AuthenticationContext and the received object as parameters.
 *
 * @param path The path of the route.
 * @param authenticationScope The AuthenticationScope used to authenticate the request.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
@KtorDsl
@JvmName("getResource")
inline fun <reified T> Route.get(
    path: String,
    authenticationScope: AuthenticationScope,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(Pair<AuthContext, T>) -> Unit
): Route =
    get(path) {
        call.request.authenticate(authenticationScope) { context ->
            call.receive<T> { resource ->
                body(this@receive, context to resource)
            }
        }
    }

/**
 * Extension function for Route that creates a new POST route with authentication.
 *
 * This function takes a path and a lambda function as parameters. The lambda function defines the behavior of the
 * route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use the
 * functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new POST route with the provided path.
 * The route uses the current AuthenticationScope to authenticate the request.
 * If the authentication is successful, the lambda function is called with the
 * PipelineContext<Unit, ApplicationCall> and the AuthenticationContext as parameters.
 *
 * @param path The path of the route.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
context(AuthenticationScope)
@KtorDsl
fun Route.post(
    path: String,
    body: suspend PipelineContext<Unit, ApplicationCall>.(AuthContext) -> Unit
): Route = post(path, this@AuthenticationScope, body)

/**
 * Extension function for Route that creates a new POST route with authentication and resource reception.
 *
 * This function takes a path and a lambda function as parameters. The lambda function defines the behavior of the
 * route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use
 * the functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new POST route with the provided path.
 * The route uses the current AuthenticationScope to authenticate the request.
 * If the authentication is successful, the route receives an object of the specified type from
 * the request and calls the lambda function with the PipelineContext<Unit, ApplicationCall> and a pair of the
 * AuthenticationContext and the received object as parameters.
 *
 * @param path The path of the route.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
context(AuthenticationScope)
@KtorDsl
@JvmName("postResource")
inline fun <reified T> Route.post(
    path: String,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(Pair<AuthContext, T>) -> Unit
): Route = post(path, this@AuthenticationScope, body)

/**
 * Extension function for Route that creates a new POST route with authentication.
 *
 * This function takes a path, an AuthenticationScope, and a lambda function as parameters.
 * The lambda function defines the behavior of the route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use the
 * functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new POST route with the provided path.
 * The route uses the provided AuthenticationScope to authenticate the request.
 * If the authentication is successful, the lambda function is called with the
 * PipelineContext<Unit, ApplicationCall> and the AuthenticationContext as parameters.
 *
 * @param path The path of the route.
 * @param authenticationScope The AuthenticationScope used to authenticate the request.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
@KtorDsl
fun Route.post(
    path: String,
    authenticationScope: AuthenticationScope,
    body: suspend PipelineContext<Unit, ApplicationCall>.(AuthContext) -> Unit
): Route =
    post(path) {
        call.request.authenticate(authenticationScope, body)
    }

/**
 * Extension function for Route that creates a new POST route with authentication and resource reception.
 *
 * This function takes a path, an AuthenticationScope, and a lambda function as parameters.
 * The lambda function defines the behavior of the route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use
 * the functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new POST route with the provided path.
 * The route uses the provided AuthenticationScope to authenticate the request.
 * If the authentication is successful, the route receives an object of the specified type from
 * the request and calls the lambda function with the PipelineContext<Unit, ApplicationCall> and a pair of the
 * AuthenticationContext and the received object as parameters.
 *
 * @param path The path of the route.
 * @param authenticationScope The AuthenticationScope used to authenticate the request.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
@KtorDsl
@JvmName("postResource")
inline fun <reified T> Route.post(
    path: String,
    authenticationScope: AuthenticationScope,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(Pair<AuthContext, T>) -> Unit
): Route =
    post(path) {
        call.request.authenticate(authenticationScope) { context ->
            call.receive<T> { resource ->
                body(this@receive, context to resource)
            }
        }
    }

/**
 * Extension function for Route that creates a new PUT route with authentication.
 *
 * This function takes a path and a lambda function as parameters. The lambda function defines the behavior of the
 * route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use the
 * functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new PUT route with the provided path.
 * The route uses the current AuthenticationScope to authenticate the request.
 * If the authentication is successful, the lambda function is called with the
 * PipelineContext<Unit, ApplicationCall> and the AuthenticationContext as parameters.
 *
 * @param path The path of the route.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
context(AuthenticationScope)
@KtorDsl
fun Route.put(
    path: String,
    body: suspend PipelineContext<Unit, ApplicationCall>.(AuthContext) -> Unit
): Route = put(path, this@AuthenticationScope, body)

/**
 * Extension function for Route that creates a new PUT route with authentication and resource reception.
 *
 * This function takes a path and a lambda function as parameters. The lambda function defines the behavior of the
 * route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use
 * the functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new PUT route with the provided path.
 * The route uses the current AuthenticationScope to authenticate the request.
 * If the authentication is successful, the route receives an object of the specified type from
 * the request and calls the lambda function with the PipelineContext<Unit, ApplicationCall> and a pair of the
 * AuthenticationContext and the received object as parameters.
 *
 * @param path The path of the route.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
context(AuthenticationScope)
@KtorDsl
@JvmName("putResource")
inline fun <reified T> Route.put(
    path: String,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(Pair<AuthContext, T>) -> Unit
): Route = put(path, this@AuthenticationScope, body)

/**
 * Extension function for Route that creates a new PUT route with authentication.
 *
 * This function takes a path, an AuthenticationScope, and a lambda function as parameters.
 * The lambda function defines the behavior of the route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use the
 * functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new PUT route with the provided path.
 * The route uses the provided AuthenticationScope to authenticate the request.
 * If the authentication is successful, the lambda function is called with the
 * PipelineContext<Unit, ApplicationCall> and the AuthenticationContext as parameters.
 *
 * @param path The path of the route.
 * @param authenticationScope The AuthenticationScope used to authenticate the request.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
@KtorDsl
fun Route.put(
    path: String,
    authenticationScope: AuthenticationScope,
    body: suspend PipelineContext<Unit, ApplicationCall>.(AuthContext) -> Unit
): Route =
    put(path) {
        call.request.authenticate(authenticationScope, body)
    }

/**
 * Extension function for Route that creates a new PUT route with authentication and resource reception.
 *
 * This function takes a path, an AuthenticationScope, and a lambda function as parameters.
 * The lambda function defines the behavior of the route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use
 * the functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new PUT route with the provided path.
 * The route uses the provided AuthenticationScope to authenticate the request.
 * If the authentication is successful, the route receives an object of the specified type from
 * the request and calls the lambda function with the PipelineContext<Unit, ApplicationCall> and a pair of the
 * AuthenticationContext and the received object as parameters.
 *
 * @param path The path of the route.
 * @param authenticationScope The AuthenticationScope used to authenticate the request.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
@KtorDsl
@JvmName("putResource")
inline fun <reified T> Route.put(
    path: String,
    authenticationScope: AuthenticationScope,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(Pair<AuthContext, T>) -> Unit
): Route =
    put(path) {
        call.request.authenticate(authenticationScope) { context ->
            call.receive<T> { resource ->
                body(this@receive, context to resource)
            }
        }
    }

/**
 * Extension function for Route that creates a new DELETE route with authentication.
 *
 * This function takes a path and a lambda function as parameters. The lambda function defines the behavior of the
 * route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use the
 * functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new DELETE route with the provided path.
 * The route uses the current AuthenticationScope to authenticate the request.
 * If the authentication is successful, the lambda function is called with the
 * PipelineContext<Unit, ApplicationCall> and the AuthenticationContext as parameters.
 *
 * @param path The path of the route.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
context(AuthenticationScope)
@KtorDsl
fun Route.delete(
    path: String,
    body: suspend PipelineContext<Unit, ApplicationCall>.(AuthContext) -> Unit
): Route = delete(path, this@AuthenticationScope, body)

/**
 * Extension function for Route that creates a new DELETE route with authentication and resource reception.
 *
 * This function takes a path and a lambda function as parameters. The lambda function defines the behavior of the
 * route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use
 * the functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new DELETE route with the provided path.
 * The route uses the current AuthenticationScope to authenticate the request.
 * If the authentication is successful, the route receives an object of the specified type from
 * the request and calls the lambda function with the PipelineContext<Unit, ApplicationCall> and a pair of the
 * AuthenticationContext and the received object as parameters.
 *
 * @param path The path of the route.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
context(AuthenticationScope)
@KtorDsl
@JvmName("deleteResource")
inline fun <reified T> Route.delete(
    path: String,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(Pair<AuthContext, T>) -> Unit
): Route = delete(path, this@AuthenticationScope, body)

/**
 * Extension function for Route that creates a new DELETE route with authentication.
 *
 * This function takes a path, an AuthenticationScope, and a lambda function as parameters.
 * The lambda function defines the behavior of the route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use the
 * functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new DELETE route with the provided path.
 * The route uses the provided AuthenticationScope to authenticate the request.
 * If the authentication is successful, the lambda function is called with the
 * PipelineContext<Unit, ApplicationCall> and the AuthenticationContext as parameters.
 *
 * @param path The path of the route.
 * @param authenticationScope The AuthenticationScope used to authenticate the request.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
@KtorDsl
fun Route.delete(
    path: String,
    authenticationScope: AuthenticationScope,
    body: suspend PipelineContext<Unit, ApplicationCall>.(AuthContext) -> Unit
): Route =
    delete(path) {
        call.request.authenticate(authenticationScope, body)
    }

/**
 * Extension function for Route that creates a new DELETE route with authentication and resource reception.
 *
 * This function takes a path, an AuthenticationScope, and a lambda function as parameters.
 * The lambda function defines the behavior of the route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use
 * the functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new DELETE route with the provided path.
 * The route uses the provided AuthenticationScope to authenticate the request.
 * If the authentication is successful, the route receives an object of the specified type from
 * the request and calls the lambda function with the PipelineContext<Unit, ApplicationCall> and a pair of the
 * AuthenticationContext and the received object as parameters.
 *
 * @param path The path of the route.
 * @param authenticationScope The AuthenticationScope used to authenticate the request.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
@KtorDsl
@JvmName("deleteResource")
inline fun <reified T> Route.delete(
    path: String,
    authenticationScope: AuthenticationScope,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(Pair<AuthContext, T>) -> Unit
): Route =
    delete(path) {
        call.request.authenticate(authenticationScope) { context ->
            call.receive<T> { resource ->
                body(this@receive, context to resource)
            }
        }
    }

/**
 * Extension function for ApplicationRequest that authenticates the request.
 *
 * This function takes an AuthenticationScope and a lambda function as parameters.
 * The lambda function defines the behavior of the route after successful authentication.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use the
 * functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the provided AuthenticationScope to authenticate the request.
 * If the authentication is successful, the lambda function is called with the
 * PipelineContext<Unit, ApplicationCall> and the AuthenticationContext as parameters.
 * If the authentication fails, the function responds with an HTTP status code of Unauthorized and a list of error
 * messages.
 *
 * @param authenticationScope The AuthenticationScope used to authenticate the request.
 * @param body A lambda function that defines the behavior of the route after successful authentication. This is an
 * extension function for
 * PipelineContext<Unit, ApplicationCall>.
 */
context(PipelineContext<Unit, ApplicationCall>)
suspend fun ApplicationRequest.authenticate(
    authenticationScope: AuthenticationScope,
    body: suspend PipelineContext<Unit, ApplicationCall>.(AuthContext) -> Unit
) {
    with(authenticationScope) {
        this@authenticate.authenticate()
            .onRight { context -> body(this@PipelineContext, context) }
            .onLeft { errors -> errors.toFailure().respond() }
    }
}
