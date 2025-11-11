package com.flagship.sdk.plugins.transport.http.config

import com.flagship.sdk.plugins.transport.http.interceptors.AuthInterceptor

/**
 * Configuration for authentication
 */
sealed class AuthConfig {
    /**
     * Bearer token authentication
     */
    data class Bearer(
        val token: String,
    ) : AuthConfig()

    /**
     * Basic authentication
     */
    data class Basic(
        val username: String,
        val password: String,
    ) : AuthConfig()

    /**
     * API key authentication
     */
    data class ApiKey(
        val headerName: String = "X-API-Key",
        val headerValue: String,
    ) : AuthConfig()

    /**
     * Custom header authentication
     */
    data class Custom(
        val headerName: String,
        val headerValue: String,
    ) : AuthConfig()

    /**
     * Create the appropriate AuthInterceptor for this configuration
     */
    fun createInterceptor(): AuthInterceptor =
        when (this) {
            is Bearer -> AuthInterceptor.bearer(token)
            is Basic -> AuthInterceptor.basic(username, password)
            is ApiKey -> AuthInterceptor.apiKey(headerValue, headerName)
            is Custom -> AuthInterceptor.custom(headerName, headerValue)
        }
}
