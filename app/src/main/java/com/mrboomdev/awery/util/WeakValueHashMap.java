package com.mrboomdev.awery.util;

import static com.mrboomdev.awery.util.NiceUtils.find;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import java9.util.stream.Collectors;

public class WeakValueHashMap<K, V> implements Serializable, Map<K, V> {
	@Serial
	private static final long serialVersionUID = 1;
	private final Map<K, WeakReference<V>> realMap = new HashMap<>();

	public WeakValueHashMap(Map<K, V> original) {
		putAll(original);
	}

	public WeakValueHashMap() {}

	public V put(K key, V value) {
		var was = realMap.put(key, new WeakReference<>(value));
		return was != null ? was.get() : null;
	}

	@Nullable
	@Override
	public V remove(@Nullable Object key) {
		var was = realMap.remove(key);
		return was != null ? was.get() : null;
	}

	@Override
	public void putAll(@NonNull Map<? extends K, ? extends V> m) {
		for(var entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void clear() {
		realMap.clear();
	}

	@NonNull
	@Override
	public Set<K> keySet() {
		return realMap.keySet();
	}

	@NonNull
	@Override
	public Collection<V> values() {
		return stream(realMap.values())
				.map(item -> item != null ? item.get() : null)
				.toList();
	}

	@NonNull
	@Override
	public Set<Entry<K, V>> entrySet() {
		return stream(realMap)
				.map(item -> new AbstractMap.SimpleEntry<>(item.getKey(),
						item.getValue() != null ? item.getValue().get() : null))
				.collect(Collectors.toSet());
	}

	public int size() {
		return realMap.size();
	}

	@Override
	public boolean isEmpty() {
		return realMap.isEmpty();
	}

	@Override
	public boolean containsKey(@Nullable Object key) {
		return realMap.containsKey(key);
	}

	@Override
	public boolean containsValue(@Nullable Object value) {
		return find(realMap.values(), item -> item.get() == value) != null;
	}

	@Nullable
	@Override
	public V get(@Nullable Object key) {
		var result = realMap.get(key);
		return result != null ? result.get() : null;
	}
}