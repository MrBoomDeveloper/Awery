package com.mrboomdev.awery.data.database

import androidx.room.Room
import androidx.room.RoomDatabase
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.context

actual fun createDatabase(
    block: RoomDatabase.Builder<AweryDatabase>.() -> Unit
) = with(Awery.context) {
    Room.databaseBuilder(
        context = this,
        klass = AweryDatabase::class.java,
        name = getDatabasePath("awery.db").absolutePath
    ).apply(block).build()
}