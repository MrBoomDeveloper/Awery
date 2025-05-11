package com.mrboomdev.awery.data

import androidx.room.Room
import androidx.room.RoomDatabase

internal actual fun AweryDB.Companion.builder(): RoomDatabase.Builder<AweryDB> {
	return Room.databaseBuilder<AweryDB>(name = "db")
}