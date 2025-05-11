package com.mrboomdev.awery.data

import androidx.room.Room
import androidx.room.RoomDatabase
import com.mrboomdev.awery.platform.Platform

internal actual fun AweryDB.Companion.builder(): RoomDatabase.Builder<AweryDB> {
	return Room.databaseBuilder<AweryDB>(
		context = Platform,
		name = "db"
	)
}