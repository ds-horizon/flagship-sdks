package com.flagship.sdk.utils

import com.flagship.sdk.core.models.DefaultRule
import com.flagship.sdk.core.models.Feature
import com.flagship.sdk.core.models.FeatureFlagsSchema
import com.flagship.sdk.core.models.Operator
import com.flagship.sdk.core.models.Type
import com.flagship.sdk.core.models.VariantElement
import com.flagship.sdk.core.models.VariantValue
import com.flagship.sdk.plugins.evaluation.AllocationUtility
import com.github.tomakehurst.wiremock.client.WireMock
import kotlinx.serialization.serializer

object MockUtils {
    @JvmStatic
    inline fun <reified T> createSimpleFeature(
        key: String = "simple-feature",
        constraints: List<String> = listOf("user_tier", "country"),
        variants: Map<String, T>,
        rolloutPercentage: Long = 100L,
    ): String {
        require(variants.isNotEmpty()) { "At least one variant is required" }

        val variantKeys = variants.keys.toList()
        require(variantKeys.size >= 2) { "At least two variants are required" }

        val ruleVariantKeys = variantKeys.dropLast(1) // All except the last one
        val defaultVariantKey = variantKeys.last() // Last one is default

        val percentagePerVariant = 100L / ruleVariantKeys.size
        val remainder = 100L % ruleVariantKeys.size
        val percentages = ruleVariantKeys.mapIndexed { index, _ ->
            if (index == 0) percentagePerVariant + remainder else percentagePerVariant
        }
        val allocations = FeatureCreateUtils.createAllocations(ruleVariantKeys, percentages)

        val ruleConstraints = FeatureCreateUtils.generateDemoConstraints(constraints)

        val rule = com.flagship.sdk.core.models.Rule(
            ruleName = "rule-1",
            constraints = ruleConstraints,
            allocations = allocations,
        )

        val feature = FeatureCreateUtils.createFeature<T>(
            key = key,
            enabled = true,
            rolloutPercentage = rolloutPercentage,
            variants = variants,
            rules = listOf(rule),
            defaultVariantKey = defaultVariantKey,
        )

        val schema = com.flagship.sdk.core.models.FeatureFlagsSchema(
            features = listOf(feature),
            updatedAt = System.currentTimeMillis() / 1000.0,
        )

        return FeatureCreateUtils.json.encodeToString(
            serializer<com.flagship.sdk.core.models.FeatureFlagsSchema>(),
            schema,
        )
    }

    /**
     * Creates a feature with empty variants array
     */
    @JvmStatic
    fun createFeatureWithEmptyVariants(
        key: String,
        type: Type = Type.Integer,
        enabled: Boolean = true,
    ): String {
        // Create a rule with constraints but no allocations (since there are no variants)
        val rule = FeatureCreateUtils.createRule(
            ruleName = "rule-1",
            contextFields = listOf("user_tier", "country"),
            allocationStrategy = FeatureCreateUtils.AllocationStrategy.SINGLE,
            variantKeys = emptyList(), // No variant keys since variants are empty
        )
        
        val feature = Feature(
            key = key,
            enabled = enabled,
            rolloutPercentage = 100L,
            type = type,
            updatedAt = System.currentTimeMillis() / 1000,
            rules = listOf(rule),
            defaultRule = null,
            variants = emptyList(),
        )

        val schema = FeatureFlagsSchema(
            features = listOf(feature),
            updatedAt = System.currentTimeMillis() / 1000.0,
        )

        return FeatureCreateUtils.json.encodeToString(
            serializer<FeatureFlagsSchema>(),
            schema,
        )
    }

