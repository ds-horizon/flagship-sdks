package com.flagship.sdk.plugins.storage.sqlite.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room database migrations for ConfigDatabase.
 *
 * This file provides migration scaffolding for future schema changes.
 * When adding new tables (like FlagsIndex), add migrations here to avoid
 * destructive database recreation.
 */
object DatabaseMigrations {
    /**
     * Drop and reload migration strategy.
     * This migration drops all existing tables and recreates the entire schema.
     * Use this when you need to make significant schema changes that are difficult
     * to migrate incrementally, or when data preservation is not critical.
     *
     * WARNING: This will destroy all existing data in the database.
     */
    fun createDropAndReloadMigration(
        fromVersion: Int,
        toVersion: Int,
    ): Migration =
        object : Migration(fromVersion, toVersion) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Drop all existing tables
                dropAllTables(database)

                // Recreate the current schema
                recreateCurrentSchema(database)
            }
        }

    /**
     * Drops all known tables in the database.
     * Add new table names here as they are added to the schema.
     */
    private fun dropAllTables(database: SupportSQLiteDatabase) {
        // Drop all known tables
        database.execSQL("DROP TABLE IF EXISTS config_snapshots")
        database.execSQL("DROP TABLE IF EXISTS flags_index")

        // Drop any indexes that might remain
        database.execSQL("DROP INDEX IF EXISTS index_config_snapshots_namespace_isActive")
        database.execSQL("DROP INDEX IF EXISTS index_flags_index_snapshot_id_flag_key")
    }

    /**
     * Recreates the current database schema.
     * Update this method to match the current Room entity definitions.
     */
    private fun recreateCurrentSchema(database: SupportSQLiteDatabase) {
        // Create config_snapshots table (current schema)
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS config_snapshots (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                namespace TEXT NOT NULL,
                version TEXT,
                etag TEXT,
                createdAt INTEGER NOT NULL,
                isActive INTEGER NOT NULL,
                json TEXT NOT NULL
            )
            """.trimIndent(),
        )

        // Create indexes for config_snapshots
        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_config_snapshots_namespace_isActive 
            ON config_snapshots (namespace, isActive)
            """.trimIndent(),
        )

        // Add future tables here as they are added to the schema
        // Example: flags_index table (uncomment when needed)

        /*
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS flags_index (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                snapshot_id INTEGER NOT NULL,
                flag_key TEXT NOT NULL,
                flag_value TEXT,
                created_at INTEGER NOT NULL,
                FOREIGN KEY(snapshot_id) REFERENCES config_snapshots(id) ON DELETE CASCADE
            )
        """.trimIndent())

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_flags_index_snapshot_id_flag_key
            ON flags_index (snapshot_id, flag_key)
        """.trimIndent())
         */
    }

    /*
     * Example migration from version 1 to 2.
     * Uncomment and modify when you need to add the FlagsIndex table.
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Example: Adding FlagsIndex table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS flags_index (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    snapshot_id INTEGER NOT NULL,
                    flag_key TEXT NOT NULL,
                    flag_value TEXT,
                    created_at INTEGER NOT NULL,
                    FOREIGN KEY(snapshot_id) REFERENCES config_snapshots(id) ON DELETE CASCADE
                )
            """.trimIndent())

            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_flags_index_snapshot_id_flag_key
                ON flags_index (snapshot_id, flag_key)
            """.trimIndent())
        }
    }
     */

    /**
     * Drop and reload migration from version 1 to 2.
     * Use this when you need to make significant schema changes.
     */
    val DROP_AND_RELOAD_1_2 = createDropAndReloadMigration(1, 2)

    /**
     * Get all available migrations.
     * Add new migrations to this array as they are created.
     */
    fun getAllMigrations(): Array<Migration> =
        arrayOf(
            // MIGRATION_1_2,  // Incremental migration (recommended when possible)
            DROP_AND_RELOAD_1_2, // Drop and reload migration (use when data loss is acceptable)
        )

    /**
     * Get only incremental migrations (preserves data).
     * Use this for production deployments where data preservation is critical.
     */
    fun getIncrementalMigrations(): Array<Migration> =
        arrayOf(
            // MIGRATION_1_2,  // Add incremental migrations here
        )

    /**
     * Get only drop and reload migrations (destroys data).
     * Use this for development, testing, or when data loss is acceptable.
     */
    fun getDropAndReloadMigrations(): Array<Migration> =
        arrayOf(
            DROP_AND_RELOAD_1_2,
        )
}
