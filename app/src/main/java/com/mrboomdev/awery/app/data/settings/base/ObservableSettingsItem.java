package com.mrboomdev.awery.app.data.settings.base;

import com.mrboomdev.awery.sdk.util.Callbacks;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

@Deprecated(forRemoval = true)
public interface ObservableSettingsItem {

	default void addSettingsObserver(Observer observer) {
		__OBSERVERS.observers.computeIfAbsent(this, k -> new HashSet<>()).add(observer);
	}

	default void removeSettingsObserver(Observer observer) {
		__OBSERVERS.observers.computeIfAbsent(this, k -> new HashSet<>()).remove(observer);
	}

	default void onSettingAddition(SettingsItem item, int position) {
		__OBSERVERS.invokeObservers(this, observer -> observer.onSettingAddition(item, position));
	}

	default void onSettingRemoval(SettingsItem item) {
		__OBSERVERS.invokeObservers(this, observer -> observer.onSettingRemoval(item));
	}

	default void onSettingChange(SettingsItem newItem, SettingsItem oldItem) {
		__OBSERVERS.invokeObservers(this, observer -> observer.onSettingChange(newItem, oldItem));
	}

	default void onSettingChange(SettingsItem item) {
		onSettingChange(item, item);
	}

	interface Observer {
		default void onSettingAddition(SettingsItem item, int position) {}
		default void onSettingRemoval(SettingsItem item) {}
		default void onSettingChange(SettingsItem newItem, SettingsItem oldItem) {}
	}

	/**
	 * DO NOT TOUCH OUTSIDE OF THIS CLASS!
	 * @author MrBoomDev
	 */
	class __OBSERVERS {
		private static final WeakHashMap<ObservableSettingsItem, Set<Observer>> observers = new WeakHashMap<>();

		private static void invokeObservers(ObservableSettingsItem parent, Callbacks.Callback1<Observer> callback) {
			var set = observers.get(parent);
			if(set == null) return;

			for(var observer : set) {
				callback.run(observer);
			}
		}
	}
}