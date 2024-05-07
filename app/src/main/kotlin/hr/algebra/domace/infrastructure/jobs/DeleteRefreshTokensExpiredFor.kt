package hr.algebra.domace.infrastructure.jobs

import hr.algebra.domace.domain.persistence.RefreshTokenPersistence
import kotlin.time.Duration

fun DeleteRefreshTokensExpiredFor(
    refreshTokenPersistence: RefreshTokenPersistence,
    duration: Duration
) = Job { refreshTokenPersistence.deleteExpiredFor(duration) }
