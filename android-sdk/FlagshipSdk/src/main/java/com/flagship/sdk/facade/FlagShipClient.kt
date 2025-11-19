package com.flagship.sdk.facade

import android.util.Log
import com.flagship.sdk.core.contracts.IEvaluator
import com.flagship.sdk.core.contracts.IRepository
import com.flagship.sdk.core.models.EvaluationContext
import com.flagship.sdk.core.models.EvaluationResult
import com.flagship.sdk.core.models.Reason
import com.flagship.sdk.core.registry.Registry
import com.flagship.sdk.facade.coroutines.SdkScope
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class FlagShipClient(
    domain: String,
    config: FlagShipConfig,
) {
    private val registry: Registry?
    private var repository: IRepository? = null
    private var evaluator: IEvaluator? = null

    init {
        SdkScope.init()
        registry = Registry(domain, config, SdkScope.scope)
        registry.create()
        repository =
            registry.getRepository()?.also {
                it.init()
            }
        evaluator = registry.getEvaluator()
        registry.startPolling()
        Log.d("Flagship", "SDK INITIALIZED")
    }

    fun getBoolean(
        key: String,
        defaultValue: Boolean = false,
        targetingKey: String,
        context: Map<String, Any?>,
    ): EvaluationResult<Boolean> {
        val config = repository?.getFlagConfig(key)
        return evaluator?.evaluateBoolean(
            key,
            defaultValue,
            config,
            EvaluationContext(targetingKey, context),
        ) ?: EvaluationResult(value = defaultValue, reason = Reason.UNKNOWN)
    }

    fun getString(
        key: String,
        defaultValue: String = "",
        targetingKey: String,
        context: Map<String, Any?>,
    ): EvaluationResult<String> {
        val config = repository?.getFlagConfig(key)
        return evaluator?.evaluateString(
            key,
            defaultValue,
            config,
            EvaluationContext(targetingKey, context),
        ) ?: EvaluationResult(value = defaultValue, reason = Reason.UNKNOWN)
    }

    fun getInt(
        key: String,
        defaultValue: Int = 0,
        targetingKey: String,
        context: Map<String, Any?>,
    ): EvaluationResult<Int> {
        val config = repository?.getFlagConfig(key)
        return evaluator?.evaluateInt(
            key,
            defaultValue,
            config,
            EvaluationContext(targetingKey, context),
        ) ?: EvaluationResult(value = defaultValue, reason = Reason.UNKNOWN)
    }

    fun getDouble(
        key: String,
        defaultValue: Double = 0.0,
        targetingKey: String,
        context: Map<String, Any?>,
    ): EvaluationResult<Double> {
        val config = repository?.getFlagConfig(key)
        return evaluator?.evaluateDouble(
            key,
            defaultValue,
            config,
            EvaluationContext(targetingKey, context),
        ) ?: EvaluationResult(value = defaultValue, reason = Reason.UNKNOWN)
    }

    fun <T> getObject(
        key: String,
        defaultValue: T,
        targetingKey: String,
        context: Map<String, Any?>,
    ): EvaluationResult<T> {
        val config = repository?.getFlagConfig(key)
        return evaluator?.evaluateObject(
            key,
            defaultValue,
            config,
            EvaluationContext(targetingKey, context),
        ) ?: EvaluationResult(value = defaultValue, reason = Reason.UNKNOWN)
    }

    fun getJson(
        key: String,
        defaultValue: String = "",
        targetingKey: String,
        context: Map<String, Any?>,
    ): EvaluationResult<String> {
        val config = repository?.getFlagConfig(key)
        return evaluator?.evaluateObject(
            key,
            defaultValue,
            config,
            EvaluationContext(targetingKey, context),
        ) ?: EvaluationResult(value = defaultValue, reason = Reason.UNKNOWN)
    }

    fun onContextChange(
        oldContext: Map<String, Any?>,
        newContext: Map<String, Any?>,
    ) {
        repository?.onContextChanged(oldContext, newContext)
        Log.d("Flagship", "CONTEXT SET")
    }

    fun shutDown() {
        SdkScope.cancelAll()
        registry?.shutdown()
    }

    // create singleton using object
    companion object {
        private var instanceMap = mutableMapOf<String, FlagShipClient>()

        fun getInstance(
            domain: String,
            config: FlagShipConfig,
        ): FlagShipClient {
            val instance = instanceMap[domain]
            if (instance == null) {
                FlagShipClient(domain, config).also {
                    instanceMap[domain] = it
                    return it
                }
            }
            return instance!!
        }
    }
}