    /**
     * Creates a feature with enabled = false
     */
    @JvmStatic
    inline fun <reified T> createDisabledFeature(
        key: String,
        variants: Map<String, T>,
    ): String {
        // Create a rule with constraints and allocation
        val variantKeys = variants.keys.toList()
        val rule = FeatureCreateUtils.createRule(
            ruleName = "rule-1",
            contextFields = listOf("user_tier", "country"),
            allocationStrategy = FeatureCreateUtils.AllocationStrategy.SINGLE,
            variantKeys = listOf(variantKeys.firstOrNull() ?: "default"),
        )
        
        val feature = FeatureCreateUtils.createFeature<T>(
            key = key,
            enabled = false,
            rolloutPercentage = 100L,
            variants = variants,
            rules = listOf(rule),
            defaultVariantKey = variantKeys[1]
        )

        val schema = FeatureFlagsSchema(
            features = listOf(feature),
            updatedAt = System.currentTimeMillis() / 1000.0,
        )

        return FeatureCreateUtils.json.encodeToString(
            serializer<FeatureFlagsSchema>(),
            schema,
        )
    }

    /**
     * Creates a feature with enabled = true but no rules/allocations (only default rule)
     */
    @JvmStatic
    inline fun <reified T> createFeatureWithNoRules(
        key: String,
        variants: Map<String, T>,
    ): String {
        val feature = FeatureCreateUtils.createFeature<T>(
            key = key,
            enabled = true,
            rolloutPercentage = 100L,
            variants = variants,
            rules = emptyList(), // No rules, only default rule
        )

        val schema = FeatureFlagsSchema(
            features = listOf(feature),
            updatedAt = System.currentTimeMillis() / 1000.0,
        )

        return FeatureCreateUtils.json.encodeToString(
            serializer<FeatureFlagsSchema>(),
            schema,
        )
    }

    /**
     * Creates a feature where variant value might be missing/null
     * This creates a feature with a variant that has an empty or problematic value
     */
    @JvmStatic
    fun createFeatureWithMissingVariantValue(
        key: String,
        type: Type = Type.Integer,
    ): String {
        // Create a feature with a variant that has no proper value
        // Using empty variant list to simulate missing variant value scenario
        val feature = Feature(
            key = key,
            enabled = true,
            rolloutPercentage = 100L,
            type = type,
            updatedAt = System.currentTimeMillis() / 1000,
            rules = emptyList(),
            defaultRule = DefaultRule(
                ruleName = "default",
                allocation = emptyList(), // No allocation means no variant to return
            ),
            variants = emptyList(), // Empty variants means variant value is missing
        )

        val schema = FeatureFlagsSchema(
            features = listOf(feature),
            updatedAt = System.currentTimeMillis() / 1000.0,
        )

        return FeatureCreateUtils.json.encodeToString(
            serializer<FeatureFlagsSchema>(),
            schema,
        )
    }

    /**
     * Creates a feature with a rule that has missing allocations (empty list)
     */
    @JvmStatic
    inline fun <reified T> createFeatureWithMissingAllocations(
        key: String,
        variants: Map<String, T>,
    ): String {
        val variantKeys = variants.keys.toList()
        val rule = FeatureCreateUtils.createRule(
            ruleName = "rule-1",
            contextFields = listOf("user_tier", "country"),
            allocationStrategy = FeatureCreateUtils.AllocationStrategy.SINGLE,
            variantKeys = emptyList(), // Empty allocations
        )

        val feature = FeatureCreateUtils.createFeature<T>(
            key = key,
            enabled = true,
            rolloutPercentage = 100L,
            variants = variants,
            rules = listOf(rule),
        )

        val schema = FeatureFlagsSchema(
            features = listOf(feature),
            updatedAt = System.currentTimeMillis() / 1000.0,
        )

        return FeatureCreateUtils.json.encodeToString(
            serializer<FeatureFlagsSchema>(),
            schema,
        )
    }

