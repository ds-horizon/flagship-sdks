package com.flagship.sdk.plugins.transport.http.polling

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Configuration for polling operations
 */
data class PollingConfig(
    /**
     * Base interval between polling attempts in milliseconds
     */
    val intervalMs: Long,
    /**
     * Maximum number of consecutive failures before stopping polling
     */
    val maxRetries: Int = 3,
    /**
     * Linear backoff multiplier - each failure adds this much delay to the base interval
     * For example: if intervalMs = 5000 and linearBackoffMs = 2000
     * - 1st attempt: 5000ms delay
     * - 2nd attempt (after 1 failure): 7000ms delay
     * - 3rd attempt (after 2 failures): 9000ms delay
     */
    val linearBackoffMs: Long = 1000,
    /**
     * Coroutine dispatcher for polling operations. Defaults to IO dispatcher
     */
    val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    init {
        require(intervalMs > 0) { "Interval must be positive" }
        require(maxRetries >= 0) { "Max retries must be non-negative" }
        require(linearBackoffMs >= 0) { "Linear backoff must be non-negative" }
    }

    /**
     * Calculate delay for the given failure count
     */
    internal fun calculateDelay(failureCount: Int): Long = intervalMs + (failureCount * linearBackoffMs)
}
