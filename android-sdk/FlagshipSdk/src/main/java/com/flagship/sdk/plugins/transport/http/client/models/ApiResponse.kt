package com.flagship.sdk.plugins.transport.http.client.models

/**
 * Sealed class representing different types of API responses
 */
sealed class ApiResponse<out T> {
    /**
     * Successful response with data
     */
    data class Success<T>(
        val data: T?,
        val code: Int,
        val headers: Map<String, List<String>> = emptyMap(),
    ) : ApiResponse<T>()

    /**
     * Error response
     */
    data class Error(
        val code: Int,
        val message: String,
        val rawResponse: String? = null,
        val exception: Throwable? = null,
    ) : ApiResponse<Nothing>()

    /**
     * Check if the response is successful
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * Check if the response is an error
     */
    val isError: Boolean
        get() = this is Error

    /**
     * Get data if successful, null otherwise
     */
    fun getOrNull(): T? =
        when (this) {
            is Success -> data
            is Error -> null
        }

    /**
     * Get data if successful, throw exception if error
     */
    fun getOrThrow(): T? =
        when (this) {
            is Success -> data
            is Error -> throw ApiException(code, message, exception)
        }

    /**
     * Transform successful response data
     */
    inline fun <R> map(transform: (T?) -> R): ApiResponse<R> =
        when (this) {
            is Success -> Success(transform(data), code, headers)
            is Error -> this
        }

    /**
     * Execute action if successful
     */
    inline fun onSuccess(action: (T?) -> Unit): ApiResponse<T> {
        if (this is Success) {
            action(data)
        }
        return this
    }

    /**
     * Execute action if error
     */
    inline fun onError(action: (Error) -> Unit): ApiResponse<T> {
        if (this is Error) {
            action(this)
        }
        return this
    }

    /**
     * Fold the response into a single value
     */
    inline fun <R> fold(
        onSuccess: (T?) -> R,
        onError: (Error) -> R,
    ): R =
        when (this) {
            is Success -> onSuccess(data)
            is Error -> onError(this)
        }
}
