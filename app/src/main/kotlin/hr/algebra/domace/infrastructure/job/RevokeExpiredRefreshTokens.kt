package hr.algebra.domace.infrastructure.job

import hr.algebra.domace.domain.persistence.RefreshTokenPersistence

fun RevokeExpiredRefreshTokens(refreshTokenPersistence: RefreshTokenPersistence) =
    Job { refreshTokenPersistence.revokeExpired() }
