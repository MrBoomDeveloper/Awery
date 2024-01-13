package com.mrboomdev.awery.anilist.data;

import java.util.Map;

public class AnilistDataWrapper<T> {
	private Map<String, T> data;

	public T get(String key) {
		if(data == null) return null;
		return data.get(key);
	}

	@SuppressWarnings("unchecked")
	public T first() {
		if(data == null) return null;

		var array = data.values().toArray();
		if(array.length == 0) return null;

		return (T) array[0];
	}
}