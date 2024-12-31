package com.mrboomdev.awery.data.settings;

import com.mrboomdev.awery.util.async.AsyncFuture;
/**
 * This interface marks an setting as lazy-loadable,
 * so all it's items and values will be initialized once it becomes needed on.
 * @author MrBooomDev
 */
public interface LazySettingsItem {

	AsyncFuture<SettingsItem> loadLazily();
}