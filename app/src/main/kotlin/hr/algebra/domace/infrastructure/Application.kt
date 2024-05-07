package hr.algebra.domace.infrastructure

import arrow.continuations.SuspendApp
import arrow.continuations.ktor.server
import arrow.fx.coroutines.resourceScope
import hr.algebra.domace.infrastructure.jobs.jobs
import hr.algebra.domace.infrastructure.plugins.configure
import hr.algebra.domace.infrastructure.routes.routes
import io.ktor.server.netty.Netty
import kotlinx.coroutines.awaitCancellation

fun main(): Unit =
    SuspendApp {
        resourceScope {
            server(Netty, port = 8080, host = "localhost") {
                configure()
                routes()
                jobs()
            }

            awaitCancellation()
        }
    }
