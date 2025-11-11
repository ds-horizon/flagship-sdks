package com.flagship.sdk.core.models

/**
 * Simple result wrapper
 */
sealed class Result<out T> {
    data class Success<T>(
        val data: T,
        val headers: Map<String, List<String>> = emptyMap(),
    ) : Result<T>()

    data class Error(
        val errorCode: Int,
        val message: String,
    ) : Result<Nothing>()

    data object Loading : Result<Nothing>()
}

/**
 * Helper to safely execute network calls
 */
suspend fun <T> safeCall(call: suspend () -> T): Result<T> =
    try {
        Result.Success(call())
    } catch (e: Exception) {
        Result.Error(0, e.message ?: "Unknown error")
    }
