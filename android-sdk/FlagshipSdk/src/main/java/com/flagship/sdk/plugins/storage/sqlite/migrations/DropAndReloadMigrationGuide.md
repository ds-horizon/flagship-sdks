# Drop and Reload Migration Strategy

This guide explains how to use the drop and reload migration strategy in the Flagship SDK SQLite storage plugin.

## Overview

The drop and reload migration strategy is a database migration approach that:

1. **Drops all existing tables** and their data
2. **Recreates the schema** from scratch with the latest structure
3. **Results in data loss** but ensures schema consistency

## When to Use Drop and Reload

### ✅ Good Use Cases

- **Development environments** where data loss is acceptable
- **Testing scenarios** where you need a clean database state
- **Major schema changes** that are difficult to migrate incrementally
- **Prototyping** when schema is still evolving rapidly
- **CI/CD pipelines** for testing with fresh data

### ❌ Avoid in These Cases

- **Production environments** where data preservation is critical
- **User-facing applications** with important cached data
- **When incremental migrations are feasible**

## Migration Strategies Available

### 1. Incremental Migrations (Recommended for Production)

```kotlin
// Preserves existing data
val database = DatabaseBuilder.build(
    context = context,
    strategy = DatabaseBuilder.MigrationStrategy.INCREMENTAL
)
```

### 2. Drop and Reload Migrations

```kotlin
// Destroys all data and recreates schema
val database = DatabaseBuilder.build(
    context = context,
    strategy = DatabaseBuilder.MigrationStrategy.DROP_AND_RELOAD
)
```

### 3. Fallback Destructive

```kotlin
// Try incremental first, fall back to destructive if they fail
val database = DatabaseBuilder.build(
    context = context,
    strategy = DatabaseBuilder.MigrationStrategy.FALLBACK_DESTRUCTIVE
)
```

### 4. Always Destructive (Development Only)

```kotlin
// Always recreate the database - most aggressive
val database = DatabaseBuilder.buildAlwaysDestructive(context)
```

## Usage Examples

### Basic Drop and Reload Setup

```kotlin
class DatabaseManager(private val context: Context) {

    fun createDevelopmentDatabase(): ConfigDatabase {
        return DatabaseBuilder.buildWithDropAndReloadMigrations(context, "dev_flagship.db")
    }

    fun createProductionDatabase(): ConfigDatabase {
        return DatabaseBuilder.buildWithIncrementalMigrations(context, "flagship.db")
    }

    fun createTestDatabase(): ConfigDatabase {
        return DatabaseBuilder.buildAlwaysDestructive(context, "test_flagship.db")
    }
}
```

### Environment-Based Configuration

```kotlin
class DatabaseFactory {

    fun createDatabase(context: Context, isDebug: Boolean): ConfigDatabase {
        return if (isDebug) {
            // Use drop and reload for development
            DatabaseBuilder.build(
                context = context,
                dbName = "flagship_debug.db",
                strategy = DatabaseBuilder.MigrationStrategy.DROP_AND_RELOAD
            )
        } else {
            // Use incremental migrations for production
            DatabaseBuilder.build(
                context = context,
                dbName = "flagship.db",
                strategy = DatabaseBuilder.MigrationStrategy.INCREMENTAL
            )
        }
    }
}
```

### Testing Setup with Fresh Schema

```kotlin
@Before
fun setUp() {
    // Always start with a fresh database for tests
    database = DatabaseBuilder.buildInMemory(context)

    // Or use a file-based database that always recreates
    database = DatabaseBuilder.buildAlwaysDestructive(context, "test.db")
}

@After
fun tearDown() {
    database.close()
    // Database file will be recreated on next test
}
```

## Adding New Drop and Reload Migrations

### Step 1: Update `dropAllTables()` Method

When adding new tables to your schema, update the `dropAllTables()` method in `DatabaseMigrations.kt`:

```kotlin
private fun dropAllTables(database: SupportSQLiteDatabase) {
    // Drop all known tables
    database.execSQL("DROP TABLE IF EXISTS config_snapshots")
    database.execSQL("DROP TABLE IF EXISTS flags_index")
    database.execSQL("DROP TABLE IF EXISTS your_new_table")  // Add new tables here

    // Drop any indexes that might remain
    database.execSQL("DROP INDEX IF EXISTS index_config_snapshots_namespace_isActive")
    database.execSQL("DROP INDEX IF EXISTS index_flags_index_snapshot_id_flag_key")
    database.execSQL("DROP INDEX IF EXISTS index_your_new_table_some_field")  // Add new indexes here
}
```

