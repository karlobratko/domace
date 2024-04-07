package hr.algebra.domace.infrastructure

import hr.algebra.domace.infrastructure.plugins.configureHTTP
import hr.algebra.domace.infrastructure.plugins.configureMonitoring
import hr.algebra.domace.infrastructure.plugins.configureRouting
import hr.algebra.domace.infrastructure.plugins.configureSecurity
import hr.algebra.domace.infrastructure.plugins.configureSerialization
import hr.algebra.domace.infrastructure.plugins.configureSockets
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSockets()
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureSecurity()
    configureRouting()
}
