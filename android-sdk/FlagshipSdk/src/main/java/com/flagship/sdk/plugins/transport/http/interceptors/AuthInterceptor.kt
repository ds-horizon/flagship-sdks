package com.flagship.sdk.plugins.transport.http.interceptors

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor for adding authentication headers to HTTP requests
 */
class AuthInterceptor private constructor(
    private val headerName: String,
    private val headerValue: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // Skip if header already exists
        if (original.header(headerName) != null) {
            return chain.proceed(original)
        }

        val authenticated =
            original
                .newBuilder()
                .header(headerName, headerValue)
                .build()

        return chain.proceed(authenticated)
    }

    companion object {
        /**
         * Create an interceptor for Bearer token authentication
         */
        fun bearer(token: String): AuthInterceptor = AuthInterceptor("Authorization", "Bearer $token")

        /**
         * Create an interceptor for Basic authentication
         */
        fun basic(
            username: String,
            password: String,
        ): AuthInterceptor {
            val credentials =
                android.util.Base64.encodeToString(
                    "$username:$password".toByteArray(),
                    android.util.Base64.NO_WRAP,
                )
            return AuthInterceptor("Authorization", "Basic $credentials")
        }

        /**
         * Create an interceptor for API key authentication
         */
        fun apiKey(
            key: String,
            headerName: String = "X-API-Key",
        ): AuthInterceptor = AuthInterceptor(headerName, key)

        /**
         * Create an interceptor for custom header authentication
         */
        fun custom(
            headerName: String,
            headerValue: String,
        ): AuthInterceptor = AuthInterceptor(headerName, headerValue)
    }
}
