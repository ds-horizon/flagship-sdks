package com.flagship.sdk.plugins.transport.http.examples

import com.flagship.sdk.plugins.transport.http.client.TypeSafeApiClient
import com.flagship.sdk.plugins.transport.http.config.AuthConfig
import com.flagship.sdk.plugins.transport.http.config.HttpClientConfig
import com.flagship.sdk.plugins.transport.http.interceptors.RetryInterceptor
import kotlinx.serialization.Serializable

/**
 * Example implementations showing how to use the reusable HTTP layer
 */
class ApiClientExamples {
    // Example data classes for API responses
    @Serializable
    data class User(
        val id: String,
        val name: String,
        val email: String,
    )

    @Serializable
    data class CreateUserRequest(
        val name: String,
        val email: String,
    )

    @Serializable
    data class UpdateUserRequest(
        val name: String? = null,
        val email: String? = null,
    )

    @Serializable
    data class ApiError(
        val code: String,
        val message: String,
        val details: Map<String, String>? = null,
    )

    /**
     * Example: Simple API client for user management
     */
    class UserApiClient(
        private val apiClient: TypeSafeApiClient,
    ) {
        suspend fun getUser(userId: String) =
            apiClient.get<User>(
                endpoint = "/users/$userId",
            )

        suspend fun getAllUsers() =
            apiClient.get<List<User>>(
                endpoint = "/users",
            )

        suspend fun createUser(request: CreateUserRequest) =
            apiClient.post<User, CreateUserRequest>(
                endpoint = "/users",
                body = request,
            )

        suspend fun updateUser(
            userId: String,
            request: UpdateUserRequest,
        ) = apiClient.put<User, UpdateUserRequest>(
            endpoint = "/users/$userId",
            body = request,
        )

        suspend fun deleteUser(userId: String) =
            apiClient.delete<Unit>(
                endpoint = "/users/$userId",
            )

        suspend fun searchUsers(query: String) =
            apiClient.get<List<User>>(
                endpoint = "/users/search",
                queryParams = mapOf("q" to query),
            )
    }

    /**
     * Example: Feature flag API client (existing functionality)
     */
    class FeatureFlagApiClient(
        private val apiClient: TypeSafeApiClient,
    ) {
        suspend fun getFeatureFlags() =
            apiClient.get<Map<String, Any>>(
                endpoint = "/api/feature-flags",
            )

        suspend fun getFeatureFlag(flagKey: String) =
            apiClient.get<Map<String, Any>>(
                endpoint = "/api/feature-flags/$flagKey",
            )

        suspend fun updateFeatureFlag(
            flagKey: String,
            enabled: Boolean,
        ) = apiClient.put<Map<String, Any>, Map<String, Boolean>>(
            endpoint = "/api/feature-flags/$flagKey",
            body = mapOf("enabled" to enabled),
        )
    }

    companion object {
        /**
         * Example: Create a production API client
         */
        fun createProductionClient(): TypeSafeApiClient {
            val config =
                HttpClientConfig.production(
                    baseUrl = "https://api.example.com",
                    authConfig = AuthConfig.Bearer("your-api-token"),
                )
            return TypeSafeApiClient.create(config.baseUrl, config.buildClient())
        }

        /**
         * Example: Create a development API client with detailed logging
         */
        fun createDevelopmentClient(): TypeSafeApiClient {
            val config =
                HttpClientConfig.development(
                    baseUrl = "https://dev-api.example.com",
                    authConfig = AuthConfig.ApiKey("X-API-Key", "dev-api-key"),
                )
            return TypeSafeApiClient.create(config.baseUrl, config.buildClient())
        }

        /**
         * Example: Create a testing API client with mock responses
         */
        fun createTestingClient(): TypeSafeApiClient {
            // You would add your mock interceptors here
            val config =
                HttpClientConfig.testing(
                    baseUrl = "https://test.example.com",
                    // mockInterceptors = listOf(yourMockInterceptor)
                )
            return TypeSafeApiClient.create(config.baseUrl, config.buildClient())
        }

        /**
         * Example: Create a custom API client with specific requirements
         */
        fun createCustomClient(): TypeSafeApiClient {
            val config =
                HttpClientConfig(
                    baseUrl = "https://custom-api.example.com",
                    connectTimeoutSeconds = 15,
                    readTimeoutSeconds = 45,
                    writeTimeoutSeconds = 45,
                    enableLogging = true,
                    retryConfig =
                        com.flagship.sdk.plugins.transport.http.config.RetryConfig.custom(
                            maxRetries = 3,
                            baseDelayMs = 1500,
                            maxDelayMs = 30000,
                        ),
                    authConfig = AuthConfig.Custom("X-Custom-Auth", "custom-token"),
                    headers =
                        mapOf(
                            "X-Client-Version" to "1.0.0",
                            "X-Platform" to "Android",
                        ),
                    customInterceptors =
                        listOf(
                            // Add custom interceptors here
                            RetryInterceptor.conservative(),
                        ),
                )
            return TypeSafeApiClient.create(config.baseUrl, config.buildClient())
        }

        /**
         * Example usage of the API clients
         */
        suspend fun exampleUsage() {
            val apiClient = createProductionClient()
            val userClient = UserApiClient(apiClient)

            // Get a user
            val userResponse = userClient.getUser("123")
            userResponse
                .onSuccess { user ->
                    println("Retrieved user: ${user?.name}")
                }.onError { error ->
                    println("Failed to get user: ${error.message}")
                }

            // Create a new user
            val createRequest = CreateUserRequest("John Doe", "john@example.com")
            val createResponse = userClient.createUser(createRequest)

            when {
                createResponse.isSuccess -> {
                    val newUser = createResponse.getOrNull()
                    println("Created user with ID: ${newUser?.id}")
                }
                createResponse.isError -> {
                    val error = createResponse as com.flagship.sdk.plugins.transport.http.client.models.ApiResponse.Error
                    println("Failed to create user: ${error.message}")
                }
            }

            // Search users
            val searchResponse = userClient.searchUsers("john")
            val users = searchResponse.getOrNull()
            println("Found ${users?.size ?: 0} users")
        }
    }
}
