package com.mrboomdev.awery.util;

import androidx.annotation.Nullable;

public class CachedValue<K, V> {
	private K key;
	private V value;

	public CachedValue() {}

	public CachedValue(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public void set(K key, V value) {
		this.key = key;
		this.value = value;
	}

	@Nullable
	public V get(K key) {
		if(key == this.key) {
			return value;
		}

		return null;
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}
}