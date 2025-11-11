package com.flagship.sdk.plugins.storage.cache

import com.flagship.sdk.core.contracts.ICache
import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe in-memory cache implementation scoped to a specific namespace.
 *
 * @param namespace The namespace for this cache instance
 */
class InMemoryCache(
    private val namespace: String,
) : ICache {
    private val cache = ConcurrentHashMap<String, Any>()

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String): T? {
        val namespacedKey = createNamespacedKey(key)
        return cache[namespacedKey] as? T
    }

    override fun <T> put(
        key: String,
        value: T,
    ) {
        val namespacedKey = createNamespacedKey(key)
        if (value != null) {
            cache[namespacedKey] = value as Any
        } else {
            cache.remove(namespacedKey)
        }
    }

    override fun putAll(map: Map<String, Any>) {
        map.forEach { (key, value) ->
            put(key, value)
        }
    }

    override fun invalidateNamespace() {
        val namespacePrefix = "$namespace:"
        val keysToRemove =
            cache.keys.filter { key ->
                key.startsWith(namespacePrefix)
            }
        keysToRemove.forEach { key ->
            cache.remove(key)
        }
    }

    private fun createNamespacedKey(key: String): String = "$namespace:$key"
}
