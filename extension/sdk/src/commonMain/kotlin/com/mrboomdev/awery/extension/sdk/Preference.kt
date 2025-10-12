package com.mrboomdev.awery.extension.sdk

import com.mrboomdev.awery.extension.sdk.utils.IntRangeSerializer
import com.mrboomdev.awery.extension.sdk.utils.LongRangeSerializer
import kotlinx.serialization.Serializable

@Serializable
sealed interface Preference<T> {
    val key: String
    val name: String
    val description: String?
    val role: Role?
    var value: T

    /**
     * May be used by the host application to generate feeds based on the content
     * or to correctly display filter in the ui.
     */
	@Serializable
    enum class Role {
        QUERY,
        DATE,
        START_DATE, 
        END_DATE,
        TAG,
        STUDIO,
        SORT
    }
}

@Serializable
data class StringPreference(
    override val key: String,
    override val name: String = key,
    override val description: String? = null,
    override val role: Preference.Role? = null,
    override var value: String
): Preference<String>

@Serializable
data class IntPreference(
	override val key: String,
	override val name: String = key,
	override val description: String? = null,
	override val role: Preference.Role? = null,
	@Serializable(IntRangeSerializer::class) val range: IntRange? = null,
	override var value: Int
): Preference<Int>

@Serializable
data class LongPreference(
	override val key: String,
	override val name: String = key,
	override val description: String? = null,
	override val role: Preference.Role? = null,
	@Serializable(LongRangeSerializer::class) val range: LongRange? = null,
	override var value: Long
): Preference<Long>

@Serializable
data class BooleanPreference(
    override val key: String,
    override val name: String = key,
    override val description: String? = null,
    override val role: Preference.Role? = null,
    override var value: Boolean
): Preference<Boolean>

@Serializable
data class SelectPreference(
    override val key: String,
    override val name: String = key,
    override val description: String? = null,
    override val role: Preference.Role? = null,
    val values: List<Item>,
    override var value: String
): Preference<String> {
	@Serializable
    data class Item(
        val key: String,
        val name: String = key
    )
}

@Serializable
data class TriStatePreference(
    override val key: String,
    override val name: String = key,
    override val description: String? = null,
    override val role: Preference.Role? = null,
    override var value: State
): Preference<TriStatePreference.State> {
	@Serializable
    enum class State {
        INCLUDED, EXCLUDED, NONE
    }
}

@Serializable
data class LabelPreference(
    override val key: String,
    override val name: String = key,
    override val description: String? = null
): Preference<Unit> {
    /**
     * Always returns nothing.
     */
    override var value: Unit = Unit

    /**
     * Always returns nothing.
     */
    override val role: Preference.Role? = null
}

@Serializable
data class PreferenceGroup(
    override val key: String,
    override val name: String = key,
    override val description: String? = null,
    override val role: Preference.Role? = null,
    val items: List<Preference<*>>
): Preference<Unit> {
    /**
     * Always returns nothing.
     */
    override var value: Unit = Unit
}