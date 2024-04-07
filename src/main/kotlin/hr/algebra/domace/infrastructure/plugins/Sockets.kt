package hr.algebra.domace.infrastructure.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = 15.seconds.toJavaDuration()
        timeout = 15.seconds.toJavaDuration()
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
}
