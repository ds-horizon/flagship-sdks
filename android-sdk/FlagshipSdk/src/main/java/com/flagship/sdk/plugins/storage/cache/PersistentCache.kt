package com.flagship.sdk.plugins.storage.cache

import com.flagship.sdk.core.contracts.ICache
import com.flagship.sdk.plugins.storage.sharedPref.ISharedPreferencesManager
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * Persistent cache implementation using SharedPreferences as the underlying storage.
 * Supports storing primitive types directly and complex objects via JSON serialization.
 *
 * @param sharedPreferencesManager The SharedPreferences manager for data persistence
 * @param namespace The namespace for this cache instance to avoid key collisions
 */
class PersistentCache(
    private val namespace: String,
    private val sharedPreferencesManager: ISharedPreferencesManager,
) : ICache {
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = false
        }

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String): T? {
        val namespacedKey = createNamespacedKey(key)

        return try {
            // Check if key exists first
            if (!sharedPreferencesManager.contains(namespacedKey)) {
                return null
            }

            // Try to get the type indicator first
            val typeKey = "${namespacedKey}_type"
            val type = sharedPreferencesManager.getString(typeKey, "")

            when (type) {
                "String" -> sharedPreferencesManager.getString(namespacedKey, "") as? T
                "Int" -> sharedPreferencesManager.getInt(namespacedKey, 0) as? T
                "Boolean" -> sharedPreferencesManager.getBoolean(namespacedKey, false) as? T
                "Long" -> sharedPreferencesManager.getLong(namespacedKey, 0L) as? T
                "Float" -> sharedPreferencesManager.getFloat(namespacedKey, 0f) as? T
                "Double" -> {
                    // SharedPreferences doesn't support Double, so we store it as Float
                    sharedPreferencesManager.getFloat(namespacedKey, 0f).toDouble() as? T
                }
                "JSON" -> {
                    // Complex object stored as JSON
                    val jsonString = sharedPreferencesManager.getString(namespacedKey, "")
                    if (jsonString.isEmpty()) {
                        null
                    } else {
                        json.decodeFromString(serializer<Any>(), jsonString) as? T
                    }
                }
                else -> {
                    // Fallback: try to get as string for backward compatibility
                    val value = sharedPreferencesManager.getString(namespacedKey, "")
                    if (value.isEmpty()) null else value as? T
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun <T> put(
        key: String,
        value: T,
    ) {
        val namespacedKey = createNamespacedKey(key)
        val typeKey = "${namespacedKey}_type"

        if (value == null) {
            sharedPreferencesManager.remove(namespacedKey)
            sharedPreferencesManager.remove(typeKey)
            return
        }

        try {
            when (value) {
                is String -> {
                    sharedPreferencesManager.putString(namespacedKey, value)
                    sharedPreferencesManager.putString(typeKey, "String")
                }
                is Int -> {
                    sharedPreferencesManager.putInt(namespacedKey, value)
                    sharedPreferencesManager.putString(typeKey, "Int")
                }
                is Boolean -> {
                    sharedPreferencesManager.putBoolean(namespacedKey, value)
                    sharedPreferencesManager.putString(typeKey, "Boolean")
                }
                is Long -> {
                    sharedPreferencesManager.putLong(namespacedKey, value)
                    sharedPreferencesManager.putString(typeKey, "Long")
                }
                is Float -> {
                    sharedPreferencesManager.putFloat(namespacedKey, value)
                    sharedPreferencesManager.putString(typeKey, "Float")
                }
                is Double -> {
                    // SharedPreferences doesn't support Double natively, store as Float
                    sharedPreferencesManager.putFloat(namespacedKey, value.toFloat())
                    sharedPreferencesManager.putString(typeKey, "Double")
                }
                else -> {
                    // Serialize complex objects as JSON
                    val jsonString = json.encodeToString(serializer<Any>(), value as Any)
                    sharedPreferencesManager.putString(namespacedKey, jsonString)
                    sharedPreferencesManager.putString(typeKey, "JSON")
                }
            }
        } catch (e: SerializationException) {
            throw IllegalArgumentException("Failed to serialize value for key: $key", e)
        }
    }

    override fun putAll(map: Map<String, Any>) {
        map.forEach { (key, value) ->
            put(key, value)
        }
    }

    override fun invalidateNamespace() {
        val namespacePrefix = "$namespace:"
        val allKeys = sharedPreferencesManager.getAllKeys()

        allKeys
            .filter { key ->
                key.startsWith(namespacePrefix)
            }.forEach { key ->
                sharedPreferencesManager.remove(key)
            }
    }

    private fun createNamespacedKey(key: String): String = "$namespace:$key"
}
