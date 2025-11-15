package com.flagship.sdk.core.registry

import android.content.Context
import com.flagship.sdk.core.contracts.ICache
import com.flagship.sdk.core.contracts.IEvaluator
import com.flagship.sdk.core.contracts.IRepository
import com.flagship.sdk.core.contracts.IStore
import com.flagship.sdk.core.contracts.ITransport
import com.flagship.sdk.facade.FlagShipConfig
import com.flagship.sdk.facade.coroutines.DispatcherProvider
import com.flagship.sdk.plugins.Repository
import com.flagship.sdk.plugins.evaluation.EdgeEvaluator
import com.flagship.sdk.plugins.storage.cache.InMemoryCache
import com.flagship.sdk.plugins.storage.cache.PersistentCache
import com.flagship.sdk.plugins.storage.sharedPref.SharedPreferencesFactory
import com.flagship.sdk.plugins.storage.sqlite.DatabaseBuilder
import com.flagship.sdk.plugins.storage.sqlite.SQLiteStore
import com.flagship.sdk.plugins.storage.sqlite.entities.ConfigSnapshot
import com.flagship.sdk.plugins.transport.http.FlagshipHttpTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class Registry(
    private val domain: String,
    private val config: FlagShipConfig,
    private val scope: CoroutineScope,
    private val dispatchers: DispatcherProvider,
) {
    private lateinit var flagCache: ICache
    private lateinit var flagConfigCache: ICache
    private lateinit var persistentCache: ICache
    private var repository: IRepository? = null
    private lateinit var store: IStore<ConfigSnapshot>
    private lateinit var transport: ITransport
    private var evaluator: IEvaluator? = null

    fun create() {
        flagCache = InMemoryCache(domain)
        flagConfigCache = InMemoryCache(domain)
        persistentCache = PersistentCache(domain, SharedPreferencesFactory.create(config.applicationContext))
        transport =
            FlagshipHttpTransport.createForTesting(
                baseUrl = config.baseUrl,
                pollingInterval = config.refreshInterval,
                tenantId = config.tenantId,
                enableLogging = true,
                mockInterceptors = emptyList(),
            )
        store = initializeStorage(config.applicationContext)
        repository =
            Repository(
                cache = flagCache,
                configCache = flagConfigCache,
                persistentCache = persistentCache,
                store = store,
                transport = transport,
                scope = scope,
                dispatcher = dispatchers,
                refreshInterval = config.refreshInterval,
            )
        evaluator = EdgeEvaluator(flagCache, persistentCache)
    }

    fun getEvaluator(): IEvaluator? = evaluator

    fun getRepository(): IRepository? = repository

    private fun initializeStorage(context: Context): IStore<ConfigSnapshot> {
        // Build the database with migration support
        val database = DatabaseBuilder.build(context)

        // Create the repository with IO dispatcher
        return SQLiteStore(
            dao = database.configDao(),
            io = Dispatchers.IO,
        )
    }

    fun shutdown() {
    }
}
