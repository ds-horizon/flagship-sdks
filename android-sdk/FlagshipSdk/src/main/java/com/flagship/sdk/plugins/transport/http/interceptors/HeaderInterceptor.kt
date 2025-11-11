package com.flagship.sdk.plugins.transport.http.interceptors

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor for adding common headers to all HTTP requests
 */
class HeaderInterceptor(
    private val headers: Map<String, String>,
    private val overrideExisting: Boolean = false,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()

        headers.forEach { (name, value) ->
            if (overrideExisting || original.header(name) == null) {
                builder.header(name, value)
            }
        }

        return chain.proceed(builder.build())
    }

    companion object {
        /**
         * Create a header interceptor with common headers
         */
        fun common(
            userAgent: String? = null,
            acceptLanguage: String? = null,
            additionalHeaders: Map<String, String> = emptyMap(),
        ): HeaderInterceptor {
            val headers = mutableMapOf<String, String>()

            userAgent?.let { headers["User-Agent"] = it }
            acceptLanguage?.let { headers["Accept-Language"] = it }
            headers.putAll(additionalHeaders)

            return HeaderInterceptor(headers)
        }

        /**
         * Create a header interceptor for JSON API calls
         */
        fun jsonApi(additionalHeaders: Map<String, String> = emptyMap()): HeaderInterceptor {
            val headers =
                mutableMapOf(
                    "Accept" to "application/json",
                    "Content-Type" to "application/json",
                )
            headers.putAll(additionalHeaders)

            return HeaderInterceptor(headers)
        }

        /**
         * Create a header interceptor with custom headers
         */
        fun custom(
            headers: Map<String, String>,
            overrideExisting: Boolean = false,
        ): HeaderInterceptor = HeaderInterceptor(headers, overrideExisting)
    }
}
