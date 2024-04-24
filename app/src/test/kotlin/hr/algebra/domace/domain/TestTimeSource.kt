package hr.algebra.domace.domain

import kotlin.time.AbstractLongTimeSource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit.MILLISECONDS as Milliseconds

class TestTimeSource : AbstractLongTimeSource(unit = Milliseconds) {
    private var reading: Duration = 0.milliseconds

    override fun read(): Long = reading.inWholeMilliseconds

    operator fun plusAssign(duration: Duration) {
        reading += duration
    }

    operator fun minusAssign(duration: Duration) {
        reading -= duration
    }
}
