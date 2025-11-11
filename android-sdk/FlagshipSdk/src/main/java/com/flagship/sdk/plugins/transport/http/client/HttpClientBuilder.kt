package com.flagship.sdk.plugins.transport.http.client

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Builder class for creating configured OkHttpClient instances.
 * Provides a fluent API for setting up HTTP clients with common configurations.
 */
class HttpClientBuilder {
    private var connectTimeoutSeconds: Long = 30
    private var readTimeoutSeconds: Long = 30
    private var writeTimeoutSeconds: Long = 30
    private var enableLogging: Boolean = false
    private var loggingLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BODY
    private val interceptors: MutableList<Interceptor> = mutableListOf()
    private val networkInterceptors: MutableList<Interceptor> = mutableListOf()
    private var retryOnConnectionFailure: Boolean = true

    /**
     * Set connection timeout in seconds
     */
    fun connectTimeout(seconds: Long): HttpClientBuilder {
        connectTimeoutSeconds = seconds
        return this
    }

    /**
     * Set read timeout in seconds
     */
    fun readTimeout(seconds: Long): HttpClientBuilder {
        readTimeoutSeconds = seconds
        return this
    }

    /**
     * Set write timeout in seconds
     */
    fun writeTimeout(seconds: Long): HttpClientBuilder {
        writeTimeoutSeconds = seconds
        return this
    }

    /**
     * Enable or disable HTTP logging
     */
    fun enableLogging(
        enable: Boolean,
        level: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BODY,
    ): HttpClientBuilder {
        enableLogging = enable
        loggingLevel = level
        return this
    }

    /**
     * Add an application interceptor
     */
    fun addInterceptor(interceptor: Interceptor): HttpClientBuilder {
        interceptors.add(interceptor)
        return this
    }

    /**
     * Add multiple application interceptors
     */
    fun addInterceptors(vararg interceptors: Interceptor): HttpClientBuilder {
        this.interceptors.addAll(interceptors)
        return this
    }

    /**
     * Add a network interceptor
     */
    fun addNetworkInterceptor(interceptor: Interceptor): HttpClientBuilder {
        networkInterceptors.add(interceptor)
        return this
    }

    /**
     * Add multiple network interceptors
     */
    fun addNetworkInterceptors(vararg interceptors: Interceptor): HttpClientBuilder {
        this.networkInterceptors.addAll(interceptors)
        return this
    }

    /**
     * Enable or disable retry on connection failure
     */
    fun retryOnConnectionFailure(retry: Boolean): HttpClientBuilder {
        retryOnConnectionFailure = retry
        return this
    }

    /**
     * Build the OkHttpClient with all configured settings
     */
    fun build(): OkHttpClient {
        val builder =
            OkHttpClient
                .Builder()
                .connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(writeTimeoutSeconds, TimeUnit.SECONDS)
                .retryOnConnectionFailure(retryOnConnectionFailure)

        // Add custom interceptors first
        interceptors.forEach { interceptor ->
            builder.addInterceptor(interceptor)
        }

        // Add logging interceptor if enabled
        if (enableLogging) {
            val loggingInterceptor =
                HttpLoggingInterceptor().apply {
                    level = loggingLevel
                }
            builder.addInterceptor(loggingInterceptor)
        }

        // Add network interceptors
        networkInterceptors.forEach { interceptor ->
            builder.addNetworkInterceptor(interceptor)
        }

        return builder.build()
    }

    companion object {
        /**
         * Create a new HttpClientBuilder instance
         */
        fun create(): HttpClientBuilder = HttpClientBuilder()

        /**
         * Create a basic HTTP client with default settings
         */
        fun createDefault(): OkHttpClient = create().build()

        /**
         * Create an HTTP client optimized for production use
         */
        fun createProduction(): OkHttpClient =
            create()
                .connectTimeout(10)
                .readTimeout(30)
                .writeTimeout(30)
                .enableLogging(false)
                .retryOnConnectionFailure(true)
                .build()

        /**
         * Create an HTTP client optimized for development/debugging
         */
        fun createDebug(): OkHttpClient =
            create()
                .connectTimeout(60)
                .readTimeout(60)
                .writeTimeout(60)
                .enableLogging(true, HttpLoggingInterceptor.Level.BODY)
                .retryOnConnectionFailure(true)
                .build()

        /**
         * Create an HTTP client optimized for testing
         */
        fun createTesting(): OkHttpClient =
            create()
                .connectTimeout(5)
                .readTimeout(10)
                .writeTimeout(10)
                .enableLogging(false)
                .retryOnConnectionFailure(false)
                .build()
    }
}
