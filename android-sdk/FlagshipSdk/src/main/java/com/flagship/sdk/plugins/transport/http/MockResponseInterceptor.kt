package com.flagship.sdk.plugins.transport.http

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

/**
 * Mock response interceptor for testing HTTP requests without making actual network calls.
 * This interceptor allows you to define custom responses for specific URLs or use predefined responses.
 */
class MockResponseInterceptor : Interceptor {
    private val mockResponses = mutableMapOf<String, MockResponse>()
    private var defaultResponse: MockResponse? = null
    private var networkDelay: Long = 0

    /**
     * Data class representing a mock HTTP response
     */
    data class MockResponse(
        val code: Int = 200,
        val body: String = "",
        val headers: Map<String, String> = emptyMap(),
        val mediaType: String = "application/json",
    )

    /**
     * Add a mock response for a specific URL pattern
     */
    fun addMockResponse(
        urlPattern: String,
        response: MockResponse,
    ): MockResponseInterceptor {
        mockResponses[urlPattern] = response
        return this
    }

    /**
     * Set a default response for any unmatched URLs
     */
    fun setDefaultResponse(response: MockResponse): MockResponseInterceptor {
        defaultResponse = response
        return this
    }

    /**
     * Add network delay simulation (in milliseconds)
     */
    fun setNetworkDelay(delayMs: Long): MockResponseInterceptor {
        networkDelay = delayMs
        return this
    }

    /**
     * Add a successful feature flags response
     */
    fun addFeatureFlagsResponse(body: String): MockResponseInterceptor {
        val response =
            MockResponse(
                code = 200,
                body = body,
                headers = mapOf("Content-Type" to "application/json"),
            )
        return addMockResponse("/api/feature-flags", response)
    }

    /**
     * Add an error response
     */
    fun addErrorResponse(
        urlPattern: String,
        code: Int,
        message: String,
    ): MockResponseInterceptor {
        val response =
            MockResponse(
                code = code,
                body = """{"error": "$message"}""",
                headers = mapOf("Content-Type" to "application/json"),
            )
        return addMockResponse(urlPattern, response)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        // Simulate network delay if configured
        if (networkDelay > 0) {
            Thread.sleep(networkDelay)
        }

        // Find matching mock response
        val mockResponse =
            findMockResponse(url)
                ?: defaultResponse
                ?: throw IOException("No mock response configured for URL: $url")

        return createMockResponse(request, mockResponse)
    }

    private fun findMockResponse(url: String): MockResponse? =
        mockResponses.entries
            .find { (pattern, _) ->
                url.contains(pattern)
            }?.value

    private fun createMockResponse(
        request: Request,
        mockResponse: MockResponse,
    ): Response {
        val responseBuilder =
            Response
                .Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(mockResponse.code)
                .message(getHttpMessage(mockResponse.code))
                .body(mockResponse.body.toResponseBody(mockResponse.mediaType.toMediaType()))

        // Add headers
        mockResponse.headers.forEach { (key, value) ->
            responseBuilder.addHeader(key, value)
        }

        return responseBuilder.build()
    }

    private fun getHttpMessage(code: Int): String =
        when (code) {
            200 -> "OK"
            201 -> "Created"
            400 -> "Bad Request"
            401 -> "Unauthorized"
            403 -> "Forbidden"
            404 -> "Not Found"
            500 -> "Internal Server Error"
            502 -> "Bad Gateway"
            503 -> "Service Unavailable"
            else -> "Unknown"
        }

    companion object {
        /**
         * Create a mock interceptor with sample feature flags data
         */
        fun withSampleData(): MockResponseInterceptor = MockResponseInterceptor().addFeatureFlagsResponse(SAMPLE_FEATURE_FLAGS_JSON)

        /**
         * Create a mock interceptor that simulates network errors
         */
        fun withNetworkError(): MockResponseInterceptor =
            MockResponseInterceptor()
                .addErrorResponse("/api/feature-flags", 500, "Internal Server Error")

        /**
         * Create a mock interceptor that simulates slow network
         */
        fun withSlowNetwork(delayMs: Long = 2000): MockResponseInterceptor =
            MockResponseInterceptor()
                .setNetworkDelay(delayMs)
                .addFeatureFlagsResponse(SAMPLE_FEATURE_FLAGS_JSON)

        /**
         * Sample JSON response for testing
         *
         * Now using iOS-style direct JSON values that work with the new serializers.
         * This matches how iOS handles JSON parsing with direct primitive values.
         */
        const val SAMPLE_FEATURE_FLAGS_JSON = """{
  "features": [
    {
      "enabled": true,
      "key": "new-checkout-flow",
      "rollout_percentage": 50,
      "type": "boolean",
      "rules": [
        {
          "name": "6",
          "allocations": [
            {
              "percentage": 50,
              "variant_key": "variant-a"
            },
            {
              "percentage": 50,
              "variant_key": "variant-b"
            }
          ],
          "constraints": [
            {
              "context_field": "userId",
              "operator": "eq",
              "value": "12345"
            }
          ]
        }
      ],
      "default_rule": {
        "name": "4",
        "allocation": [
          {
            "variant_key": "off",
            "percentage": 100
          }
        ]
      },
      "updated_at": 1759231203,
    "variants": [
      {
        "key": "variant-a",
        "value": "simple-string-value"
      },
      {
        "key": "variant-b",
        "value": {
          "color": "blue",
          "buttonText": "Continue"
        }
      },
      {
        "key": "variant-c",
        "value": true
      },
      {
        "key": "variant-d",
        "value": 42.5
      }
    ]
    },
    {
      "enabled": false,
      "key": "beta-feature",
      "rollout_percentage": 0,
      "default_rule": {
        "name": "4",
        "allocation": [
          {
            "variant_key": "off",
            "percentage": 100
          }
        ]
      },
      "type": "boolean",
      "updated_at": 1759231203,
      "rules": [],
      "variants": []
    }
  ],
  "updated_at": 1759231299
}"""
    }
}
