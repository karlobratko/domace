package hr.algebra.domace.infrastructure.plugins

import io.ktor.server.application.Application

fun Application.configure() {
    configureDI()
    configureHTTP()
    configureMonitoring()
    configureRouting()
    configureSerialization()
    configureSockets()
}
