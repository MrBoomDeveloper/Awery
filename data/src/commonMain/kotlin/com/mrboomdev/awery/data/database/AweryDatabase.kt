package com.mrboomdev.awery.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.data.database.dao.*
import com.mrboomdev.awery.data.database.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    version = 2,
    
    entities = [
        DBMedia::class,
        DBList::class,
        DBListMediaCrossRef::class,
        DBWatchProgress::class,
        DBRepository::class,
        DBBlacklistedMedia::class,
        DBBlacklistedKeyword::class
    ],
    
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
@TypeConverters(DBTypeConverters::class)
abstract class AweryDatabase: RoomDatabase() {
    abstract val media: MediaDao
    abstract val lists: ListDao
    abstract val progress: WatchProgressDao
    abstract val repositories: RepositoryDao
    abstract val mediaBlacklist: MediaBlacklistDao
    abstract val keywordBlacklist: KeywordBlacklistDao
    companion object {}
}

val Awery.database: AweryDatabase by lazy {
    createDatabase {
        setDriver(BundledSQLiteDriver())
        setQueryCoroutineContext(Dispatchers.IO)
        addMigrations(*migrations)
    }
}

internal expect fun createDatabase(
    block: RoomDatabase.Builder<AweryDatabase>.() -> Unit
): AweryDatabase