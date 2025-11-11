package com.flagship.sdk.plugins.transport.http.polling

import android.util.Log
import com.flagship.sdk.core.models.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * General utility for polling operations with configurable interval, retries, and backoff
 */
class PollingManager<T>(
    private val config: PollingConfig,
    private val operation: suspend () -> Result<T>?,
    private val emitPredicate: (result: Result<T>?) -> Boolean,
) {
    private var pollingJob: Job? = null
    private val isPollingActive = AtomicBoolean(false)
    private val isPermanentlyStopped = AtomicBoolean(false)
    private val consecutiveFailures = AtomicInteger(0)

    /**
     * Check if polling is currently running
     */
    val isRunning: Boolean
        get() = isPollingActive.get()

    /**
     * Check if polling has been permanently stopped (killed)
     */
    val isKilled: Boolean
        get() = isPermanentlyStopped.get()

    /**
     * Start polling operations and return a Flow of results
     * If already running, returns the existing flow
     */
    fun start(): Flow<Result<T>> {
        if (isPermanentlyStopped.get()) {
            return flowOf(Result.Error(-1, "PollingManager has been permanently stopped"))
        }

        if (isPollingActive.get()) {
            // Already running, return existing flow
            return getCurrentFlow()
        }

        return createPollingFlow()
    }

    /**
     * Stop polling temporarily - can be resumed with start()
     */
    fun stop() {
        if (!isPermanentlyStopped.get()) {
            isPollingActive.set(false)
            pollingJob?.cancel()
            pollingJob = null
        }
    }

    /**
     * Permanently stop polling and clean up resources
     * Cannot be resumed after calling this method
     */
    fun kill() {
        isPermanentlyStopped.set(true)
        isPollingActive.set(false)
        pollingJob?.cancel()
        pollingJob = null
        consecutiveFailures.set(0)
    }

    /**
     * Reset failure count (useful for external error recovery)
     */
    fun resetFailureCount() {
        consecutiveFailures.set(0)
    }

    /**
     * Get current failure count
     */
    fun getFailureCount(): Int = consecutiveFailures.get()

    private fun createPollingFlow(): Flow<Result<T>> =
        flow {
            isPollingActive.set(true)

            while (isPollingActive.get() && !isPermanentlyStopped.get()) {
                try {
                    // Execute the operation
                    Log.d("PollingManager", "Polling operation started")
                    val result = operation()

                    when (result) {
                        is Result.Success -> {
                            // Reset failure count on success
                            consecutiveFailures.set(0)
                            if (emitPredicate(result)) emit(result)
                        }

                        is Result.Error -> {
                            val failures = consecutiveFailures.incrementAndGet()
                            if (emitPredicate(result)) emit(result)

                            // Check if we've exceeded max retries
                            if (failures >= config.maxRetries) {
                                if (emitPredicate(result)) {
                                    emit(
                                        Result.Error(
                                            -2,
                                            "Polling stopped after $failures consecutive failures",
                                        ),
                                    )
                                    stop()
                                    break
                                }
                            }
                        }

                        is Result.Loading -> {
                            if (emitPredicate(result)) emit(result)
                        }

                        else -> Unit
                    }

                    // Calculate delay based on failure count
                    val delayMs = config.calculateDelay(consecutiveFailures.get())
                    delay(delayMs)
                } catch (e: CancellationException) {
                    // Expected when stopping polling
                    Log.d("PollingManager", "Polling operation cancelled: ${e.message}")
                    break
                } catch (e: Exception) {
                    // Unexpected error
                    Log.d("PollingManager", "Polling operation failed: ${e.message}")
                    val failures = consecutiveFailures.incrementAndGet()
                    emit(Result.Error(-3, "Unexpected polling error: ${e.message}"))

                    if (failures >= config.maxRetries) {
                        emit(Result.Error(-2, "Polling stopped after $failures consecutive failures"))
                        stop()
                        break
                    }

                    val delayMs = config.calculateDelay(failures)
                    delay(delayMs)
                }
            }

            isPollingActive.set(false)
        }.flowOn(config.dispatcher)
            .onCompletion {
                isPollingActive.set(false)
                Log.d("PollingManager", "Polling operation completed")
            }

    private fun getCurrentFlow(): Flow<Result<T>> =
        if (isPollingActive.get()) {
            // Return a simple flow that indicates polling is already active
            flowOf(Result.Error(-4, "Polling already active"))
        } else {
            createPollingFlow()
        }
}
