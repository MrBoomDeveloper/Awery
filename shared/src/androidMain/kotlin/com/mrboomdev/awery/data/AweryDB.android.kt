package com.mrboomdev.awery.data

import androidx.room.Room
import androidx.room.RoomDatabase
import com.mrboomdev.awery.platform.android.AndroidGlobals

internal actual fun AweryDB.Companion.builder(): RoomDatabase.Builder<AweryDB> {
	return Room.databaseBuilder<AweryDB>(
		context = AndroidGlobals.applicationContext,
		name = "db"
	)
}