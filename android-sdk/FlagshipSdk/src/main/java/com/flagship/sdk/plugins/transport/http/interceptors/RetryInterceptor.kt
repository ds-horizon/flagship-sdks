package com.flagship.sdk.plugins.transport.http.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.math.min
import kotlin.math.pow

/**
 * Interceptor that automatically retries failed requests with exponential backoff
 */
class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val baseDelayMs: Long = 1000,
    private val maxDelayMs: Long = 30000,
    private val retryableStatusCodes: Set<Int> = setOf(408, 429, 500, 502, 503, 504),
    private val retryableExceptions: Set<Class<out Exception>> =
        setOf(
            SocketTimeoutException::class.java,
            UnknownHostException::class.java,
            IOException::class.java,
        ),
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var lastResponse: Response? = null
        var lastException: Exception? = null

        repeat(maxRetries + 1) { attempt ->
            try {
                lastResponse?.close() // Close previous response to free resources
                val response = chain.proceed(request)

                // If successful or non-retryable error, return response
                if (response.isSuccessful || !isRetryableStatusCode(response.code) || attempt == maxRetries) {
                    return response
                }

                lastResponse = response

                // Wait before retry (except on last attempt)
                if (attempt < maxRetries) {
                    Thread.sleep(calculateDelay(attempt))
                }
            } catch (e: Exception) {
                lastException = e

                // If not retryable exception or last attempt, throw
                if (!isRetryableException(e) || attempt == maxRetries) {
                    throw e
                }

                // Wait before retry
                if (attempt < maxRetries) {
                    Thread.sleep(calculateDelay(attempt))
                }
            }
        }

        // This should not be reached, but handle just in case
        lastResponse?.let { return it }
        lastException?.let { throw it }
        throw IOException("Max retries exceeded")
    }

    /**
     * Calculate delay for exponential backoff with jitter
     */
    private fun calculateDelay(attempt: Int): Long {
        val exponentialDelay = (baseDelayMs * 2.0.pow(attempt.toDouble())).toLong()
        val delayWithJitter = exponentialDelay + (Math.random() * baseDelayMs).toLong()
        return min(delayWithJitter, maxDelayMs)
    }

    /**
     * Check if HTTP status code is retryable
     */
    private fun isRetryableStatusCode(code: Int): Boolean = retryableStatusCodes.contains(code)

    /**
     * Check if exception is retryable
     */
    private fun isRetryableException(exception: Exception): Boolean = retryableExceptions.any { it.isInstance(exception) }

    companion object {
        /**
         * Create a retry interceptor with default settings
         */
        fun create(): RetryInterceptor = RetryInterceptor()

        /**
         * Create a retry interceptor for aggressive retrying
         */
        fun aggressive(): RetryInterceptor =
            RetryInterceptor(
                maxRetries = 5,
                baseDelayMs = 500,
                maxDelayMs = 60000,
            )

        /**
         * Create a retry interceptor for conservative retrying
         */
        fun conservative(): RetryInterceptor =
            RetryInterceptor(
                maxRetries = 2,
                baseDelayMs = 2000,
                maxDelayMs = 10000,
            )

        /**
         * Create a retry interceptor with custom settings
         */
        fun custom(
            maxRetries: Int,
            baseDelayMs: Long = 1000,
            maxDelayMs: Long = 30000,
            retryableStatusCodes: Set<Int> = setOf(408, 429, 500, 502, 503, 504),
        ): RetryInterceptor =
            RetryInterceptor(
                maxRetries = maxRetries,
                baseDelayMs = baseDelayMs,
                maxDelayMs = maxDelayMs,
                retryableStatusCodes = retryableStatusCodes,
            )
    }
}
