package com.mrboomdev.awery.utils

/**
 * An support class, which helps with numbers generation. Useful for generating  unique ids.
 * By default crashes after an overflow.
 */
class UniqueIdGenerator @JvmOverloads constructor(
    private val initialValue: Long = 0,
    private val overflowMode: OverflowMode = OverflowMode.THROW
) {
    private var value = 0L

    val long: Long
        get() = (++value).apply {
            if(this >= Long.MAX_VALUE) {
                if(overflowMode == OverflowMode.RESET) {
                    value = initialValue
                    return value
                }

                throw NumberOverflowException("Long.MAX_VALUE exceeded!")
            }
        }

    val integer: Int
        get() = long.toInt().apply {
            if(this >= Int.MAX_VALUE) {
                if(overflowMode == OverflowMode.RESET) {
                    value = initialValue
                    return value.toInt()
                }

                throw NumberOverflowException("Int.MAX_VALUE exceeded!")
            }
        }

    val float: Float
        get() = long.toFloat().apply {
            if(this >= Float.MAX_VALUE) {
                if(overflowMode == OverflowMode.RESET) {
                    value = initialValue
                    return value.toFloat()
                }

                throw NumberOverflowException("Float.MAX_VALUE exceeded!")
            }
        }

    fun reset() {
        this.value = initialValue
    }

    enum class OverflowMode {
        RESET, THROW
    }
}

class NumberOverflowException(message: String) : RuntimeException(message)