    /**
     * Creates a feature with allocation sum less than 100
     */
    @JvmStatic
    inline fun <reified T> createFeatureWithAllocationSumLessThan100(
        key: String,
        variants: Map<String, T>,
    ): String {
        val variantKeys = variants.keys.toList()
        require(variantKeys.size >= 2) { "At least 2 variants required" }

        val rule = FeatureCreateUtils.createRule(
            ruleName = "rule-1",
            contextFields = listOf("user_tier", "country"),
            allocationStrategy = FeatureCreateUtils.AllocationStrategy.LT_HUNDRED,
            variantKeys = variantKeys,
        )

        val feature = FeatureCreateUtils.createFeature<T>(
            key = key,
            enabled = true,
            rolloutPercentage = 100L,
            variants = variants,
            rules = listOf(rule),
        )

        val schema = FeatureFlagsSchema(
            features = listOf(feature),
            updatedAt = System.currentTimeMillis() / 1000.0,
        )

        return FeatureCreateUtils.json.encodeToString(
            serializer<FeatureFlagsSchema>(),
            schema,
        )
    }

    /**
     * Creates a feature with allocation sum greater than 100
     */
    @JvmStatic
    inline fun <reified T> createFeatureWithAllocationSumGreaterThan100(
        key: String,
        variants: Map<String, T>,
    ): String {
        val variantKeys = variants.keys.toList()
        require(variantKeys.size >= 2) { "At least 2 variants required" }

        val rule = FeatureCreateUtils.createRule(
            ruleName = "rule-1",
            contextFields = listOf("user_tier", "country"),
            allocationStrategy = FeatureCreateUtils.AllocationStrategy.GT_HUNDRED,
            variantKeys = variantKeys,
        )

        val feature = FeatureCreateUtils.createFeature<T>(
            key = key,
            enabled = true,
            rolloutPercentage = 100L,
            variants = variants,
            rules = listOf(rule),
        )

        val schema = FeatureFlagsSchema(
            features = listOf(feature),
            updatedAt = System.currentTimeMillis() / 1000.0,
        )

        return FeatureCreateUtils.json.encodeToString(
            serializer<FeatureFlagsSchema>(),
            schema,
        )
    }

    /**
     * Creates a feature with missing default rule (null)
     */
    @JvmStatic
    inline fun <reified T> createFeatureWithMissingDefaultRule(
        key: String,
        variants: Map<String, T>,
    ): String {
        val variantKeys = variants.keys.toList()
        val rule = FeatureCreateUtils.createRule(
            ruleName = "rule-1",
            contextFields = listOf("user_tier", "country"),
            allocationStrategy = FeatureCreateUtils.AllocationStrategy.SINGLE,
            variantKeys = listOf(variantKeys.firstOrNull() ?: "default"),
        )

        val feature = FeatureCreateUtils.createFeature<T>(
            key = key,
            enabled = true,
            rolloutPercentage = 100L,
            variants = variants,
            rules = listOf(rule),
            defaultVariantKey = variantKeys.firstOrNull() ?: "default",
        )

        // Manually create feature with null defaultRule
        val featureWithNullDefault = Feature(
            key = feature.key,
            enabled = feature.enabled,
            rolloutPercentage = feature.rolloutPercentage,
            type = feature.type,
            updatedAt = feature.updatedAt,
            rules = feature.rules,
            defaultRule = null, // Missing default rule
            variants = feature.variants,
        )

        val schema = FeatureFlagsSchema(
            features = listOf(featureWithNullDefault),
            updatedAt = System.currentTimeMillis() / 1000.0,
        )

        return FeatureCreateUtils.json.encodeToString(
            serializer<FeatureFlagsSchema>(),
            schema,
        )
    }

