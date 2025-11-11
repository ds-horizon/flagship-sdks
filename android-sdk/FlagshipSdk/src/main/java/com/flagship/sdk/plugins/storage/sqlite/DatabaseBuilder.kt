package com.flagship.sdk.plugins.storage.sqlite

import android.content.Context
import androidx.room.Room
import com.flagship.sdk.plugins.storage.sqlite.migrations.DatabaseMigrations

/**
 * Utility object for building ConfigDatabase instances.
 *
 * This provides a centralized way to create database instances with proper
 * migration support, avoiding destructive database recreations.
 */
object DatabaseBuilder {
    /**
     * Migration strategy for database building.
     */
    enum class MigrationStrategy {
        /** Use incremental migrations that preserve existing data (recommended for production) */
        INCREMENTAL,

        /** Use drop and reload migrations that destroy all data (useful for development/testing) */
        DROP_AND_RELOAD,

        /** Use all available migrations (both incremental and drop/reload) */
        ALL,

        /** Fallback to destructive recreation if migrations fail */
        FALLBACK_DESTRUCTIVE,
    }

    /**
     * Creates a ConfigDatabase instance with migration support.
     *
     * @param context Android context
     * @param dbName Database name (defaults to "flagship_config.db")
     * @param strategy Migration strategy to use
     * @return ConfigDatabase instance ready for use
     */
    fun build(
        context: Context,
        dbName: String = "flagship_config.db",
        strategy: MigrationStrategy = MigrationStrategy.DROP_AND_RELOAD,
    ): ConfigDatabase =
        when (strategy) {
            MigrationStrategy.INCREMENTAL -> buildWithIncrementalMigrations(context, dbName)
            MigrationStrategy.DROP_AND_RELOAD -> buildWithDropAndReloadMigrations(context, dbName)
            MigrationStrategy.ALL -> buildWithAllMigrations(context, dbName)
            MigrationStrategy.FALLBACK_DESTRUCTIVE -> buildWithFallbackDestruction(context, dbName)
        }

    /**
     * Creates a ConfigDatabase with incremental migrations only.
     * This preserves existing data and is recommended for production.
     *
     * @param context Android context
     * @param dbName Database name
     * @return ConfigDatabase instance with incremental migrations
     */
    fun buildWithIncrementalMigrations(
        context: Context,
        dbName: String = "flagship_config.db",
    ): ConfigDatabase =
        Room
            .databaseBuilder(
                context,
                ConfigDatabase::class.java,
                dbName,
            ).addMigrations(*DatabaseMigrations.getIncrementalMigrations())
            .build()

    /**
     * Creates a ConfigDatabase with drop and reload migrations.
     * This destroys existing data and recreates the schema.
     * Useful for development, testing, or when data loss is acceptable.
     *
     * @param context Android context
     * @param dbName Database name
     * @return ConfigDatabase instance with drop and reload migrations
     */
    fun buildWithDropAndReloadMigrations(
        context: Context,
        dbName: String = "flagship_config.db",
    ): ConfigDatabase =
        Room
            .databaseBuilder(
                context,
                ConfigDatabase::class.java,
                dbName,
            ).addMigrations(*DatabaseMigrations.getDropAndReloadMigrations())
            .build()

    /**
     * Creates a ConfigDatabase with all available migrations.
     * This includes both incremental and drop/reload migrations.
     *
     * @param context Android context
     * @param dbName Database name
     * @return ConfigDatabase instance with all migrations
     */
    fun buildWithAllMigrations(
        context: Context,
        dbName: String = "flagship_config.db",
    ): ConfigDatabase =
        Room
            .databaseBuilder(
                context,
                ConfigDatabase::class.java,
                dbName,
            ).addMigrations(*DatabaseMigrations.getAllMigrations())
            .build()

    /**
     * Creates a ConfigDatabase with fallback to destructive recreation.
     * This will first try to use incremental migrations, but if they fail,
     * it will fall back to dropping and recreating the entire database.
     *
     * WARNING: This can result in data loss if migrations fail.
     *
     * @param context Android context
     * @param dbName Database name
     * @return ConfigDatabase instance with fallback destructive recreation
     */
    fun buildWithFallbackDestruction(
        context: Context,
        dbName: String = "flagship_config.db",
    ): ConfigDatabase =
        Room
            .databaseBuilder(
                context,
                ConfigDatabase::class.java,
                dbName,
            ).addMigrations(*DatabaseMigrations.getIncrementalMigrations())
            .fallbackToDestructiveMigration()
            .build()

    /**
     * Creates a ConfigDatabase that always destroys and recreates the database.
     * This is the most aggressive strategy and will always result in data loss.
     * Only use this for development, testing, or when you want a fresh start.
     *
     * @param context Android context
     * @param dbName Database name
     * @return ConfigDatabase instance that always recreates the schema
     */
    fun buildAlwaysDestructive(
        context: Context,
        dbName: String = "flagship_config.db",
    ): ConfigDatabase =
        Room
            .databaseBuilder(
                context,
                ConfigDatabase::class.java,
                dbName,
            ).fallbackToDestructiveMigration()
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()

    /**
     * Creates an in-memory database instance for testing.
     *
     * @param context Android context
     * @return In-memory ConfigDatabase instance
     */
    fun buildInMemory(context: Context): ConfigDatabase =
        Room
            .inMemoryDatabaseBuilder(
                context,
                ConfigDatabase::class.java,
            ).build()

    // Legacy method for backward compatibility
    @Deprecated(
        "Use build() with MigrationStrategy parameter instead",
        ReplaceWith("build(context, dbName, MigrationStrategy.INCREMENTAL)"),
    )
    fun buildLegacy(
        context: Context,
        dbName: String = "flagship_config.db",
    ): ConfigDatabase = build(context, dbName, MigrationStrategy.INCREMENTAL)
}
