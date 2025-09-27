package com.mrboomdev.awery.core.utils.collection

interface SuspendMutableList<T>: Iterable<T>, List<T> {
	suspend fun add(element: T): Boolean
	suspend fun remove(element: T): Boolean
	suspend fun addAll(elements: Collection<T>): Boolean
	suspend fun addAll(index: Int, elements: Collection<T>): Boolean
	suspend fun removeAll(elements: Collection<T>): Boolean
	suspend fun retainAll(elements: Collection<T>): Boolean
	suspend fun clear()
	suspend operator fun set(index: Int, element: T): T
	suspend fun add(index: Int, element: T)
	suspend fun removeAt(index: Int): T
	/*suspend*/ override fun listIterator(): /*Mutable*/ListIterator<T>
	/*suspend*/ override fun listIterator(index: Int): /*Mutable*/ListIterator<T>
	override fun subList(fromIndex: Int, toIndex: Int): MutableList<T>
	/*suspend*/ override fun iterator(): /*Mutable*/Iterator<T>
}

/**
 * Adds the specified [element] to this mutable collection.
 */
@Suppress("INVISIBLE_REFERENCE")
@kotlin.internal.InlineOnly
suspend inline operator fun <T> SuspendMutableList<in T>.plusAssign(element: T) {
	this.add(element)
}

/**
 * Adds all elements of the given [elements] collection to this mutable collection.
 */
@Suppress("INVISIBLE_REFERENCE")
@kotlin.internal.InlineOnly
suspend inline operator fun <T> SuspendMutableList<in T>.plusAssign(elements: Iterable<T>) {
	this.addAll(elements)
}

/**
 * Adds all elements of the given [elements] array to this mutable collection.
 */
@Suppress("INVISIBLE_REFERENCE")
@kotlin.internal.InlineOnly
suspend inline operator fun <T> SuspendMutableList<in T>.plusAssign(elements: Array<T>) {
	this.addAll(elements)
}

/**
 * Adds all elements of the given [elements] sequence to this mutable collection.
 */
@Suppress("INVISIBLE_REFERENCE")
@kotlin.internal.InlineOnly
suspend inline operator fun <T> SuspendMutableList<in T>.plusAssign(elements: Sequence<T>) {
	this.addAll(elements)
}

/**
 * Removes a single instance of the specified [element] from this mutable collection.
 */
@Suppress("INVISIBLE_REFERENCE")
@kotlin.internal.InlineOnly
suspend inline operator fun <T> SuspendMutableList<in T>.minusAssign(element: T) {
	this.remove(element)
}

/**
 * Removes all elements contained in the given [elements] collection from this mutable collection.
 */
@Suppress("INVISIBLE_REFERENCE")
@kotlin.internal.InlineOnly
suspend inline operator fun <T> SuspendMutableList<in T>.minusAssign(elements: Iterable<T>) {
	this.removeAll(elements)
}

/**
 * Removes all elements contained in the given [elements] array from this mutable collection.
 */
@Suppress("INVISIBLE_REFERENCE")
@kotlin.internal.InlineOnly
suspend inline operator fun <T> SuspendMutableList<in T>.minusAssign(elements: Array<T>) {
	this.removeAll(elements)
}

/**
 * Removes all elements contained in the given [elements] sequence from this mutable collection.
 */
@Suppress("INVISIBLE_REFERENCE")
@kotlin.internal.InlineOnly
suspend inline operator fun <T> SuspendMutableList<in T>.minusAssign(elements: Sequence<T>) {
	this.removeAll(elements)
}

/**
 * Adds all elements of the given [elements] collection to this [MutableCollection].
 */
suspend fun <T> SuspendMutableList<in T>.addAll(elements: Iterable<T>): Boolean {
	when (elements) {
		is Collection -> return addAll(elements)
		else -> {
			var result: Boolean = false
			for (item in elements)
				if (add(item)) result = true
			return result
		}
	}
}

/**
 * Adds all elements of the given [elements] sequence to this [MutableCollection].
 */
suspend fun <T> SuspendMutableList<in T>.addAll(elements: Sequence<T>): Boolean {
	var result = false
	for (item in elements) {
		if (add(item)) result = true
	}
	return result
}

/**
 * Adds all elements of the given [elements] array to this [MutableCollection].
 */
suspend fun <T> SuspendMutableList<in T>.addAll(elements: Array<out T>): Boolean {
	return addAll(elements.asList())
}

/**
 * Converts this [Iterable] to a list if it is not a [Collection].
 * Otherwise, returns this.
 */
private fun <T> Iterable<T>.convertToListIfNotCollection(): Collection<T> =
	this as? Collection ?: toList()

