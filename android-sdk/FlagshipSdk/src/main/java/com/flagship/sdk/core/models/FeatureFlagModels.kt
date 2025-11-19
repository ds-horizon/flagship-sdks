package com.flagship.sdk.core.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import com.github.zafarkhaja.semver.Version

@Serializable
data class FeatureFlagsSchema(
    val features: List<Feature>,
    /**
     * Epoch timestamp (in seconds) when the configuration was last updated
     */
    @SerialName("updated_at")
    val updatedAt: Double,
)

/**
 * List of all feature flags
 */
@Serializable
data class Feature(
    /**
     * Default rule to be applied if no other rules match or if the flag is disabled.
     */
    @SerialName("default_rule")
    val defaultRule: DefaultRule? = null,
    /**
     * Is the flag enabled or not. If not enabled, then default rule allocation is served
     */
    val enabled: Boolean,
    /**
     * Unique identifier of the flag, used while evaluating the flag
     */
    val key: String,
    /**
     * Percentage of users that will qualify for flag evaluation
     */
    @SerialName("rollout_percentage")
    val rolloutPercentage: Long,
    /**
     * List of rules for this feature flag, where order defines priority.
     */
    val rules: List<Rule> = emptyList(),
    /**
     * Type of the flag, which defines the value type of its variants
     */
    val type: Type,
    /**
     * Epoch timestamp (in seconds) when the flag was last updated
     */
    @SerialName("updated_at")
    val updatedAt: Double,
    /**
     * Variants of the flag. All variants will have the same value type
     */
    val variants: List<VariantElement> = emptyList(),
)

/**
 * Default rule to be applied if no other rules match or if the flag is disabled.
 */
@Serializable
data class DefaultRule(
    /**
     * List of variant allocations for the default rule. The sum of all percentages must equal
     * 100.
     */
    val allocation: List<AllocationElement> = emptyList(),
    /**
     * Unique identifier of the default rule
     */
    @SerialName("name")
    val ruleName: String,
)

/**
 * Allocation of a variant to a percentage of users
 */
@Serializable
data class AllocationElement(
    val percentage: Long,
    /**
     * Unique key of the variant to be allocated
     */
    @SerialName("variant_key")
    val variantKey: String,
)

/**
 * If constraints of a rule evaluate to true, its corresponding allocation is served.
 */
@Serializable
data class Rule(
    /**
     * List of variant allocations for this rule. The sum of all percentages must equal 100.
     */
    val allocations: List<AllocationElement> = emptyList(),
    /**
     * List of constraints for this rule. Logical AND is applied between multiple constraints.
     */
    val constraints: List<Constraint> = emptyList(),
    /**
     * Unique identifier of the rule
     */
    @SerialName("name")
    val ruleName: String,
)

/**
 * Constraint to be evaluated against the user context
 */
@Serializable
data class Constraint(
    @SerialName("context_field")
    val contextField: String,
    val operator: Operator,
    val value: ConstraintValue,
)

@Serializable
enum class Operator {
    @SerialName("eq")
    Eq,

    @SerialName("gt")
    Gt,

    @SerialName("gte")
    Gte,

    @SerialName("in")
    In,

    @SerialName("ct")
    Ct,

    @SerialName("lte")
    LTE,

    @SerialName("lt")
    Lt,

    @SerialName("neq")
    Neq,
}

@Serializable(with = ConstraintValueSerializer::class)
sealed class ConstraintValue {
    @Serializable
    data class AnythingArrayValue(
        val value: List<ArrayValue>,
    ) : ConstraintValue()

    @Serializable
    data class BoolValue(
        val value: Boolean,
    ) : ConstraintValue()

    @Serializable
    data class DoubleValue(
        val value: Double,
    ) : ConstraintValue()

    @Serializable
    data class IntegerValue(
        val value: Long,
    ) : ConstraintValue()

    @Serializable
    data class StringValue(
        val value: String,
    ) : ConstraintValue()
}

@Serializable(with = ArrayValueSerializer::class)
sealed class ArrayValue {
    data class StringValue(
        val value: String,
    ) : ArrayValue()

    data class IntegerValue(
        val value: Long,
    ) : ArrayValue()

    data class SemverValue(
        val value: String,
    ) : ArrayValue()
}

object ArrayValueSerializer : KSerializer<ArrayValue> {
    override val descriptor: SerialDescriptor
        get() = buildClassSerialDescriptor("ArrayValue")

    override fun deserialize(decoder: Decoder): ArrayValue {
        val jsonDecoder = decoder as JsonDecoder
        val element = jsonDecoder.decodeJsonElement()
        return when {
            element is JsonPrimitive && element.isString -> {
                try {
                    Version.parse(element.content)
                    return ArrayValue.SemverValue(element.content)
                } catch (e: Exception) {
                    return ArrayValue.StringValue(element.content)
                }
            }
            element is JsonPrimitive && element.longOrNull != null -> ArrayValue.IntegerValue(element.long)
            else -> throw SerializationException("Unknown ArrayValue type: $element")
        }
    }

    override fun serialize(
        encoder: Encoder,
        value: ArrayValue,
    ) {
        val jsonEncoder = encoder as JsonEncoder
        when (value) {
            is ArrayValue.StringValue -> jsonEncoder.encodeJsonElement(JsonPrimitive(value.value))
            is ArrayValue.IntegerValue -> jsonEncoder.encodeJsonElement(JsonPrimitive(value.value))
            is ArrayValue.SemverValue -> jsonEncoder.encodeJsonElement(JsonPrimitive(value.value))
        }
    }
}

