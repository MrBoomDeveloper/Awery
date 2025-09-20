package com.mrboomdev.awery.extension.bundled.anilist.entity

import kotlinx.serialization.Serializable
import java.util.Calendar

@Serializable
data class FuzzyDate(
    val year: Int?,
    val month: Int?,
    val day: Int?
)

fun FuzzyDate.toMillis(): Long? {
    if(year == null) return null
    
    return Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        
        if(month != null) {
            set(Calendar.MONTH, month)
        }
        
        if(day != null) {
            set(Calendar.DAY_OF_MONTH, day)
        }
    }.timeInMillis
}

class FuzzyDateInt(val value: Int) {
    constructor(
        year: Int,
        month: Int,
        day: Int
    ): this("$year$month$day".toInt())
}