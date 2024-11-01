package com.mrboomdev.awery.util.extensions

import java.util.Calendar

fun Calendar.mutate(): Calendar {
	return Calendar.getInstance(timeZone).also {
		it.timeInMillis = timeInMillis
	}
}

fun Long.toCalendar(): Calendar {
	return Calendar.getInstance().apply {
		timeInMillis = this@toCalendar
	}
}