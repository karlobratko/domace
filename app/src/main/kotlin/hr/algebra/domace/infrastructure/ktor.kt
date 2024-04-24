package hr.algebra.domace.infrastructure

import io.ktor.http.HttpHeaders
import io.ktor.server.plugins.origin
import io.ktor.server.request.ApplicationRequest

/**
 * Extension property for the ApplicationRequest class.
 *
 * This property provides a way to get the remote host of the request.
 * It first tries to get the host from the 'X-Forwarded-For' header, which is used when the request is forwarded
 * through a proxy.
 * If the 'X-Forwarded-For' header is present, it splits the value by comma (as it can contain multiple IPs), takes the
 * first IP, and trims any whitespace.
 * If the 'X-Forwarded-For' header is not present or its value is null, it falls back to the remote host of the origin
 * of the request.
 *
 * @return The remote host of the request, obtained from the 'X-Forwarded-For' header or the origin of the request.
 */
val ApplicationRequest.remoteHost
    get() = headers[HttpHeaders.XForwardedFor]
        ?.split(",")
        ?.firstOrNull()
        ?.trim()
        ?: origin.remoteHost
