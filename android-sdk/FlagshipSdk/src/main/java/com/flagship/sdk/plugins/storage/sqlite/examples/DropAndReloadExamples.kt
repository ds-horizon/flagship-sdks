package com.flagship.sdk.plugins.storage.sqlite.examples

import android.content.Context
import com.flagship.sdk.plugins.storage.sqlite.ConfigDatabase
import com.flagship.sdk.plugins.storage.sqlite.DatabaseBuilder
import com.flagship.sdk.plugins.storage.sqlite.entities.ConfigSnapshot
import kotlinx.coroutines.runBlocking

/**
 * Examples demonstrating how to use the drop and reload migration strategy.
 *
 * These examples show different scenarios where drop and reload migrations
 * are useful and how to implement them safely.
 */
class DropAndReloadExamples {
    /**
     * Example 1: Environment-based database creation
     * Use different migration strategies based on the app environment.
     */
    class EnvironmentBasedDatabaseManager(
        private val context: Context,
    ) {
        enum class Environment {
            PRODUCTION,
            STAGING,
            DEVELOPMENT,
            TESTING,
        }

        fun createDatabase(environment: Environment): ConfigDatabase =
            when (environment) {
                Environment.PRODUCTION -> {
                    // Always use incremental migrations in production to preserve data
                    DatabaseBuilder.build(
                        context = context,
                        dbName = "flagship_prod.db",
                        strategy = DatabaseBuilder.MigrationStrategy.INCREMENTAL,
                    )
                }

                Environment.STAGING -> {
                    // Use fallback destructive for staging - try incremental first
                    DatabaseBuilder.build(
                        context = context,
                        dbName = "flagship_staging.db",
                        strategy = DatabaseBuilder.MigrationStrategy.FALLBACK_DESTRUCTIVE,
                    )
                }

                Environment.DEVELOPMENT -> {
                    // Use drop and reload for rapid development
                    DatabaseBuilder.build(
                        context = context,
                        dbName = "flagship_dev.db",
                        strategy = DatabaseBuilder.MigrationStrategy.DROP_AND_RELOAD,
                    )
                }

                Environment.TESTING -> {
                    // Always start fresh for tests
                    DatabaseBuilder.buildAlwaysDestructive(
                        context = context,
                        dbName = "flagship_test.db",
                    )
                }
            }
    }

    /**
     * Example 2: Development workflow with drop and reload
     * Useful when rapidly iterating on schema changes during development.
     */
    class DevelopmentDatabaseManager(
        private val context: Context,
    ) {
        fun createDevelopmentDatabase(): ConfigDatabase =
            DatabaseBuilder.buildWithDropAndReloadMigrations(
                context = context,
                dbName = "dev_flagship.db",
            )

        fun resetDatabaseForTesting(): ConfigDatabase {
            // This will always create a fresh database, perfect for testing
            return DatabaseBuilder.buildAlwaysDestructive(
                context = context,
                dbName = "test_flagship.db",
            )
        }

        fun createInMemoryForUnitTests(): ConfigDatabase {
            // Fastest option for unit tests - no file I/O
            return DatabaseBuilder.buildInMemory(context)
        }
    }

    /**
     * Example 3: Migration strategy selection based on app configuration
     */
    class ConfigurableDatabaseManager(
        private val context: Context,
    ) {
        data class DatabaseConfig(
            val allowDataLoss: Boolean = false,
            val isDebugBuild: Boolean = false,
            val preferPerformance: Boolean = false,
        )

        fun createDatabase(config: DatabaseConfig): ConfigDatabase {
            val strategy =
                when {
                    // If we allow data loss and prefer performance, use drop and reload
                    config.allowDataLoss && config.preferPerformance -> {
                        DatabaseBuilder.MigrationStrategy.DROP_AND_RELOAD
                    }

                    // If it's a debug build, use fallback destructive
                    config.isDebugBuild -> {
                        DatabaseBuilder.MigrationStrategy.FALLBACK_DESTRUCTIVE
                    }

                    // Default to incremental for data preservation
                    else -> {
                        DatabaseBuilder.MigrationStrategy.INCREMENTAL
                    }
                }

            return DatabaseBuilder.build(
                context = context,
                dbName = "flagship.db",
                strategy = strategy,
            )
        }
    }

    /**
     * Example 4: Testing utilities that leverage drop and reload
     */
    class TestDatabaseUtilities {
        /**
         * Creates a fresh database for each test case.
         * Ensures no test data pollution between test runs.
         */
        fun createFreshTestDatabase(context: Context): ConfigDatabase =
            DatabaseBuilder.buildAlwaysDestructive(
                context = context,
                dbName = "test_${System.currentTimeMillis()}.db",
            )

        /**
         * Creates an in-memory database for fast unit tests.
         */
        fun createInMemoryTestDatabase(context: Context): ConfigDatabase = DatabaseBuilder.buildInMemory(context)

