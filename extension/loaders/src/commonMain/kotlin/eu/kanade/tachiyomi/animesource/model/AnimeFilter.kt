package eu.kanade.tachiyomi.animesource.model

import com.mrboomdev.awery.core.utils.PlatformSdk

@PlatformSdk
sealed class AnimeFilter<T>(
    val name: String,
    var state: T
) {
    @PlatformSdk
    open class Header(
        name: String
    ): AnimeFilter<Any>(name, 0)

    @PlatformSdk
    open class Separator(
        name: String = ""
    ): AnimeFilter<Any>(name, 0)

    @PlatformSdk
    abstract class Select<V>(
        name: String,
        val values: Array<V>,
        state: Int = 0
    ): AnimeFilter<Int>(name, state)

    @PlatformSdk
    abstract class Text(
        name: String,
        state: String = ""
    ): AnimeFilter<String>(name, state)

    @PlatformSdk
    abstract class CheckBox(
        name: String,
        state: Boolean = false
    ): AnimeFilter<Boolean>(name, state)

    @PlatformSdk
    abstract class TriState(
        name: String,
        state: Int = STATE_IGNORE
    ): AnimeFilter<Int>(name, state) {
        @PlatformSdk
        fun isIgnored() = state == STATE_IGNORE

        @PlatformSdk
        fun isIncluded() = state == STATE_INCLUDE

        @PlatformSdk
        fun isExcluded() = state == STATE_EXCLUDE

        companion object {
            @PlatformSdk
            const val STATE_IGNORE = 0

            @PlatformSdk
            const val STATE_INCLUDE = 1

            @PlatformSdk
            const val STATE_EXCLUDE = 2
        }
    }

    @PlatformSdk
    abstract class Group<V>(
        name: String,
        state: List<V>
    ): AnimeFilter<List<V>>(name, state)

    @PlatformSdk
    abstract class Sort(
        name: String,
        val values: Array<String>,
        state: Selection? = null
    ): AnimeFilter<Sort.Selection?>(name, state) {
        @PlatformSdk
        data class Selection(
            val index: Int,
            val ascending: Boolean
        )
    }

    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(other !is AnimeFilter<*>) return false
        return name == other.name && state == other.state
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (state?.hashCode() ?: 0)
        return result
    }
}