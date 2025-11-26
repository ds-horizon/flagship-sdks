package com.flagship.sdk.utils

import com.flagship.sdk.core.models.DefaultRule
import com.flagship.sdk.core.models.Feature
import com.flagship.sdk.core.models.FeatureFlagsSchema
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
