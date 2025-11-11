package com.flagship.sdk.plugins.storage.sqlite

import androidx.room.Database
import androidx.room.RoomDatabase
import com.flagship.sdk.plugins.storage.sqlite.dao.ConfigDao
import com.flagship.sdk.plugins.storage.sqlite.entities.ConfigSnapshot

@Database(
    entities = [ConfigSnapshot::class],
    version = 1,
    exportSchema = true,
)
abstract class ConfigDatabase : RoomDatabase() {
    abstract fun configDao(): ConfigDao
}
