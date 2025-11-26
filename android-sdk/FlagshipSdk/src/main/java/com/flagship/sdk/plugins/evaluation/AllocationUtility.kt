package com.flagship.sdk.plugins.evaluation

import android.util.Log
import com.flagship.sdk.core.models.AllocationElement
import com.flagship.sdk.core.models.EvaluationResult
import com.flagship.sdk.core.models.Reason
import com.flagship.sdk.core.models.VariantElement
import com.flagship.sdk.core.models.VariantValue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import java.security.MessageDigest

object AllocationUtility {
    /**
     * Determines which allocation bucket a user falls into based on a deterministic hash
     * of the flagKey and targetingKey combination.
     *
     * @param key The feature flag key/ variant key
     * @param targetingKey The user's targeting key
     * @param buckets List of allocation buckets with their percentages
     * @return The allocation bucket the user belongs to
     */
    fun getAllocationResult(
        key: String,
        targetingKey: String,
        buckets: List<AllocationBucket>,
        ruleName: String? = null
    ): AllocationBucket {
        require(buckets.isNotEmpty()) { "Buckets list cannot be empty" }

        // Validate that percentages sum to 100
        val totalPercentage = buckets.sumOf { it.percentage }
        require(totalPercentage == 100L) {
            "Total percentage must equal 100, but was $totalPercentage"
        }

        // Generate deterministic hash based on flagKey and targetingKey
        val hashValue = generateHash(key, targetingKey, ruleName)

        // Convert hash to a percentage (0-99)
        val userPercentile = (hashValue % 100).toInt()

        // Find which bucket this user falls into
        var cumulativePercentage = 0L
        for (bucket in buckets) {
            cumulativePercentage += bucket.percentage
            if (userPercentile < cumulativePercentage) {
                return bucket
            }
        }

        // Fallback to last bucket (should never happen with proper validation)
        return buckets.last()
    }

