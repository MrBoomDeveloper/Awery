package com.mrboomdev.awery.util

class UniqueIdGenerator {
    private var initialValue = 0L
    private var value = 0L

    constructor(initialValue: Long) {
        this.value = initialValue
        this.initialValue = initialValue
    }

    constructor(): this(0)

    val long: Long
        get() = ++value

    val integer: Int
        get() = long.toInt()

    val float: Float
        get() = long.toFloat()

    fun reset(initialValue: Long) {
        this.initialValue = initialValue
        this.value = initialValue
    }

    fun reset() {
        reset(initialValue)
    }
}