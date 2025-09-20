package com.mrboomdev.awery.core.utils

import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.util.*

infix fun Long.safeDivide(other: Long): Long? {
    if(other == 0L) return 0L
    return this / other
}

infix fun Float.safeDivide(other: Float): Float? {
    if(other == 0F) return 0F
    return this / other
}

fun calendarOf(
    year: Int = 0,
    month: Int = 0,
    dayOfMonth: Int = 0,
    hourOfDay: Int = 0,
    minute: Int = 0,
    second: Int = 0,
    millisecond: Int = 0
): Calendar = Calendar.getInstance().apply {
    set(Calendar.YEAR, year)
    set(Calendar.MONTH, month)
    set(Calendar.DAY_OF_MONTH, dayOfMonth)
    set(Calendar.HOUR_OF_DAY, hourOfDay)
    set(Calendar.MINUTE, minute)
    set(Calendar.SECOND, second)
    set(Calendar.MILLISECOND, millisecond)
}

fun Long.toCalendar(): Calendar =
    Calendar.getInstance().apply { timeInMillis = this@toCalendar }

fun Long.formatTime(): String {
    var value = this

    if(value < 0) {
        return "00:00"
    }

    value /= 1000

    val hours = value.toInt() / 3600
    val days = hours / 24

    if(days >= 1) {
        return java.lang.String.format(
            Locale.ENGLISH, "%dd %02d:%02d:%02d",
            days, hours % 24, value.toInt() / 60, value.toInt() % 60
        )
    }

    if(hours >= 1) {
        return java.lang.String.format(
            Locale.ENGLISH, "%02d:%02d:%02d",
            hours, value.toInt() / 60, value.toInt() % 60
        )
    }

    return java.lang.String.format(
        Locale.ENGLISH, "%02d:%02d",
        value.toInt() / 60, value.toInt() % 60
    )
}

fun String.replaceAll(
    oldValue: String,
    newValue: String,
    ignoreCase: Boolean = false
): String {
    var result = this
    
    while(result.contains(oldValue, ignoreCase)) {
        result = result.replace(oldValue, newValue, ignoreCase)
    }
    
    return result
}

fun String.isValidUrl(): Boolean {
    if(isBlank()) {
        return false
    }

    try {
        URI(this)
        return true
    } catch(_: URISyntaxException) {
        return false
    } catch(_: MalformedURLException) {
        return false
    }
}

/**
 * @return 1 if this is true and 0 else.
 */
fun Boolean.toInt(): Int {
    return if(this) 1 else 0
}