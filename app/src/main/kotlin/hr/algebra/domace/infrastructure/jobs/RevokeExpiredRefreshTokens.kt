package hr.algebra.domace.infrastructure.jobs

import hr.algebra.domace.domain.persistence.RefreshTokenPersistence

fun RevokeExpiredRefreshTokens(refreshTokenPersistence: RefreshTokenPersistence) =
    Job { refreshTokenPersistence.revokeExpired() }
