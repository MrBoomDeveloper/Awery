package com.mrboomdev.awery.core.utils.collection

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
class MutableStateListFlow<T> private constructor(
	initialItems: Collection<T>
): MutableStateFlow<List<T>>, List<T>, SuspendMutableList<T> {
	private val list = initialItems.toMutableList()
	private val mutableStateFlow = MutableStateFlow<List<T>>(list)
	private val mutex = Mutex()
	
	constructor(vararg initialItems: T): this(initialItems.toList())
	constructor(): this(emptyList())
	
	override var value: List<T>
		get() = mutableStateFlow.value
		set(value) {
			list.clear()
			list += value
			mutableStateFlow.value = value
		}

	override fun compareAndSet(expect: List<T>, update: List<T>): Boolean {
		return mutableStateFlow.compareAndSet(expect, update)
	}

	override val replayCache: List<List<T>>
		get() = mutableStateFlow.replayCache

	override suspend fun collect(collector: FlowCollector<List<T>>): Nothing {
		mutableStateFlow.collect(collector)
	}

	override val subscriptionCount: StateFlow<Int>
		get() = mutableStateFlow.subscriptionCount

	override suspend fun emit(value: List<T>) {
		list.clear()
		list += value
		mutableStateFlow.emit(value)
	}

	override fun tryEmit(value: List<T>): Boolean {
		list.clear()
		list += value
		return mutableStateFlow.tryEmit(value)
	}

	@ExperimentalCoroutinesApi
	override fun resetReplayCache() {
		mutableStateFlow.resetReplayCache()
	}

	override suspend fun add(element: T): Boolean {
		return list.add(element).apply { 
			mutableStateFlow.emit(list)
		}
	}

	override suspend fun remove(element: T): Boolean {
		return list.remove(element).apply { 
			mutableStateFlow.emit(list)
		}
	}

	override suspend fun addAll(elements: Collection<T>): Boolean {
		return list.addAll(elements).apply { 
			mutableStateFlow.emit(list)
		}
	}

	override suspend fun addAll(index: Int, elements: Collection<T>): Boolean {
		return list.addAll(index, elements).apply { 
			mutableStateFlow.emit(list)
		}
	}

	override suspend fun removeAll(elements: Collection<T>): Boolean {
		return list.removeAll(elements).apply { 
			mutableStateFlow.emit(list)
		}
	}

	override suspend fun retainAll(elements: Collection<T>): Boolean {
		return list.retainAll(elements).apply { 
			mutableStateFlow.emit(list)
		}
	}

	override suspend fun clear() {
		list.clear()
		mutableStateFlow.emit(list)
	}

	override suspend fun set(index: Int, element: T): T {
		return list.set(index, element).apply { 
			mutableStateFlow.emit(list)
		}
	}

	override suspend fun add(index: Int, element: T) {
		list.add(index, element)
		mutableStateFlow.emit(list)
	}

	override suspend fun removeAt(index: Int): T {
		return list.removeAt(index).apply { 
			mutableStateFlow.emit(list)
		}
	}

	override fun listIterator(): /*Mutable*/ListIterator<T> {
		return list.listIterator()
	}

	override fun listIterator(index: Int): /*Mutable*/ListIterator<T> {
		return list.listIterator(index)
	}

	override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> {
		return list.subList(fromIndex, toIndex)
	}

	override val size: Int
		get() = list.size

	override fun isEmpty(): Boolean {
		return list.isEmpty()
	}

	override fun contains(element: T): Boolean {
		return list.contains(element)
	}

	override fun containsAll(elements: Collection<T>): Boolean {
		return list.containsAll(elements)
	}

	override fun get(index: Int): T {
		return list[index]
	}

	override fun indexOf(element: T): Int {
		return list.indexOf(element)
	}

	override fun lastIndexOf(element: T): Int {
		return list.lastIndexOf(element)
	}

	override fun iterator(): /*Mutable*/Iterator<T> {
		return list.iterator()
	}
}