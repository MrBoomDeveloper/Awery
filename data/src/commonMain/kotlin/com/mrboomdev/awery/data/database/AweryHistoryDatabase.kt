package com.mrboomdev.awery.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mrboomdev.awery.data.database.dao.HistoryDao
import com.mrboomdev.awery.data.database.entity.DBHistoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    version = 1,
    entities = [
        DBHistoryItem::class
    ]
)
abstract class AweryHistoryDatabase: RoomDatabase() {
    abstract val media: HistoryDao
}

val AweryDatabase.history: AweryHistoryDatabase by lazy {
    createHistoryDatabase {
        setDriver(BundledSQLiteDriver())
        setQueryCoroutineContext(Dispatchers.IO)
        addMigrations(*migrations)
    }
}

internal expect fun createHistoryDatabase(
    block: RoomDatabase.Builder<AweryHistoryDatabase>.() -> Unit
): AweryHistoryDatabase