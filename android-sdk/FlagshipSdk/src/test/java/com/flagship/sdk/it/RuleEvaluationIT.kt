package com.flagship.sdk.it

import android.app.Application
import com.flagship.sdk.core.models.ArrayValue
import com.flagship.sdk.core.models.EvaluationResult
import com.flagship.sdk.core.models.Operator
import com.flagship.sdk.core.models.Reason
import com.flagship.sdk.facade.FlagShipClient
import com.flagship.sdk.facade.FlagShipConfig
import com.flagship.sdk.utils.MockUtils
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.mockk.mockk
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.concurrent.TimeUnit
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RuleEvaluationIT {
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
        "userId" to 3456L,
        "app_version" to "2.5.0",
        "user_tags" to listOf("early-adopter", "beta-tester", "premium"),
        "cohort" to listOf("early-adopter", "beta-tester", "premium"),
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
    fun `returns first variant allocation on first rule match`() {
        MockUtils.stubWireMockFeatureConfig {
            MockUtils.createFeatureWithMultipleRules<Int>(
                key = "first-rule-match",
                variants = mapOf("variant-a" to 100, "variant-b" to 200, "default" to 300),
                ruleConfigs = listOf(
                    Triple(listOf("user_tier", "country"), false, "variant-a"), // Rule matches (evaluateToFalse = false)
                ),
            )
        }
        Thread.sleep(100)

        val flag = flagshipClient.getInt(
            "first-rule-match",
            42,
            "test-key",
            defaultContext,
        )
        assertEquals(100, flag.value) // Should get variant-a from first rule
        assertEquals(Reason.TARGETING_MATCH, flag.reason)
    }

    @Test
    fun `returns default variant allocation on first rule mismatch and only one rule`() {
        MockUtils.stubWireMockFeatureConfig {
            MockUtils.createFeatureWithMultipleRules<Int>(
                key = "first-rule-mismatch",
                variants = mapOf("variant-a" to 100, "default" to 300),
                ruleConfigs = listOf(
                    Triple(listOf("user_tier", "country"), true, "variant-a"), // Rule doesn't match (evaluateToFalse = true)
                ),
            )
        }
        Thread.sleep(100)

        val flag = flagshipClient.getInt(
            "first-rule-mismatch",
            42,
            "test-key",
            defaultContext,
        )
        assertEquals(300, flag.value) // Should get default variant
        assertEquals(Reason.DEFAULT_TARGETING_MATCH, flag.reason)
    }

    private inline fun <reified T> getFlagValue(
        key: String,
        defaultValue: T,
        targetingKey: String,
        context: Map<String, Any?>,
    ): EvaluationResult<T> {
        return when (T::class) {
            Boolean::class -> flagshipClient.getBoolean(key, defaultValue as Boolean, targetingKey, context) as EvaluationResult<T>
            String::class -> flagshipClient.getString(key, defaultValue as String, targetingKey, context) as EvaluationResult<T>
            Int::class -> flagshipClient.getInt(key, defaultValue as Int, targetingKey, context) as EvaluationResult<T>
            Double::class -> flagshipClient.getDouble(key, defaultValue as Double, targetingKey, context) as EvaluationResult<T>
            JsonObject::class -> flagshipClient.getJson(key, defaultValue as JsonObject, targetingKey, context) as EvaluationResult<T>
            else -> flagshipClient.getObject(key, defaultValue, targetingKey, context)
        }
    }

    private inline fun <reified T> getFlag(
        type: String,
        key: String,
        defaultValue: T,
        targetingKey: String,
        context: Map<String, Any?>,
    ): EvaluationResult<T> {
        return when (type) {
            "Int" -> getFlagValue<Int>(key, defaultValue as Int, targetingKey, context) as EvaluationResult<T>
            "Double" -> getFlagValue<Double>(key, defaultValue as Double, targetingKey, context) as EvaluationResult<T>
            "Boolean" -> getFlagValue<Boolean>(key, defaultValue as Boolean, targetingKey, context) as EvaluationResult<T>
            "String" -> getFlagValue<String>(key, defaultValue as String, targetingKey, context) as EvaluationResult<T>
            "Object" -> getFlagValue<JsonObject>(key, defaultValue as JsonObject, targetingKey, context) as EvaluationResult<T>
            else -> throw IllegalArgumentException("Unsupported type: $type")
        }
    }

    private fun createFeatureWithTwoRules(
        type: String,
        testKey: String,
        variantA: Any,
        variantB: Any,
        variantDefault: Any,
    ): String {
        val ruleConfigs = listOf(
            Triple(listOf("user_tier", "country"), true, "variant-a"), // First rule doesn't match
            Triple(listOf("user_tier"), false, "variant-b"), // Second rule matches
        )
        return when (type) {
            "Int" -> MockUtils.createFeatureWithMultipleRules<Int>(testKey, mapOf("variant-a" to variantA as Int, "variant-b" to variantB as Int, "default" to variantDefault as Int), ruleConfigs)
            "Double" -> MockUtils.createFeatureWithMultipleRules<Double>(testKey, mapOf("variant-a" to variantA as Double, "variant-b" to variantB as Double, "default" to variantDefault as Double), ruleConfigs)
            "Boolean" -> MockUtils.createFeatureWithMultipleRules<Boolean>(testKey, mapOf("variant-a" to variantA as Boolean, "variant-b" to variantB as Boolean, "default" to variantDefault as Boolean), ruleConfigs)
            "String" -> MockUtils.createFeatureWithMultipleRules<String>(testKey, mapOf("variant-a" to variantA as String, "variant-b" to variantB as String, "default" to variantDefault as String), ruleConfigs)
            "Object" -> MockUtils.createFeatureWithMultipleRules<JsonObject>(testKey, mapOf("variant-a" to variantA as JsonObject, "variant-b" to variantB as JsonObject, "default" to variantDefault as JsonObject), ruleConfigs)
            else -> throw IllegalArgumentException("Unsupported type: $type")
        }
    }


    @ParameterizedTest(name = "returns second variant allocation on first rule mismatch and two rules - {0}")
    @MethodSource("secondRuleMatchTestCases")
    fun `returns second variant allocation on first rule mismatch and two rules`(
        type: String,
        variantA: Any,
        variantB: Any,
        variantDefault: Any,
        defaultValue: Any,
    ) {
        val testKey = "second-rule-match-$type"
        MockUtils.stubWireMockFeatureConfig {
            createFeatureWithTwoRules(type, testKey, variantA, variantB, variantDefault)
        }
        Thread.sleep(100)

        val flag = getFlag<Any>(type, testKey, defaultValue, "test-key", defaultContext)
        
        val expectedValue = if (type == "Object") (variantB as JsonObject).toString() else variantB
        val actualValue = if (type == "Object") (flag.value as JsonObject).toString() else flag.value
        assertEquals(expectedValue, actualValue)
        assertEquals(Reason.TARGETING_MATCH, flag.reason)
    }

    @Test
    fun `returns third variant allocation on first and second rule mismatch and three rules`() {
        MockUtils.stubWireMockFeatureConfig {
            MockUtils.createFeatureWithMultipleRules<Int>(
                key = "third-rule-match",
                variants = mapOf("variant-a" to 100, "variant-b" to 200, "variant-c" to 400, "default" to 300),
                ruleConfigs = listOf(
                    Triple(listOf("user_tier", "country"), true, "variant-a"), // First rule doesn't match
                    Triple(listOf("user_tier", "country"), true, "variant-b"), // Second rule doesn't match
                    Triple(listOf("user_tier"), false, "variant-c"), // Third rule matches
                ),
            )
        }
        Thread.sleep(100)

        val flag = flagshipClient.getInt(
            "third-rule-match",
            42,
            "test-key",
            defaultContext,
        )
        assertEquals(400, flag.value) // Should get variant-c from third rule
        assertEquals(Reason.TARGETING_MATCH, flag.reason)
    }

    companion object {
        @JvmStatic
        fun secondRuleMatchTestCases(): Stream<Arguments> {
            val objA = buildJsonObject { put("key", "value-a") }
            val objB = buildJsonObject { put("key", "value-b") }
            val objDefault = buildJsonObject { put("key", "value-default") }
            val objDefaultValue = buildJsonObject { put("key", "default") }
            return Stream.of(
                Arguments.of("Int", 100, 200, 300, 42),
                Arguments.of("Double", 100.5, 200.5, 300.5, 42.0),
                Arguments.of("Boolean", true, false, true, false),
                Arguments.of("String", "value-a", "value-b", "value-default", "default"),
                Arguments.of("Object", objA, objB, objDefault, objDefaultValue)
            )
        }

        @JvmStatic
        fun operatorTestCases(): Stream<Arguments> {
            return Stream.of(
                // Eq operator tests
                Arguments.of(Operator.Eq, "user_tier", "premium", "String"),
                Arguments.of(Operator.Eq, "is_logged_in", true, "Boolean"),
                Arguments.of(Operator.Eq, "userId", 3456L, "Long"),
                Arguments.of(Operator.Eq, "session_count", 150.0, "Double"),
                // Neq operator tests
                Arguments.of(Operator.Neq, "user_tier", "free", "String"),
                Arguments.of(Operator.Neq, "is_logged_in", false, "Boolean"),
                Arguments.of(Operator.Neq, "userId", 9999L, "Long"),
                Arguments.of(Operator.Neq, "session_count", 200.0, "Double"),
                // Gt operator tests
                Arguments.of(Operator.Gt, "userId", 3000L, "Long"),
                Arguments.of(Operator.Gt, "session_count", 100.0, "Double"),
                // Gte operator tests
                Arguments.of(Operator.Gte, "userId", 3456L, "Long"),
                Arguments.of(Operator.Gte, "session_count", 150.0, "Double"),
                // Lt operator tests
                Arguments.of(Operator.Lt, "userId", 4000L, "Long"),
                Arguments.of(Operator.Lt, "session_count", 200.0, "Double"),
                // LTE operator tests
                Arguments.of(Operator.LTE, "userId", 3456L, "Long"),
                Arguments.of(Operator.LTE, "session_count", 150.0, "Double"),
                // In operator tests (value is list)
                Arguments.of(Operator.In, "user_tier", listOf(ArrayValue.StringValue("premium"), ArrayValue.StringValue("free")), "String"),
                Arguments.of(Operator.In, "userId", listOf(ArrayValue.IntegerValue(3456L), ArrayValue.IntegerValue(9999L)), "Long"),
                Arguments.of(Operator.In, "app_version", listOf(ArrayValue.SemverValue("2.5.0"), ArrayValue.SemverValue("3.0.0")), "Semver"),
                // Ct (Contains) operator tests (context field is list - cohort)
                Arguments.of(Operator.Ct, "cohort", "premium", "String"),
                Arguments.of(Operator.Ct, "cohort", "early-adopter", "String")
            )
        }

        @JvmStatic
        fun operatorMismatchTestCases(): Stream<Arguments> {
            return Stream.of(
                // Eq operator tests - values that don't match
                Arguments.of(Operator.Eq, "user_tier", "free", "String"),
                Arguments.of(Operator.Eq, "is_logged_in", false, "Boolean"),
                Arguments.of(Operator.Eq, "userId", 9999L, "Long"),
                Arguments.of(Operator.Eq, "session_count", 200.0, "Double"),
                // Neq operator tests - values that DO match (so Neq is false)
                Arguments.of(Operator.Neq, "user_tier", "premium", "String"),
                Arguments.of(Operator.Neq, "is_logged_in", true, "Boolean"),
                Arguments.of(Operator.Neq, "userId", 3456L, "Long"),
                Arguments.of(Operator.Neq, "session_count", 150.0, "Double"),
                // Gt operator tests - values greater than context (so Gt is false)
                Arguments.of(Operator.Gt, "userId", 4000L, "Long"),
                Arguments.of(Operator.Gt, "session_count", 200.0, "Double"),
                // Gte operator tests - values greater than context (so Gte is false)
                Arguments.of(Operator.Gte, "userId", 4000L, "Long"),
                Arguments.of(Operator.Gte, "session_count", 200.0, "Double"),
                // Lt operator tests - values less than context (so Lt is false)
                Arguments.of(Operator.Lt, "userId", 3000L, "Long"),
                Arguments.of(Operator.Lt, "session_count", 100.0, "Double"),
                // LTE operator tests - values less than context (so LTE is false)
                Arguments.of(Operator.LTE, "userId", 3000L, "Long"),
                Arguments.of(Operator.LTE, "session_count", 100.0, "Double"),
                // In operator tests - lists that don't contain the context value
                Arguments.of(Operator.In, "user_tier", listOf(ArrayValue.StringValue("free"), ArrayValue.StringValue("basic")), "String"),
                Arguments.of(Operator.In, "userId", listOf(ArrayValue.IntegerValue(9999L), ArrayValue.IntegerValue(8888L)), "Long"),
                Arguments.of(Operator.In, "app_version", listOf(ArrayValue.SemverValue("1.0.0"), ArrayValue.SemverValue("3.0.0")), "Semver"),
                // Ct (Contains) operator tests - values not in cohort list
                Arguments.of(Operator.Ct, "cohort", "not-in-cohort", "String"),
                Arguments.of(Operator.Ct, "cohort", "unknown-value", "String")
            )
        }
    }

    @ParameterizedTest(name = "operator test: {0} with value {2} and type {3}")
    @MethodSource("operatorTestCases")
    fun `test all operators with different context field types`(
        operator: Operator,
        contextField: String,
        constraintValue: Any,
        fieldType: String,
    ) {
        val testKey = "operator-test-${operator.name}-${contextField}-${fieldType}"
        MockUtils.stubWireMockFeatureConfig {
            MockUtils.createFeatureWithCustomConstraints<Int>(
                key = testKey,
                variants = mapOf("variant-a" to 100, "default" to 200),
                ruleConfigs = listOf(
                    MockUtils.CustomRuleConfig(
                        constraints = listOf(
                            MockUtils.CustomConstraint(
                                contextField = contextField,
                                operator = operator,
                                value = constraintValue,
                            ),
                        ),
                        variantKey = "variant-a",
                        allocationPercentage = 100L,
                        ruleName = "rule-1",
                    ),
                ),
            )
        }
        Thread.sleep(100)

        val flag = flagshipClient.getInt(
            testKey,
            42,
            "test-key",
            defaultContext,
        )
        assertEquals(100, flag.value) // Should get variant-a from rule
        assertEquals(Reason.TARGETING_MATCH, flag.reason)
    }

    @ParameterizedTest(name = "operator mismatch test: {0} with value {2} and type {3}")
    @MethodSource("operatorMismatchTestCases")
    fun `test all operators with different context field types when rule does not match`(
        operator: Operator,
        contextField: String,
        constraintValue: Any,
        fieldType: String,
    ) {
        val testKey = "operator-mismatch-test-${operator.name}-${contextField}-${fieldType}"
        MockUtils.stubWireMockFeatureConfig {
            MockUtils.createFeatureWithCustomConstraints<Int>(
                key = testKey,
                variants = mapOf("variant-a" to 100, "default" to 200),
                ruleConfigs = listOf(
                    MockUtils.CustomRuleConfig(
                        constraints = listOf(
                            MockUtils.CustomConstraint(
                                contextField = contextField,
                                operator = operator,
                                value = constraintValue,
                            ),
                        ),
                        variantKey = "variant-a",
                        allocationPercentage = 100L,
                        ruleName = "rule-1",
                    ),
                ),
            )
        }
        Thread.sleep(100)

        val flag = flagshipClient.getInt(
            testKey,
            42,
            "test-key",
            defaultContext,
        )
        assertEquals(200, flag.value) // Should get default variant since rule doesn't match
        assertEquals(Reason.DEFAULT_TARGETING_MATCH, flag.reason)
    }
}
