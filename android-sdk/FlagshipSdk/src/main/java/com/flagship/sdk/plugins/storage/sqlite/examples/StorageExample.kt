package com.flagship.sdk.plugins.storage.sqlite.examples

import android.content.Context
import com.flagship.sdk.core.contracts.IStore
import com.flagship.sdk.plugins.storage.sqlite.DatabaseBuilder
import com.flagship.sdk.plugins.storage.sqlite.SQLiteStore
import com.flagship.sdk.plugins.storage.sqlite.entities.ConfigSnapshot
import kotlinx.coroutines.Dispatchers

/**
 * Example usage of the Room-based config storage system.
 *
 * This shows how to integrate the storage components into your SDK.
 */
class StorageExample {
    /**
     * Initialize the storage system with a given Android context.
     */
    fun initializeStorage(context: Context): IStore<ConfigSnapshot> {
        // Build the database with migration support
        val database = DatabaseBuilder.build(context)

        // Create the repository with IO dispatcher
        return SQLiteStore(
            dao = database.configDao(),
            io = Dispatchers.IO,
        )
    }

    /**
     * Example of storing a feature flags snapshot.
     */
    suspend fun storeFeatureFlags(
        repository: IStore<ConfigSnapshot>,
        namespace: String = "default",
        flagsJson: String,
        version: String? = null,
        etag: String? = null,
    ) {
        val currentTime = System.currentTimeMillis()

        repository.replace(
            ConfigSnapshot(
                namespace = namespace,
                json = flagsJson,
                version = version,
                etag = etag,
                createdAt = currentTime,
            ),
        )
    }

    /**
     * Example of retrieving the active feature flags.
     */
    suspend fun getActiveFeatureFlags(
        repository: IStore<ConfigSnapshot>,
        namespace: String = "default",
    ): ConfigSnapshot? = repository.current(namespace)
}