        /**
         * Sets up a database with test data and demonstrates usage.
         */
        suspend fun setupTestDatabaseWithData(context: Context): ConfigDatabase {
            val database = createFreshTestDatabase(context)
            val dao = database.configDao()

            // Insert test data
            val testSnapshot =
                ConfigSnapshot(
                    namespace = "test_namespace",
                    version = "1.0.0",
                    etag = "test_etag",
                    createdAt = System.currentTimeMillis(),
                    isActive = true,
                    json = """{"feature_flag_1": true, "feature_flag_2": false}""",
                )

            dao.insertSnapshot(testSnapshot)
            return database
        }
    }

    /**
     * Example 5: Safe migration with backup (conceptual example)
     */
    class SafeMigrationManager(
        private val context: Context,
    ) {
        data class MigrationResult(
            val success: Boolean,
            val database: ConfigDatabase?,
            val backupCreated: Boolean = false,
            val errorMessage: String? = null,
        )

        /**
         * Attempts to migrate with backup strategy.
         * This is a conceptual example - actual backup implementation would depend
         * on your specific requirements.
         */
        suspend fun migrateWithBackup(dbName: String): MigrationResult =
            try {
                // Step 1: Try incremental migration first
                val incrementalDb = DatabaseBuilder.buildWithIncrementalMigrations(context, dbName)

                // Test if the database works
                incrementalDb.configDao().activeSnapshot("test")

                MigrationResult(success = true, database = incrementalDb)
            } catch (e: Exception) {
                // Step 2: If incremental fails, create backup and use drop/reload
                try {
                    // In a real implementation, you would backup data here
                    // val backup = createBackup(dbName)

                    val dropReloadDb = DatabaseBuilder.buildWithDropAndReloadMigrations(context, dbName)

                    MigrationResult(
                        success = true,
                        database = dropReloadDb,
                        backupCreated = true,
                    )
                } catch (backupException: Exception) {
                    MigrationResult(
                        success = false,
                        database = null,
                        errorMessage = "Migration failed: ${backupException.message}",
                    )
                }
            }
    }

    /**
     * Example 6: Performance comparison between migration strategies
     */
    class MigrationPerformanceExample {
        suspend fun compareMigrationStrategies(context: Context) {
            val strategies =
                listOf(
                    "Incremental" to DatabaseBuilder.MigrationStrategy.INCREMENTAL,
                    "Drop and Reload" to DatabaseBuilder.MigrationStrategy.DROP_AND_RELOAD,
                    "Fallback Destructive" to DatabaseBuilder.MigrationStrategy.FALLBACK_DESTRUCTIVE,
                )

            strategies.forEach { (name, strategy) ->
                val startTime = System.currentTimeMillis()

                val database =
                    DatabaseBuilder.build(
                        context = context,
                        dbName = "perf_test_${name.lowercase()}.db",
                        strategy = strategy,
                    )

                // Perform some operations to measure performance
                val dao = database.configDao()
                dao.activeSnapshot("test_namespace")

                val endTime = System.currentTimeMillis()
                println("$name migration took ${endTime - startTime}ms")

                database.close()
            }
        }
    }
}

/**
 * Usage examples for different scenarios.
 */
object DropAndReloadUsageExamples {
    /**
     * Simple development setup
     */
    fun developmentSetup(context: Context): ConfigDatabase = DatabaseBuilder.buildWithDropAndReloadMigrations(context, "dev.db")

    /**
     * Testing setup with fresh state
     */
    fun testingSetup(context: Context): ConfigDatabase = DatabaseBuilder.buildAlwaysDestructive(context, "test.db")

    /**
     * Production setup with data preservation
     */
    fun productionSetup(context: Context): ConfigDatabase = DatabaseBuilder.buildWithIncrementalMigrations(context, "prod.db")

    /**
     * Staging setup with fallback
     */
    fun stagingSetup(context: Context): ConfigDatabase = DatabaseBuilder.buildWithFallbackDestruction(context, "staging.db")

    /**
     * Demonstrate all available methods
     */
    fun demonstrateAllMethods(context: Context) {
        runBlocking {
            // Method 1: Using strategy enum
            val db1 =
                DatabaseBuilder.build(
                    context,
                    "method1.db",
                    DatabaseBuilder.MigrationStrategy.DROP_AND_RELOAD,
                )

            // Method 2: Direct method call
            val db2 = DatabaseBuilder.buildWithDropAndReloadMigrations(context, "method2.db")

            // Method 3: Always destructive
            val db3 = DatabaseBuilder.buildAlwaysDestructive(context, "method3.db")

            // Method 4: Fallback destructive
            val db4 = DatabaseBuilder.buildWithFallbackDestruction(context, "method4.db")

            // Method 5: In-memory (for tests)
            val db5 = DatabaseBuilder.buildInMemory(context)

            // Clean up
            listOf(db1, db2, db3, db4, db5).forEach { it.close() }
        }
    }
}
