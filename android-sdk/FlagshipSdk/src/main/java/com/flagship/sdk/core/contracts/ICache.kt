package com.flagship.sdk.core.contracts

interface ICache {
    fun <T> get(key: String): T?

    fun <T> put(
        key: String,
        value: T,
    )

    fun putAll(map: Map<String, Any>)

    fun invalidateNamespace()
}
