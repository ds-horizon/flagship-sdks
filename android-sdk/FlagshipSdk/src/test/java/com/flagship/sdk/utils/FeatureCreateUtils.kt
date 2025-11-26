package com.flagship.sdk.utils

import com.flagship.sdk.core.models.AllocationElement
import com.flagship.sdk.core.models.ArrayValue
import com.flagship.sdk.core.models.Constraint
import com.flagship.sdk.core.models.ConstraintValue
import com.flagship.sdk.core.models.DefaultRule
import com.flagship.sdk.core.models.Feature
import com.flagship.sdk.core.models.FeatureFlagsSchema
import com.flagship.sdk.core.models.Operator
import com.flagship.sdk.core.models.Rule
import com.flagship.sdk.core.models.Type
import com.flagship.sdk.core.models.VariantElement
import com.flagship.sdk.core.models.VariantValue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

object FeatureCreateUtils {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = true
    }

    /**
     * Creates allocations based on rollout percentage distribution
     * @param variantKeys List of variant keys to allocate
     * @param percentages List of percentages (must sum to 100)
     * @return List of AllocationElement
     */
    @JvmStatic
    fun createAllocations(
        variantKeys: List<String>,
        percentages: List<Long>,
    ): List<AllocationElement> {
        require(variantKeys.size == percentages.size) {
            "Variant keys and percentages must have the same size"
        }
        require(percentages.sum() == 100L) {
            "Percentages must sum to 100, got ${percentages.sum()}"
        }
        return variantKeys.zip(percentages).map { (key, percentage) ->
            AllocationElement(percentage = percentage, variantKey = key)
        }
    }

    /**
     * Creates a 50-50 allocation between two variants
     */
    @JvmStatic
    fun createFiftyFiftyAllocation(
        variantKey1: String,
        variantKey2: String,
    ): List<AllocationElement> = createAllocations(
        variantKeys = listOf(variantKey1, variantKey2),
        percentages = listOf(50L, 50L),
    )

    /**
     * Creates a single variant allocation (100%)
     */
    @JvmStatic
    fun createSingleAllocation(variantKey: String): List<AllocationElement> =
        createAllocations(
            variantKeys = listOf(variantKey),
            percentages = listOf(100L),
        )

    /**
     * Creates a three-way allocation (33-33-34)
     */
    @JvmStatic
    fun createThreeWayAllocation(
        variantKey1: String,
        variantKey2: String,
        variantKey3: String,
    ): List<AllocationElement> = createAllocations(
        variantKeys = listOf(variantKey1, variantKey2, variantKey3),
        percentages = listOf(33L, 33L, 34L),
    )

    /**
     * Creates constraints based on context fields with type inference
     * Supports String, Boolean, Long, Double, and List<ArrayValue>
     */
    @JvmStatic
    inline fun <reified T> createConstraint(
        contextField: String,
        operator: Operator,
        value: T,
    ): Constraint {
        val constraintValue = when (T::class) {
            String::class -> ConstraintValue.StringValue(value as String)
            Boolean::class -> ConstraintValue.BoolValue(value as Boolean)
            Long::class -> ConstraintValue.IntegerValue(value as Long)
            Double::class -> ConstraintValue.DoubleValue(value as Double)
            List::class -> {
                val list = value as? List<*>
                require(list != null && list.all { it is ArrayValue }) {
                    "List must contain ArrayValue elements"
                }
                ConstraintValue.AnythingArrayValue(list as List<ArrayValue>)
            }
            else -> throw IllegalArgumentException(
                "Unsupported type ${T::class.simpleName}. Supported types: String, Boolean, Long, Double, List<ArrayValue>",
            )
        }
        return Constraint(
            contextField = contextField,
            operator = operator,
            value = constraintValue,
        )
    }

    /**
     * Allocation strategy for rule creation
     */
    enum class AllocationStrategy {
        FIFTY_FIFTY,
        SINGLE,
        THREE_WAY,
        CUSTOM,
    }

    /**
     * Creates a generic feature with variants and values
     * Type is automatically inferred from the variant values
     * @param key Feature key
     * @param enabled Whether the feature is enabled
     * @param rolloutPercentage Rollout percentage
     * @param variants Map of variant keys to their values
     * @param rules List of rules for the feature
     * @param defaultVariantKey Key for the default variant
     */
    @JvmStatic
    inline fun <reified T> createFeature(
        key: String,
        enabled: Boolean = true,
        rolloutPercentage: Long = 100L,
        variants: Map<String, T>,
        rules: List<Rule> = emptyList(),
        defaultVariantKey: String = "default",
    ): Feature {
        require(variants.isNotEmpty()) { "At least one variant is required" }
        
        // Infer feature type from the reified type parameter T
        val featureType = when (T::class) {
            Boolean::class -> Type.Boolean
            String::class -> Type.String
            Int::class, Long::class, Short::class, Byte::class -> Type.Integer
            Double::class, Float::class -> Type.Double
            JsonObject::class -> Type.Object
            else -> throw IllegalArgumentException("Unsupported variant value type: ${T::class.simpleName}")
        }
        
        val variantElements = variants.map { (variantKey, value) ->
            VariantElement(
                key = variantKey,
                value = convertToVariantValue(value, featureType),
            )
        }

        variants[defaultVariantKey]
            ?: variants.values.firstOrNull()
            ?: throw IllegalArgumentException("No default variant found and no variants provided")

        val defaultRule = DefaultRule(
            ruleName = "default",
            allocation = createSingleAllocation(defaultVariantKey),
        )

        return Feature(
            key = key,
            enabled = enabled,
            rolloutPercentage = rolloutPercentage,
            type = featureType,
            updatedAt = System.currentTimeMillis() / 1000,
            rules = rules,
            defaultRule = defaultRule,
            variants = variantElements,
        )
    }

    /**
     * Converts a generic value to VariantValue based on type
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T> convertToVariantValue(value: T, type: Type): VariantValue {
        return when (type) {
            Type.Boolean -> VariantValue.BoolValue(value as Boolean)
            Type.String -> VariantValue.StringValue(value as String)
            Type.Integer -> VariantValue.IntegerValue((value as Number).toLong())
            Type.Double -> VariantValue.DoubleValue((value as Number).toDouble())
            Type.Object -> VariantValue.AnythingMapValue(value as JsonObject)
            Type.Semver -> VariantValue.StringValue(value as String)
        }
    }

    /**
     * Creates a rule with demo constraints and allocations based on strategy
     * @param ruleName Name of the rule
     * @param contextFields List of context field names to use as constraints
     * @param allocationStrategy Strategy for allocation distribution
     * @param variantKeys List of variant keys for allocation
     * @return Rule with generated constraints and allocations
     */
    @JvmStatic
    fun createRule(
        ruleName: String,
        contextFields: List<String>,
        allocationStrategy: AllocationStrategy = AllocationStrategy.FIFTY_FIFTY,
        variantKeys: List<String> = emptyList(),
    ): Rule {
        val constraints = generateDemoConstraints(contextFields)

        val allocations: List<AllocationElement> = if (variantKeys.isEmpty()) {
            emptyList()
        } else {
            when (allocationStrategy) {
                AllocationStrategy.FIFTY_FIFTY -> {
                    require(variantKeys.size >= 2) { "FIFTY_FIFTY requires at least 2 variant keys" }
                    createFiftyFiftyAllocation(variantKeys[0], variantKeys[1])
                }
                AllocationStrategy.SINGLE -> {
                    require(variantKeys.isNotEmpty()) { "SINGLE requires at least 1 variant key" }
                    createSingleAllocation(variantKeys[0])
                }
                AllocationStrategy.THREE_WAY -> {
                    require(variantKeys.size >= 3) { "THREE_WAY requires at least 3 variant keys" }
                    createThreeWayAllocation(variantKeys[0], variantKeys[1], variantKeys[2])
                }
                AllocationStrategy.CUSTOM -> {
                    require(variantKeys.isNotEmpty()) { "CUSTOM requires at least 1 variant key" }
                    val percentagePerVariant = 100L / variantKeys.size
                    val remainder = 100L % variantKeys.size
                    val percentages = variantKeys.mapIndexed { index, _ ->
                        if (index == 0) percentagePerVariant + remainder else percentagePerVariant
                    }
                    createAllocations(variantKeys, percentages)
                }
            }
        }

        return Rule(
            ruleName = ruleName,
            constraints = constraints,
            allocations = allocations,
        )
    }

    /**
     * Generates demo constraints based on provided context fields
     * @param contextFields List of context field names to use as constraints
     * @return List of constraints using Operator.Eq for all fields except cohort (which uses Operator.In)
     */
    @JvmStatic
    fun generateDemoConstraints(
        contextFields: List<String>,
    ): List<Constraint> {
        val cohortField = "cohort"
        val valueMap = getDefaultContextWithCohort()

        return contextFields.map { field ->
            val value = valueMap[field]
            if (field == cohortField) {
                // Cohort field uses Operator.In with list of ArrayValue
                val cohortList = value as? List<*>
                    ?: throw IllegalArgumentException("cohort field must be a list")
                val arrayValues = cohortList.mapNotNull { item ->
                    when (item) {
                        is String -> ArrayValue.StringValue(item)
                        is Number -> ArrayValue.IntegerValue(item.toLong())
                        else -> null
                    }
                }
                createConstraint(field, Operator.In, arrayValues)
            } else {
                when (value) {
                    is Boolean -> createConstraint(field, Operator.Eq, value)
                    is Long -> createConstraint(field, Operator.Eq, value)
                    is Int -> createConstraint(field, Operator.Eq, value.toLong())
                    is Double -> createConstraint(field, Operator.Eq, value)
                    is String -> createConstraint(field, Operator.Eq, value)
                    is List<*> -> {
                        // Convert list to ArrayValue list for Operator.In
                        val arrayValues = value.mapNotNull { item ->
                            when (item) {
                                is String -> ArrayValue.StringValue(item)
                                is Number -> ArrayValue.IntegerValue(item.toLong())
                                else -> null
                            }
                        }
                        createConstraint(field, Operator.In, arrayValues)
                    }
                    else -> throw IllegalArgumentException("Unsupported value type for field")
                }
            }
        }
    }

    /**
     * Gets the default context fields including cohort
     */
    @JvmStatic
    fun getDefaultContextWithCohort(): Map<String, Any?> {
        return mapOf(
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
            "cohort" to listOf("early-adopter", "beta-tester", "premium"),
        )
    }

    /**
     * Serializes FeatureFlagsSchema to JSON string for WireMock
     */
    @JvmStatic
    fun serializeToJson(schema: FeatureFlagsSchema): String {
        return json.encodeToString(FeatureFlagsSchema.serializer(), schema)
    }
}
