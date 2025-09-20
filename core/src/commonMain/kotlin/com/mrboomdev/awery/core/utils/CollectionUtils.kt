package com.mrboomdev.awery.core.utils

import kotlin.enums.enumEntries

fun MutableIterator<*>.removeAll() {
    remove()
    
    while(hasNext()) {
        next()
        remove()
    }
}

fun MutableIterator<*>.removeAllNext() {
    if(hasNext()) {
        next()
    }
    
    removeAll()
}

inline fun <T> MutableIterable<T>.iterateMutable(block: MutableIterator<T>.(T) -> Unit) {
    val iterator = iterator()

    while(iterator.hasNext()) {
        block(iterator, iterator.next())
    }
}

inline fun <T> Iterable<T>.iterateIndexed(block: Iterator<T>.(Int, T) -> Unit) {
    val iterator = iterator()
    var index = 0

    while(iterator.hasNext()) {
        block(iterator, index, iterator.next())
        index++
    }
}

inline fun <T> Iterable<T>.iterate(block: Iterator<T>.(T) -> Unit) {
    val iterator = iterator()
    
    while(iterator.hasNext()) {
        block(iterator, iterator.next())
    }
}

fun <T> List<T>.next(after: T): T {
    val index = indexOf(after)
    return get(if(index > size - 2) 0 else index + 1)
}

fun <E> MutableList<E>.replace(oldElement: E, newElement: E) {
    val index = indexOf(oldElement)

    if(index != -1) {
        removeAt(index)
        add(index, newElement)
    } else {
        add(newElement)
    }
}