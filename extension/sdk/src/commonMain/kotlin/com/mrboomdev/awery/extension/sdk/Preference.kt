package com.mrboomdev.awery.extension.sdk

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

data class StringPreference(
    override val key: String,
    override val name: String = key,
    override val description: String? = null,
    override val role: Preference.Role? = null,
    override var value: String
): Preference<String>

data class IntPreference(
    override val key: String,
    override val name: String = key,
    override val description: String? = null,
    override val role: Preference.Role? = null,
    val range: IntRange? = null,
    override var value: Int
): Preference<Int>

data class LongPreference(
    override val key: String,
    override val name: String = key,
    override val description: String? = null,
    override val role: Preference.Role? = null,
    val range: LongRange? = null,
    override var value: Long
): Preference<Long>

data class BooleanPreference(
    override val key: String,
    override val name: String = key,
    override val description: String? = null,
    override val role: Preference.Role? = null,
    override var value: Boolean
): Preference<Boolean>

data class SelectPreference(
    override val key: String,
    override val name: String = key,
    override val description: String? = null,
    override val role: Preference.Role? = null,
    val values: List<Item>,
    override var value: String
): Preference<String> {
    data class Item(
        val key: String,
        val name: String = key
    )
}

data class TriStatePreference(
    override val key: String,
    override val name: String = key,
    override val description: String? = null,
    override val role: Preference.Role? = null,
    override var value: State
): Preference<TriStatePreference.State> {
    enum class State {
        INCLUDED, EXCLUDED, NONE
    }
}

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