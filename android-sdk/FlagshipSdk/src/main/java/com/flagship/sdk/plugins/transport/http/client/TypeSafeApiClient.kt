package com.flagship.sdk.plugins.transport.http.client

import com.flagship.sdk.plugins.transport.http.client.models.ApiResponse
import com.flagship.sdk.plugins.transport.http.client.models.HttpMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Type-safe API client that uses reified types for better serialization handling.
 * This version provides better type safety by using reified generics.
 */
class TypeSafeApiClient(
    @PublishedApi internal val baseUrl: String,
    @PublishedApi internal val httpClient: OkHttpClient,
    @PublishedApi internal val json: Json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = false
        },
) {
    /**
     * Execute a GET request with type safety
     */
    suspend inline fun <reified T> get(
        endpoint: String,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
    ): ApiResponse<T> =
        executeTypeSafe(
            method = HttpMethod.GET,
            endpoint = endpoint,
            headers = headers,
            queryParams = queryParams,
        )

    /**
     * Execute a POST request with type safety
     */
    suspend inline fun <reified T, reified R> post(
        endpoint: String,
        body: R? = null,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
    ): ApiResponse<T> =
        executeTypeSafe(
            method = HttpMethod.POST,
            endpoint = endpoint,
            body = body,
            headers = headers,
            queryParams = queryParams,
        )

    /**
     * Execute a PUT request with type safety
     */
    suspend inline fun <reified T, reified R> put(
        endpoint: String,
        body: R? = null,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
    ): ApiResponse<T> =
        executeTypeSafe(
            method = HttpMethod.PUT,
            endpoint = endpoint,
            body = body,
            headers = headers,
            queryParams = queryParams,
        )

    /**
     * Execute a PATCH request with type safety
     */
    suspend inline fun <reified T, reified R> patch(
        endpoint: String,
        body: R? = null,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
    ): ApiResponse<T> =
        executeTypeSafe(
            method = HttpMethod.PATCH,
            endpoint = endpoint,
            body = body,
            headers = headers,
            queryParams = queryParams,
        )

    /**
     * Execute a DELETE request with type safety
     */
    suspend inline fun <reified T> delete(
        endpoint: String,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
    ): ApiResponse<T> =
        executeTypeSafe(
            method = HttpMethod.DELETE,
            endpoint = endpoint,
            headers = headers,
            queryParams = queryParams,
        )

    /**
     * Execute HTTP request with full type safety
     */
    @PublishedApi
    internal suspend inline fun <reified T> executeTypeSafe(
        method: HttpMethod,
        endpoint: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap(),
    ): ApiResponse<T> =
        withContext(Dispatchers.IO) {
            try {
                val httpRequest = buildHttpRequest(method, endpoint, body, headers, queryParams)
                val response = httpClient.newCall(httpRequest).execute()

                response.use {
                    parseTypeSafeResponse<T>(response)
                }
            } catch (e: Exception) {
                ErrorHandler.handleException(e)
            }
        }

    /**
     * Build HTTP request
     */
    @PublishedApi
    internal fun buildHttpRequest(
        method: HttpMethod,
        endpoint: String,
        body: Any?,
        headers: Map<String, String>,
        queryParams: Map<String, String>,
    ): Request {
        val url = buildUrl(endpoint, queryParams)
        val requestBuilder = Request.Builder().url(url)

        // Add headers
        headers.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }

        // Add default headers if not present
        if (!headers.containsKey("Accept")) {
            requestBuilder.addHeader("Accept", "application/json")
        }

        // Set HTTP method and body
        when (method) {
            HttpMethod.GET -> requestBuilder.get()
            HttpMethod.POST -> requestBuilder.post(createRequestBody(body))
            HttpMethod.PUT -> requestBuilder.put(createRequestBody(body))
            HttpMethod.PATCH -> requestBuilder.patch(createRequestBody(body))
            HttpMethod.DELETE -> requestBuilder.delete(createRequestBody(body))
        }

        return requestBuilder.build()
    }

    /**
     * Build full URL with query parameters
     */
    @PublishedApi
    internal fun buildUrl(
        endpoint: String,
        queryParams: Map<String, String>,
    ): String {
        val cleanBaseUrl = baseUrl.trimEnd('/')
        val cleanEndpoint = endpoint.trimStart('/')
        val baseUrl = "$cleanBaseUrl/$cleanEndpoint"

        if (queryParams.isEmpty()) {
            return baseUrl
        }

        val queryString =
            queryParams.entries.joinToString("&") { (key, value) ->
                "$key=${java.net.URLEncoder.encode(value, "UTF-8")}"
            }

        return "$baseUrl?$queryString"
    }

    /**
     * Create request body from object
     */
    @PublishedApi
    internal fun createRequestBody(body: Any?): RequestBody {
        if (body == null) {
            return "".toRequestBody("application/json".toMediaType())
        }

        return when (body) {
            is String -> body.toRequestBody("application/json".toMediaType())
            is ByteArray -> body.toRequestBody("application/octet-stream".toMediaType())
            else -> {
                val jsonString = json.encodeToString(body)
                jsonString.toRequestBody("application/json".toMediaType())
            }
        }
    }

    /**
     * Parse HTTP response with full type safety using reified types
     */
    @PublishedApi
    internal inline fun <reified T> parseTypeSafeResponse(response: okhttp3.Response): ApiResponse<T> {
        val responseBody = response.body?.string() ?: ""

        return if (response.isSuccessful) {
            try {
                val data: T? =
                    when {
                        responseBody.isEmpty() -> null
                        T::class == String::class -> responseBody as T?
                        T::class == Unit::class -> Unit as T?
                        else -> {
                            // Use reified type for safe deserialization
                            json.decodeFromString<T>(responseBody)
                        }
                    }
                ApiResponse.Success(
                    data = data,
                    code = response.code,
                    headers = response.headers.toMultimap(),
                )
            } catch (e: Exception) {
                ApiResponse.Error(
                    code = response.code,
                    message = "Failed to parse response: ${e.message}",
                    rawResponse = responseBody,
                    exception = e,
                )
            }
        } else {
            ApiResponse.Error(
                code = response.code,
                message = response.message,
                rawResponse = responseBody,
            )
        }
    }

    companion object {
        /**
         * Create a TypeSafeApiClient with default HTTP client
         */
        fun create(baseUrl: String): TypeSafeApiClient =
            TypeSafeApiClient(
                baseUrl = baseUrl,
                httpClient = HttpClientBuilder.createDefault(),
            )

        /**
         * Create a TypeSafeApiClient with custom HTTP client
         */
        fun create(
            baseUrl: String,
            httpClient: OkHttpClient,
        ): TypeSafeApiClient = TypeSafeApiClient(baseUrl, httpClient)

        /**
         * Create a TypeSafeApiClient for debugging
         */
        fun createDebug(baseUrl: String): TypeSafeApiClient =
            TypeSafeApiClient(
                baseUrl = baseUrl,
                httpClient = HttpClientBuilder.createDebug(),
            )

        /**
         * Create a TypeSafeApiClient for production
         */
        fun createProduction(baseUrl: String): TypeSafeApiClient =
            TypeSafeApiClient(
                baseUrl = baseUrl,
                httpClient = HttpClientBuilder.createProduction(),
            )
    }
}