    /**
     * Creates a feature with type mismatch constraint (e.g., constraint expects String but context has Int)
     */
    @JvmStatic
    inline fun <reified T> createFeatureWithTypeMismatchConstraint(
        key: String,
        variants: Map<String, T>,
        contextField: String,
        expectedType: String, // "String", "Int", "Double", "Boolean"
    ): String {
        val variantKeys = variants.keys.toList()
        
        // Create a constraint with type mismatch
        val constraint = when (expectedType) {
            "String" -> FeatureCreateUtils.createConstraint<String>(
                contextField = contextField,
                operator = Operator.Eq,
                value = "premium", // But context has Int userId = 3456
            )
            "Int" -> FeatureCreateUtils.createConstraint<Long>(
                contextField = contextField,
                operator = Operator.Eq,
                value = 3456L,
            )
            else -> throw IllegalArgumentException("Unsupported expectedType: $expectedType")
        }

        val rule = com.flagship.sdk.core.models.Rule(
            ruleName = "rule-1",
            constraints = listOf(constraint),
            allocations = FeatureCreateUtils.createSingleAllocation(variantKeys.firstOrNull() ?: "default"),
        )

        val feature = FeatureCreateUtils.createFeature<T>(
            key = key,
            enabled = true,
            rolloutPercentage = 100L,
            variants = variants,
            rules = listOf(rule),
        )

        val schema = FeatureFlagsSchema(
            features = listOf(feature),
            updatedAt = System.currentTimeMillis() / 1000.0,
        )

        return FeatureCreateUtils.json.encodeToString(
            serializer<FeatureFlagsSchema>(),
            schema,
        )
    }

    /**
     * Creates a feature with unknown context field in constraint
     */
    @JvmStatic
    inline fun <reified T> createFeatureWithUnknownContextField(
        key: String,
        variants: Map<String, T>,
    ): String {
        val variantKeys = variants.keys.toList()
        
        // Create constraint with unknown field
        val constraint = FeatureCreateUtils.createConstraint<String>(
            contextField = "unknown_field_not_in_context",
            operator = com.flagship.sdk.core.models.Operator.Eq,
            value = "some_value",
        )

        val rule = com.flagship.sdk.core.models.Rule(
            ruleName = "rule-1",
            constraints = listOf(constraint),
            allocations = FeatureCreateUtils.createSingleAllocation(variantKeys.firstOrNull() ?: "default"),
        )

        // Create a second rule that should match
        val rule2 = FeatureCreateUtils.createRule(
            ruleName = "rule-2",
            contextFields = listOf("user_tier"),
            allocationStrategy = FeatureCreateUtils.AllocationStrategy.SINGLE,
            variantKeys = listOf(variantKeys.getOrNull(1) ?: variantKeys.firstOrNull() ?: "default"),
        )

        val feature = FeatureCreateUtils.createFeature<T>(
            key = key,
            enabled = true,
            rolloutPercentage = 100L,
            variants = variants,
            rules = listOf(rule, rule2), // First rule won't match, second should
        )

        val schema = FeatureFlagsSchema(
            features = listOf(feature),
            updatedAt = System.currentTimeMillis() / 1000.0,
        )

        return FeatureCreateUtils.json.encodeToString(
            serializer<FeatureFlagsSchema>(),
            schema,
        )
    }

    /**
     * Creates a feature with missing enabled field (omitted from JSON, should default to false)
     * Uses string replacement to remove the "enabled" field from serialized JSON
     */
    @JvmStatic
    inline fun <reified T> createFeatureWithMissingEnabled(
        key: String,
        variants: Map<String, T>,
    ): String {
        val feature = FeatureCreateUtils.createFeature<T>(
            key = key,
            enabled = false,
            rolloutPercentage = 100L,
            variants = variants,
            rules = emptyList(),
        )

        val schema = FeatureFlagsSchema(
            features = listOf(feature),
            updatedAt = System.currentTimeMillis() / 1000.0,
        )

        val json = FeatureCreateUtils.json.encodeToString(
            serializer<FeatureFlagsSchema>(),
            schema,
        )

        // Remove "enabled" field using regex
        return json.replace(Regex(""","enabled"\s*:\s*(true|false)\s*,"""), ",")
            .replace(Regex(""","enabled"\s*:\s*(true|false)\s*}"""), "}")
    }

