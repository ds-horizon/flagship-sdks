package com.flagship.sdk.plugins.storage.sqlite.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.flagship.sdk.plugins.storage.sqlite.entities.ConfigSnapshot

@Dao
interface ConfigDao {
    @Query("SELECT * FROM config_snapshots WHERE namespace = :ns AND isActive = 1 LIMIT 1")
    suspend fun activeSnapshot(ns: String): ConfigSnapshot?

    @Insert
    suspend fun insertSnapshot(snapshot: ConfigSnapshot): Long

    @Query("UPDATE config_snapshots SET isActive = 0 WHERE namespace = :ns")
    suspend fun deactivateAll(ns: String)

    @Query(
        """
        DELETE FROM config_snapshots 
        WHERE namespace = :ns AND id NOT IN (
            SELECT id FROM config_snapshots WHERE namespace = :ns ORDER BY createdAt DESC LIMIT :keep
        )
    """,
    )
    suspend fun gcSnapshots(
        ns: String,
        keep: Int,
    )

    @Transaction
    suspend fun activateSnapshot(
        ns: String,
        snapshot: ConfigSnapshot,
        historyToKeep: Int = 3,
    ): Long {
        deactivateAll(ns)
        val id = insertSnapshot(snapshot.copy(isActive = true))
        gcSnapshots(ns, historyToKeep)
        return id
    }
}
