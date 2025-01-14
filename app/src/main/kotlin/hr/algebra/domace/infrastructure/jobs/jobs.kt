package hr.algebra.domace.infrastructure.jobs

import hr.algebra.domace.domain.persistence.RefreshTokenPersistence
import io.ktor.server.application.Application
import org.koin.ktor.ext.inject
import kotlin.time.Duration.Companion.days

fun Application.jobs() {
    val refreshTokenPersistence by inject<RefreshTokenPersistence>()

    schedule(
        every = 1.days,
        execute = RevokeExpiredRefreshTokens(refreshTokenPersistence)
    )

    schedule(
        every = 1.days,
        execute = DeleteRefreshTokensExpiredFor(refreshTokenPersistence, 7.days)
    )
}
