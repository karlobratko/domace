package hr.algebra.domace.infrastructure

import arrow.continuations.SuspendApp
import arrow.continuations.ktor.server
import arrow.fx.coroutines.resourceScope
import hr.algebra.domace.domain.persistence.RefreshTokenPersistence
import hr.algebra.domace.infrastructure.job.DeleteRefreshTokensExpiredFor
import hr.algebra.domace.infrastructure.job.RevokeExpiredRefreshTokens
import hr.algebra.domace.infrastructure.job.schedule
import hr.algebra.domace.infrastructure.persistence.Database.dev
import hr.algebra.domace.infrastructure.persistence.exposed.ExposedRefreshTokenPersistence
import hr.algebra.domace.infrastructure.plugins.configureHTTP
import hr.algebra.domace.infrastructure.plugins.configureMonitoring
import hr.algebra.domace.infrastructure.plugins.configureRouting
import hr.algebra.domace.infrastructure.plugins.configureSerialization
import hr.algebra.domace.infrastructure.plugins.configureSockets
import io.ktor.server.application.Application
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlin.time.Duration.Companion.days

fun main(): Unit =
    SuspendApp {
//        val client = HttpClient(Jetty) {
//            install(ContentNegotiation) {
//                json()
//            }
//        }

        resourceScope {
            val refreshTokenPersistence = ExposedRefreshTokenPersistence(dev.postgres)

            server(Netty, port = 8080, host = "localhost") {
                module()
                routes()
            }

            startJobs(refreshTokenPersistence)

            awaitCancellation()
        }
    }

fun Application.module() {
    configure()
    routes()
}

fun Application.configure() {
    configureHTTP()
    configureMonitoring()
    configureRouting()
    configureSerialization()
    configureSockets()
}

fun Application.routes() {
    routing {
    }
}

private fun CoroutineScope.startJobs(refreshTokenPersistence: RefreshTokenPersistence) {
    schedule(
        every = 1.days,
        execute = RevokeExpiredRefreshTokens(refreshTokenPersistence)
    )

    schedule(
        every = 1.days,
        execute = DeleteRefreshTokensExpiredFor(refreshTokenPersistence, 7.days)
    )
}
