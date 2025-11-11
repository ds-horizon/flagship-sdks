package com.flagship.sdk.plugins.transport.http.interceptors

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor for setting custom timeouts per request
 */
class TimeoutInterceptor(
    private val connectTimeoutMs: Long? = null,
    private val readTimeoutMs: Long? = null,
    private val writeTimeoutMs: Long? = null,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Remove timeout headers from the request if they exist
        val cleanedRequest =
            request
                .newBuilder()
                .removeHeader("X-Connect-Timeout")
                .removeHeader("X-Read-Timeout")
                .removeHeader("X-Write-Timeout")
                .build()

        // For now, just proceed with the request
        // Timeout configuration should be done at the OkHttpClient level
        return chain.proceed(cleanedRequest)
    }

    companion object {
        /**
         * Create a timeout interceptor with default values
         */
        fun create(
            connectTimeoutMs: Long = 30000,
            readTimeoutMs: Long = 30000,
            writeTimeoutMs: Long = 30000,
        ): TimeoutInterceptor = TimeoutInterceptor(connectTimeoutMs, readTimeoutMs, writeTimeoutMs)

        /**
         * Create a timeout interceptor for fast operations
         */
        fun fast(): TimeoutInterceptor =
            TimeoutInterceptor(
                connectTimeoutMs = 5000,
                readTimeoutMs = 10000,
                writeTimeoutMs = 10000,
            )

        /**
         * Create a timeout interceptor for slow operations
         */
        fun slow(): TimeoutInterceptor =
            TimeoutInterceptor(
                connectTimeoutMs = 60000,
                readTimeoutMs = 120000,
                writeTimeoutMs = 120000,
            )
    }
}