object ConstraintValueSerializer : KSerializer<ConstraintValue> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ConstraintValue")

    override fun deserialize(decoder: Decoder): ConstraintValue {
        val jsonDecoder = decoder as JsonDecoder
        val element = jsonDecoder.decodeJsonElement()

        return when {
            element is JsonPrimitive && element.isString ->
                ConstraintValue.StringValue(element.content)

            element is JsonPrimitive && element.booleanOrNull != null ->
                ConstraintValue.BoolValue(element.boolean)

            element is JsonPrimitive && element.longOrNull != null ->
                ConstraintValue.IntegerValue(element.long)

            element is JsonPrimitive && element.doubleOrNull != null ->
                ConstraintValue.DoubleValue(element.double)

            element is JsonArray -> {
                val arrayValues = mutableListOf<ArrayValue>()
                element.forEach {
                    when (it) {
                        is JsonPrimitive -> {
                            if (it.isString) {
                                arrayValues.add(ArrayValue.StringValue(it.content))
                            }
                            if (it.longOrNull != null) {
                                arrayValues.add(ArrayValue.IntegerValue(it.long))
                            }
                        }
                        else -> throw SerializationException("Unknown ArrayValue type: $it")
                    }
                }
                ConstraintValue.AnythingArrayValue(arrayValues)
            }

            else -> throw SerializationException("Unknown ConstraintValue type: $element")
        }
    }

    override fun serialize(
        encoder: Encoder,
        value: ConstraintValue,
    ) {
        val jsonEncoder = encoder as JsonEncoder
        when (value) {
            is ConstraintValue.StringValue -> jsonEncoder.encodeJsonElement(JsonPrimitive(value.value))
            is ConstraintValue.BoolValue -> jsonEncoder.encodeJsonElement(JsonPrimitive(value.value))
            is ConstraintValue.IntegerValue -> jsonEncoder.encodeJsonElement(JsonPrimitive(value.value))
            is ConstraintValue.DoubleValue -> jsonEncoder.encodeJsonElement(JsonPrimitive(value.value))
            is ConstraintValue.AnythingArrayValue -> {
                val jsonArray =
                    buildJsonArray {
                        value.value.forEach {
                            when (it) {
                                is ArrayValue.StringValue -> add(JsonPrimitive(it.value))
                                is ArrayValue.IntegerValue -> add(JsonPrimitive(it.value))
                                is ArrayValue.SemverValue -> add(JsonPrimitive(it.value))
                            }
                        }
                    }
                jsonEncoder.encodeJsonElement(jsonArray)
            }
        }
    }
}

/**
 * Type of the flag, which defines the value type of its variants
 */
@Serializable
enum class Type {
    Integer,

    @SerialName("object")
    Object,

    @SerialName("boolean")
    Boolean,

    @SerialName("double")
    Double,

    @SerialName("string")
    String,

    @SerialName("semver")
    Semver,
}

@Serializable
data class VariantElement(
    val key: String,
    val value: VariantValue,
)

@Serializable(with = VariantValueSerializer::class)
sealed class VariantValue {
    @Serializable
    data class AnythingMapValue(
        val value: JsonObject,
    ) : VariantValue()

    @Serializable
    data class BoolValue(
        val value: Boolean,
    ) : VariantValue()

    @Serializable
    data class DoubleValue(
        val value: Double,
    ) : VariantValue()

    @Serializable
    data class StringValue(
        val value: String,
    ) : VariantValue()

    @Serializable
    data class IntegerValue(
        val value: Long,
    ) : VariantValue()
}

object VariantValueSerializer : KSerializer<VariantValue> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("VariantValue")

    override fun deserialize(decoder: Decoder): VariantValue {
        val jsonDecoder = decoder as JsonDecoder
        val element = jsonDecoder.decodeJsonElement()

        return when {
            element is JsonPrimitive && element.isString ->
                VariantValue.StringValue(element.content)

            element is JsonPrimitive && element.booleanOrNull != null ->
                VariantValue.BoolValue(element.boolean)

            element is JsonPrimitive && element.doubleOrNull != null ->
                VariantValue.DoubleValue(element.double)

            element is JsonPrimitive && element.longOrNull != null ->
                VariantValue.IntegerValue(
                    element.long,
                )

            element is JsonObject -> {
                VariantValue.AnythingMapValue(element)
            }

            else -> throw SerializationException("Unknown VariantValue type: $element")
        }
    }

    override fun serialize(
        encoder: Encoder,
        value: VariantValue,
    ) {
        val jsonEncoder = encoder as JsonEncoder
        when (value) {
            is VariantValue.StringValue -> jsonEncoder.encodeJsonElement(JsonPrimitive(value.value))
            is VariantValue.BoolValue -> jsonEncoder.encodeJsonElement(JsonPrimitive(value.value))
            is VariantValue.IntegerValue -> jsonEncoder.encodeJsonElement(JsonPrimitive(value.value))
            is VariantValue.DoubleValue -> jsonEncoder.encodeJsonElement(JsonPrimitive(value.value))
            is VariantValue.AnythingMapValue -> jsonEncoder.encodeJsonElement(value.value)
        }
    }
}
