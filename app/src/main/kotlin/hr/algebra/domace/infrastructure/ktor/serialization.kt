package hr.algebra.domace.infrastructure.ktor

import hr.algebra.domace.domain.RequestError.RequestBodyCouldNotBeParsed
import hr.algebra.domace.domain.toEitherNel
import hr.algebra.domace.infrastructure.routes.toFailure
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.util.KtorDsl
import io.ktor.util.pipeline.PipelineContext

/**
 * Extension function for the Route class that creates a new GET route.
 *
 * This function takes a path and a lambda function as parameters. The lambda function defines the behavior of the
 * route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use the
 * functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new GET route with the provided path.
 * The route receives an object of the specified type from the request and calls the lambda function with the
 * PipelineContext<Unit, ApplicationCall> and the received object as parameters.
 *
 * @param path The path of the route.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
@KtorDsl
inline fun <reified T> Route.get(
    path: String,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route = get(path) { call.receive<T>(body) }

@KtorDsl
inline fun <reified T> Route.get(
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route = get { call.receive<T>(body) }

/**
 * Extension function for the Route class that creates a new POST route.
 *
 * This function takes a path and a lambda function as parameters. The lambda function defines the behavior of the
 * route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use the
 * functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new POST route with the provided path.
 * The route receives an object of the specified type from the request and calls the lambda function with the
 * PipelineContext<Unit, ApplicationCall> and the received object as parameters.
 *
 * @param path The path of the route.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
@KtorDsl
inline fun <reified T> Route.post(
    path: String,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route = post(path) { call.receive<T>(body) }

@KtorDsl
inline fun <reified T> Route.post(
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route = post { call.receive<T>(body) }

/**
 * Extension function for the Route class that creates a new PUT route.
 *
 * This function takes a path and a lambda function as parameters. The lambda function defines the behavior of the
 * route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use
 * the functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new PUT route with the provided path.
 * The route receives an object of the specified type from the request and calls the lambda function with the
 * PipelineContext<Unit, ApplicationCall> and the received object as parameters.
 *
 * @param path The path of the route.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
@KtorDsl
inline fun <reified T> Route.put(
    path: String,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route = put(path) { call.receive<T>(body) }

@KtorDsl
inline fun <reified T> Route.put(
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route = put { call.receive<T>(body) }

/**
 * Extension function for the Route class that creates a new DELETE route.
 *
 * This function takes a path and a lambda function as parameters. The lambda function defines the behavior of the
 * route.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use
 * the functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function uses the Ktor DSL to create a new DELETE route with the provided path.
 * The route receives an object of the specified type from the request and calls the lambda function with the
 * PipelineContext<Unit, ApplicationCall> and the received object as parameters.
 *
 * @param path The path of the route.
 * @param body A lambda function that defines the behavior of the route. This is an extension function for
 * PipelineContext<Unit, ApplicationCall>.
 * @return The created Route.
 */
@KtorDsl
inline fun <reified T> Route.delete(
    path: String,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route = delete(path) { call.receive<T>(body) }

@KtorDsl
inline fun <reified T> Route.delete(
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route = delete { call.receive<T>(body) }

/**
 * Extension function for the ApplicationCall class that receives an object of a specified type from the request.
 *
 * This function takes a lambda function as a parameter. The lambda function defines the behavior after the
 * object is received.
 * The lambda function is an extension function for PipelineContext<Unit, ApplicationCall>, which means it can use
 * the functions and properties of PipelineContext<Unit, ApplicationCall>.
 * The function first tries to receive a nullable object of the specified type from the request.
 * If the reception is successful, it calls the lambda function with the PipelineContext<Unit, ApplicationCall> and
 * the received object as parameters.
 * If the reception fails (for example, if the request does not contain an object of the specified type), it responds
 * with a BadRequest status and a Failure response containing the error messages.
 *
 * @param body A lambda function that defines the behavior after the object is received. This is an extension function
 * for PipelineContext<Unit, ApplicationCall>.
 */
context(PipelineContext<Unit, ApplicationCall>)
suspend inline fun <reified T> ApplicationCall.receive(
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
) {
    this@receive.receiveOrNone<T>().toEitherNel { RequestBodyCouldNotBeParsed }
        .onRight { body(this@PipelineContext, it) }
        .onLeft { errors -> call.respond(errors.toFailure()) }
}