    /**
     * Generates a deterministic hash from flagKey and targetingKey.
     * The same flagKey and targetingKey combination will always produce the same hash.
     *
     * @param flagKey The feature flag key
     * @param targetingKey The user's targeting key
     * @return A positive long hash value
     */
    fun generateHash(
        flagKey: String,
        targetingKey: String,
        ruleName: String? = null
    ): Long {
        val input = ruleName?.let {
            "$flagKey:$ruleName:$targetingKey"
        } ?: run {
            "$flagKey:$targetingKey"
        }
        val digest = MessageDigest.getInstance("MD5")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))

        // Convert first 8 bytes to a long value using little-endian (matching iOS)
        // Load bytes as little-endian to match iOS implementation exactly
        var hash = 0L
        for (i in 0 until minOf(8, hashBytes.size)) {
            hash = hash or ((hashBytes[i].toLong() and 0xFF) shl (i * 8))
        }

        // Ensure positive value
        return hash and Long.MAX_VALUE
    }

    private fun getRolloutBuckets(rolloutPercentage: Long): List<AllocationBucket> {
        require(rolloutPercentage in 0..100) { "Rollout percentage must be between 0 and 100" }
        val onBucket = AllocationBucket("ON", rolloutPercentage)
        val offBucket = AllocationBucket("OFF", (100 - rolloutPercentage))
        return listOf(onBucket, offBucket)
    }

    fun isUserInRolloutBucket(
        flagKey: String,
        targetingKey: String,
        rolloutPercentage: Long,
    ): Boolean {
        val buckets = getRolloutBuckets(rolloutPercentage)
        val result = getAllocationResult(flagKey, targetingKey, buckets)
        return result.key == "ON"
    }

    fun allocationBucketFor(
        flagKey: String,
        targetingKey: String,
        allocations: List<AllocationElement>,
        ruleName: String,
    ): AllocationBucket {
        val buckets = allocations.map { allocation ->
            AllocationBucket(
                allocation.variantKey,
                allocation.percentage,
            )
        }
        return getAllocationResult(
            flagKey,
            targetingKey,
            buckets,
            ruleName = ruleName,
        )
    }

    fun buildBooleanResultFromVariant(
        variant: VariantElement?,
        defaultValue: Boolean,
        reason: Reason,
    ): EvaluationResult<Boolean> {
        val variantElement = variant ?: return EvaluationResult(
            value = defaultValue,
            reason = Reason.ERROR,
            metadata = mapOf("error" to "No variant found"),
        )

        val value = variantElement.value
        if (value !is VariantValue.BoolValue) {
            return EvaluationResult(
                value = defaultValue,
                reason = Reason.ERROR,
                metadata = mapOf("error" to "Variant not found with Boolean value"),
            )
        }

        return EvaluationResult(
            value = value.value,
            variant = variantElement.key,
            reason = reason,
        )
    }

    fun buildStringResultFromVariant(
        variant: VariantElement?,
        defaultValue: String,
        reason: Reason,
    ): EvaluationResult<String> {
        val variantElement = variant ?: return EvaluationResult(
            value = defaultValue,
            reason = Reason.ERROR,
            metadata = mapOf("error" to "No variant found"),
        )

        val value = variantElement.value
        if (value !is VariantValue.StringValue) {
            return EvaluationResult(
                value = defaultValue,
                reason = Reason.ERROR,
                metadata = mapOf("error" to "Variant not found with String value"),
            )
        }

        return EvaluationResult(
            value = value.value,
            variant = variantElement.key,
            reason = reason,
        )
    }

    fun buildIntResultFromVariant(
        variant: VariantElement?,
        defaultValue: Int,
        reason: Reason,
    ): EvaluationResult<Int> {
        val variantElement = variant ?: return EvaluationResult(
            value = defaultValue,
            reason = Reason.ERROR,
            metadata = mapOf("error" to "No variant found"),
        )

        val value = variantElement.value
        if (value !is VariantValue.IntegerValue) {
            return EvaluationResult(
                value = defaultValue,
                reason = Reason.ERROR,
                metadata = mapOf("error" to "Variant not found with Integer value"),
            )
        }

        return EvaluationResult(
            value = value.value.toInt(),
            variant = variantElement.key,
            reason = reason,
        )
    }

    fun buildDoubleResultFromVariant(
        variant: VariantElement?,
        defaultValue: Double,
        reason: Reason,
    ): EvaluationResult<Double> {
        val variantElement = variant ?: return EvaluationResult(
            value = defaultValue,
            reason = Reason.ERROR,
            metadata = mapOf("error" to "No variant found"),
        )

        val value = variantElement.value
        if (value !is VariantValue.DoubleValue) {
            return EvaluationResult(
                value = defaultValue,
                reason = Reason.ERROR,
                metadata = mapOf("error" to "Variant not found with Double value"),
            )
        }

        return EvaluationResult(
            value = value.value,
            variant = variantElement.key,
            reason = reason,
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> buildObjectResultFromVariant(
        variant: VariantElement?,
        defaultValue: T,
        reason: Reason,
    ): EvaluationResult<T> {
        val variantElement = variant ?: return EvaluationResult(
            value = defaultValue,
            reason = Reason.ERROR,
            metadata = mapOf("error" to "No variant found"),
        )

        val raw = variantElement.value
        val jsonObject: JsonObject? = when (raw) {
            is VariantValue.AnythingMapValue -> raw.value
            is VariantValue.StringValue ->
                runCatching {
                    val parsed = Json.parseToJsonElement(raw.value)
                    parsed as? JsonObject
                }.getOrNull()
            else -> null
        }

        if (jsonObject == null) {
            return EvaluationResult(
                value = defaultValue,
                reason = Reason.ERROR,
                metadata = mapOf("error" to "Variant not found with Object value"),
            )
        }

        @Suppress("UNCHECKED_CAST")
        val casted: T = when (defaultValue) {
            // For getJson callers we return compact JSON string
            is String -> jsonObject.toString() as T
            is JsonObject -> jsonObject as T
            // If caller expects a Map-like structure, convert JsonObject into a Map<String, Any?> recursively
            is Map<*, *> -> jsonObjectToMap(jsonObject) as T
            // If caller passes through a JsonObject default, return JsonObject
            else -> jsonObject as T
        }

        return EvaluationResult(
            value = casted,
            variant = variantElement.key,
            reason = reason,
        )
    }

    private fun jsonElementToKotlin(element: JsonElement): Any? = when (element) {
        is JsonPrimitive -> {
            when {
                element.isString -> element.content
                element.booleanOrNull != null -> element.boolean
                element.longOrNull != null -> element.long
                element.doubleOrNull != null -> element.double
                else -> null
            }
        }
        is JsonObject -> jsonObjectToMap(element)
        is JsonArray -> element.map { jsonElementToKotlin(it) }
        else -> null
    }

    private fun jsonObjectToMap(obj: JsonObject): Map<String, Any?> {
        val result = LinkedHashMap<String, Any?>()
        obj.entries.forEach { (k, v) ->
            result[k] = jsonElementToKotlin(v)
        }
        return result
    }
}

data class AllocationBucket(
    val key: String,
    val percentage: Long,
)
