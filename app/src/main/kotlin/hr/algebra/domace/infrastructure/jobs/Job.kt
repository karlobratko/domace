package hr.algebra.domace.infrastructure.jobs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

/**
 * A functional interface representing a job that can be invoked.
 *
 * This interface defines a single suspendable function, invoke, which can be implemented to perform a specific task.
 * The suspend modifier allows the function to be paused and resumed, making it suitable for use in coroutines.
 *
 * The Job interface can be used to create instances of jobs using lambda expressions or function references.
 */
fun interface Job {
    /**
     * Invokes the job.
     *
     * This is a suspendable function, meaning it can be paused and resumed. This makes it suitable for use in
     * coroutines where it can be used to perform a task that may take some time to complete, such as a network request
     * or a database operation.
     */
    suspend operator fun invoke()
}

/**
 * Extension function for CoroutineScope that schedules a job to be executed at regular intervals.
 *
 * @param every The interval at which the job should be executed. This is a Duration object.
 * @param execute The job that should be executed. This is an instance of the Job functional interface.
 * @param failureHandler An optional parameter. A function that handles any exceptions thrown by the job.
 *                       By default, it does nothing.
 * @return A Job object from the kotlinx.coroutines package. This represents the coroutine that is executing the job.
 */
fun CoroutineScope.schedule(
    every: Duration,
    execute: Job,
    failureHandler: (error: Throwable) -> Unit = {}
): kotlinx.coroutines.Job =
    launch {
        // An infinite loop that executes the job at the specified interval.
        while (true) {
            // Pause the coroutine for the specified duration.
            delay(every)
            try {
                // Try to execute the job.
                execute()
            } catch (e: Throwable) {
                // If the job throws an exception, handle it with the failureHandler function.
                failureHandler(e)
            }
        }
    }
