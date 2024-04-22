package hr.algebra.domace.infrastructure.job

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

fun interface Job {
    suspend operator fun invoke()
}

fun CoroutineScope.schedule(
    every: Duration,
    execute: Job,
    failureHandler: (error: Exception) -> Unit = {}
): kotlinx.coroutines.Job =
    launch {
        while (true) {
            delay(every)
            try {
                execute()
            } catch (e: Exception) {
                failureHandler(e)
            }
        }
    }
