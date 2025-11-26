package com.flagship.sdk.it

import android.app.Application
import com.flagship.sdk.core.models.Reason
import com.flagship.sdk.facade.FlagShipClient
import com.flagship.sdk.facade.FlagShipConfig
import com.flagship.sdk.utils.CommonUtils
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.Arguments
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FlagExistenceIT {
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

    fun configureWireMock(){
        // Configure WireMock to serve the sample JSON response
        MockUtils.stubWireMockFeatureConfig {
            MockUtils.createSimpleFeature<Int>(
                constraints = listOf("user_tier", "country"),
                variants = mapOf(
                    "variant-a" to 100,
                    "variant-b" to 200,
                ),
            )
        }
    }

    @Test
    fun `should return input value when feature does not exist`() {
        // Test the flag does not exist
        configureWireMock()
        Thread.sleep(100);

        val flag = flagshipClient.getInt("test-flag", 69, "test-flag", defaultContext)
        assertEquals(69, flag.value)
        assertEquals(Reason.INVALID_FEATURE, flag.reason);
    }

    @Test
    fun `should return input value when feature type does not match`() {
        // Test the flag does not exist
        configureWireMock()
        Thread.sleep(100);

        val flag = flagshipClient.getBoolean("simple-feature", false, "simple-feature", defaultContext)
        assertEquals(false, flag.value)
        assertEquals(Reason.ERROR, flag.reason);
    }

    @Test
    fun `should return user default when variants array is missing or empty`() {
        MockUtils.stubWireMockFeatureConfig {
            MockUtils.createFeatureWithEmptyVariants("empty-variants-feature")
        }
        Thread.sleep(100)

        val flag = flagshipClient.getInt("empty-variants-feature", 42, "empty-variants-feature", defaultContext)
        assertEquals(42, flag.value)
        assertEquals(Reason.ERROR, flag.reason)
    }

    @Test
    fun `should return user default when flag variant value is null or missing`() {
        MockUtils.stubWireMockFeatureConfig {
            MockUtils.createFeatureWithMissingVariantValue("missing-variant-value-feature")
        }
        Thread.sleep(100)

        val flag = flagshipClient.getInt("missing-variant-value-feature", 42, "missing-variant-value-feature", defaultContext)
        assertEquals(42, flag.value)
        assertEquals(Reason.ERROR, flag.reason)
    }

    @Test
    fun `should return user default when enabled is false`() {
        MockUtils.stubWireMockFeatureConfig {
            MockUtils.createDisabledFeature<Int>(
                "disabled-feature",
                variants = mapOf("variant-a" to 100, "variant-b" to 200),
            )
        }
        Thread.sleep(100)

        val flag = flagshipClient.getInt("disabled-feature", 42, "disabled-feature", defaultContext)
        assertEquals(42, flag.value)
        assertEquals(Reason.DISABLED, flag.reason)
    }

    @Test
    fun `should return config default when flag enabled but no rules or allocations`() {
        MockUtils.stubWireMockFeatureConfig {
            MockUtils.createFeatureWithNoRules<Int>(
                "no-rules-feature",
                variants = mapOf("variant-a" to 100, "variant-b" to 200),
            )
        }
        Thread.sleep(100)

        val flag = flagshipClient.getInt("no-rules-feature", 42, "no-rules-feature", defaultContext)
        // Should return the default variant value from the feature (variant-a = 100)
        assertEquals(42, flag.value)
        assertEquals(Reason.ERROR, flag.reason)
    }

    @Test
    fun `should return default when targeting key is outside rollout percentage`() {
        val flagKey = "rollout-test-feature"
        val rolloutPercentage = 50L // 50% rollout
        
        // Find a targeting key that falls outside the rollout (percentile >= 50)
        val targetingKeyOutsideRollout = CommonUtils.findTargetingKeyWithPercentile(
            flagKey = flagKey,
            minPercentile = 50,
            maxPercentile = 99,
        )
        
        requireNotNull(targetingKeyOutsideRollout) {
            "Could not find a targeting key outside rollout percentage"
        }
        
        MockUtils.stubWireMockFeatureConfig {
            MockUtils.createSimpleFeature<Int>(
                key = flagKey,
                rolloutPercentage = rolloutPercentage,
                variants = mapOf("variant-a" to 100, "variant-b" to 200),
            )
        }
        Thread.sleep(100)
        
        val flag = flagshipClient.getInt(flagKey, 42, targetingKeyOutsideRollout, defaultContext)
        assertEquals(42, flag.value)
        assertEquals(Reason.SPLIT, flag.reason)
    }

    @Test
    fun `should return default when rollout percentage is zero`() {
        val flagKey = "zero-rollout-feature"
        
        MockUtils.stubWireMockFeatureConfig {
            MockUtils.createSimpleFeature<Int>(
                key = flagKey,
                rolloutPercentage = 0L,
                variants = mapOf("variant-a" to 100, "variant-b" to 200),
            )
        }
        Thread.sleep(100)
        
        // Any targeting key should return default with Reason.SPLIT when rollout is 0%
        val flag = flagshipClient.getInt(flagKey, 42, "any-user-key", defaultContext)
        assertEquals(42, flag.value)
        assertEquals(Reason.SPLIT, flag.reason)
    }

    @ParameterizedTest
    @MethodSource("variantAllocationTestDataProvider")
    fun `should return first variant for targeting key which lies in 50 percent rollout but 0-49 rollout for variant`
                (type: String, var1: Any, var2: Any, default: Any, defaultM: Any) {
        val flagKey = "fifty-fifty-split-feature"
        val ruleName = "rule-1"
        val rolloutPercentage = 50L // 50% rollout
        
        // Find two targeting keys:
        // - One that gets variant-a (allocation percentile 0-49)
        // - One that gets variant-b (allocation percentile 50-99)
        // Both must be in rollout (rollout percentile < 50)
        val keys = CommonUtils.findTwoTargetingKeysForVariants(
            flagKey = flagKey,
            ruleName = ruleName,
            rolloutPercentage = rolloutPercentage,
        )
        
        requireNotNull(keys) {
            "Could not find two targeting keys for the variant split test"
        }
        
        val (keyForVariantA, keyForVariantB) = keys
        
        configureWireMockForFeature(type, flagKey, rolloutPercentage, var1, var2, default)

        Thread.sleep(100);
        
        val flag1 = getFlagValue(type, flagKey, defaultM, keyForVariantA)
        assertEquals(var1, flag1.value)
        assertEquals(Reason.TARGETING_MATCH, flag1.reason)
        assertEquals("variant-a", flag1.variant)
    }

    @ParameterizedTest
    @MethodSource("variantAllocationTestDataProvider")
    fun `should return second variant for targeting key which lies in 50 percent rollout but 51-99 rollout for variant`
                (type: String, var1: Any, var2: Any, default: Any, defaultM: Any) {
        val flagKey = "fifty-fifty-split-feature-2"
        val ruleName = "rule-1-2"
        val rolloutPercentage = 50L // 50% rollout

        // Find two targeting keys:
        // - One that gets variant-a (allocation percentile 0-49)
        // - One that gets variant-b (allocation percentile 50-99)
        // Both must be in rollout (rollout percentile < 50)
        val keys = CommonUtils.findTwoTargetingKeysForVariants(
            flagKey = flagKey,
            ruleName = ruleName,
            rolloutPercentage = rolloutPercentage,
        )

        requireNotNull(keys) {
            "Could not find two targeting keys for the variant split test"
        }

        val (keyForVariantA, keyForVariantB) = keys

        configureWireMockForFeature(type, flagKey, rolloutPercentage, var1, var2, default)

        Thread.sleep(100);

        val flag1 = getFlagValue(type, flagKey, defaultM, keyForVariantB)
        assertEquals(var2, flag1.value)
        assertEquals(Reason.TARGETING_MATCH, flag1.reason)
        assertEquals("variant-b", flag1.variant)
    }

    private fun configureWireMockForFeature(
        type: String,
        flagKey: String,
        rolloutPercentage: Long,
        var1: Any,
        var2: Any,
        default: Any,
    ) {
        MockUtils.stubWireMockFeatureConfig {
            when (type) {
                "Int" -> MockUtils.createSimpleFeature<Int>(
                    key = flagKey,
                    rolloutPercentage = rolloutPercentage,
                    variants = mapOf(
                        "variant-a" to (var1 as Int),
                        "variant-b" to (var2 as Int),
                        "default" to (default as Int),
                    ),
                )
                "Double" -> MockUtils.createSimpleFeature<Double>(
                    key = flagKey,
                    rolloutPercentage = rolloutPercentage,
                    variants = mapOf(
                        "variant-a" to (var1 as Double),
                        "variant-b" to (var2 as Double),
                        "default" to (default as Double),
                    ),
                )
                "String" -> MockUtils.createSimpleFeature<String>(
                    key = flagKey,
                    rolloutPercentage = rolloutPercentage,
                    variants = mapOf(
                        "variant-a" to (var1 as String),
                        "variant-b" to (var2 as String),
                        "default" to (default as String),
                    ),
                )
                "Object" -> MockUtils.createSimpleFeature<JsonObject>(
                    key = flagKey,
                    rolloutPercentage = rolloutPercentage,
                    variants = mapOf(
                        "variant-a" to (var1 as JsonObject),
                        "variant-b" to (var2 as JsonObject),
                        "default" to (default as JsonObject),
                    ),
                )
                else -> throw IllegalArgumentException("Unsupported type: $type")
            }
        }
    }

    private fun getFlagValue(
        type: String,
        flagKey: String,
        defaultM: Any,
        targetingKey: String,
    ): com.flagship.sdk.core.models.EvaluationResult<*> {
        return when (type) {
            "Int" -> flagshipClient.getInt(flagKey, defaultM as Int, targetingKey, defaultContext)
            "Double" -> flagshipClient.getDouble(flagKey, defaultM as Double, targetingKey, defaultContext)
            "String" -> flagshipClient.getString(flagKey, defaultM as String, targetingKey, defaultContext)
            "Object" -> flagshipClient.getJson(flagKey, (defaultM as JsonObject), targetingKey, defaultContext)
            else -> throw IllegalArgumentException("Unsupported type: $type")
        }
    }

    companion object {
        @JvmStatic
        fun variantAllocationTestDataProvider(): List<Arguments> {
            val intVariantA = 100
            val intVariantB = 200
            val intDefault = 69
            val intDefaultFn = 75

            val doubleVariantA = 100.5
            val doubleVariantB = 200.7
            val doubleDefault = 69.2
            val doubleDefaultFn = 15.5

            val stringVariantA = "variant-a-value"
            val stringVariantB = "variant-b-value"
            val stringDefault = "default-value"
            val stringDefaultFn = "method-default"

            val objectVariantA = buildJsonObject {
                put("key", "variant-a")
                put("value", 100)
            }
            val objectVariantB = buildJsonObject {
                put("key", "variant-b")
                put("value", 200)
            }
            val objectDefault = buildJsonObject {
                put("key", "default")
                put("value", 69)
            }
            val objectDefaultFn = buildJsonObject {
                put("key", "fn_default")
                put("value", 101)
            }

            return listOf(
                Arguments.of("Int", intVariantA, intVariantB, intDefault, intDefaultFn),
                Arguments.of("Double", doubleVariantA, doubleVariantB, doubleDefault, doubleDefaultFn),
                Arguments.of("String", stringVariantA, stringVariantB, stringDefault, stringDefaultFn),
                Arguments.of("Object", objectVariantA, objectVariantB, objectDefault, objectDefaultFn),
            )
        }
    }
}
