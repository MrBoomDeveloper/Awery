package com.mrboomdev.awery.ext.data;

import static java.util.Objects.requireNonNull;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Settings extends ArrayList<Setting> {
	public static final Settings EMPTY = new Settings() {
		@Override
		public Setting set(int index, Setting element) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean add(Setting setting) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void add(int index, Setting element) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection<? extends Setting> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(int index, Collection<? extends Setting> c) {
			throw new UnsupportedOperationException();
		}
	};

	public Settings(Setting... items) {
		super(List.of(items));
	}

	public Settings(Collection<? extends Setting> original) {
		super(original);
	}

	public Setting get(@NotNull String key) {
		for(var item : this) {
			if(key.equals(item.getKey())) {
				return item;
			}
		}

		return null;
	}

	public Setting require(@NotNull String key) {
		return requireNonNull(get(key), key);
	}
}