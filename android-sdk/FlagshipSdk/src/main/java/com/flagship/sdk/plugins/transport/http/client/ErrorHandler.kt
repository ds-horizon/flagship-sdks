package com.flagship.sdk.plugins.transport.http.client

import com.flagship.sdk.plugins.transport.http.client.models.ApiException
import com.flagship.sdk.plugins.transport.http.client.models.ApiResponse
import kotlinx.coroutines.TimeoutCancellationException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Centralized error handling for HTTP operations
 */
object ErrorHandler {
    /**
     * Convert exceptions to appropriate ApiResponse.Error
     */
    fun handleException(exception: Throwable): ApiResponse.Error =
        when (exception) {
            is ApiException ->
                ApiResponse.Error(
                    code = exception.code,
                    message = exception.message,
                    exception = exception,
                )

            is UnknownHostException ->
                ApiResponse.Error(
                    code = 0,
                    message = "No internet connection or server not reachable",
                    exception = exception,
                )

            is ConnectException ->
                ApiResponse.Error(
                    code = 0,
                    message = "Unable to connect to server",
                    exception = exception,
                )

            is SocketTimeoutException ->
                ApiResponse.Error(
                    code = 408,
                    message = "Request timed out",
                    exception = exception,
                )

            is TimeoutCancellationException ->
                ApiResponse.Error(
                    code = 408,
                    message = "Request was cancelled due to timeout",
                    exception = exception,
                )

            is SSLException ->
                ApiResponse.Error(
                    code = 0,
                    message = "SSL/TLS connection error",
                    exception = exception,
                )

            is IOException ->
                ApiResponse.Error(
                    code = 0,
                    message = "Network error: ${exception.message}",
                    exception = exception,
                )

            else ->
                ApiResponse.Error(
                    code = 0,
                    message = "Unexpected error: ${exception.message}",
                    exception = exception,
                )
        }

    /**
     * Get user-friendly error message based on error code and type
     */
    fun getUserFriendlyMessage(error: ApiResponse.Error): String =
        when {
            error.code == 0 && error.exception is UnknownHostException ->
                "Please check your internet connection"

            error.code == 0 && error.exception is ConnectException ->
                "Unable to reach the server. Please try again later"

            error.code == 400 -> "Invalid request. Please check your input"
            error.code == 401 -> "Authentication required. Please log in"
            error.code == 403 -> "Access denied. You don't have permission"
            error.code == 404 -> "The requested resource was not found"
            error.code == 408 -> "Request timed out. Please try again"
            error.code == 429 -> "Too many requests. Please wait and try again"

            error.code in 500..599 -> "Server error. Please try again later"

            else -> error.message
        }

    /**
     * Check if an error is retryable
     */
    fun isRetryableError(error: ApiResponse.Error): Boolean =
        when {
            error.code in setOf(408, 429, 500, 502, 503, 504) -> true
            error.exception is SocketTimeoutException -> true
            error.exception is ConnectException -> true
            error.exception is UnknownHostException -> true
            else -> false
        }

    /**
     * Check if an error requires authentication
     */
    fun requiresAuthentication(error: ApiResponse.Error): Boolean = error.code == 401

    /**
     * Check if an error is a client error (4xx)
     */
    fun isClientError(error: ApiResponse.Error): Boolean = error.code in 400..499

    /**
     * Check if an error is a server error (5xx)
     */
    fun isServerError(error: ApiResponse.Error): Boolean = error.code in 500..599

    /**
     * Check if an error is a network error
     */
    fun isNetworkError(error: ApiResponse.Error): Boolean = error.code == 0 || error.exception is IOException
}
