package com.mrboomdev.awery.core.utils.collection

/**
 * Removes all elements from this iterator by calling [remove] after calling [next].
 * This method will call [next] until [hasNext] returns false, and then call [remove] after each call to [next].
 * This method is useful when you want to remove all elements from an iterator without having to manually call [next] and [remove].
 */
fun MutableIterator<*>.removeAll() {
    remove()
    
    while(hasNext()) {
        next()
        remove()
    }
}

/**
 * Removes all elements from this iterator starting from the next one.
 * If there is no next element (i.e. hasNext returns false), this method does nothing.
 * Otherwise, this method will call [next] once, and then call [removeAll] to remove all remaining elements.
 */
fun MutableIterator<*>.removeAllNext() {
    if(hasNext()) {
        next()
    }
    
    removeAll()
}

/**
 * Iterates over the elements of this mutable iterable and calls the provided [block] function after each iteration.
 * The [block] function takes two parameters: the current [MutableIterator] and the next element of the iterable.
 * The [block] function is called until the iterator has no more elements.
 * This method is useful when you need to remove elements from the iterable while iterating over it.
 *
 * @param block A lambda function that takes two parameters: the current [MutableIterator] and the next element of the iterable.
 */
inline fun <T> MutableIterable<T>.iterateMutable(
    block: MutableIterator<T>.(T) -> Unit
) {
    val iterator = iterator()

    while(iterator.hasNext()) {
        block(iterator, iterator.next())
    }
}

/**
 * Iterates over the elements of this iterable and calls the provided [block] function after each iteration.
 * The [block] function takes two parameters: the current [Iterator] and the next element of the iterable with its index.
 * The [block] function is called until the iterator has no more elements.
 * This method is useful when you need to remove elements from the iterable while iterating over it.
 *
 * @param block A lambda function that takes two parameters: the current [Iterator] and the next element of the iterable with its index.
 */
inline fun <T> Iterable<T>.iterateIndexed(
    block: Iterator<T>.(Int, T) -> Unit
) {
    val iterator = iterator()
    var index = 0

    while(iterator.hasNext()) {
        block(iterator, index, iterator.next())
        index++
    }
}

/**
 * Iterates over the elements of this iterator and calls the provided [block] function after each iteration.
 * The [block] function takes two parameters: the current [Iterator] and the next element of the iterator.
 * The [block] function is called until the iterator has no more elements.
 * This method is useful when you need to remove elements from the iterator while iterating over it.
 *
 * @param block A lambda function that takes two parameters: the current [Iterator] and the next element of the iterator.
 */
inline fun <T> Iterator<T>.iterate(
    block: Iterator<T>.(T) -> Unit
) {
    while(hasNext()) {
        block(this, next())
    }
}

/**
 * Iterates over the elements of this iterable and calls the provided [block] function after each iteration.
 * The [block] function takes two parameters: the current [Iterator] and the next element of the iterable.
 * The [block] function is called until the iterator has no more elements.
 * This method is useful when you need to remove elements from the iterable while iterating over it.
 *
 * @param block A lambda function that takes two parameters: the current [Iterator] and the next element of the iterable.
 */
inline fun <T> Iterable<T>.iterate(
    block: Iterator<T>.(T) -> Unit
) = iterator().iterate(block)

/**
 * Iterates over the elements of this array and calls the provided [block] function after each iteration.
 * The [block] function takes two parameters: the current [Iterator] and the next element of the array.
 * The [block] function is called until the iterator has no more elements.
 * This method is useful when you need to remove elements from the array while iterating over it.
 *
 * @param block A lambda function that takes two parameters: the current [Iterator] and the next element of the array.
 */
inline fun <T> Array<T>.iterate(
    block: Iterator<T>.(T) -> Unit
) = iterator().iterate(block)

/**
 * Returns the element after the given [after] element in this list.
 * If the [after] element is not found in this list, or if it is the last element in this list,
 * the first element of this list is returned.
 *
 * @param after The element after which to find the next element.
 * @return The element after the given [after] element in this list.
 */
fun <T> List<T>.next(after: T): T {
    val index = indexOf(after)
    return get(if(index > size - 2) 0 else index + 1)
}

/**
 * Replaces an element in this list with a new element.
 *
 * If the [oldElement] is found in this list, it is removed and replaced with the [newElement].
 * If the [oldElement] is not found in this list, the [newElement] is added to the end of this list.
 *
 * @param oldElement The element to be replaced in this list.
 * @param newElement The element to replace the [oldElement] with in this list.
 */
fun <E> MutableList<E>.replace(
    oldElement: E, 
    newElement: E
) {
    val index = indexOf(oldElement)

    if(index != -1) {
        removeAt(index)
        add(index, newElement)
    } else {
        add(newElement)
    }
}

/**
 * Limits the size of this list to the specified [maxSize].
 * If the size of this list is less than or equal to [maxSize], this list is returned unchanged.
 * Otherwise, a new list containing the first [maxSize] elements of this list is returned.
 *
 * @param maxSize The maximum size of the list.
 * @return A list containing at most [maxSize] elements of this list.
 */
fun <T> List<T>.limit(maxSize: Int): List<T> {
    return if(size <= maxSize) toList() else subList(0, maxSize)
}