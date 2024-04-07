package hr.algebra.domace.infrastructure.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.doublereceive.DoubleReceive

fun Application.configureRouting() {
    install(DoubleReceive)
    install(AutoHeadResponse)
}
