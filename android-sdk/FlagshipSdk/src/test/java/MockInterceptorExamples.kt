package com.flagship.sdk.plugins.transport.http

import com.flagship.sdk.core.models.Result
import kotlinx.coroutines.runBlocking

/**
 * Examples showing how to use MockResponseInterceptor for testing
 */
object MockInterceptorExamples {
    /**
     * Example 1: Basic testing with sample data
     */
    fun testWithSampleData() =
        runBlocking {
            // Create transport with mock interceptor
            val mockInterceptor = MockResponseInterceptor.withSampleData()
            val transport =
                FlagshipHttpTransport.createForTesting(
                    mockInterceptors = listOf(mockInterceptor),
                    enableLogging = true,
                    tenantId = "",
                    baseUrl = "https://google.com",
                )

            val result = transport.fetchConfig("full")

            when (result) {
                is Result.Success -> {
                    println("✅ Success: Received ${result.data.features.size} feature flags")
                    result.data.features.forEach { flag ->
                        println("  - ${flag.key}: ${if (flag.enabled) "enabled" else "disabled"}")
                    }
                }
                is Result.Error -> println("❌ Error: ${result.message}")
                is Result.Loading -> println("🔄 Loading...")
            }

            // Stop polling when done
        }

    /**
     * Example 2: Testing error scenarios
     */
    fun testErrorScenarios() =
        runBlocking {
            // Create transport that will return server errors
            val mockInterceptor = MockResponseInterceptor.withNetworkError()
            val transport =
                FlagshipHttpTransport.createForTesting(
                    mockInterceptors = listOf(mockInterceptor),
                    enableLogging = true,
                    tenantId = "",
                    baseUrl = "https://google.com",
                )

            val result = transport.fetchConfig("full")

            when (result) {
                is Result.Success -> println("✅ Unexpected success")
                is Result.Error -> println("❌ Expected error: ${result.message}")
                is Result.Loading -> println("🔄 Loading...")
            }
        }

    /**
     * Example 3: Testing slow network conditions
     */
    fun testSlowNetwork() =
        runBlocking {
            // Create transport with 2-second delay
            val mockInterceptor = MockResponseInterceptor.withSlowNetwork(2000)
            val transport =
                FlagshipHttpTransport.createForTesting(
                    mockInterceptors = listOf(mockInterceptor),
                    enableLogging = true,
                    tenantId = "",
                    baseUrl = "https://google.com",
                )

            val startTime = System.currentTimeMillis()
            val result = transport.fetchConfig("full")
            val endTime = System.currentTimeMillis()

            println("⏱️ Request took ${endTime - startTime}ms")

            when (result) {
                is Result.Success -> println("✅ Success after delay")
                is Result.Error -> println("❌ Error: ${result.message}")
                is Result.Loading -> println("🔄 Loading...")
            }
        }

    /**
     * Example 4: Custom mock responses
     */
    fun testCustomResponses() =
        runBlocking {
            // Create custom mock response
            val customResponse =
                MockResponseInterceptor.MockResponse(
                    code = 200,
                    body = """{
                "flags": [
                    {
                        "enabled": true,
                        "key": "test-flag",
                        "rolloutPercentage": 100,
                        "rules": [],
                        "variants": []
                    }
                ]
            }""",
                    headers = mapOf("X-Custom-Header" to "test-value"),
                )

            val mockInterceptor =
                MockResponseInterceptor()
                    .addMockResponse("/api/feature-flags", customResponse)

            val transport =
                FlagshipHttpTransport.createForTesting(
                    mockInterceptors = listOf(mockInterceptor),
                    enableLogging = true,
                    tenantId = "",
                    baseUrl = "https://google.com",
                )

            val result = transport.fetchConfig("full")

            when (result) {
                is Result.Success -> {
                    println("✅ Custom response: ${result.data.features.first().key}")
                }
                is Result.Error -> println("❌ Error: ${result.message}")
                is Result.Loading -> println("🔄 Loading...")
            }
        }

    /**
     * Example 5: Testing different HTTP status codes
     */
    fun testDifferentStatusCodes() =
        runBlocking {
            val testCases =
                listOf(
                    200 to "Success",
                    401 to "Unauthorized",
                    404 to "Not Found",
                    500 to "Internal Server Error",
                )

            for ((statusCode, description) in testCases) {
                println("\n🧪 Testing $statusCode - $description")

                val mockInterceptor =
                    if (statusCode == 200) {
                        MockResponseInterceptor.withSampleData()
                    } else {
                        MockResponseInterceptor().addErrorResponse(
                            "/api/feature-flags",
                            statusCode,
                            description,
                        )
                    }

                val transport =
                    FlagshipHttpTransport.createForTesting(
                        mockInterceptors = listOf(mockInterceptor),
                        enableLogging = true,
                        tenantId = "",
                        baseUrl = "https://google.com",
                    )

                val result = transport.fetchConfig("full")

                when (result) {
                    is Result.Success -> println("   ✅ Success")
                    is Result.Error -> println("   ❌ Error $statusCode: ${result.message}")
                    is Result.Loading -> println("   🔄 Loading...")
                }
            }
        }
}

/**
 * Run all examples
 */
fun main() {
    println("🚀 Running Mock Interceptor Examples\n")

    println("1️⃣ Testing with sample data:")
    MockInterceptorExamples.testWithSampleData()

    println("\n2️⃣ Testing error scenarios:")
    MockInterceptorExamples.testErrorScenarios()

    println("\n3️⃣ Testing slow network:")
    MockInterceptorExamples.testSlowNetwork()

    println("\n4️⃣ Testing custom responses:")
    MockInterceptorExamples.testCustomResponses()

    println("\n5️⃣ Testing different status codes:")
    MockInterceptorExamples.testDifferentStatusCodes()

    println("\n✨ All examples completed!")
}
