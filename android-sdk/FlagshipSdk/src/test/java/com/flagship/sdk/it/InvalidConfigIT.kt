package com.flagship.sdk.it

import android.app.Application
import com.flagship.sdk.core.models.Reason
import com.flagship.sdk.facade.FlagShipClient
import com.flagship.sdk.facade.FlagShipConfig
import com.flagship.sdk.utils.MockUtils
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.mockk.mockk
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InvalidConfigIT {
    private lateinit var mockApplication: Application
    private lateinit var flagshipClient: FlagShipClient
    private lateinit var wireMockServer: WireMockServer
    private val defaultContext = mapOf(
        "user_tier" to "premium",
        "country" to "US",
        "user_group" to "beta_testerss",
        "is_logged_in" to true,
        "is_accessibility_user" to false,
        "device" to "mobile",
        "theme_pref" to "light",
        "session_count" to 150.0,
        "region" to "US",
        "userId" to 3456,
        "app_version" to "2.5.0",
        "user_tags" to listOf("early-adopter", "beta-tester", "premium"),
    )

    @BeforeAll
    fun setUpAll() {
        // Start WireMock server on a random port
        wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())
        wireMockServer.start()
        WireMock.configureFor("localhost", wireMockServer.port())


        // Mock the Android Application context
        mockApplication = mockk<Application>(relaxed = true)

        // Get WireMock server base URL
        val baseUrl = "http://localhost:${wireMockServer.port()}"

        // Create FlagShipConfig with WireMock server URL
        // Note: Not using MockResponseInterceptor here since we're using WireMock for mocking
        val config = FlagShipConfig(
            applicationContext = mockApplication,
            baseUrl = baseUrl,
            flagshipApiKey = "tenant1",
            refreshInterval = TimeUnit.MILLISECONDS.toMillis(30),
            mockInterceptors = emptyList(), // Empty list since we're using WireMock
        )

        // Initialize FlagShipClient
        flagshipClient = FlagShipClient.getInstance("test-domain", config)

        // Set the context
        flagshipClient.onContextChange(emptyMap(), defaultContext)
    }

    @AfterAll
    fun tearDown() {
        flagshipClient.shutDown()
        wireMockServer.stop()
    }

    @Test
    fun `returns config default for rule with missing allocations`() {
        MockUtils.stubWireMockFeatureConfig {
            MockUtils.createFeatureWithMissingAllocations<Int>(
                key = "missing-allocations-feature",
                variants = mapOf("variant-a" to 100, "variant-b" to 200),
            )
        }
        Thread.sleep(100)

        val flag = flagshipClient.getInt(
            "missing-allocations-feature",
            42,
            "test-key",
            defaultContext,
        )
        assertEquals(42, flag.value)
        assertEquals(Reason.ERROR, flag.reason)
    }

    @Test
    fun `should not match rule for type mismatch of context field in context, config`() {
        MockUtils.stubWireMockFeatureConfig {
            // Create constraint expecting String for userId, but context has Int
            MockUtils.createFeatureWithTypeMismatchConstraint<Int>(
                key = "type-mismatch-feature",
                variants = mapOf("variant-a" to 100, "variant-b" to 200, "default" to 69),
                contextField = "userId",
                expectedType = "String", // But context has userId as Int
            )
        }
        Thread.sleep(100)

        val flag = flagshipClient.getInt(
            "type-mismatch-feature",
            42,
            "test-key",
            defaultContext,
        )
        // Rule won't match due to type mismatch, should fall back to default rule
        assertEquals(69, flag.value) // Should get variant-a from default rule
        assertEquals(Reason.DEFAULT_TARGETING_MATCH, flag.reason)
    }

    @Test
    fun `unknown context field in constraint, constraint false, next rule continues`() {
        MockUtils.stubWireMockFeatureConfig {
            MockUtils.createFeatureWithUnknownContextField<Int>(
                key = "unknown-field-feature",
                variants = mapOf("variant-a" to 100, "variant-b" to 200, "default" to 69),
            )
        }
        Thread.sleep(100)

        val flag = flagshipClient.getInt(
            "unknown-field-feature",
            42,
            "test-key",
            defaultContext,
        )
        // First rule has unknown field, won't match. Second rule should match.
        assertEquals(200, flag.value) // Should get variant-b from rule-2
        assertEquals(Reason.TARGETING_MATCH, flag.reason)
    }

    @Test
    fun `Allocation sum less than 100 return config default`() {
        MockUtils.stubWireMockFeatureConfig {
            MockUtils.createFeatureWithAllocationSumLessThan100<Int>(
                key = "allocation-lt-100-feature",
                variants = mapOf("variant-a" to 100, "variant-b" to 200),
            )
        }
        Thread.sleep(100)

        val flag = flagshipClient.getInt(
            "allocation-lt-100-feature",
            42,
            "test-key",
            defaultContext,
        )
        assertEquals(42, flag.value)
        assertEquals(Reason.ERROR, flag.reason)
    }

    @Test
    fun `Allocation sum greater than 100 return config default`() {
        MockUtils.stubWireMockFeatureConfig {
            MockUtils.createFeatureWithAllocationSumGreaterThan100<Int>(
                key = "allocation-gt-100-feature",
                variants = mapOf("variant-a" to 100, "variant-b" to 200),
            )
        }
        Thread.sleep(100)

        val flag = flagshipClient.getInt(
            "allocation-gt-100-feature",
            42,
            "test-key",
            defaultContext,
        )
        assertEquals(42, flag.value)
        assertEquals(Reason.ERROR, flag.reason)
    }

    @Test
    fun `returns user default when default rule missing entirely`() {
        MockUtils.stubWireMockFeatureConfig {
            MockUtils.createFeatureWithMissingDefaultRule<Int>(
                key = "missing-default-rule-feature",
                variants = mapOf("variant-a" to 100, "variant-b" to 200),
            )
        }
        Thread.sleep(100)

        // Use a targeting key that doesn't match any rule constraints
        val flag = flagshipClient.getInt(
            "missing-default-rule-feature",
            42,
            "test-key-no-match",
            mapOf("user_tier" to "free", "country" to "CA"), // Different context that won't match rules
        )
        // No rules match, and no default rule, should return user default
        assertEquals(42, flag.value)
        assertEquals(Reason.DEFAULT, flag.reason)
    }

    @Test
    fun `Missing enabled, treated as false (user default)`() {
        MockUtils.stubWireMockFeatureConfig {
            MockUtils.createFeatureWithMissingEnabled<Int>(
                key = "missing-enabled-feature",
                variants = mapOf("variant-a" to 100, "variant-b" to 200),
            )
        }
        Thread.sleep(100)

        val flag = flagshipClient.getInt(
            "missing-enabled-feature",
            42,
            "test-key",
            defaultContext,
        )
        // If enabled is missing, should be treated as false, returning user default
        assertEquals(42, flag.value)
        assertEquals(Reason.DISABLED, flag.reason)
    }

    @Test
    fun `Missing rollout_percentage, revert to default value`() {
        MockUtils.stubWireMockFeatureConfig {
            MockUtils.createFeatureWithMissingRolloutPercentage<Int>(
                key = "missing-rollout-feature",
                variants = mapOf("variant-a" to 100, "variant-b" to 200),
            )
        }
        Thread.sleep(100)

        val flag = flagshipClient.getInt(
            "missing-rollout-feature",
            42,
            "test-key",
            defaultContext,
        )
        // If rollout_percentage is missing, should be treated as 100, normal evaluation
        assertEquals(42, flag.value) // Should get variant-a from default rule
        assertEquals(Reason.ERROR, flag.reason)
    }

    @Test
    fun `Missing rules array, use config default`() {
        MockUtils.stubWireMockFeatureConfig {
            MockUtils.createFeatureWithNoRules<Int>(
                key = "missing-rules-feature",
                variants = mapOf("variant-a" to 100, "variant-b" to 200),
            )
        }
        Thread.sleep(100)

        val flag = flagshipClient.getInt(
            "missing-rules-feature",
            42,
            "test-key",
            defaultContext,
        )
        // No rules, should use default rule
        assertEquals(42, flag.value) // Should get variant-a from default rule
        assertEquals(Reason.ERROR, flag.reason)
    }
}
