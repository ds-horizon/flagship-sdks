package com.flagship.sdk

import com.flagship.sdk.core.contracts.ICache
import com.flagship.sdk.core.models.AllocationElement
import com.flagship.sdk.core.models.ArrayValue
import com.flagship.sdk.core.models.Constraint
import com.flagship.sdk.core.models.ConstraintValue
import com.flagship.sdk.core.models.DefaultRule
import com.flagship.sdk.core.models.EvaluationContext
import com.flagship.sdk.core.models.Feature
import com.flagship.sdk.core.models.Operator
import com.flagship.sdk.core.models.Reason
import com.flagship.sdk.core.models.Rule
import com.flagship.sdk.core.models.Type
import com.flagship.sdk.core.models.VariantElement
import com.flagship.sdk.core.models.VariantValue
import com.flagship.sdk.plugins.evaluation.EdgeEvaluator
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EdgeEvaluatorTest {
    private lateinit var evaluateCache: ICache
    private lateinit var persistentCache: ICache
    private lateinit var edgeEvaluator: EdgeEvaluator

    @Before
    fun setUp() {
        evaluateCache = mockk()
        persistentCache = mockk()
        edgeEvaluator = EdgeEvaluator(evaluateCache, persistentCache)
        // Default stubs for cache writes used by all tests (explicit types to satisfy generics)
        every { evaluateCache.put(any(), any<Boolean>()) } just Runs
        every { evaluateCache.put(any(), any<String>()) } just Runs
        every { evaluateCache.put(any(), any<Int>()) } just Runs
        every { evaluateCache.put(any(), any<Double>()) } just Runs
        every { evaluateCache.put(any(), any<Map<String, Any>>()) } just Runs
        every { evaluateCache.put(any(), any<Map<String, Any?>>()) } just Runs
    }

    // ========== evaluateBoolean Tests ==========

    @Test
    fun `evaluateBoolean returns cached value when available`() {
        // Given
        val flagKey = "test_flag"
        val cachedValue = true
        every { evaluateCache.get<Boolean>(flagKey) } returns cachedValue

        // When
        val result = edgeEvaluator.evaluateBoolean(flagKey, false, null, EvaluationContext.empty())

        // Then
        assertEquals(cachedValue, result.value)
        assertEquals(Reason.CACHED, result.reason)
        verify { evaluateCache.get<Boolean>(flagKey) }
    }

    @Test
    fun `evaluateBoolean returns default when config is null`() {
        // Given
        val flagKey = "test_flag"
        val defaultValue = false
        every { evaluateCache.get<Boolean>(flagKey) } returns null
        every { evaluateCache.put(flagKey, defaultValue) } just Runs

        // When
        val result =
            edgeEvaluator.evaluateBoolean(flagKey, defaultValue, null, EvaluationContext.empty())

        // Then

        assertEquals(defaultValue, result.value)
        assertEquals(Reason.DEFAULT, result.reason)
    }

    @Test
    fun `evaluateBoolean returns default when config is disabled`() {
        // Given
        val flagKey = "test_flag"
        val defaultValue = false
        val config = createTestFeature(enabled = false)
        every { evaluateCache.get<Boolean>(flagKey) } returns null
        every { evaluateCache.put(flagKey, defaultValue) } just Runs

        // When
        val result =
            edgeEvaluator.evaluateBoolean(flagKey, defaultValue, config, EvaluationContext("user123", emptyMap()))

        // Then
        assertEquals(defaultValue, result.value)
        assertEquals(Reason.DISABLED, result.reason)
    }

    @Test
    fun `evaluateBoolean returns default when user not in rollout bucket`() {
        // Given
        val flagKey = "test_flag"
        val defaultValue = false
        val config = createTestFeature(rolloutPercentage = 0) // 0% rollout
        val context = EvaluationContext("user123", emptyMap())
        every { evaluateCache.get<Boolean>(flagKey) } returns null

        // When
        val result = edgeEvaluator.evaluateBoolean(flagKey, defaultValue, config, context)

        // Then
        assertEquals(defaultValue, result.value)
        assertEquals(Reason.SPLIT, result.reason)
    }

    @Test
    fun `evaluateBoolean returns variant value when rule matches`() {
        // Given
        val flagKey = "test_flag"
        val defaultValue = false
        val targetingKey = "user123"
        val context = EvaluationContext(targetingKey, mapOf("country" to "US"))

        val variant = VariantElement("variant_a", VariantValue.BoolValue(true))
        val allocation = AllocationElement(100, "variant_a")
        val constraint = Constraint("country", Operator.Eq, ConstraintValue.StringValue("US"))
        val rule = Rule(listOf(allocation), listOf(constraint), "1")
        val config =
            createTestFeature(
                rolloutPercentage = 100,
                rules = listOf(rule),
                variants = listOf(variant),
            )

        every { evaluateCache.get<Boolean>(flagKey) } returns null

        // When
        val result = edgeEvaluator.evaluateBoolean(flagKey, defaultValue, config, context)

        // Then
        assertEquals(true, result.value)
        assertEquals("variant_a", result.variant)
        assertEquals(Reason.TARGETING_MATCH, result.reason)
    }

    @Test
    fun `evaluateBoolean returns error when variant has wrong type`() {
        // Given
        val flagKey = "test_flag"
        val defaultValue = false
        val targetingKey = "user123"
        val context = EvaluationContext(targetingKey, mapOf("country" to "US"))

        val variant = VariantElement("variant_a", VariantValue.StringValue("not_boolean"))
        val allocation = AllocationElement(100, "variant_a")
        val constraint = Constraint("country", Operator.Eq, ConstraintValue.StringValue("US"))
        val rule = Rule(listOf(allocation), listOf(constraint), "1")
        val config =
            createTestFeature(
                rolloutPercentage = 100,
                rules = listOf(rule),
                variants = listOf(variant),
            )

        every { evaluateCache.get<Boolean>(flagKey) } returns null

        // When
        val result = edgeEvaluator.evaluateBoolean(flagKey, defaultValue, config, context)

        // Then
        assertEquals(defaultValue, result.value)
        assertEquals(Reason.ERROR, result.reason)
        assertTrue(result.metadata.containsKey("error"))
    }

    // ========== evaluateString Tests ==========

    @Test
    fun `evaluateString returns cached value when available`() {
        // Given
        val flagKey = "test_flag"
        val cachedValue = "cached_string"
        every { evaluateCache.get<String>(flagKey) } returns cachedValue

        // When
        val result =
            edgeEvaluator.evaluateString(flagKey, "default", null, EvaluationContext.empty())

        // Then
        assertEquals(cachedValue, result.value)
        assertEquals(Reason.CACHED, result.reason)
    }

    @Test
    fun `evaluateString returns variant value when rule matches`() {
        // Given
        val flagKey = "test_flag"
        val defaultValue = "default"
        val targetingKey = "user123"
        val context = EvaluationContext(targetingKey, mapOf("version" to "1.0"))

        val variant = VariantElement("variant_b", VariantValue.StringValue("feature_enabled"))
        val allocation = AllocationElement(100, "variant_b")
        val constraint = Constraint("version", Operator.Eq, ConstraintValue.StringValue("1.0"))
        val rule = Rule(listOf(allocation), listOf(constraint), "1")
        val config =
            createTestFeature(
                rolloutPercentage = 100,
                rules = listOf(rule),
                variants = listOf(variant),
            )

        every { evaluateCache.get<String>(flagKey) } returns null

        // When
        val result = edgeEvaluator.evaluateString(flagKey, defaultValue, config, context)

        // Then
        assertEquals("feature_enabled", result.value)
        assertEquals("variant_b", result.variant)
        assertEquals(Reason.TARGETING_MATCH, result.reason)
    }

    @Test
    fun `evaluateString returns default rule allocation when no rules match`() {
        val flagKey = "str_flag"
        val defaultValue = "default"
        val context = EvaluationContext("user", mapOf("cond" to "nope"))

        val variant = VariantElement("var_str", VariantValue.StringValue("from_default"))
        val rule = Rule(listOf(AllocationElement(100, "xx")), listOf(Constraint("cond", Operator.Eq, ConstraintValue.StringValue("yes"))), "1")
        val config = createTestFeature(
            rolloutPercentage = 100,
            rules = listOf(rule),
            variants = listOf(variant),
            defaultAllocation = listOf(AllocationElement(100, "var_str")),
        )

        every { evaluateCache.get<String>(flagKey) } returns null

        val result = edgeEvaluator.evaluateString(flagKey, defaultValue, config, context)
        assertEquals("from_default", result.value)
        assertEquals("var_str", result.variant)
        assertEquals(Reason.DEFAULT_TARGETING_MATCH, result.reason)
    }

    // ========== evaluateInt Tests ==========

    @Test
    fun `evaluateInt returns cached value when available`() {
        // Given
        val flagKey = "test_flag"
        val cachedValue = 42
        every { evaluateCache.get<Int>(flagKey) } returns cachedValue

        // When
        val result = edgeEvaluator.evaluateInt(flagKey, 0, null, EvaluationContext.empty())

        // Then
        assertEquals(cachedValue, result.value)
        assertEquals(Reason.CACHED, result.reason)
    }

    @Test
    fun `evaluateInt returns variant value when rule matches`() {
        // Given
        val flagKey = "test_flag"
        val defaultValue = 0
        val targetingKey = "user123"
        val context = EvaluationContext(targetingKey, mapOf("age" to 25))

        val variant = VariantElement("variant_c", VariantValue.IntegerValue(100))
        val allocation = AllocationElement(100, "variant_c")
        val constraint = Constraint("age", Operator.Gte, ConstraintValue.IntegerValue(18))
        val rule = Rule(listOf(allocation), listOf(constraint), "1")
        val config =
            createTestFeature(
                rolloutPercentage = 100,
                rules = listOf(rule),
                variants = listOf(variant),
            )

        every { evaluateCache.get<Int>(flagKey) } returns null

        // When
        val result = edgeEvaluator.evaluateInt(flagKey, defaultValue, config, context)

        // Then
        assertEquals(100, result.value)
        assertEquals("variant_c", result.variant)
        assertEquals(Reason.TARGETING_MATCH, result.reason)
    }

    @Test
    fun `evaluateInt returns default rule allocation when no rules match`() {
        val flagKey = "int_flag"
        val defaultValue = 7
        val context = EvaluationContext("user", mapOf("age" to 10))
        val variant = VariantElement("var_int", VariantValue.IntegerValue(11))
        val rule = Rule(listOf(AllocationElement(100, "yy")), listOf(Constraint("age", Operator.Gte, ConstraintValue.IntegerValue(18))), "1")
        val config = createTestFeature(
            rolloutPercentage = 100,
            rules = listOf(rule),
            variants = listOf(variant),
            defaultAllocation = listOf(AllocationElement(100, "var_int")),
        )

        every { evaluateCache.get<Int>(flagKey) } returns null
        val result = edgeEvaluator.evaluateInt(flagKey, defaultValue, config, context)
        assertEquals(11, result.value)
        assertEquals("var_int", result.variant)
        assertEquals(Reason.DEFAULT_TARGETING_MATCH, result.reason)
    }

    // ========== evaluateDouble Tests ==========

    @Test
    fun `evaluateDouble returns cached value when available`() {
        // Given
        val flagKey = "test_flag"
        val cachedValue = 3.14
        every { evaluateCache.get<Double>(flagKey) } returns cachedValue

        // When
        val result = edgeEvaluator.evaluateDouble(flagKey, 0.0, null, EvaluationContext.empty())

        // Then
        assertEquals(cachedValue, result.value, 0.001)
        assertEquals(Reason.CACHED, result.reason)
    }

    @Test
    fun `evaluateDouble returns variant value when rule matches`() {
        // Given
        val flagKey = "test_flag"
        val defaultValue = 0.0
        val targetingKey = "user123"
        val context = EvaluationContext(targetingKey, mapOf("score" to 85.5))

        val variant = VariantElement("variant_d", VariantValue.DoubleValue(99.9))
        val allocation = AllocationElement(100, "variant_d")
        val constraint = Constraint("score", Operator.Gt, ConstraintValue.DoubleValue(80.0))
        val rule = Rule(listOf(allocation), listOf(constraint), "1")
        val config =
            createTestFeature(
                rolloutPercentage = 100,
                rules = listOf(rule),
                variants = listOf(variant),
            )

        every { evaluateCache.get<Double>(flagKey) } returns null

        // When
        val result = edgeEvaluator.evaluateDouble(flagKey, defaultValue, config, context)

        // Then
        assertEquals(99.9, result.value, 0.001)
        assertEquals("variant_d", result.variant)
        assertEquals(Reason.TARGETING_MATCH, result.reason)
    }

    @Test
    fun `evaluateDouble returns default rule allocation when no rules match`() {
        val flagKey = "dbl_flag"
        val defaultValue = 1.0
        val context = EvaluationContext("user", mapOf("score" to 10.0))
        val variant = VariantElement("var_dbl", VariantValue.DoubleValue(2.5))
        val rule = Rule(listOf(AllocationElement(100, "aa")), listOf(Constraint("score", Operator.Gt, ConstraintValue.DoubleValue(50.0))), "1")
        val config = createTestFeature(
            rolloutPercentage = 100,
            rules = listOf(rule),
            variants = listOf(variant),
            defaultAllocation = listOf(AllocationElement(100, "var_dbl")),
        )

        every { evaluateCache.get<Double>(flagKey) } returns null
        val result = edgeEvaluator.evaluateDouble(flagKey, defaultValue, config, context)
        assertEquals(2.5, result.value, 0.001)
        assertEquals("var_dbl", result.variant)
        assertEquals(Reason.DEFAULT_TARGETING_MATCH, result.reason)
    }

    // ========== evaluateObject Tests ==========

    @Test
    fun `evaluateObject returns cached value when available`() {
        // Given
        val flagKey = "test_flag"
        val cachedValue = mapOf("key" to "value")
        every { evaluateCache.get<Map<String, String>>(flagKey) } returns cachedValue

        // When
        val result =
            edgeEvaluator.evaluateObject(
                flagKey,
                emptyMap<String, String>(),
                null,
                EvaluationContext.empty(),
            )

        // Then
        assertEquals(cachedValue, result.value)
        assertEquals(Reason.CACHED, result.reason)
    }

    @Test
    fun `evaluateObject returns variant value when rule matches`() {
        // Given
        val flagKey = "test_flag"
        val defaultValue = emptyMap<String, Any>()
        val targetingKey = "user123"
        val context = EvaluationContext(targetingKey, mapOf("premium" to true))

        val variantJson: JsonObject = Json.parseToJsonElement("""{"feature":"enabled"}""").jsonObject
        val variant = VariantElement("variant_e", VariantValue.AnythingMapValue(variantJson))
        val allocation = AllocationElement(100, "variant_e")
        val constraint = Constraint("premium", Operator.Eq, ConstraintValue.BoolValue(true))
        val rule = Rule(listOf(allocation), listOf(constraint), "1")
        val config =
            createTestFeature(
                rolloutPercentage = 100,
                rules = listOf(rule),
                variants = listOf(variant),
            )

        every { evaluateCache.get<Map<String, Any>>(flagKey) } returns null

        // When
        val result = edgeEvaluator.evaluateObject(flagKey, defaultValue, config, context)

        // Then
        assertEquals(mapOf("feature" to "enabled"), result.value)
        assertEquals("variant_e", result.variant)
        assertEquals(Reason.TARGETING_MATCH, result.reason)
    }

    @Test
    fun `evaluateObject returns default rule allocation when no rules match`() {
        val flagKey = "obj_flag"
        val defaultValue = emptyMap<String, Any>()
        val context = EvaluationContext("user", mapOf("premium" to false))
        val defaultVariantJson: JsonObject = Json.parseToJsonElement("""{"k":"v"}""").jsonObject
        val variant = VariantElement("var_obj", VariantValue.AnythingMapValue(defaultVariantJson))
        val rule = Rule(listOf(AllocationElement(100, "bb")), listOf(Constraint("premium", Operator.Eq, ConstraintValue.BoolValue(true))), "1")
        val config = createTestFeature(
            rolloutPercentage = 100,
            rules = listOf(rule),
            variants = listOf(variant),
            defaultAllocation = listOf(AllocationElement(100, "var_obj")),
        )

        every { evaluateCache.get<Map<String, Any>>(flagKey) } returns null
        val result = edgeEvaluator.evaluateObject(flagKey, defaultValue, config, context)
        assertEquals(mapOf("k" to "v"), result.value)
        assertEquals("var_obj", result.variant)
        assertEquals(Reason.DEFAULT_TARGETING_MATCH, result.reason)
    }

    @Test
    fun `evaluateObject returns simple type provided`() {
        val flagKey = "test_key"
        val defaultValue = """{"userId":"123456"}"""
        val returnValue = """{"isVisible":"true"}"""
        val targetingKey = "user123"
        val context = EvaluationContext(targetingKey, mapOf("key" to "value"))
        val variantObj = Json.parseToJsonElement(returnValue).jsonObject
        val variant = VariantElement("variant_f", VariantValue.AnythingMapValue(variantObj))
        val allocation = AllocationElement(100, "variant_f")
        val constraint = Constraint("key", Operator.Eq, ConstraintValue.StringValue("value"))
        val rule = Rule(listOf(allocation), listOf(constraint), "1")
        val config =
            createTestFeature(
                rolloutPercentage = 100,
                rules = listOf(rule),
                variants = listOf(variant),
            )
        every { evaluateCache.get<Map<String, Any>>(flagKey) } returns null

        val result = edgeEvaluator.evaluateObject(flagKey, defaultValue, config, context)
        assertEquals(result.value, returnValue)
        assertEquals(result.reason, Reason.TARGETING_MATCH)
    }

    // ========== Constraint Matching Tests ==========

    @Test
    fun `evaluateBoolean handles In operator constraint`() {
        // Given
        val flagKey = "test_flag"
        val defaultValue = false
        val targetingKey = "user123"
        val context = EvaluationContext(targetingKey, mapOf("region" to "US"))

        val variant = VariantElement("variant_f", VariantValue.BoolValue(true))
        val allocation = AllocationElement(100, "variant_f")
        val constraint =
            Constraint(
                "region",
                Operator.In,
                ConstraintValue.AnythingArrayValue(
                    listOf(
                        ArrayValue.StringValue("US"),
                        ArrayValue.StringValue("CA"),
                    ),
                ),
            )
        val rule = Rule(listOf(allocation), listOf(constraint), "1")
        val config =
            createTestFeature(
                rolloutPercentage = 100,
                rules = listOf(rule),
                variants = listOf(variant),
            )

        every { evaluateCache.get<Boolean>(flagKey) } returns null

        // When
        val result = edgeEvaluator.evaluateBoolean(flagKey, defaultValue, config, context)

        // Then
        assertEquals(true, result.value)
        assertEquals(Reason.TARGETING_MATCH, result.reason)
    }

    @Test
    fun `evaluateBoolean handles Neq operator constraint`() {
        // Given
        val flagKey = "test_flag"
        val defaultValue = false
        val targetingKey = "user123"
        val context = EvaluationContext(targetingKey, mapOf("status" to "active"))

        val variant = VariantElement("variant_g", VariantValue.BoolValue(true))
        val allocation = AllocationElement(100, "variant_g")
        val constraint = Constraint("status", Operator.Neq, ConstraintValue.StringValue("inactive"))
        val rule = Rule(listOf(allocation), listOf(constraint), "1")
        val config =
            createTestFeature(
                rolloutPercentage = 100,
                rules = listOf(rule),
                variants = listOf(variant),
            )

        every { evaluateCache.get<Boolean>(flagKey) } returns null

        // When
        val result = edgeEvaluator.evaluateBoolean(flagKey, defaultValue, config, context)

        // Then
        assertEquals(true, result.value)
        assertEquals(Reason.TARGETING_MATCH, result.reason)
    }

    @Test
    fun `evaluateBoolean returns default rule allocation when no rules match`() {
        // Given
        val flagKey = "test_flag"
        val defaultValue = false
        val targetingKey = "user123"
        val context = EvaluationContext(targetingKey, mapOf("country" to "UK"))

        val variant = VariantElement("variant_h", VariantValue.BoolValue(true))
        val allocation = AllocationElement(100, "variant_h")
        val constraint = Constraint("country", Operator.Eq, ConstraintValue.StringValue("US"))
        val rule = Rule(listOf(allocation), listOf(constraint), "1")
        val config =
            createTestFeature(
                rolloutPercentage = 100,
                rules = listOf(rule),
                variants = listOf(variant),
                defaultAllocation = listOf(AllocationElement(100, "variant_h")),
            )

        every { evaluateCache.get<Boolean>(flagKey) } returns null

        // When
        val result = edgeEvaluator.evaluateBoolean(flagKey, defaultValue, config, context)

        // Then
        assertEquals(true, result.value)
        assertEquals("variant_h", result.variant)
        assertEquals(Reason.DEFAULT_TARGETING_MATCH, result.reason)
    }

    @Test
    fun `evaluateBoolean handles multiple constraints with AND logic`() {
        // Given
        val flagKey = "test_flag"
        val defaultValue = false
        val targetingKey = "user123"
        val context =
            EvaluationContext(
                targetingKey,
                mapOf(
                    "country" to "US",
                    "age" to 25,
                ),
            )

        val variant = VariantElement("variant_i", VariantValue.BoolValue(true))
        val allocation = AllocationElement(100, "variant_i")
        val constraints =
            listOf(
                Constraint("country", Operator.Eq, ConstraintValue.StringValue("US")),
                Constraint("age", Operator.Gte, ConstraintValue.IntegerValue(18)),
            )
        val rule = Rule(listOf(allocation), constraints, "1")
        val config =
            createTestFeature(
                rolloutPercentage = 100,
                rules = listOf(rule),
                variants = listOf(variant),
            )

        every { evaluateCache.get<Boolean>(flagKey) } returns null

        // When
        val result = edgeEvaluator.evaluateBoolean(flagKey, defaultValue, config, context)

        // Then
        assertEquals(true, result.value)
        assertEquals(Reason.TARGETING_MATCH, result.reason)
    }

    @Test
    fun `evaluateBoolean applies default rule when one constraint in AND logic fails`() {
        // Given
        val flagKey = "test_flag"
        val defaultValue = false
        val targetingKey = "user123"
        val context =
            EvaluationContext(
                targetingKey,
                mapOf(
                    "country" to "US",
                    "age" to 16, // Below 18
                ),
            )

        val variant = VariantElement("variant_j", VariantValue.BoolValue(true))
        val allocation = AllocationElement(100, "variant_j")
        val constraints =
            listOf(
                Constraint("country", Operator.Eq, ConstraintValue.StringValue("US")),
                Constraint("age", Operator.Gte, ConstraintValue.IntegerValue(18)),
            )
        val rule = Rule(listOf(allocation), constraints, "1")
        val config =
            createTestFeature(
                rolloutPercentage = 100,
                rules = listOf(rule),
                variants = listOf(variant),
                defaultAllocation = listOf(AllocationElement(100, "variant_j")),
            )

        every { evaluateCache.get<Boolean>(flagKey) } returns null

        // When
        val result = edgeEvaluator.evaluateBoolean(flagKey, defaultValue, config, context)

        // Then
        assertEquals(true, result.value)
        assertEquals("variant_j", result.variant)
        assertEquals(Reason.DEFAULT_TARGETING_MATCH, result.reason)
    }

    // ========== Helper Methods ==========

    private fun createTestFeature(
        enabled: Boolean = true,
        rolloutPercentage: Long = 100,
        rules: List<Rule> = emptyList(),
        variants: List<VariantElement> = emptyList(),
        defaultAllocation: List<AllocationElement> = emptyList(),
    ): Feature =
        Feature(
            defaultRule = DefaultRule(defaultAllocation, "0"),
            enabled = enabled,
            key = "test_feature",
            rolloutPercentage = rolloutPercentage,
            rules = rules,
            type = Type.Boolean,
            updatedAt = System.currentTimeMillis().toDouble(),
            variants = variants,
        )
}
//Test Commit