/**
 * Removes all elements from this [MutableCollection] that are also contained in the given [elements] collection.
 */
suspend fun <T> SuspendMutableList<in T>.removeAll(elements: Iterable<T>): Boolean {
	return removeAll(elements.convertToListIfNotCollection())
}

/**
 * Removes all elements from this [MutableCollection] that are also contained in the given [elements] sequence.
 */
suspend fun <T> SuspendMutableList<in T>.removeAll(elements: Sequence<T>): Boolean {
	val list = elements.toList()
	return list.isNotEmpty() && removeAll(list)
}

/**
 * Removes all elements from this [MutableCollection] that are also contained in the given [elements] array.
 */
suspend fun <T> SuspendMutableList<in T>.removeAll(elements: Array<out T>): Boolean {
	return elements.isNotEmpty() && removeAll(elements.asList())
}

/**
 * Retains only elements of this [MutableCollection] that are contained in the given [elements] collection.
 */
suspend fun <T> SuspendMutableList<in T>.retainAll(elements: Iterable<T>): Boolean {
	return retainAll(elements.convertToListIfNotCollection())
}

/**
 * Retains only elements of this [MutableCollection] that are contained in the given [elements] array.
 */
suspend fun <T> SuspendMutableList<in T>.retainAll(elements: Array<out T>): Boolean {
	return if(elements.isNotEmpty()) {
		retainAll(elements.asList())
	} else retainNothing()
}

/**
 * Retains only elements of this [MutableCollection] that are contained in the given [elements] sequence.
 */
suspend fun <T> SuspendMutableList<in T>.retainAll(elements: Sequence<T>): Boolean {
	val list = elements.toList()
	return if(list.isNotEmpty()) retainAll(list) else retainNothing()
}

private suspend fun SuspendMutableList<*>.retainNothing(): Boolean {
	val result = isNotEmpty()
	clear()
	return result
}

/**
 * Removes the first element from this mutable list and returns that removed element, or throws [NoSuchElementException] if this list is empty.
 */
suspend fun <T> SuspendMutableList<T>.removeFirst(): T = if(isEmpty()) {
	throw NoSuchElementException("List is empty.")
} else removeAt(0)

/**
 * Removes the first element from this mutable list and returns that removed element, or returns `null` if this list is empty.
 */
suspend fun <T> SuspendMutableList<T>.removeFirstOrNull(): T? = if(isEmpty()) null else removeAt(0)

/**
 * Removes the last element from this mutable list and returns that removed element, or throws [NoSuchElementException] if this list is empty.
 */
suspend fun <T> SuspendMutableList<T>.removeLast(): T = if(isEmpty()) {
	throw NoSuchElementException("List is empty.")
} else removeAt(lastIndex)

/**
 * Removes the last element from this mutable list and returns that removed element, or returns `null` if this list is empty.
 */
suspend fun <T> SuspendMutableList<T>.removeLastOrNull(): T? = if (isEmpty()) null else removeAt(lastIndex)

/**
 * Removes all elements from this [MutableList] that match the given [predicate].
 *
 * @return `true` if any element was removed from this collection, or `false` when no elements were removed and collection was not modified.
 */
suspend fun <T> SuspendMutableList<T>.removeAll(predicate: (T) -> Boolean): Boolean = filterInPlace(predicate, true)

/**
 * Retains only elements of this [MutableList] that match the given [predicate].
 *
 * @return `true` if any element was removed from this collection, or `false` when all elements were retained and collection was not modified.
 */
suspend fun <T> SuspendMutableList<T>.retainAll(predicate: (T) -> Boolean): Boolean = filterInPlace(predicate, false)

private suspend fun <T> SuspendMutableList<T>.filterInPlace(predicate: (T) -> Boolean, predicateResultToRemove: Boolean): Boolean {
//	if (this !is RandomAccess)
//		return (this as MutableIterable<T>).filterInPlace(predicate, predicateResultToRemove)

	var writeIndex: Int = 0
	for (readIndex in 0..lastIndex) {
		val element = this[readIndex]
		if (predicate(element) == predicateResultToRemove)
			continue

		if (writeIndex != readIndex)
			this[writeIndex] = element

		writeIndex++
	}
	if (writeIndex < size) {
		for (removeIndex in lastIndex downTo writeIndex)
			removeAt(removeIndex)

		return true
	} else {
		return false
	}
}

suspend fun <E> SuspendMutableList<E>.replace(oldElement: E, newElement: E) {
	val index = indexOf(oldElement)

	if(index != -1) {
		removeAt(index)
		add(index, newElement)
	} else {
		add(newElement)
	}
}