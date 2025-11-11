package com.flagship.sdk.storage

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.flagship.sdk.plugins.storage.sqlite.ConfigDatabase
import com.flagship.sdk.plugins.storage.sqlite.DatabaseBuilder
import com.flagship.sdk.plugins.storage.sqlite.SQLiteStore
import com.flagship.sdk.plugins.storage.sqlite.entities.ConfigSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SQLiteStoreTest {
    private lateinit var db: ConfigDatabase
    private lateinit var repo: SQLiteStore

    @Before
    fun setUp() {
        val ctx: Context = ApplicationProvider.getApplicationContext()
        db = DatabaseBuilder.buildInMemory(ctx)
        repo = SQLiteStore(db.configDao(), Dispatchers.IO)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun activate_and_query_snapshot() =
        runBlocking {
            val ns = "default"
            val now = 1_700_000_000_000
            val json = """{"flags":[{"key":"paywall","enabled":true}]}"""

            repo.replace(
                ConfigSnapshot(
                    namespace = ns,
                    json = json,
                    version = "v1",
                    etag = "W/\"123\"",
                    createdAt = now,
                ),
            )

            val activeJson = repo.current(ns)?.json
            assertEquals(json, activeJson)
        }
}
