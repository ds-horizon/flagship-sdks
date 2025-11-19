package com.flagship.sdk.plugins

import android.util.Log
import com.flagship.sdk.core.contracts.ICache
import com.flagship.sdk.core.contracts.IRepository
import com.flagship.sdk.core.contracts.IStore
import com.flagship.sdk.core.contracts.ITransport
import com.flagship.sdk.core.models.Feature
import com.flagship.sdk.core.models.FeatureFlagsSchema
import com.flagship.sdk.plugins.storage.TimestampUtility
import com.flagship.sdk.plugins.storage.sqlite.entities.ConfigSnapshot
import com.flagship.sdk.plugins.storage.sqlite.utility.JsonUtility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class Repository(
    private val cache: ICache,
    private val configCache: ICache,
    private val persistentCache: ICache,
    private val store: IStore<ConfigSnapshot>,
    private val transport: ITransport,
    private val scope: CoroutineScope,
) : IRepository {
    private val timestampUtility = TimestampUtility(persistentCache)

    override fun init() {
        scope.launch {
            val snapShot = store.current()
            if (snapShot != null) {
                val features = JsonUtility.fromJson<FeatureFlagsSchema>(snapShot.json).features
                configCache.putAll(features.associateBy { it.key })
            }
        }
    }

    suspend fun syncFlags() {
        try {
            val storedTimestamp = timestampUtility.getStoredTimestamp()

            if (storedTimestamp == null) {
                syncWithFullConfig()
            } else {
                syncWithTimeOnlyCheck()
            }
        } catch (e: Exception) {
        }
    }

    override suspend fun fetchConfig(isFirstTime: Boolean) {
        syncFlags()
    }

    private suspend fun syncWithFullConfig() {
        try {
            Log.d("Flagship", "API CALLED: full")
            val fullResult = transport.fetchConfig("full")

            when (fullResult) {
                is com.flagship.sdk.core.models.Result.Error -> {
                }
                is com.flagship.sdk.core.models.Result.Success<FeatureFlagsSchema> -> {
                    val newTimestamp = extractTimestampFromHeaders(fullResult.headers)

                    if (newTimestamp != null) {
                        Log.d("Flagship", "CONFIG CHANGED")
                        
                        cache.invalidateNamespace()
                        configCache.invalidateNamespace()
                        configCache.putAll(fullResult.data.features.associateBy { it.key })
                        
                        val snapShot =
                            ConfigSnapshot(
                                namespace = "default",
                                createdAt = System.currentTimeMillis(),
                                etag = newTimestamp.toString(),
                                json = JsonUtility.toJson(fullResult.data),
                            )
                        store.replace(snapShot)
                        timestampUtility.storeTimestamp(newTimestamp)
                    }
                }
                else -> {
                }
            }
        } catch (e: Exception) {
        }
    }

    private suspend fun syncWithTimeOnlyCheck() {
        Log.d("Flagship", "API CALLED: time-only")
        val timeOnlyResult = transport.fetchConfig("time-only")

        when (timeOnlyResult) {
            is com.flagship.sdk.core.models.Result.Error -> {
            }

            is com.flagship.sdk.core.models.Result.Success<FeatureFlagsSchema> -> {
                val newTimestamp = extractTimestampFromHeaders(timeOnlyResult.headers)

                if (newTimestamp != null) {
                    if (timestampUtility.hasTimestampChanged(newTimestamp)) {
                        syncWithFullConfig()
                    } else {
                        Log.d("Flagship", "CONFIG UNCHANGED")
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


