package com.mrboomdev.awery.data.database

import androidx.room.Room
import androidx.room.RoomDatabase
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.context

internal actual fun createHistoryDatabase(
    block: RoomDatabase.Builder<AweryHistoryDatabase>.() -> Unit
) = with(Awery.context) {
    Room.databaseBuilder(
        context = this,
        klass = AweryHistoryDatabase::class.java,
        name = getDatabasePath("history.db").absolutePath
    ).apply(block).build()
}