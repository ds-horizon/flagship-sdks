package com.flagship.sdk.plugins.transport.http.client.models

/**
 * Exception thrown when API calls fail
 */
class ApiException(
    val code: Int,
    override val message: String,
    override val cause: Throwable? = null,
) : Exception(message, cause) {
    /**
     * Check if this is a client error (4xx)
     */
    val isClientError: Boolean
        get() = code in 400..499

    /**
     * Check if this is a server error (5xx)
     */
    val isServerError: Boolean
        get() = code in 500..599

    /**
     * Check if this is a network error (code 0)
     */
    val isNetworkError: Boolean
        get() = code == 0

    /**
     * Check if this is an unauthorized error (401)
     */
    val isUnauthorized: Boolean
        get() = code == 401

    /**
     * Check if this is a forbidden error (403)
     */
    val isForbidden: Boolean
        get() = code == 403

    /**
     * Check if this is a not found error (404)
     */
    val isNotFound: Boolean
        get() = code == 404

    override fun toString(): String = "ApiException(code=$code, message='$message')"
}
