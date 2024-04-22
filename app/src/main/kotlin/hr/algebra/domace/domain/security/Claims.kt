package hr.algebra.domace.domain.security

import arrow.core.NonEmptyList
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

sealed interface Claims {
    val issuer: Issuer
    val subject: Subject
    val audience: NonEmptyList<Audience>
    val use: Use
    val issuedAt: IssuedAt
    val expiresAt: ExpiresAt

    data class Refresh(
        override val issuer: Issuer,
        override val subject: Subject,
        override val audience: NonEmptyList<Audience>,
        override val issuedAt: IssuedAt,
        override val expiresAt: ExpiresAt
    ) : Claims {
        override val use = Use.Refresh

        constructor(
            issuer: Issuer,
            subject: Subject,
            audience: NonEmptyList<Audience>,
            issuedAt: IssuedAt,
            lasting: Token.Lasting
        ) : this(issuer, subject, audience, issuedAt, ExpiresAt(issuedAt.value + lasting.value))
    }

    data class Access(
        override val issuer: Issuer,
        override val subject: Subject,
        override val audience: NonEmptyList<Audience>,
        override val issuedAt: IssuedAt,
        override val expiresAt: ExpiresAt
    ) : Claims {
        override val use = Use.Access

        constructor(
            issuer: Issuer,
            subject: Subject,
            audience: NonEmptyList<Audience>,
            issuedAt: IssuedAt,
            lasting: Token.Lasting
        ) : this(issuer, subject, audience, issuedAt, ExpiresAt(issuedAt.value + lasting.value))
    }

    @JvmInline value class Issuer(val value: String)

    @JvmInline value class Subject(val value: String)

    @Serializable
    @JvmInline value class Audience(val value: String)

    enum class Use { Refresh, Access }

    @JvmInline value class IssuedAt(val value: Instant = Clock.System.now())

    @JvmInline value class ExpiresAt(val value: Instant)
}
