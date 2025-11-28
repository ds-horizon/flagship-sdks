package com.flagship.sdk.facade

import com.flagship.sdk.core.models.EvaluationResult
import dev.openfeature.kotlin.sdk.EvaluationContext
import dev.openfeature.kotlin.sdk.EvaluationMetadata
import dev.openfeature.kotlin.sdk.FeatureProvider
import dev.openfeature.kotlin.sdk.Hook
import dev.openfeature.kotlin.sdk.ProviderEvaluation
import dev.openfeature.kotlin.sdk.ProviderMetadata
import dev.openfeature.kotlin.sdk.Value
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class FlagshipProvider(
    domain: String,
    config: FlagShipConfig,
) : FeatureProvider {
    private val flagShipClient = FlagShipClient.getInstance(domain, config)

    override val hooks: List<Hook<*>>
        get() = emptyList()
    override val metadata: ProviderMetadata
        get() =
            object : ProviderMetadata {
                override val name: String
                    get() = "FlagshipProvider"
            }

    override fun getBooleanEvaluation(
        key: String,
        defaultValue: Boolean,
        context: EvaluationContext?,
    ): ProviderEvaluation<Boolean> =
        mapEvaluation(
            flagShipClient.getBoolean(
                key,
                defaultValue,
                context?.getTargetingKey() ?: "",
                context.sanitizeEvaluationContext(),
            ),
        )

    override fun getDoubleEvaluation(
        key: String,
        defaultValue: Double,
        context: EvaluationContext?,
    ): ProviderEvaluation<Double> =
        mapEvaluation(
            flagShipClient.getDouble(
                key,
                defaultValue,
                context?.getTargetingKey() ?: "",
                context.sanitizeEvaluationContext(),
            ),
        )

    override fun getIntegerEvaluation(
        key: String,
        defaultValue: Int,
        context: EvaluationContext?,
    ): ProviderEvaluation<Int> =
        mapEvaluation(
            flagShipClient.getInt(
                key,
                defaultValue,
                context?.getTargetingKey() ?: "",
                context.sanitizeEvaluationContext(),
            ),
        )

    override fun getObjectEvaluation(
        key: String,
        defaultValue: Value,
        context: EvaluationContext?,
    ): ProviderEvaluation<Value> {
        val defaultJson = valueToJson(defaultValue)
        val evaluated =
            flagShipClient.getJson(
                key,
                defaultJson,
                context?.getTargetingKey() ?: "",
                context.sanitizeEvaluationContext(),
            )

        if (evaluated.value == defaultJson) {
            return mapEvaluation(
                EvaluationResult(
                    value = defaultValue,
                    reason = evaluated.reason,
                    variant = evaluated.variant,
                    metadata = evaluated.metadata,
                ),
            )
        }

        val evaluatedValue =
            EvaluationResult(
                value =
                    jsonToValue(evaluated.value.toString()).run {
                        if (this is Value.Null) {
                            defaultValue
                        } else {
                            this
                        }
                    },
                reason = evaluated.reason,
                variant = evaluated.variant,
                metadata = evaluated.metadata,
            )

        return mapEvaluation(evaluatedValue)
    }

    override fun getStringEvaluation(
        key: String,
        defaultValue: String,
        context: EvaluationContext?,
    ): ProviderEvaluation<String> =
        mapEvaluation(
            flagShipClient.getString(
                key,
                defaultValue,
                context?.getTargetingKey() ?: "",
                context.sanitizeEvaluationContext(),
            ),
        )

    override suspend fun initialize(initialContext: EvaluationContext?) = Unit

    override suspend fun onContextSet(
        oldContext: EvaluationContext?,
        newContext: EvaluationContext,
    ) {
        flagShipClient.onContextChange(
            oldContext.sanitizeEvaluationContext(),
            newContext.sanitizeEvaluationContext(),
        )
    }

    override fun shutdown() = flagShipClient.shutDown()

    private fun <T> mapEvaluation(result: EvaluationResult<T>): ProviderEvaluation<T> {
        var metadata: EvaluationMetadata? = null
        result.metadata.forEach { (K, V) ->
            val builder = EvaluationMetadata.builder()
            when (V) {
                is String -> builder.putString(K, V)
                is Int -> builder.putInt(K, V)
                is Double -> builder.putDouble(K, V)
                is Boolean -> builder.putBoolean(K, V)
                else -> builder.putString(K, V.toString())
            }
            metadata = builder.build()
        }

        return ProviderEvaluation(
            value = result.value,
            variant = result.variant,
            reason = result.reason.name,
            metadata = metadata ?: EvaluationMetadata.EMPTY,
        )
    }

    companion object {
        private val instanceMap = mutableMapOf<String, FlagshipProvider>()

        fun getInstance(
            domain: String,
            config: FlagShipConfig,
        ): FlagshipProvider {
            val instance = instanceMap[domain]
            if (instance == null) {
                FlagshipProvider(domain, config).also {
                    instanceMap[domain] = it
                }
            }
            return instance!!
        }
    }
}
