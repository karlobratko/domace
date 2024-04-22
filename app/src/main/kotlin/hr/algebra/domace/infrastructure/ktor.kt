package hr.algebra.domace.infrastructure

import io.ktor.http.HttpHeaders
import io.ktor.server.plugins.origin
import io.ktor.server.request.ApplicationRequest

val ApplicationRequest.remoteHost
    get() = headers[HttpHeaders.XForwardedFor]
        ?.split(",")
        ?.firstOrNull()
        ?.trim()
        ?: origin.remoteHost
