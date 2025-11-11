package com.flagship.sdk.plugins.storage.sqlite

import com.flagship.sdk.core.contracts.IStore
import com.flagship.sdk.plugins.storage.sqlite.dao.ConfigDao
import com.flagship.sdk.plugins.storage.sqlite.entities.ConfigSnapshot
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class SQLiteStore(
    private val dao: ConfigDao,
    private val io: CoroutineDispatcher,
) : IStore<ConfigSnapshot> {
    override suspend fun current(namespace: String): ConfigSnapshot? =
        withContext(io) {
            dao.activeSnapshot(namespace)
        }

    override suspend fun replace(value: ConfigSnapshot): Long =
        withContext(io) {
            dao.activateSnapshot(value.namespace, value)
        }
}
