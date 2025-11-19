package com.flagship.sdk.plugins.storage

import com.flagship.sdk.core.contracts.ICache
import com.flagship.sdk.plugins.storage.sharedPref.SharedPreferencesKeys

class TimestampUtility(
    private val persistentCache: ICache,
) {
    fun getStoredTimestamp(): Long? {
        return persistentCache.get<Long>(SharedPreferencesKeys.FeatureFlags.LAST_SYNC_TIMESTAMP_IN_MILLIS)
    }

    fun storeTimestamp(timestamp: Long) {
        persistentCache.put(
            SharedPreferencesKeys.FeatureFlags.LAST_SYNC_TIMESTAMP_IN_MILLIS,
            timestamp,
        )
    }

    fun hasTimestampChanged(newTimestamp: Long): Boolean {
        val storedTimestamp = getStoredTimestamp() ?: return true
        return newTimestamp != storedTimestamp
    }
}

