package com.mrboomdev.awery.data.database

import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.databasesDir
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.resolve
import java.io.File

actual fun createDatabase(
    block: RoomDatabase.Builder<AweryDatabase>.() -> Unit
) = Room.databaseBuilder<AweryDatabase>(
    name = FileKit.databasesDir.resolve("awery.db").absolutePath()
).apply(block).build()