### Step 2: Update `recreateCurrentSchema()` Method

Add the new table creation SQL to match your Room entities:

```kotlin
private fun recreateCurrentSchema(database: SupportSQLiteDatabase) {
    // Existing tables...

    // Add your new table
    database.execSQL("""
        CREATE TABLE IF NOT EXISTS your_new_table (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            field1 TEXT NOT NULL,
            field2 INTEGER,
            created_at INTEGER NOT NULL
        )
    """.trimIndent())

    // Add indexes for the new table
    database.execSQL("""
        CREATE INDEX IF NOT EXISTS index_your_new_table_field1
        ON your_new_table (field1)
    """.trimIndent())
}
```

### Step 3: Create New Migration Instance

```kotlin
// Add to DatabaseMigrations.kt
val DROP_AND_RELOAD_2_3 = createDropAndReloadMigration(2, 3)

fun getDropAndReloadMigrations(): Array<Migration> {
    return arrayOf(
        DROP_AND_RELOAD_1_2,
        DROP_AND_RELOAD_2_3,  // Add new migration here
    )
}
```

## Best Practices

### 1. Environment Separation

```kotlin
// Use different databases for different environments
class DatabaseConfig {
    companion object {
        const val PROD_DB = "flagship.db"
        const val DEV_DB = "flagship_dev.db"
        const val TEST_DB = "flagship_test.db"
    }
}
```

### 2. Migration Strategy Selection

```kotlin
fun selectMigrationStrategy(environment: Environment): DatabaseBuilder.MigrationStrategy {
    return when (environment) {
        Environment.PRODUCTION -> DatabaseBuilder.MigrationStrategy.INCREMENTAL
        Environment.STAGING -> DatabaseBuilder.MigrationStrategy.FALLBACK_DESTRUCTIVE
        Environment.DEVELOPMENT -> DatabaseBuilder.MigrationStrategy.DROP_AND_RELOAD
        Environment.TESTING -> DatabaseBuilder.MigrationStrategy.DROP_AND_RELOAD
        else -> DatabaseBuilder.MigrationStrategy.DROP_AND_RELOAD
    }
}
```

### 3. Data Backup Before Drop and Reload

```kotlin
class MigrationWithBackup {

    suspend fun migrateWithBackup(
        context: Context,
        backupStrategy: BackupStrategy = BackupStrategy.JSON_EXPORT
    ) {
        // 1. Backup existing data if needed
        if (backupStrategy != BackupStrategy.NONE) {
            backupData(context, backupStrategy)
        }

        // 2. Create new database with drop and reload
        val newDatabase = DatabaseBuilder.buildWithDropAndReloadMigrations(context)

        // 3. Optionally restore critical data
        restoreData(newDatabase, backupStrategy)
    }
}
```

## Troubleshooting

### Migration Fails

```kotlin
// Use fallback destructive if migrations consistently fail
val database = DatabaseBuilder.build(
    context = context,
    strategy = DatabaseBuilder.MigrationStrategy.FALLBACK_DESTRUCTIVE
)
```

### Schema Mismatch Errors

```kotlin
// Ensure recreateCurrentSchema() matches your Room entities exactly
// Check that all @Entity classes are represented in the schema recreation
```

### Performance Considerations

```kotlin
// For large databases, consider:
// 1. Using in-memory database for tests
// 2. Implementing background migration strategies
// 3. Adding migration progress callbacks

val database = Room.databaseBuilder(context, ConfigDatabase::class.java, "flagship.db")
    .addMigrations(*DatabaseMigrations.getDropAndReloadMigrations())
    .setQueryCallback(queryCallback, executor)  // Monitor migration performance
    .build()
```

## Migration Checklist

- [ ] Updated `dropAllTables()` with new tables/indexes
- [ ] Updated `recreateCurrentSchema()` with current schema
- [ ] Created new migration instance (e.g., `DROP_AND_RELOAD_X_Y`)
- [ ] Added migration to `getDropAndReloadMigrations()`
- [ ] Tested migration in development environment
- [ ] Verified schema matches Room entities
- [ ] Considered data backup strategy if needed
- [ ] Updated database version in `@Database` annotation

## See Also

- `DatabaseMigrations.kt` - Core migration implementations
- `DatabaseBuilder.kt` - Database creation utilities
- `ConfigDatabase.kt` - Room database definition
- Room Migration Documentation: https://developer.android.com/training/data-storage/room/migrating-db-versions
