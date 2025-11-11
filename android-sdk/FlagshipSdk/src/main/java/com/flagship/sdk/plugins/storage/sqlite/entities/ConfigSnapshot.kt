package com.flagship.sdk.plugins.storage.sqlite.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "config_snapshots",
    indices = [Index(value = ["namespace", "isActive"])],
)
data class ConfigSnapshot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val namespace: String,
    val version: String? = null,
    val etag: String? = null,
    val createdAt: Long,
    val isActive: Boolean = false,
    val json: String,
)
