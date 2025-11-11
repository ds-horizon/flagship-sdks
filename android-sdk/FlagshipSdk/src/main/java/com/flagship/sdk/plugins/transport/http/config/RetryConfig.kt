package com.flagship.sdk.plugins.transport.http.config

import com.flagship.sdk.plugins.transport.http.interceptors.RetryInterceptor

/**
 * Configuration for retry behavior
 */
data class RetryConfig(
    val maxRetries: Int = 3,
    val baseDelayMs: Long = 1000,
    val maxDelayMs: Long = 30000,
    val retryableStatusCodes: Set<Int> = setOf(408, 429, 500, 502, 503, 504),
) {
    /**
     * Create a RetryInterceptor from this configuration
     */
    fun createInterceptor(): RetryInterceptor =
        RetryInterceptor(
            maxRetries = maxRetries,
            baseDelayMs = baseDelayMs,
            maxDelayMs = maxDelayMs,
            retryableStatusCodes = retryableStatusCodes,
        )

    companion object {
        /**
         * Conservative retry configuration
         */
        fun conservative(): RetryConfig =
            RetryConfig(
                maxRetries = 2,
                baseDelayMs = 2000,
                maxDelayMs = 10000,
            )

        /**
         * Aggressive retry configuration
         */
        fun aggressive(): RetryConfig =
            RetryConfig(
                maxRetries = 5,
                baseDelayMs = 500,
                maxDelayMs = 60000,
            )

        /**
         * No retry configuration
         */
        fun none(): RetryConfig? = null

        /**
         * Custom retry configuration
         */
        fun custom(
            maxRetries: Int,
            baseDelayMs: Long = 1000,
            maxDelayMs: Long = 30000,
            retryableStatusCodes: Set<Int> = setOf(408, 429, 500, 502, 503, 504),
        ): RetryConfig =
            RetryConfig(
                maxRetries = maxRetries,
                baseDelayMs = baseDelayMs,
                maxDelayMs = maxDelayMs,
                retryableStatusCodes = retryableStatusCodes,
            )
    }
}
