package hr.algebra.domace.domain.kotest

import io.kotest.property.Arb
import io.kotest.property.kotlinx.datetime.instant
import kotlinx.datetime.Instant

fun Arb.Companion.instant(range: ClosedRange<Instant>): Arb<Instant> =
    instant(range.start.toEpochMilliseconds()..range.endInclusive.toEpochMilliseconds())
