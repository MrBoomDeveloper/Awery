package com.mrboomdev.awery.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.data.database.dao.ListDao
import com.mrboomdev.awery.data.database.dao.MediaDao
import com.mrboomdev.awery.data.database.dao.RepositoryDao
import com.mrboomdev.awery.data.database.dao.WatchProgressDao
import com.mrboomdev.awery.data.database.entity.DBList
import com.mrboomdev.awery.data.database.entity.DBListMediaCrossRef
import com.mrboomdev.awery.data.database.entity.DBMedia
import com.mrboomdev.awery.data.database.entity.DBRepository
import com.mrboomdev.awery.data.database.entity.DBWatchProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking

@Database(
    version = 1,
    entities = [
        DBMedia::class,
        DBList::class,
        DBListMediaCrossRef::class,
        DBWatchProgress::class,
        DBRepository::class
    ]
)
@TypeConverters(DBTypeConverters::class)
abstract class AweryDatabase: RoomDatabase() {
    abstract val media: MediaDao
    abstract val lists: ListDao
    abstract val progress: WatchProgressDao
    abstract val repositories: RepositoryDao
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