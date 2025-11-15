package com.flagship.sdk.plugins

import android.util.Log
import com.flagship.sdk.core.contracts.ICache
import com.flagship.sdk.core.contracts.IRepository
import com.flagship.sdk.core.contracts.IStore
import com.flagship.sdk.core.contracts.ITransport
import com.flagship.sdk.core.models.Feature
import com.flagship.sdk.core.models.FeatureFlagsSchema
import com.flagship.sdk.facade.coroutines.DispatcherProvider
import com.flagship.sdk.plugins.storage.sharedPref.SharedPreferencesKeys
import com.flagship.sdk.plugins.storage.sqlite.entities.ConfigSnapshot
import com.flagship.sdk.plugins.storage.sqlite.utility.JsonUtility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class Repository(
    private val cache: ICache,
    private val configCache: ICache,
    private val persistentCache: ICache,
    private val store: IStore<ConfigSnapshot>,
    private val transport: ITransport,
    private val scope: CoroutineScope,
    private val dispatcher: DispatcherProvider,
    private val refreshInterval: Long = 30000,
) : IRepository {
    override fun init() {
        scope.launch {
            val snapShot = store.current()
            if (snapShot != null) {
                val features = JsonUtility.fromJson<FeatureFlagsSchema>(snapShot.json).features
                configCache.putAll(features.associateBy { it.key })
            }
            startPolling()
        }
    }

    private suspend fun startPolling() {
        fetchConfig(true)
        while (scope.isActive) {
            delay(refreshInterval)
            fetchConfig(false)
        }
    }

    override suspend fun fetchConfig(isFirstTime: Boolean) {
        val storedTimestamp = persistentCache.get<Long>(SharedPreferencesKeys.FeatureFlags.LAST_SYNC_TIMESTAMP_IN_MILLIS)

        if (storedTimestamp == null) {
            syncWithFullConfig()
        } else {
            syncWithTimeOnlyCheck()
        }
    }

    private suspend fun syncWithFullConfig() {
        val fullResult = transport
            .fetchConfig("full")
            .first { it !is com.flagship.sdk.core.models.Result.Loading }

        when (fullResult) {
            is com.flagship.sdk.core.models.Result.Error -> {
            }
            is com.flagship.sdk.core.models.Result.Success<FeatureFlagsSchema> -> {
                val timestamp = extractTimestampFromHeaders(fullResult.headers)

                if (timestamp != null) {
                    Log.d("Flagship", "CONFIG CHANGED")
                    
                    cache.invalidateNamespace()
                    configCache.putAll(fullResult.data.features.associateBy { it.key })
                    
                    val snapShot =
                        ConfigSnapshot(
                            namespace = "default",
                            createdAt = System.currentTimeMillis(),
                            etag = timestamp.toString(),
                            json = JsonUtility.toJson(fullResult.data),
                        )
                    store.replace(snapShot)
                    
                    persistentCache.put(
                        SharedPreferencesKeys.FeatureFlags.LAST_SYNC_TIMESTAMP_IN_MILLIS,
                        timestamp,
                    )
                }
            }
            else -> Unit
        }
    }

    private suspend fun syncWithTimeOnlyCheck() {
        val timeOnlyResult = transport
            .fetchConfig("time-only")
            .first { it !is com.flagship.sdk.core.models.Result.Loading }

        when (timeOnlyResult) {
            is com.flagship.sdk.core.models.Result.Error -> {
            }

            is com.flagship.sdk.core.models.Result.Success<FeatureFlagsSchema> -> {
                val newTimestamp = extractTimestampFromHeaders(timeOnlyResult.headers)
                val storedTimestamp = persistentCache.get<Long>(
                    SharedPreferencesKeys.FeatureFlags.LAST_SYNC_TIMESTAMP_IN_MILLIS
                )

                if (newTimestamp != null && storedTimestamp != null) {
                    val timestampChanged = newTimestamp != storedTimestamp

                    if (timestampChanged) {
                        syncWithFullConfig()
                    } else {
                        Log.d("Flagship", "+++ CONFIG UNCHANGED")
                    }
                }
            }

            else -> Unit
        }
    }

    private fun extractTimestampFromHeaders(headers: Map<String, List<String>>): Long? {
        val updatedAtHeader = headers["updated-at"]?.firstOrNull()
        return updatedAtHeader?.toLongOrNull()?.let {
            java.util.concurrent.TimeUnit.SECONDS.toMillis(it)
        }
    }

    override fun onContextChanged(
        oldContext: Map<String, Any?>,
        newContext: Map<String, Any?>,
    ) {
        cache.invalidateNamespace()
    }

    override fun getFlagConfig(key: String): Feature? = configCache.get(key)

    override fun shutDown() {
        cache.invalidateNamespace()
    }
}