    /**
     * Creates a feature with missing rollout_percentage field (omitted from JSON, should default to 100)
     * Uses string replacement to remove the "rollout_percentage" field from serialized JSON
     */
    @JvmStatic
    inline fun <reified T> createFeatureWithMissingRolloutPercentage(
        key: String,
        variants: Map<String, T>,
    ): String {
        val feature = FeatureCreateUtils.createFeature<T>(
            key = key,
            enabled = true,
            rolloutPercentage = 100L,
            variants = variants,
            rules = emptyList(),
        )

        val schema = FeatureFlagsSchema(
            features = listOf(feature),
            updatedAt = System.currentTimeMillis() / 1000.0,
        )

        val json = FeatureCreateUtils.json.encodeToString(
            serializer<FeatureFlagsSchema>(),
            schema,
        )

        // Remove "rollout_percentage" field using regex
        return json.replace(Regex(""","rollout_percentage"\s*:\s*\d+\s*,"""), ",")
            .replace(Regex(""","rollout_percentage"\s*:\s*\d+\s*}"""), "}")
    }

    /**
     * Creates a feature with multiple rules where each rule can be configured to match or not match
     * @param key Feature key
     * @param variants Map of variant keys to their values
     * @param ruleConfigs List of rule configurations: (ruleName, contextFields, evaluateToFalse, variantKey)
     * @param defaultVariantKey Key for the default variant
     */
    @JvmStatic
    inline fun <reified T> createFeatureWithMultipleRules(
        key: String,
        variants: Map<String, T>,
        ruleConfigs: List<Triple<List<String>, Boolean, String>>, // (contextFields, evaluateToFalse, variantKey)
        defaultVariantKey: String? = null,
    ): String {
        val variantKeys = variants.keys.toList()
        val defaultKey = defaultVariantKey ?: variantKeys.lastOrNull() ?: "default"

        val rules = ruleConfigs.mapIndexed { index, (contextFields, evaluateToFalse, variantKey) ->
            FeatureCreateUtils.createRule(
                ruleName = "rule-${index + 1}",
                contextFields = contextFields,
                allocationStrategy = FeatureCreateUtils.AllocationStrategy.SINGLE,
                variantKeys = listOf(variantKey),
                evaluateToFalse = evaluateToFalse,
            )
        }

        val feature = FeatureCreateUtils.createFeature<T>(
            key = key,
            enabled = true,
            rolloutPercentage = 100L,
            variants = variants,
            rules = rules,
            defaultVariantKey = defaultKey,
        )

        val schema = FeatureFlagsSchema(
            features = listOf(feature),
            updatedAt = System.currentTimeMillis() / 1000.0,
        )

        return FeatureCreateUtils.json.encodeToString(
            serializer<FeatureFlagsSchema>(),
            schema,
        )
    }

    /**
     * Custom constraint configuration: (contextField, operator, value)
     */
    data class CustomConstraint(
        val contextField: String,
        val operator: Operator,
        val value: Any, // String, Boolean, Long, Double, or List<ArrayValue>
    )

    /**
     * Custom rule configuration: (constraints, variantKey, allocationPercentage)
     */
    data class CustomRuleConfig(
        val constraints: List<CustomConstraint>,
        val variantKey: String,
        val allocationPercentage: Long = 100L,
        val ruleName: String? = null,
    )

