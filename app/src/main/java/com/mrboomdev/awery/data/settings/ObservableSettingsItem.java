package com.mrboomdev.awery.data.settings;

import androidx.annotation.IntDef;

import com.mrboomdev.awery.sdk.util.Callbacks;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public interface ObservableSettingsItem {

	default void addSettingAdditionListener(Callbacks.Callback2<SettingsItem, Integer> listener) {
		__OBSERVERS.addObserver(this, listener, __OBSERVERS.ADDITIONS);
	}

	default void addSettingRemovalListener(Callbacks.Callback2<SettingsItem, Integer> listener) {
		__OBSERVERS.addObserver(this, listener, __OBSERVERS.REMOVALS);
	}

	default void addSettingChangeListener(Callbacks.Callback2<SettingsItem, Integer> listener) {
		__OBSERVERS.addObserver(this, listener, __OBSERVERS.CHANGES);
	}

	default void onSettingAddition(SettingsItem item, Integer position) {
		__OBSERVERS.callObservers(this, item, position, __OBSERVERS.ADDITIONS);
	}

	default void onSettingRemoval(SettingsItem item, Integer position) {
		__OBSERVERS.callObservers(this, item, position, __OBSERVERS.REMOVALS);
	}

	default void onSettingChange(SettingsItem item, Integer position) {
		__OBSERVERS.callObservers(this, item, position, __OBSERVERS.CHANGES);
	}

	/**
	 * DO NOT TOUCH OUTSIDE OF THIS CLASS!
	 * @author MrBoomDev
	 */
	class __OBSERVERS {
		private static final int CHANGES = 1, REMOVALS = 2, ADDITIONS = 3;
		private static final WeakHashMap<ObservableSettingsItem,
				List<Callbacks.Callback2<SettingsItem, Integer>>> changeObservers = new WeakHashMap<>();

		private static final WeakHashMap<ObservableSettingsItem,
				List<Callbacks.Callback2<SettingsItem, Integer>>> removalObservers = new WeakHashMap<>();

		private static final WeakHashMap<ObservableSettingsItem,
				List<Callbacks.Callback2<SettingsItem, Integer>>> additionObservers = new WeakHashMap<>();

		@IntDef({ CHANGES, ADDITIONS, REMOVALS })
		@interface ObserverType {}

		private static void callObservers(
				ObservableSettingsItem target,
				SettingsItem item,
				Integer position,
				@ObserverType int type
		) {
			var observers = (switch(type) {
				case ADDITIONS -> additionObservers;
				case CHANGES -> changeObservers;
				case REMOVALS -> removalObservers;
				default -> throw new IllegalStateException("Unexpected value: " + type);
			}).get(target);

			if(observers == null || observers.isEmpty()) return;

			for(var observer : observers) {
				observer.run(item, position);
			}
		}

		private static void addObserver(
				ObservableSettingsItem target,
				Callbacks.Callback2<SettingsItem, Integer> callback,
				@ObserverType int type
		) {
			var observersMap = (switch(type) {
				case ADDITIONS -> additionObservers;
				case CHANGES -> changeObservers;
				case REMOVALS -> removalObservers;
				default -> throw new IllegalStateException("Unexpected value: " + type);
			});

			observersMap.computeIfAbsent(target,
					k -> new ArrayList<>()).add(callback);
		}
	}
}