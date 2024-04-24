package hr.algebra.domace.domain.config

import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.Instant

/**
 * A functional interface that provides the current time.
 *
 * This interface defines a single method `now` that returns the current time as an `Instant`.
 * It can be implemented by any class that can provide the current time.
 * For example, it can be implemented by a class that reads the system clock, or by a class that reads the time from a
 * remote server.
 *
 * The `now` method does not take any parameters and returns an `Instant` representing the current time.
 */
fun interface InstantProvider {
    /**
     * Gets the current time.
     *
     * This method returns the current time as an `Instant`.
     * The source of the time can vary depending on the implementation.
     *
     * @return The current time as an `Instant`.
     */
    fun now(): Instant
}

/**
 * An instance of the `InstantProvider` interface that uses the system clock to provide the current time.
 *
 * This instance uses the `now` function from the `kotlinx.datetime.Clock.System` object to get the current time.
 * The `now` function reads the system clock and returns the current time as an `Instant`.
 *
 * This instance can be used whenever the current system time is needed.
 */
val DefaultInstantProvider = InstantProvider { now() }

/**
 * An instance of the `InstantProvider` interface that provides the current time rounded to the nearest second.
 *
 * This instance uses the `now` function from the `kotlinx.datetime.Clock.System` object to get the current time.
 * The `now` function reads the system clock and returns the current time as an `Instant`.
 * The time is then converted to epoch seconds using the `epochSeconds` property of the `Instant` class, and a new
 * `Instant` is created from these epoch seconds.
 * This effectively rounds the time to the nearest second, as the fractional part of the second is discarded.
 *
 * This instance can be used whenever the current system time is needed, but a precision of one second is sufficient.
 */
val RoundedInstantProvider = InstantProvider { Instant.fromEpochSeconds(now().epochSeconds) }