    /**
     * Creates a feature with rules that have custom constraints
     * @param key Feature key
     * @param variants Map of variant keys to their values
     * @param ruleConfigs List of custom rule configurations
     * @param defaultVariantKey Key for the default variant
     */
    @JvmStatic
    inline fun <reified T> createFeatureWithCustomConstraints(
        key: String,
        variants: Map<String, T>,
        ruleConfigs: List<CustomRuleConfig>,
        defaultVariantKey: String? = null,
    ): String {
        val variantKeys = variants.keys.toList()
        val defaultKey = defaultVariantKey ?: variantKeys.lastOrNull() ?: "default"

        val rules = ruleConfigs.mapIndexed { index, ruleConfig ->
            val constraints = ruleConfig.constraints.map { customConstraint ->
                when (val value = customConstraint.value) {
                    is String -> FeatureCreateUtils.createConstraint<String>(
                        contextField = customConstraint.contextField,
                        operator = customConstraint.operator,
                        value = value,
                    )
                    is Boolean -> FeatureCreateUtils.createConstraint<Boolean>(
                        contextField = customConstraint.contextField,
                        operator = customConstraint.operator,
                        value = value,
                    )
                    is Long -> FeatureCreateUtils.createConstraint<Long>(
                        contextField = customConstraint.contextField,
                        operator = customConstraint.operator,
                        value = value,
                    )
                    is Int -> FeatureCreateUtils.createConstraint<Long>(
                        contextField = customConstraint.contextField,
                        operator = customConstraint.operator,
                        value = value.toLong(),
                    )
                    is Double -> FeatureCreateUtils.createConstraint<Double>(
                        contextField = customConstraint.contextField,
                        operator = customConstraint.operator,
                        value = value,
                    )
                    is Float -> FeatureCreateUtils.createConstraint<Double>(
                        contextField = customConstraint.contextField,
                        operator = customConstraint.operator,
                        value = value.toDouble(),
                    )
                    is List<*> -> {
                        // Check if it's a list of ArrayValue
                        if (value.all { it is com.flagship.sdk.core.models.ArrayValue }) {
                            @Suppress("UNCHECKED_CAST")
                            val arrayValueList = value as List<com.flagship.sdk.core.models.ArrayValue>
                            FeatureCreateUtils.createConstraint<List<com.flagship.sdk.core.models.ArrayValue>>(
                                contextField = customConstraint.contextField,
                                operator = customConstraint.operator,
                                value = arrayValueList,
                            )
                        } else {
                            throw IllegalArgumentException("List must contain ArrayValue elements")
                        }
                    }
                    else -> throw IllegalArgumentException(
                        "Unsupported value type: ${value::class.simpleName}. Supported types: String, Boolean, Long, Int, Double, Float, List<ArrayValue>",
                    )
                }
            }

            val allocations = if (ruleConfig.allocationPercentage == 100L) {
                FeatureCreateUtils.createSingleAllocation(ruleConfig.variantKey)
            } else {
                // For non-100% allocation, create a custom allocation
                val remainingPercentage = 100L - ruleConfig.allocationPercentage
                if (remainingPercentage > 0) {
                    // Allocate remaining to default variant
                    listOf(
                        com.flagship.sdk.core.models.AllocationElement(
                            percentage = ruleConfig.allocationPercentage,
                            variantKey = ruleConfig.variantKey,
                        ),
                        com.flagship.sdk.core.models.AllocationElement(
                            percentage = remainingPercentage,
                            variantKey = defaultKey,
                        ),
                    )
                } else {
                    FeatureCreateUtils.createSingleAllocation(ruleConfig.variantKey)
                }
            }

            com.flagship.sdk.core.models.Rule(
                ruleName = ruleConfig.ruleName ?: "rule-${index + 1}",
                constraints = constraints,
                allocations = allocations,
            )
        }

        val feature = FeatureCreateUtils.createFeature<T>(
            key = key,
            enabled = true,
            rolloutPercentage = 100L,
            variants = variants,
            rules = rules,
            defaultVariantKey = defaultKey,
        )

        val schema = FeatureFlagsSchema(
            features = listOf(feature),
            updatedAt = System.currentTimeMillis() / 1000.0,
        )

        return FeatureCreateUtils.json.encodeToString(
            serializer<FeatureFlagsSchema>(),
            schema,
        )
    }

    /**
     * Configures WireMock to stub the /v1/feature/config endpoint with a response provided by the function
     * @param responseProvider Function that returns the JSON response body as a String
     */
    @JvmStatic
    fun stubWireMockFeatureConfig(responseProvider: () -> String) {
        WireMock.stubFor(
            WireMock.get(WireMock.urlPathMatching("/v1/feature/config"))
                .withQueryParam("type", WireMock.matching(".*"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("updated-at", (System.currentTimeMillis()).toString())
                        .withBody(responseProvider())
                )
        )
    }
}
