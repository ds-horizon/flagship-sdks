package com.flagship.sdk.plugins.evaluation

import android.util.Log
import com.flagship.sdk.core.contracts.ICache
import com.flagship.sdk.core.contracts.IEvaluator
import com.flagship.sdk.core.models.AllocationElement
import com.flagship.sdk.core.models.ArrayValue
import com.flagship.sdk.core.models.Constraint
import com.flagship.sdk.core.models.ConstraintValue
import com.flagship.sdk.core.models.EvaluationContext
import com.flagship.sdk.core.models.EvaluationResult
import com.flagship.sdk.core.models.Feature
import com.flagship.sdk.core.models.Operator
import com.flagship.sdk.core.models.Reason
import com.flagship.sdk.core.models.Rule
import com.flagship.sdk.core.models.VariantElement
import com.flagship.sdk.core.models.VariantValue
import com.github.zafarkhaja.semver.Version

class EdgeEvaluator(
    private val evaluateCache: ICache,
    private val persistentCache: ICache,
) : IEvaluator {
    override fun evaluateBoolean(
        flagKey: String,
        defaultValue: Boolean,
        config: Feature?,
        context: EvaluationContext,
    ): EvaluationResult<Boolean> {
        return evaluateCache.get<Boolean>(flagKey)?.let {
            EvaluationResult(value = it, reason = Reason.CACHED)
        } ?: run {
            val targetingKey = getSanitizedTargetingKey(context)
            if (config == null || targetingKey.isEmpty()) {
                return@run EvaluationResult(
                    value = defaultValue,
                    reason = Reason.DEFAULT,
                )
            }

            if (!config.enabled) {
                return@run EvaluationResult(
                    value = defaultValue,
                    reason = Reason.DISABLED,
                )
            }

            if (!AllocationUtility.isUserInRolloutBucket(
                    flagKey,
                    targetingKey,
                    config.rolloutPercentage,
                )
            ) {
                return@run EvaluationResult(value = defaultValue, reason = Reason.SPLIT)
            }

            for (rule in config.rules) {
                val isMatch = checkRuleConstraints(rule, context)
                if (isMatch) {
                    val allocation = AllocationUtility.allocationBucketFor(flagKey, targetingKey, rule.allocations, rule.ruleName)
                    val variant = getVariantValue(config.variants, allocation)
                    return@run AllocationUtility.buildBooleanResultFromVariant(variant, defaultValue, Reason.TARGETING_MATCH)
                }
            }

            val defaultRule = config.defaultRule
            if (defaultRule != null) {
                val defaultAllocation = AllocationUtility.allocationBucketFor(flagKey, targetingKey, defaultRule.allocation, defaultRule.ruleName)
                val defaultVariant = getVariantValue(config.variants, defaultAllocation)
                return@run AllocationUtility.buildBooleanResultFromVariant(defaultVariant, defaultValue, Reason.DEFAULT_TARGETING_MATCH)
            }
            return@run EvaluationResult(value = defaultValue, reason = Reason.DEFAULT)
        }.also {
            evaluateCache.put(flagKey, it.value)
        }
    }

    override fun evaluateString(
        flagKey: String,
        defaultValue: String,
        config: Feature?,
        context: EvaluationContext,
    ): EvaluationResult<String> {
        return evaluateCache.get<String>(flagKey)?.let {
            EvaluationResult(value = it, reason = Reason.CACHED)
        } ?: run {
            val targetingKey = getSanitizedTargetingKey(context)
            if (config == null || targetingKey.isEmpty()) {
                return@run EvaluationResult(
                    value = defaultValue,
                    reason = Reason.DEFAULT,
                )
            }

            if (!config.enabled) {
                return@run EvaluationResult(
                    value = defaultValue,
                    reason = Reason.DISABLED,
                )
            }

            if (!AllocationUtility.isUserInRolloutBucket(flagKey, targetingKey, config.rolloutPercentage)) {
                return@run EvaluationResult(value = defaultValue, reason = Reason.SPLIT)
            }

            for (rule in config.rules) {
                val isMatch = checkRuleConstraints(rule, context)
                if (isMatch) {
                    val allocation = AllocationUtility.allocationBucketFor(flagKey, targetingKey, rule.allocations, rule.ruleName)
                    val variant = getVariantValue(config.variants, allocation)
                    return@run AllocationUtility.buildStringResultFromVariant(variant, defaultValue, Reason.TARGETING_MATCH)
                }
            }

            val defaultRule = config.defaultRule
            if (defaultRule != null) {
                val defaultAllocation = AllocationUtility.allocationBucketFor(flagKey, targetingKey, defaultRule.allocation, defaultRule.ruleName)
                val defaultVariant = getVariantValue(config.variants, defaultAllocation)
                return@run AllocationUtility.buildStringResultFromVariant(defaultVariant, defaultValue, Reason.DEFAULT_TARGETING_MATCH)
            }
            return@run EvaluationResult(value = defaultValue, reason = Reason.DEFAULT)
        }.also {
            evaluateCache.put(flagKey, it.value)
        }
    }

    override fun evaluateInt(
        flagKey: String,
        defaultValue: Int,
        config: Feature?,
        context: EvaluationContext,
    ): EvaluationResult<Int> {
        return evaluateCache.get<Int>(flagKey)?.let {
            EvaluationResult(value = it, reason = Reason.CACHED)
        } ?: run {
            val targetingKey = getSanitizedTargetingKey(context)
            if (config == null || targetingKey.isEmpty()) {
                return@run EvaluationResult(
                    value = defaultValue,
                    reason = Reason.DEFAULT,
                )
            }

            if (!config.enabled) {
                return@run EvaluationResult(
                    value = defaultValue,
                    reason = Reason.DISABLED,
                )
            }

            if (!AllocationUtility.isUserInRolloutBucket(flagKey, targetingKey, config.rolloutPercentage)) {
                return@run EvaluationResult(value = defaultValue, reason = Reason.SPLIT)
            }

            for (rule in config.rules) {
                val isMatch = checkRuleConstraints(rule, context)
                if (isMatch) {
                    val allocation = AllocationUtility.allocationBucketFor(flagKey, targetingKey, rule.allocations, rule.ruleName)
                    val variant = getVariantValue(config.variants, allocation)
                    return@run AllocationUtility.buildIntResultFromVariant(variant, defaultValue, Reason.TARGETING_MATCH)
                }
            }

            val defaultRule = config.defaultRule
            if (defaultRule != null) {
                val defaultAllocation = AllocationUtility.allocationBucketFor(flagKey, targetingKey, defaultRule.allocation, defaultRule.ruleName)
                val defaultVariant = getVariantValue(config.variants, defaultAllocation)
                return@run AllocationUtility.buildIntResultFromVariant(defaultVariant, defaultValue, Reason.DEFAULT_TARGETING_MATCH)
            }
            return@run EvaluationResult(value = defaultValue, reason = Reason.DEFAULT)
        }.also {
            evaluateCache.put(flagKey, it.value)
        }
    }

    override fun evaluateDouble(
        flagKey: String,
        defaultValue: Double,
        config: Feature?,
        context: EvaluationContext,
    ): EvaluationResult<Double> {
        return evaluateCache.get<Double>(flagKey)?.let {
            EvaluationResult(value = it, reason = Reason.CACHED)
        } ?: run {
            val targetingKey = getSanitizedTargetingKey(context)
            if (config == null || targetingKey.isEmpty()) {
                return@run EvaluationResult(
                    value = defaultValue,
                    reason = Reason.DEFAULT,
                )
            }

            if (!config.enabled) {
                return@run EvaluationResult(
                    value = defaultValue,
                    reason = Reason.DISABLED,
                )
            }

            if (!AllocationUtility.isUserInRolloutBucket(flagKey, targetingKey, config.rolloutPercentage)) {
                return@run EvaluationResult(value = defaultValue, reason = Reason.SPLIT)
            }

            for (rule in config.rules) {
                val isMatch = checkRuleConstraints(rule, context)
                if (isMatch) {
                    val allocation = AllocationUtility.allocationBucketFor(flagKey, targetingKey, rule.allocations, rule.ruleName)
                    val variant = getVariantValue(config.variants, allocation)
                    return@run AllocationUtility.buildDoubleResultFromVariant(variant, defaultValue, Reason.TARGETING_MATCH)
                }
            }

            val defaultRule = config.defaultRule
            if (defaultRule != null) {
                val defaultAllocation = AllocationUtility.allocationBucketFor(flagKey, targetingKey, defaultRule.allocation, defaultRule.ruleName)
                val defaultVariant = getVariantValue(config.variants, defaultAllocation)
                return@run AllocationUtility.buildDoubleResultFromVariant(defaultVariant, defaultValue, Reason.DEFAULT_TARGETING_MATCH)
            }
            return@run EvaluationResult(value = defaultValue, reason = Reason.DEFAULT)
        }.also {
            evaluateCache.put(flagKey, it.value)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> evaluateObject(
        flagKey: String,
        defaultValue: T,
        config: Feature?,
        context: EvaluationContext,
    ): EvaluationResult<T> {
        return evaluateCache.get<T>(flagKey)?.let {
            EvaluationResult(value = it, reason = Reason.CACHED)
        } ?: run {
            val targetingKey = getSanitizedTargetingKey(context)
            if (config == null || targetingKey.isEmpty()) {
                return@run EvaluationResult(
                    value = defaultValue,
                    reason = Reason.DEFAULT,
                )
            }

            if (!config.enabled) {
                return@run EvaluationResult(
                    value = defaultValue,
                    reason = Reason.DISABLED,
                )
            }

            if (!AllocationUtility.isUserInRolloutBucket(flagKey, targetingKey, config.rolloutPercentage)) {
                return@run EvaluationResult(value = defaultValue, reason = Reason.SPLIT)
            }

            for (rule in config.rules) {
                val isMatch = checkRuleConstraints(rule, context)
                if (isMatch) {
                    val allocation = AllocationUtility.allocationBucketFor(flagKey, targetingKey, rule.allocations, rule.ruleName)
                    val variant = getVariantValue(config.variants, allocation)
                    return@run AllocationUtility.buildObjectResultFromVariant(variant, defaultValue, Reason.TARGETING_MATCH)
                }
            }

            val defaultRule = config.defaultRule
            if (defaultRule != null) {
                val defaultAllocation = AllocationUtility.allocationBucketFor(flagKey, targetingKey, defaultRule.allocation, defaultRule.ruleName)
                val defaultVariant = getVariantValue(config.variants, defaultAllocation)
                return@run AllocationUtility.buildObjectResultFromVariant(defaultVariant, defaultValue, Reason.DEFAULT_TARGETING_MATCH)
            }
            return@run EvaluationResult(value = defaultValue, reason = Reason.DEFAULT)
        }.also {
            evaluateCache.put(flagKey, it.value)
        }
    }

    private fun checkRuleConstraints(
        rule: Rule,
        context: EvaluationContext,
    ): Boolean {
        for (constraint in rule.constraints) {
            val matches = matches(constraint, context)
            if (!matches) return false
        }
        return true
    }

    private fun getVariantValue(
        variants: List<VariantElement>,
        allocationBucket: AllocationBucket,
    ): VariantElement? = variants.find { variant -> variant.key == allocationBucket.key }

    /*
    Helper method to sanitize the targeting key in-case special handling is required.
     * */
    private fun getSanitizedTargetingKey(context: EvaluationContext): String = context.targetingKey

    private fun matches(
        constraint: Constraint,
        context: EvaluationContext,
    ): Boolean {
        val left = context.attributes[constraint.contextField]
        return when (constraint.operator) {
            Operator.In -> {
                (
                    (constraint.value as? ConstraintValue.AnythingArrayValue)
                        ?.value
                        ?.any {
                            when (it) {
                                is ArrayValue.StringValue -> it.value == left
                                is ArrayValue.IntegerValue -> it.value == (left as? Number)?.toLong()
                                is ArrayValue.SemverValue -> {
                                    try {
                                        val leftString = left as? String ?: return false
                                        compareSemver(leftString, it.value) == 0
                                    } catch (e: Exception) {
                                        false
                                    }
                                }
                            }
                        }
                        ?: false
                )
            }

            Operator.Eq -> eq(left, constraint.value)

            Operator.Neq -> !eq(left, constraint.value)

            Operator.Gt -> compare(left, constraint.value)?.let { it > 0 } ?: false
            Operator.Gte -> compare(left, constraint.value)?.let { it >= 0 } ?: false
            Operator.Lt -> compare(left, constraint.value)?.let { it < 0 } ?: false
            Operator.LTE -> compare(left, constraint.value)?.let { it <= 0 } ?: false
        }
    }

    private fun eq(
        left: Any?,
        right: ConstraintValue,
    ): Boolean {
        return when (right) {
            is ConstraintValue.BoolValue -> left is Boolean && left == right.value
            is ConstraintValue.DoubleValue -> left.toDoubleOrNull()?.let { it == right.value } ?: false
            is ConstraintValue.IntegerValue -> left.toLongOrNull()?.let { it == right.value } ?: false
            is ConstraintValue.StringValue -> {
                try {
                    val leftString = left as? String ?: return false
                    return compareSemver(leftString, right.value) == 0
                } catch (e: Exception) {
                    return left is String && left == right.value
                }
            }

            is ConstraintValue.AnythingArrayValue -> right.value.any { it == left }
        }
    }

    private fun compare(
        left: Any?,
        right: ConstraintValue,
    ): Int? {
        return when (right) {
            is ConstraintValue.DoubleValue -> left.toDoubleOrNull()?.compareTo(right.value)
            is ConstraintValue.IntegerValue -> left.toLongOrNull()?.compareTo(right.value)
            is ConstraintValue.StringValue -> {
                // use java-semver library to compare semver strings
                try {
                    val leftString = left as? String ?: return null
                    return compareSemver(leftString, right.value)
                } catch (e: Exception) {
                    return null
                }
            }

            else -> null
        }
    }

    /**
     * Compares two semver version strings, ignoring build metadata.
     * @param left The left version string to compare
     * @param right The right version string to compare
     * @return A negative integer if left < right, zero if left == right, or a positive integer if left > right
     * @throws Exception if either version string cannot be parsed as a valid semver
     */
    private fun compareSemver(left: String, right: String): Int {
        val leftVersion = Version.parse(left)
        val rightVersion = Version.parse(right)
        return leftVersion.compareToIgnoreBuildMetadata(rightVersion)
    }

    private fun Any?.toDoubleOrNull(): Double? =
        when (this) {
            is Double -> this
            is Float -> this.toDouble()
            is Number -> this.toDouble()
            is String -> this.toDoubleOrNull()
            else -> null
        }

    private fun Any?.toLongOrNull(): Long? =
        when (this) {
            is Long -> this
            is Int -> this.toLong()
            is Number -> this.toLong()
            is String -> this.toLongOrNull()
            else -> null
        }
}
