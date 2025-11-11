package com.flagship.sdk.plugins.transport.http.config

import com.flagship.sdk.plugins.transport.http.client.HttpClientBuilder
import com.flagship.sdk.plugins.transport.http.interceptors.HeaderInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Configuration class for HTTP clients with pre-defined setups
 */
data class HttpClientConfig(
    val baseUrl: String,
    val connectTimeoutSeconds: Long = 30,
    val readTimeoutSeconds: Long = 30,
    val writeTimeoutSeconds: Long = 30,
    val enableLogging: Boolean = false,
    val loggingLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BODY,
    val retryConfig: RetryConfig? = null,
    val authConfig: AuthConfig? = null,
    val headers: Map<String, String> = emptyMap(),
    val customInterceptors: List<Interceptor> = emptyList(),
    val retryOnConnectionFailure: Boolean = true,
) {
    /**
     * Build an OkHttpClient from this configuration
     */
    fun buildClient(): OkHttpClient {
        val builder =
            HttpClientBuilder
                .create()
                .connectTimeout(connectTimeoutSeconds)
                .readTimeout(readTimeoutSeconds)
                .writeTimeout(writeTimeoutSeconds)
                .enableLogging(enableLogging, loggingLevel)
                .retryOnConnectionFailure(retryOnConnectionFailure)

        // Add custom interceptors first
        customInterceptors.forEach { interceptor ->
            builder.addInterceptor(interceptor)
        }

        // Add auth interceptor if configured
        authConfig?.let { config ->
            builder.addInterceptor(config.createInterceptor())
        }

        // Add common headers if configured
        if (headers.isNotEmpty()) {
            builder.addInterceptor(HeaderInterceptor.custom(headers))
        }

        // Add retry interceptor if configured
        retryConfig?.let { config ->
            builder.addInterceptor(config.createInterceptor())
        }

        return builder.build()
    }

    companion object {
        /**
         * Create a basic HTTP client configuration
         */
        fun basic(baseUrl: String): HttpClientConfig = HttpClientConfig(baseUrl = baseUrl)

        /**
         * Create a production HTTP client configuration
         */
        fun production(
            baseUrl: String,
            authConfig: AuthConfig? = null,
        ): HttpClientConfig =
            HttpClientConfig(
                baseUrl = baseUrl,
                connectTimeoutSeconds = 10,
                readTimeoutSeconds = 30,
                writeTimeoutSeconds = 30,
                enableLogging = false,
                retryConfig = RetryConfig.conservative(),
                authConfig = authConfig,
                headers =
                    mapOf(
                        "Accept" to "application/json",
                        "Content-Type" to "application/json",
                    ),
            )

        /**
         * Create a development HTTP client configuration
         */
        fun development(
            baseUrl: String,
            authConfig: AuthConfig? = null,
        ): HttpClientConfig =
            HttpClientConfig(
                baseUrl = baseUrl,
                connectTimeoutSeconds = 60,
                readTimeoutSeconds = 60,
                writeTimeoutSeconds = 60,
                enableLogging = true,
                loggingLevel = HttpLoggingInterceptor.Level.BODY,
                retryConfig = RetryConfig.aggressive(),
                authConfig = authConfig,
                headers =
                    mapOf(
                        "Accept" to "application/json",
                        "Content-Type" to "application/json",
                    ),
            )

        /**
         * Create a testing HTTP client configuration
         */
        fun testing(
            baseUrl: String = "https://test.example.com",
            authConfig: AuthConfig? = null,
            mockInterceptors: List<Interceptor> = emptyList(),
            enableLogging: Boolean = false,
        ): HttpClientConfig =
            HttpClientConfig(
                baseUrl = baseUrl,
                connectTimeoutSeconds = 5,
                readTimeoutSeconds = 10,
                writeTimeoutSeconds = 10,
                enableLogging = enableLogging,
                retryConfig = null, // No retries in tests
                customInterceptors = mockInterceptors,
                authConfig = authConfig,
                retryOnConnectionFailure = false,
            )
    }
}
