package com.mrboomdev.awery.core.utils

import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.util.*

/**
 * Divides this long by another, returning null if the divisor is zero.
 *
 * @param other The divisor.
 * @return The result of the division, or null if the divisor is zero.
 */
infix fun Long.safeDivide(other: Long): Long {
    if(other == 0L) return 0L
    return this / other
}

/**
 * Divides this float by another, returning 0.0 if the divisor is zero.
 *
 * @param other The divisor.
 * @return The result of the division, or 0.0 if the divisor is zero.
 */
infix fun Float.safeDivide(other: Float): Float {
    if(other == 0F) return 0F
    return this / other
}

/**
 * Creates a new [Calendar] object with the specified values.
 *
 * All parameters have default values of 0, so you can omit them if you don't want to change the corresponding field.
 *
 * @param year The year of the calendar.
 * @param month The month of the calendar. (0 - 11)
 * @param dayOfMonth The day of the month of the calendar. (1 - 31)
 * @param hourOfDay The hour of the day of the calendar. (0 - 23)
 * @param minute The minute of the hour of the calendar. (0 - 59)
 * @param second The second of the minute of the calendar. (0 - 59)
 * @param millisecond The millisecond of the second of the calendar. (0 - 999)
 *
 * @return The created [Calendar] object.
 */
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

/**
 * Converts this long to a [Calendar] object.
 *
 * The returned [Calendar] object will have its time set to the value of this long.
 *
 * @return The created [Calendar] object.
 */
fun Long.toCalendar(): Calendar =
    Calendar.getInstance().apply { timeInMillis = this@toCalendar }

/**
 * Formats this long as a time string.
 *
 * The returned time string is in the format "DD HH:MM:SS" if the value is greater than or equal to one day.
 * Otherwise, it is in the format "HH:MM:SS" if the value is greater than or equal to one hour.
 * Otherwise, it is in the format "MM:SS".
 *
 * If the value is less than zero, the returned time string is "00:00".
 *
 * @return The formatted time string.
 */
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

/**
 * Replaces all occurrences of [oldValue] with [newValue] in this string.
 * If [ignoreCase] is true, the replacement is case-insensitive.
 *
 * @param oldValue The substring to be replaced.
 * @param newValue The substring to replace [oldValue] with.
 * @param ignoreCase Whether the replacement should be case-insensitive.
 * @return A new string with all occurrences of [oldValue] replaced with [newValue].
 */
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


/**
 * Checks whether this string is a valid URL or not.
 *
 * @return Whether this string is a valid URL or not.
 */
/**
 * Checks whether this string is a valid URL or not.
 *
 * A valid URL is a string that can be parsed into a [URI] object without throwing
 * any exceptions.
 *
 * @return Whether this string is a valid URL or not.
 **/
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