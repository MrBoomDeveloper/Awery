package com.mrboomdev.awery.app.data.settings.base;

import com.mrboomdev.awery.ext.data.Setting;
import com.mrboomdev.awery.util.async.AsyncFuture;

/**
 * This interface marks an setting as lazy-loadable,
 * so all it's items and values will be initialized once it becomes needed on.
 * @author MrBooomDev
 */
@Deprecated(forRemoval = true)
public interface LazySettingsItem {

	AsyncFuture<Setting> loadLazily();
}