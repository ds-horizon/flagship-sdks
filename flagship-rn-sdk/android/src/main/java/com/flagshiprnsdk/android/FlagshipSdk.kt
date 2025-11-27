package com.flagshiprnsdk.android

import android.app.Application
import android.content.Context
import android.util.Log
import com.flagship.sdk.facade.FlagShipConfig
import com.flagship.sdk.facade.FlagshipProvider
import com.flagshiprnsdk.core.FlagshipState
import dev.openfeature.kotlin.sdk.ImmutableContext
import dev.openfeature.kotlin.sdk.OpenFeatureAPI
import dev.openfeature.kotlin.sdk.Value

object FlagshipSdk {
    private const val TAG = "FlagshipSdk"

    fun initialize(
        context: Context,
        baseUrl: String,
        flagshipApiKey: String,
        refreshIntervalSeconds: Long = 10L
    ): Boolean {
        if (FlagshipState.isInitialized()) {
            return true
        }

        return try {
            val application = context.applicationContext as? Application
                ?: throw IllegalStateException("Unable to get Application context")

            val refreshIntervalMs = refreshIntervalSeconds * 1000

            val flagShipConfig = FlagShipConfig(
                applicationContext = application,
                baseUrl = baseUrl,
                flagshipApiKey = flagshipApiKey,
                refreshInterval = refreshIntervalMs
            )

            val provider = FlagshipProvider("native", flagShipConfig)
            OpenFeatureAPI.setProvider(provider)

            FlagshipState.markInitialized()
            Log.i(TAG, "Initialized with baseUrl=$baseUrl, refreshInterval=${refreshIntervalSeconds}s")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Initialization failed: ${e.message}", e)
            false
        }
    }

    fun setContext(targetingKey: String, context: Map<String, Any?> = emptyMap()): Boolean {
        return try {
            val contextAttributes = mutableMapOf<String, Value>()

            context.forEach { (key, value) ->
                val convertedValue = convertAnyToValue(value)
                contextAttributes[key] = convertedValue
            }

            OpenFeatureAPI.setEvaluationContext(
                ImmutableContext(
                    targetingKey = targetingKey,
                    contextAttributes
                )
            )

            Log.d(TAG, "Context set for targetingKey=$targetingKey")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set context: ${e.message}", e)
            false
        }
    }

    fun getBooleanValue(key: String, defaultValue: Boolean): Boolean {
        return try {
            val client = OpenFeatureAPI.getClient()
            client.getBooleanValue(key, defaultValue)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get boolean value for key=$key: ${e.message}", e)
            defaultValue
        }
    }

    fun getStringValue(key: String, defaultValue: String): String {
        return try {
            val client = OpenFeatureAPI.getClient()
            client.getStringValue(key, defaultValue)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get string value for key=$key: ${e.message}", e)
            defaultValue
        }
    }

    fun getIntegerValue(key: String, defaultValue: Int): Int {
        return try {
            val client = OpenFeatureAPI.getClient()
            client.getIntegerValue(key, defaultValue)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get integer value for key=$key: ${e.message}", e)
            defaultValue
        }
    }

    fun getDoubleValue(key: String, defaultValue: Double): Double {
        return try {
            val client = OpenFeatureAPI.getClient()
            client.getDoubleValue(key, defaultValue)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get double value for key=$key: ${e.message}", e)
            defaultValue
        }
    }

    fun getObjectValue(key: String, defaultValue: Map<String, Any?> = emptyMap()): Map<String, Any?> {
        return try {
            val client = OpenFeatureAPI.getClient()
            val defaultOpenFeatureValue = convertMapToValue(defaultValue)
            val result = client.getObjectValue(key, defaultOpenFeatureValue)
            convertValueToMap(result)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get object value for key=$key: ${e.message}", e)
            defaultValue
        }
    }

    fun isInitialized(): Boolean = FlagshipState.isInitialized()

    private fun convertAnyToValue(value: Any?): Value {
        return when (value) {
            null -> Value.Null
            is Boolean -> Value.Boolean(value)
            is Int -> Value.Integer(value)
            is Long -> Value.Integer(value.toInt())
            is Double -> Value.Double(value)
            is Float -> Value.Double(value.toDouble())
            is String -> Value.String(value)
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                convertMapToValue(value as Map<String, Any?>)
            }
            is List<*> -> {
                val list = value.map { convertAnyToValue(it) }
                Value.List(list)
            }
            else -> Value.String(value.toString())
        }
    }

    private fun convertMapToValue(map: Map<String, Any?>): Value {
        val result = mutableMapOf<String, Value>()
        map.forEach { (key, value) ->
            result[key] = convertAnyToValue(value)
        }
        return Value.Structure(result)
    }

    private fun convertValueToMap(value: Value): Map<String, Any?> {
        return when (value) {
            is Value.Structure -> {
                val result = mutableMapOf<String, Any?>()
                value.structure.forEach { (key, v) ->
                    result[key] = convertValueToAny(v)
                }
                result
            }
            else -> mapOf("value" to convertValueToAny(value))
        }
    }

    private fun convertValueToAny(value: Value): Any? {
        return when (value) {
            is Value.Null -> null
            is Value.Boolean -> value.boolean
            is Value.Integer -> value.integer
            is Value.Double -> value.double
            is Value.String -> value.string
            is Value.List -> value.list.map { convertValueToAny(it) }
            is Value.Structure -> {
                val result = mutableMapOf<String, Any?>()
                value.structure.forEach { (k, v) ->
                    result[k] = convertValueToAny(v)
                }
                result
            }
            else -> value.toString()
        }
    }
}

