package com.flagship.sdk.core.contracts

import com.flagship.sdk.core.models.Feature

interface IRepository {
    fun init()

    suspend fun fetchConfig(isFirstTime: Boolean = false)

    fun getFlagConfig(key: String): Feature?

    fun onContextChanged(
        oldContext: Map<String, Any?>,
        newContext: Map<String, Any?>,
    )

    fun shutDown()
}
