package com.mrboomdev.awery.data.database

import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.databasesDir
import io.github.vinceglb.filekit.resolve

internal actual fun createHistoryDatabase(
    block: RoomDatabase.Builder<AweryHistoryDatabase>.() -> Unit
) = Room.databaseBuilder<AweryHistoryDatabase>(
    name = FileKit.databasesDir.resolve("history.db").absolutePath()
).apply(block).build()