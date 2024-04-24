package hr.algebra.domace.domain.validation

import arrow.core.nonEmptyListOf
import hr.algebra.domace.domain.SecurityError.InvalidRefreshTokenStatus
import hr.algebra.domace.domain.SecurityError.TokenExpired
import hr.algebra.domace.domain.kotest.instant
import hr.algebra.domace.domain.model.RefreshToken
import hr.algebra.domace.domain.model.User
import hr.algebra.domace.domain.security.Claims
import hr.algebra.domace.domain.security.Token
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.property.Arb
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive
import kotlinx.datetime.Clock.System.now
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.Instant.Companion.DISTANT_FUTURE as DistantFuture
import kotlinx.datetime.Instant.Companion.DISTANT_PAST as DistantPast

object RefreshTokenValidationTests : ShouldSpec({
    should("pass if not expired and token status satisfied") {
        checkAll(
            Arb.instant(now().plus(1.seconds)..DistantFuture),
            RefreshToken.Status.entries.exhaustive()
        ) { instant, status ->
            // given
            val refreshTokenEntity = RefreshToken.Entity(
                RefreshToken.Id(1),
                User.Id(1),
                Token.Refresh(""),
                Claims.IssuedAt(),
                Claims.ExpiresAt(instant),
                status
            )

            // when
            val actual = with(RefreshTokenValidation(status)) {
                refreshTokenEntity.validate()
            }

            // then
            actual shouldBeRight refreshTokenEntity
        }
    }

    should("accumulate errors from all validation steps") {
        checkAll(
            Arb.instant(DistantPast..now().minus(1.seconds)),
            RefreshToken.Status.entries.exhaustive()
        ) { instant, status ->
            // given
            val refreshTokenEntity = RefreshToken.Entity(
                RefreshToken.Id(1),
                User.Id(1),
                Token.Refresh(""),
                Claims.IssuedAt(),
                Claims.ExpiresAt(instant),
                status
            )

            // when
            val otherStatus = when (status) {
                RefreshToken.Status.Active -> RefreshToken.Status.Revoked
                RefreshToken.Status.Revoked -> RefreshToken.Status.Active
            }
            val actual = with(RefreshTokenValidation(otherStatus)) {
                refreshTokenEntity.validate()
            }

            // then
            actual shouldBeLeft nonEmptyListOf(
                TokenExpired,
                InvalidRefreshTokenStatus
            )
        }
    }
})