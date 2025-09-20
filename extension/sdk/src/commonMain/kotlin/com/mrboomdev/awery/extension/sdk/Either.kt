package com.mrboomdev.awery.extension.sdk

class Either<A, B> internal constructor(val first: A?, val second: B?) {
    companion object {
        fun <A, B> first(first: A): Either<A, B> = Either(first, null)
        fun <A, B> second(second: B): Either<A, B> = Either(null, second)
    }
}

inline fun <A, B, T> Either<A, B>.get(first: (A) -> T, second: (B) -> T): T {
    this.first?.also { return first(it) }
    this.second?.also { return second(it) }
    throw IllegalStateException("Both fields are null which is impossible in the normal scenario!")
}