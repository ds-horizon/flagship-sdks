package com.flagship.sdk.plugins.transport.http

import com.flagship.sdk.core.contracts.ITransport
import com.flagship.sdk.core.models.FeatureFlagsSchema
import com.flagship.sdk.core.models.Result
import com.flagship.sdk.plugins.transport.http.client.TypeSafeApiClient
import com.flagship.sdk.plugins.transport.http.config.AuthConfig
import com.flagship.sdk.plugins.transport.http.config.HttpClientConfig
import com.flagship.sdk.plugins.transport.http.config.RetryConfig
import com.flagship.sdk.plugins.transport.http.polling.PollingConfig
import com.flagship.sdk.plugins.transport.http.polling.PollingManager
import kotlinx.coroutines.flow.Flow
import okhttp3.Interceptor

/**
 * Modern HTTP transport implementation using the reusable HTTP layer
 * This replaces the existing HttpTransport with a cleaner, more maintainable implementation
 */
class FlagshipHttpTransport private constructor(
    private val apiClient: TypeSafeApiClient,
    private val pollingConfig: PollingConfig,
) : ITransport {
    private var pollingManager: PollingManager<*>? = null

    override fun fetchConfig(type: String): Flow<Result<FeatureFlagsSchema>> {
        val httpOperation: suspend () -> Result<FeatureFlagsSchema>? = {
            performHttpRequest(type)
        }

        val manager =
            PollingManager(pollingConfig, httpOperation, emitPredicate = { result ->
                result !is Result.Error || result.errorCode != 302
            })

        pollingManager = manager

        return manager.start()
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
                        } ?: Result.Error(response.code, "Empty response body")
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

    /**
     * Stop the current polling operation
     */
    private fun stopPolling() {
        pollingManager?.stop()
    }

    /**
     * Permanently stop polling and clean up resources
     */
    private fun killPolling() {
        pollingManager?.kill()
        pollingManager = null
    }

    /**
     * Check if polling is currently active
     */
    private val isPolling: Boolean
        get() = pollingManager?.isRunning ?: false

    companion object {
        /**
         * Create a FlagshipHttpTransport with basic configuration
         */
        fun create(
            baseUrl: String,
            pollingInterval: Long = 30000, // 30 seconds default
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
            val pollingConfig =
                PollingConfig(
                    intervalMs = pollingInterval.toLong(),
                    maxRetries = 3,
                    linearBackoffMs = 1000,
                )

            return FlagshipHttpTransport(apiClient, pollingConfig)
        }

        /**
         * Create a FlagshipHttpTransport with authentication
         */
        fun createWithAuth(
            baseUrl: String,
            apiKey: String,
            pollingInterval: Int = 30000,
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
            val pollingConfig =
                PollingConfig(
                    intervalMs = pollingInterval.toLong(),
                    maxRetries = 3,
                    linearBackoffMs = 1000,
                )

            return FlagshipHttpTransport(apiClient, pollingConfig)
        }

        /**
         * Create a FlagshipHttpTransport for testing with mock responses
         */
        fun createForTesting(
            baseUrl: String = "https://test.example.com",
            tenantId: String,
            pollingInterval: Long = 1000, // Faster polling for tests
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
            val pollingConfig =
                PollingConfig(
                    intervalMs = pollingInterval,
                    maxRetries = 3, // Fewer retries in tests
                    linearBackoffMs = 100,
                )

            return FlagshipHttpTransport(apiClient, pollingConfig)
        }

        /**
         * Create a FlagshipHttpTransport for production use
         */
        fun createProduction(
            baseUrl: String,
            tenantId: String,
            pollingInterval: Long = 30000,
        ): FlagshipHttpTransport {
            val authConfig = tenantId.let { AuthConfig.ApiKey(it, headerValue = "tenant-id") }

            val config =
                HttpClientConfig.production(
                    baseUrl = baseUrl,
                    authConfig = authConfig,
                )

            val apiClient = TypeSafeApiClient.create(config.baseUrl, config.buildClient())
            val pollingConfig =
                PollingConfig(
                    intervalMs = pollingInterval,
                    maxRetries = 3,
                    linearBackoffMs = 1000,
                )

            return FlagshipHttpTransport(apiClient, pollingConfig)
        }

        /**
         * Create a FlagshipHttpTransport with custom configuration
         */
        fun createCustom(
            baseUrl: String,
            pollingInterval: Int = 30000,
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
            val pollingConfig =
                PollingConfig(
                    intervalMs = pollingInterval.toLong(),
                    maxRetries = 3,
                    linearBackoffMs = 1000,
                )

            return FlagshipHttpTransport(apiClient, pollingConfig)
        }
    }
}
