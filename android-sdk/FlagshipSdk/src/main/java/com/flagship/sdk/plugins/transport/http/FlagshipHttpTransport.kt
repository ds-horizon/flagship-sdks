package com.flagship.sdk.plugins.transport.http

import com.flagship.sdk.core.contracts.ITransport
import com.flagship.sdk.core.models.FeatureFlagsSchema
import com.flagship.sdk.core.models.Result
import com.flagship.sdk.plugins.transport.http.client.TypeSafeApiClient
import com.flagship.sdk.plugins.transport.http.config.AuthConfig
import com.flagship.sdk.plugins.transport.http.config.HttpClientConfig
import com.flagship.sdk.plugins.transport.http.config.RetryConfig
import okhttp3.Interceptor

class FlagshipHttpTransport private constructor(
    private val apiClient: TypeSafeApiClient,
) : ITransport {
    override suspend fun fetchConfig(type: String): Result<FeatureFlagsSchema> {
        return performHttpRequest(type) ?: Result.Error(0, "Failed to perform HTTP request")
    }

    private suspend fun performHttpRequest(type: String): Result<FeatureFlagsSchema>? {
        return try {
            val queryParams = mapOf("type" to type)

            when (val response = apiClient.get<FeatureFlagsSchema>("v1/feature/config", queryParams = queryParams)) {
                is com.flagship.sdk.plugins.transport.http.client.models.ApiResponse.Success -> {
                    if (type == "time-only" && response.data == null) {
                        Result.Success(FeatureFlagsSchema(features = emptyList(), updatedAt = 0.0), response.headers)
                    } else {
                        response.data?.let {
                            Result.Success(it, response.headers)
                        } ?: Result.Error(response.code, "Empty response body for type=$type, code=${response.code}")
                    }
                }

                is com.flagship.sdk.plugins.transport.http.client.models.ApiResponse.Error -> {
                    Result.Error(response.code, response.message)
                }
            }
        } catch (e: Exception) {
            Result.Error(0, e.message ?: "Unknown error")
        }
    }

    companion object {
        fun create(
            baseUrl: String,
            customInterceptors: List<Interceptor> = emptyList(),
            enableLogging: Boolean = false,
        ): FlagshipHttpTransport {
            val config =
                HttpClientConfig(
                    baseUrl = baseUrl,
                    enableLogging = enableLogging,
                    retryConfig = RetryConfig.conservative(),
                    headers =
                        mapOf(
                            "Accept" to "application/json",
                            "Content-Type" to "application/json",
                        ),
                    customInterceptors = customInterceptors,
                )

            val apiClient = TypeSafeApiClient.create(config.baseUrl, config.buildClient())

            return FlagshipHttpTransport(apiClient)
        }

        fun createWithAuth(
            baseUrl: String,
            apiKey: String,
            enableLogging: Boolean = false,
        ): FlagshipHttpTransport {
            val config =
                HttpClientConfig(
                    baseUrl = baseUrl,
                    enableLogging = enableLogging,
                    retryConfig = RetryConfig.conservative(),
                    authConfig = AuthConfig.Bearer(apiKey),
                    headers =
                        mapOf(
                            "Accept" to "application/json",
                            "Content-Type" to "application/json",
                        ),
                )

            val apiClient = TypeSafeApiClient.create(config.baseUrl, config.buildClient())

            return FlagshipHttpTransport(apiClient)
        }

        fun createForTesting(
            baseUrl: String = "https://test.example.com",
            tenantId: String,
            mockInterceptors: List<Interceptor> = emptyList(),
            enableLogging: Boolean = false,
        ): FlagshipHttpTransport {
            val authConfig = tenantId.let { AuthConfig.ApiKey(headerName = "tenant-id", headerValue = it) }

            val config =
                HttpClientConfig.testing(
                    baseUrl = baseUrl,
                    mockInterceptors = mockInterceptors,
                    authConfig = authConfig,
                    enableLogging = enableLogging,
                )

            val apiClient = TypeSafeApiClient.create(config.baseUrl, config.buildClient())

            return FlagshipHttpTransport(apiClient)
        }

        fun createProduction(
            baseUrl: String,
            tenantId: String,
        ): FlagshipHttpTransport {
            val authConfig = tenantId.let { AuthConfig.ApiKey(it, headerValue = "tenant-id") }

            val config =
                HttpClientConfig.production(
                    baseUrl = baseUrl,
                    authConfig = authConfig,
                )

            val apiClient = TypeSafeApiClient.create(config.baseUrl, config.buildClient())

            return FlagshipHttpTransport(apiClient)
        }

        fun createCustom(
            baseUrl: String,
            clientConfig: HttpClientConfig? = null,
            customInterceptors: List<Interceptor> = emptyList(),
        ): FlagshipHttpTransport {
            val config =
                clientConfig ?: HttpClientConfig(
                    baseUrl = baseUrl,
                    customInterceptors = customInterceptors,
                    retryConfig = RetryConfig.conservative(),
                )

            val apiClient = TypeSafeApiClient.create(config.baseUrl, config.buildClient())

            return FlagshipHttpTransport(apiClient)
        }
    }
}
